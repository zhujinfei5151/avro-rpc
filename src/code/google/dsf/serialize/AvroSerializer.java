package code.google.dsf.serialize;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.apache.avro.Protocol;
import org.apache.avro.Protocol.Message;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;

/**
 * avro通用模式 序列化及反序列化 
 * @author taohuifei
 *
 */
public class AvroSerializer implements ISerializer {

  static final EncoderFactory E_FACTORY = EncoderFactory.get();
  static final DecoderFactory D_FACTORY = DecoderFactory.get();

  private AvroSerializer() {
  }

  public static AvroSerializer getInstance() {
    return AvroSerializerHold._instance;
  }

  private static class AvroSerializerHold {
    private static AvroSerializer _instance = new AvroSerializer();
  }


  /**
   * 服务名与Avro协议模式
   */
  private static Map<String, Protocol> protocolmap = new HashMap<String, Protocol>();

  /**
   * 注册Avro协议模式
   * 
   * @param beanName
   * @param fileclasspath
   * @throws IOException
   */
  public static Protocol registerAvroProtocol(String beanName, String fileclasspath)
      throws IOException {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    String file = classLoader.getResource(fileclasspath).getFile();
    Protocol avroprotocol = Protocol.parse(new File(file));
    protocolmap.put(beanName, avroprotocol);
    return avroprotocol;
  }

  public static Protocol getProtocol(String beanName) {
    return protocolmap.get(beanName);
  }

  @Override
  public Object deserialize(ByteBuffer buffer, SerializerContext context) {
    Schema schema = context.getSchema();
    if(schema == null){
      if(context.isRequest())
        schema = getProtocol(context.getRpcMetaDTO().getBeanName()).getMessages().get((context.getRpcMetaDTO().getMethodName())).getRequest();
      else {
        schema = getProtocol(context.getRpcMetaDTO().getBeanName()).getMessages().get((context.getRpcMetaDTO().getMethodName())).getResponse();
      }
    }
    BinaryDecoder in = D_FACTORY.binaryDecoder(buffer.array(), null);
    try {
      Object result = new GenericDatumReader<Object>(schema).read(null, in);
      return result;
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  @Override
  public ByteBuffer serialize(Object obj, SerializerContext context) {
    Object arg = obj;
    if(context.isRequest()){
       arg = ((Object[])obj)[0];
    }
    ByteArrayOutputStream bao = new ByteArrayOutputStream();
    Schema schema = context.getSchema();
    if(schema == null){
      if(context.isRequest())
        schema = getProtocol(context.getRpcMetaDTO().getBeanName()).getMessages().get((context.getRpcMetaDTO().getMethodName())).getRequest();
      else {
        schema = getProtocol(context.getRpcMetaDTO().getBeanName()).getMessages().get((context.getRpcMetaDTO().getMethodName())).getResponse();
      }
    }
    GenericDatumWriter<Object> w = new GenericDatumWriter<Object>(schema);
    Encoder e = E_FACTORY.binaryEncoder(bao, null);
    try {
      w.write(arg, e);
      e.flush();
    } catch (IOException e1) {
      throw new RuntimeException(e1);
    }
    return ByteBuffer.wrap(bao.toByteArray());
  }

  public static void main(String[] args) throws IOException {
    String filePath = "code/google/dsf/serialize/tcpnetty.avpr";
    String beanName = "test";
    registerAvroProtocol(beanName, filePath);
    Protocol protocol = getProtocol(beanName);
    Message message = protocol.getMessages().get("dosendpacket_av");
    System.out.println(message.getRequest());

    GenericRecord arg = new GenericData.Record(message.getRequest());
    GenericRecord dto = new GenericData.Record(protocol.getType("vsimpacket"));
    dto.put("imsi", "460030912121001");
    ByteBuffer data = ByteBuffer.wrap("avrotest".getBytes("UTF-8"));
    dto.put(1, 1);
    dto.put("data", data);
    arg.put(0, dto);

    ISerializer iser = SerializerFactory.getSerializer(SerializerFactory.SERIALIZER_AVRO);
    SerializerContext context = new SerializerContext();
    context.setSchema(message.getRequest());
    ByteBuffer buffer = iser.serialize(arg, context);
    Object obj = iser.deserialize(buffer, context);
   // Assert.assertEquals(arg, obj);
    System.out.println(obj.toString());

  }
}
