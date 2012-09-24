package code.google.dsf.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import org.apache.avro.ipc.Callback;
import code.google.dsf.protocol.ProtocolPack;

/**
 * 
 * 传输层抽象
 * @author taohuifei
 *
 */
public interface ITransceiver {
  
  /**
   * 将数据发送到目标服务
   * @param ip
   * @param port
   * @param protocolPack
   * @param callback
   * @throws IOException
   */
  public void transceive(String ip,int port,ProtocolPack protocolPack, Callback<List<ByteBuffer>> callback) throws IOException;

}
