package code.google.dsf.test;

import java.util.concurrent.Executors;

import code.google.dsf.serialize.AvroSerializer;
import code.google.dsf.serialize.SerializerFactory;
import code.google.dsf.server.ProtocolHandlerFactory;
import code.google.dsf.server.netty.NettyServer;

public class StartServerTest {

  public static void main(String[] args) throws Exception {
    //注册RPC服务实体
    ProtocolHandlerFactory.getProtocolHandler(ProtocolHandlerFactory.PROTOCOLHANDLER_RPC)
        .registerProcessor(TestServerImp.BEANAME, new TestServerImp());
    
    //为AVRO序列化注册协议文件
    AvroSerializer.registerAvroProtocol(TestServerImp.BEANAME, "code/google/dsf/test/test.avpr");

    //开启 Netty服务
    int port = 7001;
    NettyServer server = new NettyServer();
    server.start(port,
        Executors.newFixedThreadPool(2 * Runtime.getRuntime().availableProcessors() + 1));
  }

}
