package code.google.dsf.protocol;

import code.google.dsf.serialize.SerializerFactory;

public class ProtocolPackHeader {

  public static final int HEADLENGTH = 12;

  public final static byte DSF_MAGIC_NUMBER = (byte) 0xE1;

  protected int serial;

  protected byte messageType;

  protected byte protocolType;

  protected byte contentType = SerializerFactory.SERIALIZER_JSON;

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
