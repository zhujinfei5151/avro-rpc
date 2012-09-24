package code.google.dsf.serialize;

import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import org.apache.avro.util.ByteBufferInputStream;

/**
 * JAVA序列化
 * @author taohuifei
 *
 */
public class JavaSerializer implements ISerializer {
  
  private JavaSerializer(){
    
  }
  
  public static JavaSerializer getInstance() {
    return JavaSerializerHolder._instance;
  }
  
  private static class JavaSerializerHolder {
    static JavaSerializer _instance = new JavaSerializer();
  }

  @Override
  public Object deserialize(ByteBuffer buffer, SerializerContext context) {
    try {
      if(buffer == null || buffer.limit() == 0){
          return null;
      }
      List<ByteBuffer> lists = new LinkedList<ByteBuffer>();
      lists.add(buffer);
      ByteBufferInputStream buffinputStream = new ByteBufferInputStream(lists);
      ObjectInputStream inputStream = new ObjectInputStream(buffinputStream);
      Object object = inputStream.readObject();
      buffinputStream.close();
      return object;
  } catch (Exception e) {
      throw new RuntimeException(e.getMessage(),e.getCause());
  }
  }

  @Override
  public ByteBuffer serialize(Object obj, SerializerContext context) {
    try {
      ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
      ObjectOutputStream stream = new ObjectOutputStream(byteArrayOS);
      stream.writeObject(obj);
      stream.close();
      return ByteBuffer.wrap(byteArrayOS.toByteArray());
  } catch (Exception e) {
      throw new RuntimeException(e.getMessage(),e.getCause());
  }
  }

}
