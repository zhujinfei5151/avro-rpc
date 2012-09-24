package code.google.dsf.client;

import org.apache.avro.ipc.Callback;

/**
 * RPC接口抽象
 * @author taohuifei
 *
 */
public interface IClient {

    /**
     * 同步调用
     * @param beanName 服务实例名称
     * @param methodName 方法名称
     * @param argTypes 方法类型列表
     * @param args 参数
     * @param returnclass 返回值类型
     * @param contentType 序列化类型
     * @return
     * @throws Exception
     */
	public Object invokeSync(String beanName,String methodName, Class[] argTypes,
			Object[] args, Class returnclass, byte contentType) throws Exception;

	/**
	 * 异步调用
	 * @param beanName 服务实例名称
	 * @param methodName 方法名称
	 * @param argTypes 方法类型列表
	 * @param args 参数
	 * @param returnclass 返回值类型
	 * @param contentType 序列化类型
	 * @param callback 回调接口
	 * @throws Exception
	 */
	public void invokeAsync(String beanName,String methodName, Class[] argTypes, Object[] args,
			Class returnclass, byte contentType, Callback callback)
			throws Exception;
}
