package code.google.dsf.test.performance;

import java.math.BigInteger;

import code.google.dsf.test.TestClient;

public class RPSTest {
  
  public static void main(String[] args) {
    TestClient client = new TestClient("192.168.1.232",7001);
    PerformaceMutilThreadTest.doRun(client,1, BigInteger.valueOf(50000000));
  }

}
