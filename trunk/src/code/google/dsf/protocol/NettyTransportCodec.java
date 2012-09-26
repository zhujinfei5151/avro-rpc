package code.google.dsf.protocol;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
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
   * 
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
  public static class NettyFrameDecoder  extends SimpleChannelUpstreamHandler {

    private final boolean unfold;
    private ChannelBuffer cumulation;

    public NettyFrameDecoder() {
        this(false);
    }

    public NettyFrameDecoder(boolean unfold) {
        this.unfold = unfold;
    }

    @Override
    public void messageReceived(
            ChannelHandlerContext ctx, MessageEvent e) throws Exception {

        Object m = e.getMessage();
        if (!(m instanceof ChannelBuffer)) {
            ctx.sendUpstream(e);
            return;
        }

        ChannelBuffer input = (ChannelBuffer) m;
        if (!input.readable()) {
            return;
        }

        ChannelBuffer cumulation = cumulation(ctx);
        if (cumulation.readable()) {
            cumulation.discardReadBytes();
            cumulation.writeBytes(input);
            callDecode(ctx, e.getChannel(), cumulation, e.getRemoteAddress());
        } else {
            callDecode(ctx, e.getChannel(), input, e.getRemoteAddress());
            if (input.readable()) {
                cumulation.writeBytes(input);
            }
        }
    }

    @Override
    public void channelDisconnected(
            ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        cleanup(ctx, e);
    }

    @Override
    public void channelClosed(
            ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        cleanup(ctx, e);
    }

    @Override
    public void exceptionCaught(
            ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        ctx.sendUpstream(e);
    }

    /**
     * Decodes the received data so far into a frame when the channel is
     * disconnected.
     *
     * @param ctx      the context of this handler
     * @param channel  the current channel
     * @param buffer   the cumulative buffer of received packets so far.
     *                 Note that the buffer might be empty, which means you
     *                 should not make an assumption that the buffer contains
     *                 at least one byte in your decoder implementation.
     *
     * @return the decoded frame if a full frame was received and decoded.
     *         {@code null} if there's not enough data in the buffer to decode a frame.
     */
    protected Object decodeLast(
            ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception {
        return decode(ctx, channel, buffer);
    }

    private void callDecode(
            ChannelHandlerContext context, Channel channel,
            ChannelBuffer cumulation, SocketAddress remoteAddress) throws Exception {
        List<Object> results = new ArrayList<Object>();
        while (cumulation.readable()) {
            int oldReaderIndex = cumulation.readerIndex();
            Object frame = decode(context, channel, cumulation);
            if (frame == null) {
                if (oldReaderIndex == cumulation.readerIndex()) {
                    // Seems like more data is required.
                    // Let us wait for the next notification.
                    break;
                } else {
                    // Previous data has been discarded.
                    // Probably it is reading on.
                    continue;
                }
            } else if (oldReaderIndex == cumulation.readerIndex()) {
                throw new IllegalStateException(
                        "decode() method must read at least one byte " +
                        "if it returned a frame (caused by: " + getClass() + ")");
            }
            results.add(frame);
        }
        if(results.size() > 0)
            unfoldAndFireMessageReceived(context, remoteAddress, results);
    }

    private void unfoldAndFireMessageReceived(ChannelHandlerContext context, SocketAddress remoteAddress, Object result) {
        if (unfold) {
            if (result instanceof Object[]) {
                for (Object r: (Object[]) result) {
                    Channels.fireMessageReceived(context, r, remoteAddress);
                }
            } else if (result instanceof Iterable<?>) {
                for (Object r: (Iterable<?>) result) {
                    Channels.fireMessageReceived(context, r, remoteAddress);
                }
            } else {
                Channels.fireMessageReceived(context, result, remoteAddress);
            }
        } else {
            Channels.fireMessageReceived(context, result, remoteAddress);
        }
    }

    private void cleanup(ChannelHandlerContext ctx, ChannelStateEvent e)
            throws Exception {
        try {
            ChannelBuffer cumulation = this.cumulation;
            if (cumulation == null) {
                return;
            } else {
                this.cumulation = null;
            }

            if (cumulation.readable()) {
                // Make sure all frames are read before notifying a closed channel.
                callDecode(ctx, ctx.getChannel(), cumulation, null);
            }

            // Call decodeLast() finally.  Please note that decodeLast() is
            // called even if there's nothing more to read from the buffer to
            // notify a user that the connection was closed explicitly.
            Object partialFrame = decodeLast(ctx, ctx.getChannel(), cumulation);
            if (partialFrame != null) {
                unfoldAndFireMessageReceived(ctx, null, partialFrame);
            }
        } finally {
            ctx.sendUpstream(e);
        }
    }

    private ChannelBuffer cumulation(ChannelHandlerContext ctx) {
        ChannelBuffer c = cumulation;
        if (c == null) {
            c = ChannelBuffers.dynamicBuffer(
                    ctx.getChannel().getConfig().getBufferFactory());
            cumulation = c;
        }
        return c;
    }


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
      List<ByteBuffer> datas = new ArrayList<ByteBuffer>();
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
