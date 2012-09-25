package code.google.dsf.client;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.ipc.CallFuture;
import org.apache.avro.ipc.Callback;

import code.google.dsf.protocol.ProtocolPack;
import code.google.dsf.protocol.rpc.RPCMetaDTO;
import code.google.dsf.serialize.SerializerContext;
import code.google.dsf.serialize.SerializerFactory;
import code.google.dsf.server.ProtocolHandlerFactory;
import code.google.dsf.server.rpc.IRPCProtocolHandler;
import code.google.dsf.util.OpaqueGenerator;

/**
 * 
 * RPC客户端实现 提供同步调用和异步调用
 * 
 * @author taohuifei 
 * email:  taohuifei@gmail.com
 * 
 */
public class RPCClient implements IClient {

  private String defaultServerIP;

  private int port;

  /**
   * 传输对象
   */
  private final ITransceiver transceiver;

  public RPCClient(String serverIP, int port, ITransceiver transceiver) {
    this.defaultServerIP = serverIP;
    this.port = port;
    this.transceiver = transceiver;
  }

  
  /**
   * 同步调用
   */
  @SuppressWarnings("rawtypes")
  public Object invokeSync(String beanName, String methodName, Class[] argTypes, Object[] args,
      Class returnclass, byte contentType) throws Exception {
    //同步回调锁
    CallFuture<Object> callback = new CallFuture<Object>();
    this.invokeAsync(beanName, methodName, argTypes, args, returnclass, contentType, callback);
    //结果没返回值，将一直处于阻塞状态
    return callback.get();
  }


  @SuppressWarnings({"rawtypes", "unchecked"})
  public void invokeAsync(String beanName, String methodName, Class[] argTypes, Object[] args,
      Class returnclass, byte contentType, Callback callback) throws Exception {
    //发送请求流量控制
    RequestFlowcontrolCallback requestFlowcontrolCallback = new RequestFlowcontrolCallback(callback);
    try {
      ProtocolPack dataPack = new ProtocolPack();

      // 协议包头
      dataPack.setProtocolType(ProtocolHandlerFactory.PROTOCOLHANDLER_RPC);
      dataPack.setSerial(OpaqueGenerator.getNextOpaque());
      dataPack.setContentType(contentType);

      // RPC元数据
      RPCMetaDTO rcpMetaDto = new RPCMetaDTO();
      rcpMetaDto.setBeanName(beanName);
      rcpMetaDto.setReturnclass(returnclass);
      rcpMetaDto.setMethodName(methodName);

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
      
      //将回调接口包装下，处理返回数据的解析
      ProtocolTransceiverCallback repsonCallback =
          new ProtocolTransceiverCallback(contentType, rcpMetaDto, requestFlowcontrolCallback);
      this.transceiver.transceive(this.defaultServerIP, this.port, dataPack, repsonCallback);
    } catch (Exception e) {
      requestFlowcontrolCallback.handleError(e);
    }
  }

  /**
   *
   * 传输层得到回复数据后，回调此对象
   * 这里需要解析返回的结果
   * 
   * @author taohuifei
   * 
   * @param <T>
   */
  protected class ProtocolTransceiverCallback<T> implements Callback<List<ByteBuffer>> {

    private final RPCMetaDTO rcpMetaDto;
    private final Callback<T> callback;
    private final byte contentType;

    public ProtocolTransceiverCallback(byte contentType, RPCMetaDTO rcpMetaDto, Callback<T> callback) {
      this.rcpMetaDto = rcpMetaDto;
      this.callback = callback;
      this.contentType = contentType;
    }

    /**
     * 传输对象收到数据后，会回调此方法
     */
    @SuppressWarnings("unchecked")
   
    public void handleResult(List<ByteBuffer> result) {
      if (result == null || result.size() == 0) {
        this.callback.handleError(new RuntimeException("返回数据格式不正确"));
        return;
      }
      ByteBuffer tagbuffer = result.get(0);
      ByteBuffer databuffer = null;
      if (result.size() > 1) {
        databuffer = result.get(1);
      }
      // 如果是调用失败
      if (tagbuffer.get() == IRPCProtocolHandler.FALSE) {
        if (databuffer == null) {
          this.callback.handleError(new Exception("调用莫名异常，无错误消息！"));
          return;
        }
        this.callback
            .handleError(new Exception(SerializerFactory.bytesToString(databuffer.array())));
        return;
      } else {
        if (databuffer == null) {
          this.callback.handleResult(null);
          return;
        }
        SerializerContext context = new SerializerContext();
        context.setRpcMetaDTO(rcpMetaDto);
        context.setReturnclass(rcpMetaDto.getReturnclass());
        context.setRequest(false);
        try {
          //反序列化数据，并回调
          this.callback.handleResult((T) SerializerFactory.getSerializer(contentType).deserialize(
              databuffer, context));
        } catch (Exception e) {
          this.callback.handleError(e);
        }
      }
    }

    public void handleError(Throwable error) {
      this.callback.handleError(error);
    }
  }
}
