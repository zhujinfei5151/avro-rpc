package code.google.dsf.test.performance;

import java.math.BigInteger;

import code.google.dsf.test.TestClient;

public class RPSTest {
  
  public static void main(String[] args) {
    String ip = "192.168.1.232";
    if(args != null && args.length > 0){
      ip = args[0];
    }
    TestClient client = new TestClient(ip,7001);
    PerformaceMutilThreadTest.doRun(client,1, BigInteger.valueOf(50000000));
  }

}
