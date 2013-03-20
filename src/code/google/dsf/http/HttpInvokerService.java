package code.google.dsf.http;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import code.google.dsf.protocol.ProtocolPack;
import code.google.dsf.server.rpc.RPCProtocolHandler;

public class HttpInvokerService {

  private static RPCProtocolHandler rpcProtocolHandler = RPCProtocolHandler.INSTANCE;

  public void handleRequest(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    List<ByteBuffer> returnbuff;
    ProtocolPack pack = null;
    try {
      pack = ProtocolPack.decode(new DataInputStream(request.getInputStream()));
      returnbuff = rpcProtocolHandler.handleRequest(pack);
      pack.setDatas(returnbuff);
    } catch (Exception e) {
      returnbuff = rpcProtocolHandler.putRespond(e, (byte) 0, null);
    }
    if (pack == null) {
      pack = new ProtocolPack();
      pack.setDatas(returnbuff);
    }
    ByteBuffer buffer = pack.encode();
    ByteArrayOutputStream baos = new ByteArrayOutputStream(buffer.limit());
    baos.write(buffer.array());
    baos.writeTo(response.getOutputStream());
  }

}
