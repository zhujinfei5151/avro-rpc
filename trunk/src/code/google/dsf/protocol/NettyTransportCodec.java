package code.google.dsf.protocol;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;



/**
 * 
 * Netty传输层解码和编码
 * 
 * @author taohuifei
 * 
 */
public class NettyTransportCodec {

  /**
   * 编码
   * @author taohuifei
   */
  public static class NettyFrameEncoder extends OneToOneEncoder {

    @Override
    protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg)
        throws Exception {

      ProtocolPack dataPack = (ProtocolPack) msg;
      List<ByteBuffer> origs = dataPack.getDatas();
      List<ByteBuffer> bbs = new ArrayList<ByteBuffer>(origs.size() * 2 + 1);
      // 消息头
      bbs.add(getPackHeader(dataPack));

      // 消息块
      for (ByteBuffer b : origs) {
        bbs.add(getLengthHeader(b));
        bbs.add(b);
      }
      ChannelBuffer buffer = ChannelBuffers.wrappedBuffer(bbs.toArray(new ByteBuffer[bbs.size()]));
      return buffer;
    }

    private ByteBuffer getPackHeader(ProtocolPack dataPack) {
      ByteBuffer header = ByteBuffer.allocate(ProtocolPack.getHeadLength());
      // 1. 一个字节的Magic Number
      header.put(ProtocolPack.DSF_MAGIC_NUMBER);
      // 2. 4个字节 Integer类型消息序号
      header.putInt(dataPack.getSerial());
      // 3. 一个字节的消息类型
      header.put(dataPack.getMessageType());
      // 4. 一个字节的协议类型
      header.put(dataPack.getProtocolType());
      // 5. 一个字节的编码类型
      header.put(dataPack.getContentType());
      // 6 4个字节的消息块数目
      header.putInt(dataPack.getDatas().size());
      header.flip();
      return header;
    }

    private ByteBuffer getLengthHeader(ByteBuffer buf) {
      ByteBuffer header = ByteBuffer.allocate(4);
      header.putInt(buf.limit());
      header.flip();
      return header;
    }
  }

  /**
   * 解码
   */
  public static class NettyFrameDecoder extends FrameDecoder {

    protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer in)
        throws Exception {
      if (in.readableBytes() < ProtocolPack.getHeadLength()) {
        return null;
      }

      in.markReaderIndex();

      byte magicNumber = in.readByte();
      // 检测 magic number
      if (magicNumber != ProtocolPack.DSF_MAGIC_NUMBER) {
        throw new Exception(channel.toString() + "消息格式不对,不符合规范。magicNumber =" + magicNumber);
      }
      int serial = in.readInt();
      byte messageType = in.readByte();
      byte protocolType = in.readByte();
      byte contentType = in.readByte();
      int listSize = in.readInt();

      // 检查是否收到完整的数据包
      int readindex = in.readerIndex();
      int pos = readindex;
      for (int i = 0; i < listSize; i++) {
        if (in.readableBytes() < 4) {
          in.resetReaderIndex();
          return null;
        }
        int length = in.readInt();
        pos = pos + 4;
        if (in.readableBytes() < length) {
          in.resetReaderIndex();
          return null;
        }
        pos = pos + length;
        in.readerIndex(pos);
      }
      in.readerIndex(readindex);

      ProtocolPack dataPack = new ProtocolPack();
      dataPack.setSerial(serial);
      dataPack.setMessageType(messageType);
      dataPack.setProtocolType(protocolType);
      dataPack.setContentType(contentType);
      List<ByteBuffer> datas = new LinkedList<ByteBuffer>();
      dataPack.setDatas(datas);
      for (int i = 0; i < listSize; i++) {
        int length = in.readInt();
        ByteBuffer bb = ByteBuffer.allocate(length);
        in.readBytes(bb);
        bb.flip();
        dataPack.getDatas().add(bb);
      }
      return dataPack;
    }
  }

}
