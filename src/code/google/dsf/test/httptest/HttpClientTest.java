package code.google.dsf.test.httptest;

import java.lang.reflect.Proxy;

import code.google.dsf.http.HttpInvokerClientInvocation;
import code.google.dsf.serialize.SerializerFactory;
import code.google.dsf.test.ITest;
import code.google.dsf.test.TestDTO;
import code.google.dsf.test.TestServerImp;

public class HttpClientTest {
  private HttpInvokerClientInvocation httpInvokerClientInvocation;
  
  private ITest remoteTest;
  
  public HttpClientTest() {
    this.httpInvokerClientInvocation = new HttpInvokerClientInvocation("http://127.0.0.1:8080/remoting",TestServerImp.BEANAME,SerializerFactory.SERIALIZER_HESSIAN);
    this.getProxy();
  }
  
  private void getProxy() {
    remoteTest = (ITest) Proxy.newProxyInstance(HttpClientTest.class.getClassLoader(),new Class[] {ITest.class}, httpInvokerClientInvocation);
  }
  
  public static void main(String[] args) throws Exception {
    HttpClientTest test = new HttpClientTest();
    System.out.println(test.remoteTest.testSimpletypes(1, "hello"));
    TestDTO dto = new TestDTO();
    dto.setFlowSize(1000.90);
    dto.setImei("dddddaAzz");
    TestDTO newdto = test.remoteTest.testReturnDTO(dto);
    System.out.println(newdto.getFlowSize());
  }
}
