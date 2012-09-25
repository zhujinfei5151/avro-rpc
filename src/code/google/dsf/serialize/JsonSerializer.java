package code.google.dsf.serialize;

import java.nio.ByteBuffer;
import java.util.Date;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * Fastjson 序列化及反实例化
 * @author taohuifei
 *
 */
public class JsonSerializer implements ISerializer{
  
  private JsonSerializer(){
    
  }
  
  public static JsonSerializer getInstance() {
    return JsonSerializerHolder._instance;
  }
  
  private static class JsonSerializerHolder {
    static JsonSerializer _instance = new JsonSerializer();
  }

  @SuppressWarnings("unchecked")
  public Object deserialize(ByteBuffer buffer, SerializerContext context) {
    if(!context.isRequest()){
      return JSON.parseObject(buffer.array(), context.getReturnclass());
    }
    if (context.getParameterTypes().length == 1)
      return  JSON.parseArray(SerializerFactory.bytesToString(buffer.array()),context.getParameterTypes()[0]).toArray();
    else
      return JSON.parseArray(SerializerFactory.bytesToString(buffer.array()),
          context.getParameterTypes()).toArray();
  }

 
  public ByteBuffer serialize(Object obj, SerializerContext context) {
    //Java全序列化支持方案
    return ByteBuffer.wrap(JSON.toJSONBytes(obj,SerializerFeature.WriteClassName));
  }
  
  public static void main(String args[]) {
    ISerializer serializer =SerializerFactory.getSerializer(SerializerFactory.SERIALIZER_JSON);
    Class<?>[] cls = new Class[]{int.class,String.class,Date.class,boolean.class};
    Object[] arg = new Object[]{1,"test",new Date(),true};
    SerializerContext context = new SerializerContext();
    context.setParameterTypes(cls);
    ByteBuffer buffer = serializer.serialize(arg, context);
    Object[]  obj = (Object[] )serializer.deserialize(buffer, context);
    for (int i = 0; i < arg.length; i++) {
      System.out.println(arg[i].getClass()+"="+arg[i]);
    }

  }

}
