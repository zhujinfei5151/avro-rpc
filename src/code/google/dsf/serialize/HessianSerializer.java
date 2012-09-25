package code.google.dsf.serialize;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.util.ByteBufferInputStream;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;

/**
 * Hessian序列化
 * @author taohuifei
 *
 */
public class HessianSerializer implements ISerializer {

  private HessianSerializer() {
  }

  public static HessianSerializer getInstance() {
    return HessianSerializerHolder._instance;
  }

  private static class HessianSerializerHolder {
    static HessianSerializer _instance = new HessianSerializer();
  }

  @Override
  public Object deserialize(ByteBuffer buffer, SerializerContext context) {
    if (buffer == null || buffer.limit() == 0) {
      return null;
    }
    List<ByteBuffer> lists = new ArrayList<ByteBuffer>();
    lists.add(buffer);
    ByteBufferInputStream buffinputStream = new ByteBufferInputStream(lists);
    try {
      Hessian2Input hessin = new Hessian2Input(buffinputStream);
      Object object = hessin.readObject();
      hessin.close();
      return object;
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e.getCause());
    } 
  }

  @Override
  public ByteBuffer serialize(Object obj, SerializerContext context) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try {
      Hessian2Output hessout = new Hessian2Output(out);
      hessout.writeObject(obj);
      hessout.close();
      return ByteBuffer.wrap(out.toByteArray());
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e.getCause());
    } finally {}
  }


}
