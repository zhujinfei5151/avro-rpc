package code.google.dsf.test.performance;

import java.math.BigInteger;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.avro.ipc.Callback;

@SuppressWarnings("rawtypes")
/**
 * RPC性能测试工具类
 */
public class PerformaceMutilThreadTest implements Callback {

  private IPerformanceTestTask client;

  public int THREADNUMBER = 1;

  public BigInteger LOOP = new BigInteger("10000000000000");

  private CountDownLatch begin;

  private CountDownLatch end;

  private AtomicLong totalrequestcount = new AtomicLong(0);

  private AtomicLong requestcount = new AtomicLong(0);

  private long avgRequesttime = 0;

  private AtomicLong requestfailcount = new AtomicLong(0);

  private long prerequestcount = 0;

  private long startuptime = 0;

  private long begintime = 0;

  private int monitoringcount = 0;

  public final int secondstep = 5;

  private long firsttime;

  private String ip;

  private int port;

  private ScheduledExecutorService scheduleTPS = Executors.newScheduledThreadPool(1);

  public PerformaceMutilThreadTest(IPerformanceTestTask test) {
    this.client = test;
  }

  private PerformaceMutilThreadTest getthis() {
    return this;
  }

  public void beginTest() {

    begin = new CountDownLatch(THREADNUMBER);

    end = new CountDownLatch(THREADNUMBER);

    startuptime = System.currentTimeMillis();
    ThreadPoolExecutor threadpool = (ThreadPoolExecutor) Executors.newFixedThreadPool(THREADNUMBER);
    begintime = System.currentTimeMillis();
    this.firsttime = this.begintime;
    scheduleTPS.scheduleWithFixedDelay(new TPSmonitoring(), 3, secondstep, TimeUnit.SECONDS);
    {

      try {
        client.doInitialize();
        Thread.sleep(1000);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      for (int i = 0; i < THREADNUMBER; i++) {
        threadpool.execute(new ThreadWork(client));
      }
    }
    try {
      end.await();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    System.out.println(requestcount.get());
    System.out.println(totalrequestcount.get());
    client.doEnd();
    threadpool.shutdown();
    scheduleTPS.shutdownNow();
  }

  private class ThreadWork implements Runnable {

    private IPerformanceTestTask client;

    public ThreadWork(IPerformanceTestTask client) {
      if (client.onlyoneTask())
        this.client = client;
      else {
        this.client = client.getNewTask();
        this.client.doInitialize();
        this.client.setCallback(getthis());
      }
    }


    public void run() {
      begin.countDown();
      client.setSession();
      try {
        begin.await();
      } catch (InterruptedException e) {
        // e.printStackTrace();
      }
      try {
        for (BigInteger i = new BigInteger("0"); i.compareTo(LOOP) < 0; i =
            i.add(new BigInteger("1"))) {
          try {
            client.doAction();
            totalrequestcount.incrementAndGet();
            // requestcount.incrementAndGet();
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      } finally {

        end.countDown();

      }
    }
  }

  private class TPSmonitoring implements Runnable {

    @Override
    public void run() {
      monitoringcount = monitoringcount + 1;
      double time = (System.currentTimeMillis() - startuptime) / 60000;
      long howlong = (System.currentTimeMillis() - begintime) / 1000;
      long allhowlong = (System.currentTimeMillis() - firsttime) / 1000;
      long count = requestcount.get() - prerequestcount;
      prerequestcount = requestcount.get();
      begintime = System.currentTimeMillis();
      System.out.println(time + "==" + monitoringcount + "   当前 RPS=" + count / howlong
          + "   平均 RPS=" + (prerequestcount / allhowlong) + "  成功请求==" + requestcount.get()
          + "  总请求:" + totalrequestcount.get() + "  失败请求:" + requestfailcount.get());
    }

  }

  public static void doRun(IPerformanceTestTask runtest, int threadnumber,
      BigInteger rptcount) {
    PerformaceMutilThreadTest test = new PerformaceMutilThreadTest(runtest);
    runtest.setCallback(test);
    test.THREADNUMBER = threadnumber;
    test.LOOP = rptcount;
    System.out.println("RPC表示每秒处理请求数，监控线程会每"+test.secondstep+"秒,打印当前PRS及平均RPS");
    System.out.println("并发线程:" + test.THREADNUMBER);
    test.beginTest();
  }



  @Override
  public void handleResult(Object result) {
    requestcount.incrementAndGet();

  }

  @Override
  public void handleError(Throwable error) {
    requestfailcount.incrementAndGet();

  }


}
