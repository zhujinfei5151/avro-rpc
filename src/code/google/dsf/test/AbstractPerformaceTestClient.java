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

  @Override
  public void doInitialize() {}


  @Override
  public void setCallback(Callback callback) {
    this.callBack = callback;
  }

  @Override
  public void setSession() {

  }

  @Override
  public void doEnd() {

  }

  @Override
  public IPerformanceTestTask getNewTask() {
    return null;
  }

  @Override
  public boolean onlyoneTask() {
    return true;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void handleError(Throwable arg0) {
    if (this.callBack != null) {
      this.callBack.handleError(arg0);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void handleResult(Object arg0) {
    if (this.callBack != null) {
      this.callBack.handleResult(arg0);
    }
  }

}
