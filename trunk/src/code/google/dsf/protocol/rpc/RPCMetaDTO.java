package code.google.dsf.protocol.rpc;

import java.nio.ByteBuffer;

import code.google.dsf.serialize.ISerializer;
import code.google.dsf.serialize.SerializerContext;

/**
 * RPC元数据
 * 
 */
public class RPCMetaDTO  {

  /**
   * 服务名
   */
  private String beanName;

  /**
   * 方法名
   */
  private String methodName;

  /**
   * session信息
   */
  private ByteBuffer session;
  
  private Class returnclass;


  public Object deserialize(ByteBuffer buffer,SerializerContext context) {
    int len = buffer.getInt();
    byte[] data = new byte[len];
    buffer.get(data);
    this.setBeanName(new String(data, ISerializer.DEFAULT_CHARSET));

    len = buffer.getInt();
    data = new byte[len];
    buffer.get(data);
    this.setMethodName(new String(data, ISerializer.DEFAULT_CHARSET));

    if (buffer.hasRemaining()) {
      len = buffer.remaining();
      data = new byte[len];
      buffer.get(data);
      ByteBuffer.wrap(data);
    }
    return null;
  }


  public ByteBuffer serialize(Object obj,SerializerContext context) {
    int len = this.beanName.length() + 4 + this.methodName.length() + 4;
    if(session != null){
      len = len +4 + this.session.limit();
    }
    ByteBuffer buffer = ByteBuffer.allocate(len);
    buffer.putInt(this.beanName.length());
    buffer.put(this.beanName.getBytes(ISerializer.DEFAULT_CHARSET));
    buffer.putInt(this.methodName.length());
    buffer.put(this.methodName.getBytes(ISerializer.DEFAULT_CHARSET));
    if (session != null) {
      buffer.putInt(this.session.limit());
      buffer.put(this.session);
    }
    else {
     // buffer.putInt(0);  
    }
    buffer.flip();
    return buffer;
  }

  public String getBeanName() {
    return beanName;
  }

  public void setBeanName(String beanName) {
    this.beanName = beanName;
  }

  public String getMethodName() {
    return methodName;
  }

  public void setMethodName(String methodName) {
    this.methodName = methodName;
  }

  public ByteBuffer getSession() {
    return session;
  }

  public void setSession(ByteBuffer session) {
    this.session = session;
  }

  public Class getReturnclass() {
    return returnclass;
  }

  public void setReturnclass(Class returnclass) {
    this.returnclass = returnclass;
  }

}
