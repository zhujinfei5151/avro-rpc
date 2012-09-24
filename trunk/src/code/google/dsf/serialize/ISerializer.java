package code.google.dsf.serialize;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * 序列化接口
 * 扩展的序列化，需要实现此接口
 * @author taohuifei
 *
 */
public interface ISerializer {
  
  public static Charset DEFAULT_CHARSET =  Charset.forName("UTF-8");
  
  public Object deserialize(ByteBuffer buffer,SerializerContext context);
  
  public ByteBuffer serialize(Object obj,SerializerContext context);

}
