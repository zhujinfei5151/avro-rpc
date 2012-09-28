package code.google.dsf.server.netty;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import code.google.dsf.protocol.ProtocolPack;
import code.google.dsf.server.ProtocolHandlerFactory;

/**
 * 
 * 将rpc request放入线程池中处理 其他事件通知不要放入线程池中处理， 不然当线程池中任务很多时 会不能迅速得到响应
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
  public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
    e.getCause().printStackTrace();
    e.getChannel().close();
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
      executor.execute(new HandlerRunnable(ctx, e.getMessage(), executor));
    } catch (RejectedExecutionException exception) {
      Exception exp =
          new Exception("server threadpool full,maybe because server is slow or too many requests");
      if (e.getMessage() instanceof List) {
        List<ProtocolPack> requests = (List<ProtocolPack>) e.getMessage();
        for (final ProtocolPack request : requests) {
          sendErrorResponse(ctx, request, exp);
        }
      } else {
        sendErrorResponse(ctx, (ProtocolPack) e.getMessage(), exp);
      }
    } catch (Throwable e2) {
      e2.printStackTrace();
    }
  }

  private void sendErrorResponse(final ChannelHandlerContext ctx, final ProtocolPack dataPack,
      Object error) {
    dataPack.setDatas(ProtocolHandlerFactory.getProtocolHandler(dataPack.getContentType())
        .putRespond(error, dataPack.getContentType(), null));
    ChannelFuture wf = ctx.getChannel().write(dataPack);
    wf.addListener(new ChannelFutureListener() {
      public void operationComplete(ChannelFuture future) throws Exception {
        if (!future.isSuccess()) {
          // "server write response
        }
      }
    });

  }

  class HandlerRunnable implements Runnable {

    private ChannelHandlerContext ctx;

    private Object message;

    private Executor threadPool;

    public HandlerRunnable(ChannelHandlerContext ctx, Object message, Executor threadPool) {
      this.ctx = ctx;
      this.message = message;
      this.threadPool = threadPool;
    }

    @SuppressWarnings("rawtypes")
    public void run() {
      if (message instanceof List) {
        //System.out.println(((List) message).size());
        List messages = (List) message;
        for (Object messageObject : messages) {
          threadPool.execute(new HandlerRunnable(ctx, messageObject, threadPool));
        }
      } else {
        ProtocolPack dataPack = (ProtocolPack) message;
        List<ByteBuffer> result =
            ProtocolHandlerFactory.getProtocolHandler(dataPack.getProtocolType()).handleRequest(
                dataPack);
        if (result != null) {
          dataPack.setDatas(result);
          this.writeRespond(ctx.getChannel(), dataPack);
        }
      }
    }

    private void writeRespond(Channel channel, ProtocolPack dataPack) {
      ChannelFuture wf = channel.write(dataPack);
      wf.addListener(new ChannelFutureListener() {
        public void operationComplete(ChannelFuture future) throws Exception {
          if (!future.isSuccess()) {
            future.getCause().printStackTrace();
            // "server write response
          }
        }
      });
    }

  }


}
