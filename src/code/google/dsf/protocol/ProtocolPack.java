package code.google.dsf.protocol;

import java.nio.ByteBuffer;
import java.util.List;


/**
 *</p> 协议包结构有消息头和消息体组成</p>
 * 
 * </p>消息头占12个字节</p>
 *</p> =================================================</p>
 *
 * </p>Offset:  0  1        5  6  7  8          12    </p> 
 * </p>         +--+--------+--+--+--+----------+</p>
 * </p>Fields:  |a |  b     |c |d |e |  f       |</p>
 * </p>         +--+--------+--+--+--+----------+</p>
 * </p>   a: DSF_MAGIC_NUMBER 魔法数， 一个字节</p>
 * </p>   b: serial 消息序号 4个字节</p>
 * </p>   c: messageType 消息类型</p>
 * </p>   d: protocolType 协议类型</p>
 *</p>    e: contentType 编码类型</p>
 *</p>    f: 消息块的数目  </p>
 * 
 * </p> 消息体  一个消息块的结构，可以有多个消息块</p>
 *</p> =================================================</p>
 *
 *</p> Offset:  0        4                   (Length + 4)</p>
 * </p>         +--------+------------------------+</p>
 *</p> Fields:  | Length | Actual message content |</p>
 *</p>          +--------+------------------------+</p>
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
  
}
