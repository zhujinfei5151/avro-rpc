package code.google.dsf.server.rpc;

import code.google.dsf.server.IProtocolHandler;

public interface IRPCProtocolHandler extends IProtocolHandler {
  
  public static final byte TRUE = (byte) 0x00;
  
  public static final byte FALSE = (byte) 0x01;
}
