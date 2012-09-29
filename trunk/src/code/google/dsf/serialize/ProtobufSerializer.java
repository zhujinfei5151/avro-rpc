package code.google.dsf.serialize;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;

import com.google.protobuf.AbstractMessage.Builder;
import com.google.protobuf.Message;

/**
 * Google Protocol Buffer序列化
 * <P>
 * Google Protocol Buffer序列化格式如下：
 * 1：写入参数个数
 * 2：对每个参数做循环
 *    写入参数类型名称长度
 *    写入参数类型
 *    写入参数序列化长度
 *    写下参数序列化
 * <P>
 * @author taohuifei
 * 
 */
public class ProtobufSerializer implements ISerializer {

  final static String METHODNME = "newBuilder";
  
  private static ConcurrentHashMap<String, Method> methodCache =
      new ConcurrentHashMap<String, Method>();

  public static ProtobufSerializer getInstance() {
    return ProtobufSerializerHolder._instance;
  }

  private static class ProtobufSerializerHolder {
    static ProtobufSerializer _instance = new ProtobufSerializer();
  }

  @SuppressWarnings("rawtypes")
  public Object deserialize(ByteBuffer buffer, SerializerContext context) {
    int ilen = buffer.getInt();
    if (ilen == 0) return null;
    Object[] result = new Object[ilen];
    for (int i = 0; i < ilen; i++) {
      int iclasslen = buffer.getInt();
      byte[] classbyte = new byte[iclasslen];
      buffer.get(classbyte);
      String classname = SerializerFactory.bytesToString(classbyte);
      // Class.forName(classname)ProtobufSer
      Method method = methodCache.get(classname);
      if(method == null){
        try {
          method =  Class.forName(classname).getMethod(METHODNME, null);
        } catch (Exception e) {
          e.printStackTrace();
          throw new RuntimeException(e.getMessage());
        }
        methodCache.put(classname, method);
      }
      int iserlen = buffer.getInt();
      byte[] arg = new byte[iserlen];
      buffer.get(arg);
      try {
        result[i] = ((Builder)method.invoke(method.getClass(), null)).mergeFrom(arg).build();
      } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException(e.getMessage());
      }
    }
    if(context.isRequest())
      return result;
    else {
      return result[0];
    }
  }

  public ByteBuffer serialize(Object obj, SerializerContext context) {
    Object[] arg = null;
    if(obj instanceof Object[])
      arg = (Object[]) obj;
    else
    {
      arg = new Object[1]; 
      arg[0] = obj;
    }
    Object[] sers = new Object[arg.length];
    Object[] types = new Object[arg.length];
    int ilen = arg.length;
    int iserlen = 4;
    for (int i = 0; i < ilen; i++) {
      Message msg = (Message) arg[i];
      // Class name
      types[i] = SerializerFactory.stringToBytes(msg.getClass().getName());
      sers[i] = msg.toByteArray();
      iserlen = iserlen + 4 + ((byte[]) types[i]).length + 4 + ((byte[]) sers[i]).length;
    }
    ByteBuffer buffer = ByteBuffer.allocate(iserlen);
    // 有多少个参数
    buffer.putInt(ilen);
    for (int i = 0; i < ilen; i++) {
      // 写入第I个参数的类型
      buffer.putInt(((byte[]) types[i]).length);
      buffer.put((byte[]) types[i]);
      // 写入第I个参数
      buffer.putInt(((byte[]) sers[i]).length);
      buffer.put((byte[]) sers[i]);
    }
    buffer.flip();
    return buffer;
  }

}
