package code.google.dsf.test.httptest;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import code.google.dsf.http.HttpInvokerService;

public class TestServlet implements Servlet {

  private HttpInvokerService httpInvokerService;

  public void destroy() {
    // TODO Auto-generated method stub

  }

  public ServletConfig getServletConfig() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getServletInfo() {
    // TODO Auto-generated method stub
    return null;
  }

  public void init(ServletConfig arg0) throws ServletException {
    httpInvokerService = new HttpInvokerService();
  }

  public void service(ServletRequest arg0, ServletResponse arg1) throws ServletException,
      IOException {
    httpInvokerService.handleRequest((HttpServletRequest) arg0, (HttpServletResponse) arg1);
  }

}
