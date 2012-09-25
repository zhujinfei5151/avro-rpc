package code.google.dsf.client;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import org.apache.avro.ipc.Callback;

/**
 * </p>发生流量限制 </p>
 * </p>主要针对异步发送时，发送请求速度过快，从而进行限量控制 </p>
 * @author taohuifei
 *
 */
@SuppressWarnings("rawtypes")
public class RequestFlowcontrolCallback implements Callback{
	static final int DEFAULT_IO_THREADS = Runtime.getRuntime().availableProcessors()+ 1;
	private static final int MAX_AVAILABLE = 1000;
    private static final Semaphore available = new Semaphore(MAX_AVAILABLE, true);
	private static ExecutorService executorService = Executors.newFixedThreadPool(DEFAULT_IO_THREADS);
	
	private Callback callback;
	
	public RequestFlowcontrolCallback(Callback callback) {
		try {
			available.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		this.callback = callback;
	}

	
	public void handleResult(final Object result) {
		available.release();
		if (callback != null) {
			callback.handleResult(result);
			/*
		
			executorService.execute(new Runnable() {
				@SuppressWarnings("unchecked")
        @Override
				public void run() {
					callback.handleResult(result);
				}
			});
		*/}
	}


	public void handleError(final Throwable error) {
		available.release();
		if(callback != null){
			executorService.execute(new Runnable() {
			
				public void run() {
					callback.handleError(error);
				}
			});
		}
	}
}
