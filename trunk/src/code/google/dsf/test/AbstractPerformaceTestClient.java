package code.google.dsf.test;

import java.io.IOException;

import org.apache.avro.ipc.Callback;

import code.google.dsf.client.ITransceiver;
import code.google.dsf.client.NettyTransceiver;
import code.google.dsf.test.performance.IPerformanceTestTask;

@SuppressWarnings("rawtypes")
public abstract class AbstractPerformaceTestClient implements Callback, IPerformanceTestTask {

  public static ITransceiver transceiver;

  static {
    try {
      transceiver = new NettyTransceiver();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  protected Callback callBack;


  public void doInitialize() {}



  public void setCallback(Callback callback) {
    this.callBack = callback;
  }


  public void setSession() {

  }


  public void doEnd() {

  }


  public IPerformanceTestTask getNewTask() {
    return null;
  }


  public boolean onlyoneTask() {
    return true;
  }

  @SuppressWarnings("unchecked")
  public void handleError(Throwable arg0) {
    if (this.callBack != null) {
      this.callBack.handleError(arg0);
    }
  }

  @SuppressWarnings("unchecked")
  public void handleResult(Object arg0) {
    if (this.callBack != null) {
      this.callBack.handleResult(arg0);
    }
  }

}
