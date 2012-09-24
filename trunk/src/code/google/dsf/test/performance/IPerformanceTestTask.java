package code.google.dsf.test.performance;

import org.apache.avro.ipc.Callback;

public interface IPerformanceTestTask {
	
	/**
	 * 执行前处理
	 */
	public void doInitialize();
	
	/**
	 * 执行
	 * @throws Exception 
	 */
	public void doAction() throws Exception;
	
	/**
	 * 异步回调
	 * @param callback
	 */
	@SuppressWarnings("rawtypes")
  public void setCallback(Callback callback);
	
	public void setSession(); 
	
	/**
	 * 执行后处理
	 */
	public void doEnd();
	
	
	/**
	 * 
	 * @return
	 */
	public IPerformanceTestTask getNewTask();
	
	public boolean onlyoneTask();
	

}
