package code.google.dsf.protocol;

import java.io.DataInputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;


/**
 * </p> 协议包结构有消息头和消息体组成</p>
 * 
 * </p>消息头占12个字节</p> </p> =================================================</p>
 * 
 * </p>Offset: 0 1 5 6 7 8 12 </p> </p> +--+--------+--+--+--+----------+</p> </p>Fields: |a | b |c
 * |d |e | f |</p> </p> +--+--------+--+--+--+----------+</p> </p> a: DSF_MAGIC_NUMBER 魔法数， 一个字节</p>
 * </p> b: serial 消息序号 4个字节</p> </p> c: messageType 消息类型</p> </p> d: protocolType 协议类型</p> </p> e:
 * contentType 编码类型</p> </p> f: 消息块的数目 </p>
 * 
 * </p> 消息体 一个消息块的结构，可以有多个消息块</p> </p> =================================================</p>
 * 
 * </p> Offset: 0 4 (Length + 4)</p> </p> +--------+------------------------+</p> </p> Fields: |
 * Length | Actual message content |</p> </p> +--------+------------------------+</p>
 * 
 */
public class ProtocolPack extends ProtocolPackHeader {

  /**
   * 消息体
   */
  private List<ByteBuffer> datas;

  public List<ByteBuffer> getDatas() {
    return datas;
  }

  public void setDatas(List<ByteBuffer> datas) {
    this.datas = datas;
  }

  public ByteBuffer encode() {
    int len = ProtocolPackHeader.HEADLENGTH;

    int idatasize = this.datas.size();
    for (int i = 0; i < idatasize; i++) {
      len = len + 4 + this.datas.get(i).limit();
    }
    ByteBuffer buffer = ByteBuffer.allocate(len);
    // 1. 一个字节的Magic Number
    buffer.put(ProtocolPack.DSF_MAGIC_NUMBER);
    // 2. 4个字节 Integer类型消息序号
    buffer.putInt(this.serial);
    // 3. 一个字节的消息类型
    buffer.put(this.messageType);
    // 4. 一个字节的协议类型
    buffer.put(this.protocolType);
    // 5. 一个字节的编码类型
    buffer.put(this.contentType);
    // 6 4个字节的消息块数目
    buffer.putInt(idatasize);

    // datas
    for (int i = 0; i < idatasize; i++) {
      ByteBuffer data = this.datas.get(i);
      int ilen = data.limit();
      buffer.putInt(ilen);
      System.arraycopy(data.array(), 0, buffer.array(), buffer.position(), ilen);
      buffer.position(buffer.position() + ilen);
    }
    buffer.flip();
    return buffer;

  }

  public static ProtocolPack decode(DataInputStream in) throws Exception {
    byte magicNumber = in.readByte();
    // 检测 magic number
    if (magicNumber != ProtocolPack.DSF_MAGIC_NUMBER) {
      throw new Exception("消息格式不对,不符合规范。magicNumber =" + magicNumber);
    }
    int serial = in.readInt();
    byte messageType = in.readByte();
    byte protocolType = in.readByte();
    byte contentType = in.readByte();
    int listSize = in.readInt();
    List<ByteBuffer> datas = new ArrayList<ByteBuffer>();
    for (int i = 0; i < listSize; i++) {
      int length = in.readInt();
      byte[] buffer = new byte[length];
      in.read(buffer);
      datas.add(ByteBuffer.wrap(buffer));
    }
    ProtocolPack pack = new ProtocolPack();
    pack.setContentType(contentType);
    pack.setProtocolType(protocolType);
    pack.setMessageType(messageType);
    pack.setSerial(serial);
    pack.setDatas(datas);
    return pack;
  }
}
