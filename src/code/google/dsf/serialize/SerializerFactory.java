package code.google.dsf.serialize;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 
 * 序列化工厂 定义序列化类型，注册序列化对象，查找序列化对象 开发者可以使用其他序列化方式，使用时只要把自定义序列化对象注册到该工厂中
 * 
 * @author taohuifei
 * 
 */
public abstract class SerializerFactory {

  // AVRO序列化
  public static final byte SERIALIZER_AVRO = (byte) 0x00;

  // JSON序列化
  public static final byte SERIALIZER_JSON = (byte) 0x01;

  // JAVA序列化
  public static final byte SERIALIZER_JAVA = (byte) 0x02;

  // HESSIAN2序列化
  public static final byte SERIALIZER_HESSIAN = (byte) 0x03;

  // protobuf序列化
  public static final byte SERIALIZER_PROTOBUF = (byte) 0x04;

  private static Map<Byte, ISerializer> serializerHandlerMap = new HashMap<Byte, ISerializer>();

  private static AtomicBoolean isinied = new AtomicBoolean(false);

  public static void registerSerializer() {
    try {
      Class.forName("org.apache.avro.Protocol");
      SerializerFactory.registerSerializer(SERIALIZER_AVRO, AvroSerializer.getInstance());
    } catch (Exception e) {

    }
    try {
      Class.forName("com.alibaba.fastjson.JSON");
      SerializerFactory.registerSerializer(SERIALIZER_JSON, JsonSerializer.getInstance());
    } catch (Exception e) {

    }
    try {
      Class.forName("com.caucho.hessian.io.Hessian2Input");
      SerializerFactory.registerSerializer(SERIALIZER_HESSIAN, HessianSerializer.getInstance());
    } catch (Exception e) {

    }
    try {
      SerializerFactory.registerSerializer(SERIALIZER_JAVA, JavaSerializer.getInstance());
    } catch (Exception e) {

    }
    try {
      Class.forName("com.google.protobuf.Message");
      SerializerFactory.registerSerializer(SERIALIZER_PROTOBUF, ProtobufSerializer.getInstance());
    } catch (Exception e) {

    }
  }

  public static void registerSerializer(byte type, ISerializer serializer) {
    serializerHandlerMap.put(Byte.valueOf(type), serializer);
  }

  public static ISerializer getSerializer(byte type) {
    if(isinied.compareAndSet(false, true))
      registerSerializer();
    return serializerHandlerMap.get(Byte.valueOf(type));
  }

  public static String bytesToString(byte[] data) {
    return new String(data, ISerializer.DEFAULT_CHARSET);
  }

  public static byte[] stringToBytes(String data) {
    return data.getBytes(ISerializer.DEFAULT_CHARSET);
  }
}
