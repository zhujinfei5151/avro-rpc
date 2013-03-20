package code.google.dsf.test.httptest;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import code.google.dsf.server.ProtocolHandlerFactory;
import code.google.dsf.test.TestServerImp;

public class JettyServerMain {
  
  public static void main(String[] args) {
    
   //注册RPC服务实体
    ProtocolHandlerFactory.getProtocolHandler(ProtocolHandlerFactory.PROTOCOLHANDLER_RPC)
        .registerProcessor(TestServerImp.BEANAME, new TestServerImp());
    
    JettyServerMain test = new JettyServerMain();
    Server server = new Server(8080);
    ServletContextHandler context = new ServletContextHandler(
            ServletContextHandler.SESSIONS);
    context.setContextPath("/");
    context.addServlet(new ServletHolder(new TestServlet()),
            "/remoting");
    ResourceHandler resource = new ResourceHandler();
    HandlerList handlers = new HandlerList();
    handlers.setHandlers(new Handler[] { resource, context });
    server.setHandler(handlers);
    try {
        server.start();
        server.join();
    } catch (Exception e) {
        e.printStackTrace();
    }
    
  
}
  
  

}
