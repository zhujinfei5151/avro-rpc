package code.google.dsf.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.avro.ipc.Callback;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.TimerTask;

import code.google.dsf.protocol.NettyTransportCodec.NettyFrameDecoder;
import code.google.dsf.protocol.NettyTransportCodec.NettyFrameEncoder;
import code.google.dsf.protocol.ProtocolPack;

/**
 * 基于Netty实现的客户端传输层
 * 应用层传入IP和端口及要传输的数据，传输层自动创建连接，将数据发给目标服务端。
 * 当服务端返回数据后，传输才会回调应用层的回调函数。
 * 单实例，多线程安全
 * 特点：
 * 1:连接复用。同一IP和端口只会创建一个连接。
 * 2:连接失效后。自动重新创建新连接。
 * 3:超时管理。当指定的时间内没有收到服务端回复时，会通知应用层超时
 * @author taohuifei
 *
 */
public class NettyTransceiver implements ITransceiver {

  /**
   * 是否为每实例分别创建一个连接 测试多连接的时候使用
   * 一般情况下连接复用，同一个IP和端口只会创建一个连接
   */
  public static boolean OneInstanceOneConnecton = false;

  /**
   * Netty 通道连接工厂 负责连接创建,连接缓存
   */
  private final AbstractChannelFactory factory = new NettyChannelFactory();

  //默认IP和端口  
  private String defaulttargetIP;
  private int defaulttargetPort;

  //默认超时时间
  public static int REQUEST_TIME_OUT = 60;

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


  /**
   * 请求序号及回调参数的映射器
   */
  @SuppressWarnings("rawtypes")
  private final Map<Integer, Callback> requests = new ConcurrentHashMap<Integer, Callback>();
  
  
  /**
   * Netty自动的超时管器
   */
  private static final org.jboss.netty.util.Timer timer = new HashedWheelTimer();

  private final long connectTimeoutMillis;
  
  private final ClientBootstrap bootstrap;
  
  private final InetSocketAddress remoteAddr;

  private final ReadWriteLock stateLock = new ReentrantReadWriteLock();

  private Channel channel; 

  public NettyTransceiver() throws IOException {
    this(null, DEFAULT_CONNECTION_TIMEOUT_MILLIS);
  }


  public NettyTransceiver(InetSocketAddress addr, Long connectTimeoutMillis) throws IOException {
    this(addr, NioClientSocketChannelFactoryHold.chanelFactor, connectTimeoutMillis);
  }

  public static class NioClientSocketChannelFactoryHold {
    private static NioClientSocketChannelFactory chanelFactor = new NioClientSocketChannelFactory(
        Executors.newCachedThreadPool(new NettyTransceiverThreadFactory("Avro "
            + NettyTransceiver.class.getSimpleName() + " Boss")),
        Executors.newCachedThreadPool(new NettyTransceiverThreadFactory("Avro "
            + NettyTransceiver.class.getSimpleName() + " I/O Worker")));
  }


  public NettyTransceiver(InetSocketAddress addr, ChannelFactory channelFactory) throws IOException {
    this(addr, channelFactory, buildDefaultBootstrapOptions(null));
  }


