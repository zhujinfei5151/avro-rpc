package code.google.dsf.server.netty;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import org.apache.avro.ipc.NettyTransportCodec.NettyDataPack;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.execution.ChannelEventRunnable;

import code.google.dsf.protocol.ProtocolPack;

/**
 * 
 * 将rpc request放入线程池中处理 其他事件通知不要放入线程池中处理，
 * 不然当线程池中任务很多时 会不能迅速得到响应
 * 
 * @author taohuifei
 * 
 */
public class NettyExecutionHandler extends SimpleChannelUpstreamHandler {

  private final Executor executor;

  public NettyExecutionHandler(Executor executor) {
    if (executor == null) {
      throw new NullPointerException("executor");
    }
    this.executor = executor;
  }


  public Executor getExecutor() {
    return executor;
  }

  @Override
  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    Object message = e.getMessage();
    if (!(message instanceof ProtocolPack) && !(message instanceof List)) {
      throw new Exception("receive message error,only support NettyDataPack || List");
    }
    handleRequest(ctx, e);
  }

 
  private void handleRequest(final ChannelHandlerContext ctx, MessageEvent e) {
    try {
      executor.execute(new ChannelEventRunnable(ctx, e));
    } catch (RejectedExecutionException exception) {
      Exception exp =
          new Exception("server threadpool full,maybe because server is slow or too many requests");
      if (e.getMessage() instanceof List) {
        List<NettyDataPack> requests = (List<NettyDataPack>) e.getMessage();
        for (final NettyDataPack request : requests) {
          sendErrorResponse(ctx, request, exp);
        }
      } else {
        sendErrorResponse(ctx, (NettyDataPack) e.getMessage(), exp);
      }
    }catch (Throwable e2) {
      e2.printStackTrace();
    }
  }

  private void sendErrorResponse(final ChannelHandlerContext ctx, final NettyDataPack dataPack,
      Object error) {
    // TODO
  }

}
