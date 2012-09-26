package code.google.dsf.server.netty;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.DefaultChannelPipeline;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import code.google.dsf.protocol.NettyTransportCodec.NettyFrameDecoder;
import code.google.dsf.protocol.NettyTransportCodec.NettyFrameEncoder;

public class NettyServer {

  private final ServerBootstrap bootstrap;
  private final ChannelFactory channelFactory;

  /**
   * socket参数选项
   */
  public static final long DEFAULT_CONNECTION_TIMEOUT_MILLIS = 60 * 1000L;
  public static final String NETTY_CONNECT_TIMEOUT_OPTION = "connectTimeoutMillis";
  public static final String NETTY_TCP_NODELAY_OPTION = "tcpNoDelay";
  public static final boolean DEFAULT_TCP_NODELAY_VALUE = true;

  public static final String NETTY_TCP_receiveBufferSize_OPTION = "receiveBufferSize";
  public static final int DEFAULT_TCP_receiveBufferSize_VALUE = 65536;

  public static final String NETTY_TCP_sendBufferSize_OPTION = "sendBufferSize";
  public static final int DEFAULT_TCP_sendBufferSize_VALUE = 65536;


  public NettyServer() {
    channelFactory =
        new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
            Executors.newCachedThreadPool());
    bootstrap = new ServerBootstrap(channelFactory);

    bootstrap.setOption(NETTY_TCP_receiveBufferSize_OPTION, DEFAULT_TCP_receiveBufferSize_VALUE);
    bootstrap.setOption(NETTY_TCP_sendBufferSize_OPTION, DEFAULT_TCP_sendBufferSize_VALUE);

    bootstrap.setOption("child.tcpNoDelay", true);
    bootstrap.setOption("child." + NETTY_TCP_receiveBufferSize_OPTION,
        DEFAULT_TCP_receiveBufferSize_VALUE);
    bootstrap.setOption("child." + NETTY_TCP_sendBufferSize_OPTION,
        DEFAULT_TCP_sendBufferSize_VALUE);
  }

  public void start(int listenPort, final ExecutorService threadPool) throws Exception {
    bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
      public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline p = new DefaultChannelPipeline();
        p.addLast("frameDecoder", new NettyFrameDecoder());
        p.addLast("frameEncoder", new NettyFrameEncoder());
        // 线程池处理
        p.addLast("executor", new NettyExecutionHandler(threadPool));
        //p.addLast("handler", new NettyServerHandler());
        return p;
      }
    });
    bootstrap.bind(new InetSocketAddress(listenPort));
    System.out.println("NettyServer started,listen at:" + listenPort);
  }

  public void stop() throws Exception {
    bootstrap.releaseExternalResources();
  }


}
