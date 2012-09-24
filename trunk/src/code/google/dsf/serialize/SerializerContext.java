package code.google.dsf.serialize;

import org.apache.avro.Schema;

import code.google.dsf.protocol.rpc.RPCMetaDTO;

/**
 * 序列化上下文参数
 * @author Admin
 *
 */
public class SerializerContext {
  
  /**
   * avro 模式文件 avro序列化及反序列化时需要
   */
  private Schema schema;
  
  /**
   * 参数类型 josn反序列化时需要
   */
  @SuppressWarnings("rawtypes")
  private Class[] parameterTypes;
  
  @SuppressWarnings("rawtypes")
  private Class returnclass;
  
  private RPCMetaDTO rpcMetaDTO;
  
  /**
   * 是否请求数据序列化 否则返回结果序列化
   */
  private boolean isRequest = true;

  public Schema getSchema() {
    return schema;
  }

  public void setSchema(Schema schema) {
    this.schema = schema;
  }

  @SuppressWarnings("rawtypes")
  public Class[] getParameterTypes() {
    return parameterTypes;
  }

  @SuppressWarnings("rawtypes")
  public void setParameterTypes(Class[] parameterTypes) {
    this.parameterTypes = parameterTypes;
  }

  public RPCMetaDTO getRpcMetaDTO() {
    return rpcMetaDTO;
  }

  public void setRpcMetaDTO(RPCMetaDTO rpcMetaDTO) {
    this.rpcMetaDTO = rpcMetaDTO;
  }

  @SuppressWarnings("rawtypes")
  public Class getReturnclass() {
    return returnclass;
  }

  @SuppressWarnings("rawtypes")
  public void setReturnclass(Class returnclass) {
    this.returnclass = returnclass;
  }

  public boolean isRequest() {
    return isRequest;
  }

  public void setRequest(boolean isRequest) {
    this.isRequest = isRequest;
  }

}
