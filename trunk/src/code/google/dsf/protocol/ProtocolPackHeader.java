package code.google.dsf.protocol;

public class ProtocolPackHeader {

  public final static byte DSF_MAGIC_NUMBER = (byte) 0xE1;

  private int serial;
 
  private byte messageType;
  
  private byte protocolType;
  
  private byte contentType;

  public int getSerial() {
    return serial;
  }
  
  public static int getHeadLength() {
    return 12;
  }

  public void setSerial(int serial) {
    this.serial = serial;
  }

  public byte getMessageType() {
    return messageType;
  }

  public void setMessageType(byte messageType) {
    this.messageType = messageType;
  }

  public byte getProtocolType() {
    return protocolType;
  }

  public void setProtocolType(byte protocolType) {
    this.protocolType = protocolType;
  }

  public byte getContentType() {
    return contentType;
  }

  public void setContentType(byte contentType) {
    this.contentType = contentType;
  }
}
