package code.google.dsf.server.netty;

import java.nio.ByteBuffer;
import java.util.List;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import code.google.dsf.protocol.ProtocolPack;
import code.google.dsf.server.ProtocolHandlerFactory;

public class NettyServerHandler extends SimpleChannelUpstreamHandler {

  @Override
  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
      if (!(e.getMessage() instanceof ProtocolPack)) {
          return;
      }
      ProtocolPack dataPack = (ProtocolPack)e.getMessage();
      List<ByteBuffer> result = ProtocolHandlerFactory.getProtocolHandler(dataPack.getProtocolType()).handleRequest(dataPack);
      if(result != null){
        dataPack.setDatas(result);
        this.writeRespond(ctx.getChannel(), dataPack);
      }
  }
  
  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
    e.getCause().printStackTrace();
    e.getChannel().close();
  }
  
  private void writeRespond(Channel channel,ProtocolPack dataPack) {
    ChannelFuture wf = channel.write(dataPack);
    wf.addListener(new ChannelFutureListener() {
        public void operationComplete(ChannelFuture future)
                throws Exception {
            if (!future.isSuccess()) {
               // "server write response 
            }
        }
    });  
  }

}
