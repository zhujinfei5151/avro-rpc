package code.google.dsf.http;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import code.google.dsf.protocol.ProtocolPack;
import code.google.dsf.protocol.rpc.RPCMetaDTO;
import code.google.dsf.serialize.SerializerContext;
import code.google.dsf.serialize.SerializerFactory;
import code.google.dsf.server.ProtocolHandlerFactory;
import code.google.dsf.server.rpc.IRPCProtocolHandler;
import code.google.dsf.util.OpaqueGenerator;

public class HttpInvokerClientInvocation implements InvocationHandler {
  protected static final String HTTP_METHOD_POST = "POST";

  protected static final String HTTP_HEADER_ACCEPT_LANGUAGE = "Accept-Language";

  protected static final String HTTP_HEADER_ACCEPT_ENCODING = "Accept-Encoding";

  protected static final String HTTP_HEADER_CONTENT_ENCODING = "Content-Encoding";

  protected static final String HTTP_HEADER_CONTENT_TYPE = "Content-Type";

  protected static final String HTTP_HEADER_CONTENT_LENGTH = "Content-Length";

  protected static final String ENCODING_GZIP = "gzip";

  // 服务Host
  private String serviceUrl;

  private String beanName;

  private int connectTimeout = 5000;
  private int readTimeout = 30000;

  // 序列化类型
  private byte contentType;

  public HttpInvokerClientInvocation(String serviceUrl, String beanName, byte contentType) {
    this.serviceUrl = serviceUrl;
    this.beanName = beanName;
    this.contentType = contentType;
  }

  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    ProtocolPack dataPack = new ProtocolPack();

    // 协议包头
    dataPack.setProtocolType(ProtocolHandlerFactory.PROTOCOLHANDLER_RPC);
    dataPack.setSerial(OpaqueGenerator.getNextOpaque());
    dataPack.setContentType(contentType);

    // RPC元数据
    RPCMetaDTO rcpMetaDto = new RPCMetaDTO();
    rcpMetaDto.setBeanName(beanName);
    rcpMetaDto.setReturnclass(method.getReturnType());
    rcpMetaDto.setMethodName(method.getName());

    // 协议包体
    List<ByteBuffer> bodys = new ArrayList<ByteBuffer>(2);
    dataPack.setDatas(bodys);
    // 将元数据序列化数据作为第一个数据块
    bodys.add(rcpMetaDto.serialize(rcpMetaDto, null));

    // 将参数序列化作为第二个数据块
    if (args != null && args.length > 0) {
      SerializerContext context = new SerializerContext();
      context.setRpcMetaDTO(rcpMetaDto);
      bodys.add(SerializerFactory.getSerializer(contentType).serialize(args, context));
    }

    HttpURLConnection conn = this.openConnection();
    ByteBuffer buffer = dataPack.encode();
    prepareConnection(conn, buffer.limit());
    ByteArrayOutputStream baos = new ByteArrayOutputStream(buffer.limit());
    baos.write(buffer.array());
    baos.writeTo(conn.getOutputStream());
    baos.flush();
    baos.close();
    buffer.clear();
    buffer = null;
    InputStream in = conn.getInputStream();
    ProtocolPack responsePack = ProtocolPack.decode(new DataInputStream(in));
    return handleResult(rcpMetaDto, responsePack);
  }

  private Object handleResult(RPCMetaDTO rcpMetaDto, ProtocolPack responsePack) throws Exception {
    List<ByteBuffer> result = responsePack.getDatas();
    if (result == null || result.size() == 0) {
      throw new Exception("返回数据格式不正确");
    }
    ByteBuffer tagbuffer = result.get(0);
    ByteBuffer databuffer = null;
    if (result.size() > 1) {
      databuffer = result.get(1);
    }
    // 如果是调用失败
    if (tagbuffer.get() == IRPCProtocolHandler.FALSE) {
      if (databuffer == null) {
        throw new Exception("调用莫名异常，无错误消息！");
      }
      throw new Exception(SerializerFactory.bytesToString(databuffer.array()));
    } else {
      if (databuffer == null) {
        return null;
      }
      SerializerContext context = new SerializerContext();
      context.setRpcMetaDTO(rcpMetaDto);
      context.setReturnclass(rcpMetaDto.getReturnclass());
      context.setRequest(false);
      try {
        // 反序列化数据，并回调
        return SerializerFactory.getSerializer(responsePack.getContentType()).deserialize(
            databuffer, context);
      } catch (Exception e) {
        throw e;
      }
    }
  }

  protected HttpURLConnection openConnection() throws IOException {
    URLConnection con = new URL(this.serviceUrl).openConnection();
    if (!(con instanceof HttpURLConnection)) {
      throw new IOException("Service URL [" + serviceUrl + "] is not an HTTP URL");
    }
    return (HttpURLConnection) con;
  }

  protected void prepareConnection(HttpURLConnection connection, int contentLength)
      throws IOException {
    if (this.connectTimeout >= 0) {
      connection.setConnectTimeout(this.connectTimeout);
    }
    if (this.readTimeout >= 0) {
      connection.setReadTimeout(this.readTimeout);
    }
    connection.setDoOutput(true);
    connection.setRequestMethod(HTTP_METHOD_POST);
    connection.setRequestProperty(HTTP_HEADER_CONTENT_LENGTH, Integer.toString(contentLength));

  }

}
