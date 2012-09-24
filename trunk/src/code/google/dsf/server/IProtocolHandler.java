package code.google.dsf.server;

import java.nio.ByteBuffer;
import java.util.List;

import code.google.dsf.protocol.ProtocolPack;

public interface IProtocolHandler {
  
  public void registerProcessor(String beanName, Object instance);

  public List<ByteBuffer> handleRequest(ProtocolPack dataPack);
  
}
