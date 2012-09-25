package code.google.dsf.server.rpc;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avro.generic.GenericRecord;

import code.google.dsf.protocol.ProtocolPack;
import code.google.dsf.protocol.rpc.RPCMetaDTO;
import code.google.dsf.serialize.SerializerContext;
import code.google.dsf.serialize.SerializerFactory;

public class RPCProtocolHandler implements IRPCProtocolHandler {
  
  static final ByteBuffer RESPOND_TRUE = ByteBuffer.allocate(1);
  static final ByteBuffer RESPOND_FALSE = ByteBuffer.allocate(1);
  
  static {
    RESPOND_TRUE.put(TRUE);
    RESPOND_TRUE.flip();
    RESPOND_FALSE.put(FALSE);
    RESPOND_FALSE.flip();
  }

  public static RPCProtocolHandler INSTANCE = new RPCProtocolHandler();

  private static final String SLIP_BEAN_METHOD = "#";

  private static Map<String, Object> processors = new HashMap<String, Object>();

  private static Map<String, Method> cacheMethods = new HashMap<String, Method>();

  public void registerProcessor(String beanName, Object instance) {
    processors.put(beanName, instance);
    Class<?> instanceClass = instance.getClass();
    Method[] methods = instanceClass.getMethods();
    for (Method method : methods) {
      cacheMethods.put(beanName + SLIP_BEAN_METHOD + method.getName(), method);
    }
  }

  @Override
  public List<ByteBuffer> handleRequest(ProtocolPack dataPack) {
    try {
      // 先解析RPC元数据
      ByteBuffer metabuffer = dataPack.getDatas().get(0);
      RPCMetaDTO rcpMeta = new RPCMetaDTO();
      rcpMeta.deserialize(metabuffer, null);

      Method method =
          cacheMethods.get(rcpMeta.getBeanName() + SLIP_BEAN_METHOD + rcpMeta.getMethodName());
      if (method == null) {
        throw new Exception(rcpMeta.getBeanName() + SLIP_BEAN_METHOD + rcpMeta.getMethodName()
            + "服务方法不存在！");
      }

      // 将参数数据反序列化
      SerializerContext context = new SerializerContext();
      context.setParameterTypes(method.getParameterTypes());
      context.setRpcMetaDTO(rcpMeta);
      Object args = null;
      if( dataPack.getDatas().size() > 1){
        args =
          SerializerFactory.getSerializer(dataPack.getContentType()).deserialize(
              dataPack.getDatas().get(1), context);
      }
      
      Object instance = processors.get(rcpMeta.getBeanName());
      // AVRO paramers
      if(args instanceof GenericRecord){
        Object[] paramers = new Object[method.getParameterTypes().length];
        for(int i = 0; i < paramers.length; i++){
          paramers[i] = ((GenericRecord) args).get(i);
        }
        Object result = method.invoke(instance, paramers);
        return putRespond(result, dataPack.getContentType(), rcpMeta);
      }
      else{
        Object result = method.invoke(instance, (Object[])args);
        return putRespond(result, dataPack.getContentType(), rcpMeta);
      }
    } catch (Exception e) {
      return putRespond(e, dataPack.getContentType(), null);
    }
  }

  /**
   * 将返回结果序列化
   * 
   * @param result
   * @return
   */
  public List<ByteBuffer> putRespond(Object result, byte contentType, RPCMetaDTO rcpMeta) {
    List<ByteBuffer> buffers = new ArrayList<ByteBuffer>();
    // 无返回值情况
    if (result == null) {
      buffers.add(RESPOND_TRUE);
      return buffers;
    }
    if (result instanceof Throwable) {
      buffers.add(RESPOND_FALSE);
      String errormsg = ((Throwable) result).getMessage();
      ByteBuffer data = ByteBuffer.wrap(SerializerFactory.stringToBytes(errormsg));
      buffers.add(data);
      return buffers;
    } else {
      buffers.add(RESPOND_TRUE);
      SerializerContext context = new SerializerContext();
      context.setRequest(false);
      context.setRpcMetaDTO(rcpMeta);
      ByteBuffer data = (SerializerFactory.getSerializer(contentType).serialize(result, context));
      buffers.add(data);
      return buffers;
    }
    
  }

}