  public NettyTransceiver(InetSocketAddress addr, ChannelFactory channelFactory,
      Long connectTimeoutMillis) throws IOException {
    this(addr, channelFactory, buildDefaultBootstrapOptions(connectTimeoutMillis));
  }

  
  public NettyTransceiver(InetSocketAddress addr, ChannelFactory channelFactory,
      Map<String, Object> nettyClientBootstrapOptions) throws IOException {
    if (channelFactory == null) {
      throw new NullPointerException("channelFactory is null");
    }

    if (addr != null) {
      this.defaulttargetIP = addr.getHostName();
      this.defaulttargetPort = addr.getPort();
    }
    
    this.connectTimeoutMillis =
        (Long) nettyClientBootstrapOptions.get(NETTY_CONNECT_TIMEOUT_OPTION);

    bootstrap = new ClientBootstrap(channelFactory);
    if (OneInstanceOneConnecton) {
      bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
        @Override
        public ChannelPipeline getPipeline() throws Exception {
          ChannelPipeline p = Channels.pipeline();

          p.addLast("frameDecoder", new NettyFrameDecoder());
          p.addLast("frameEncoder", new NettyFrameEncoder());
          p.addLast("handler", new NettyClientAvroHandler(null));
          // p.addLast("executor", executionHandler);
          return p;
        }
      });
    }
    remoteAddr = addr;
    if (nettyClientBootstrapOptions != null) {
      bootstrap.setOptions(nettyClientBootstrapOptions);
    }
    stateLock.readLock().lock();
    try {
      if (addr != null) {
        this.defaulttargetIP = addr.getHostName();
        this.defaulttargetPort = addr.getPort();
        getChannel(this.defaulttargetIP, this.defaulttargetPort);
      }

    } finally {
      stateLock.readLock().unlock();
    }
  }


  private static Map<String, Object> buildDefaultBootstrapOptions(Long connectTimeoutMillis) {
    Map<String, Object> options = new HashMap<String, Object>(2);
    options.put(NETTY_TCP_NODELAY_OPTION, DEFAULT_TCP_NODELAY_VALUE);
    options.put(NETTY_CONNECT_TIMEOUT_OPTION, connectTimeoutMillis == null
        ? DEFAULT_CONNECTION_TIMEOUT_MILLIS
        : connectTimeoutMillis);
    options.put(NettyTransceiver.NETTY_TCP_receiveBufferSize_OPTION,
        NettyTransceiver.DEFAULT_TCP_receiveBufferSize_VALUE);
    options.put(NettyTransceiver.NETTY_TCP_sendBufferSize_OPTION,
        NettyTransceiver.DEFAULT_TCP_sendBufferSize_VALUE);
    return options;
  }


  private static boolean isChannelReady(Channel channel) {
    return (channel != null) && channel.isOpen() && channel.isBound() && channel.isConnected();
  }


  @SuppressWarnings("static-access")
  private Channel getChannel(String ip, int port) throws IOException {
    if (this.factory != null && !this.OneInstanceOneConnecton) {
      try {
        return this.factory.get(ip, port, (int) this.connectTimeoutMillis);
      } catch (Exception e) {
        throw new IOException("Error connecting to " + ip + ":" + port, e.getCause());
      }
    }
    if (!isChannelReady(channel)) {
      stateLock.readLock().unlock();
      stateLock.writeLock().lock();
      try {
        if (!isChannelReady(channel)) {
          ChannelFuture channelFuture = bootstrap.connect(remoteAddr);
          channelFuture.awaitUninterruptibly(connectTimeoutMillis);
          if (!channelFuture.isSuccess()) {
            throw new IOException("Error connecting to " + remoteAddr, channelFuture.getCause());
          }
          channel = channelFuture.getChannel();
        }
      } finally {
        stateLock.readLock().lock();
        stateLock.writeLock().unlock();
      }
    }
    return channel;
  }


  @Override
  public void transceive(String ip,int port,ProtocolPack protocolPack, Callback<List<ByteBuffer>> callback) throws IOException {
      addRequstHandleMap(callback, protocolPack.getSerial());
      writeDataPack(ip, port, protocolPack);
   
  }

  private void writeDataPack(String ip, int port, final ProtocolPack dataPack) throws IOException {
    Channel channel = null;
    try {
      channel = getChannel(ip, port);
    } catch (IOException e) {
      throw e;
    }
    writeToChannel(dataPack, channel);
  }

  private void writeToChannel(final ProtocolPack dataPack, final Channel channel) {
    ChannelFuture writeFuture = channel.write(dataPack);
    writeFuture.addListener(new ChannelFutureListener() {
      @SuppressWarnings("unchecked")
      public void operationComplete(ChannelFuture future) throws Exception {
        if (future.isSuccess()) {
          return;
        }
        String errorMsg = "";
        if (future.isCancelled()) {
          errorMsg =
              "Send request to " + channel.toString() + " cancelled by user,request id is:"
                  + dataPack.getSerial();
        }
        if (!future.isSuccess()) {
          if (channel.isConnected()) {
            // maybe some exception,so close the channel
            channel.close();
          } else {
            // TODO
          }
          errorMsg = "Send request to " + channel.toString() + " error" + future.getCause();
        }
        Callback<List<ByteBuffer>> callback = getRequestHandler(dataPack);
        if (callback == null) {
          return;
        }
        Exception e = new Exception(errorMsg, future.getCause());
        callback.handleError(e);
      }
    });
  }

  private void addRequstHandleMap(@SuppressWarnings("rawtypes") Callback callback, int serial) {
    requests.put(serial, callback);
    timer.newTimeout(new ReadTimeoutTask(serial), REQUEST_TIME_OUT, TimeUnit.SECONDS);
  }

  @SuppressWarnings("rawtypes")
  private Callback getRequestHandler(ProtocolPack dataPack) {
    Callback callback = requests.remove(dataPack.getSerial());
    return callback;
  }


  private final class ReadTimeoutTask implements TimerTask {

    private int serial;

    public ReadTimeoutTask(int serial) {
      this.serial = serial;
    }

    @Override
    public void run(Timeout timeout) throws Exception {
      @SuppressWarnings("rawtypes")
      Callback callback = requests.remove(serial);
      if (callback != null) {
        callback.handleError(new TimeoutException("远程调用超时！"));
        callback = null;
      }
    }
  }


  /**
   * handler for the Netty transport
   */
  class NettyClientAvroHandler extends SimpleChannelUpstreamHandler {

    private String key;

    public NettyClientAvroHandler(String key) {
      super();
      this.key = key;
    }

    @Override
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
      super.channelOpen(ctx, e);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void messageReceived(ChannelHandlerContext ctx, final MessageEvent e) {
      ProtocolPack dataPack = (ProtocolPack) e.getMessage();
      @SuppressWarnings("rawtypes")
      Callback callback = getRequestHandler(dataPack);
      if (callback == null) {
        return;
      }
      try {
        callback.handleResult(dataPack.getDatas());
      } finally {
      }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
      e.getChannel().close();
    }

    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
      if (key != null) 
        factory.removeClient(key);
    }

  }


  private static class NettyTransceiverThreadFactory implements ThreadFactory {
    private final AtomicInteger threadId = new AtomicInteger(0);
    private final String prefix;

    public NettyTransceiverThreadFactory(String prefix) {
      this.prefix = prefix;
    }

    @Override
    public Thread newThread(Runnable r) {
      Thread thread = new Thread(r);
      thread.setName(prefix + " " + threadId.incrementAndGet());
      return thread;
    }
  }

  /**
   * Netty 通道创建工厂
   * 所有通道事件驱动共用一个反应堆
   * @author taohuifei
   *
   */
  class NettyChannelFactory extends AbstractChannelFactory {

    @Override
    protected synchronized Channel createChannel(String targetIP, int targetPort,
        int connectTimeout, final String key) throws Exception {
      bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
        @Override
        public ChannelPipeline getPipeline() throws Exception {
          ChannelPipeline p = Channels.pipeline();

          p.addLast("frameDecoder", new NettyFrameDecoder());
          p.addLast("frameEncoder", new NettyFrameEncoder());
          p.addLast("handler", new NettyClientAvroHandler(key));
          return p;
        }
      });
      ChannelFuture future = bootstrap.connect(new InetSocketAddress(targetIP, targetPort));
      future.awaitUninterruptibly(connectTimeout);
      if (!future.isDone()) {
        throw new Exception("Create connection to " + targetIP + ":" + targetPort + " timeout!");
      }
      if (future.isCancelled()) {
        throw new Exception("Create connection to " + targetIP + ":" + targetPort
            + " cancelled by user!");
      }
      if (!future.isSuccess()) {
        throw new Exception("Create connection to " + targetIP + ":" + targetPort + " error",
            future.getCause());
      }
      return future.getChannel();
    }

  }
}
