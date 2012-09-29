package code.google.dsf.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.avro.Protocol;
import org.apache.avro.Protocol.Message;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.util.Utf8;

import code.google.dsf.client.IClient;
import code.google.dsf.client.RPCClient;
import code.google.dsf.serialize.AvroSerializer;
import code.google.dsf.serialize.SerializerFactory;
import code.google.dsf.test.protobuf.Test.PbDTO;
import code.google.dsf.test.protobuf.Test.PbList;

import code.google.dsf.test.protobuf.Test.PbList.Builder;

@SuppressWarnings("static-access")
public class TestClient extends AbstractPerformaceTestClient {

  private IClient client;
  static GenericRecord avroparams = null;
  static TestDTO testDTO;
  static PbDTO pbDTO;
  static PbList pbList;
  static String methodName = "testReturnDTO_avro";
  static int listsize = 10;
  static GenericRecord avrolist;
  static List<TestDTO> dtoList;
  static Protocol protocol;

  static {
 
    // 为AVRO序列化注册协议文件
    AvroSerializer avroSerializer =
        (AvroSerializer) SerializerFactory.getSerializer(SerializerFactory.SERIALIZER_AVRO);
    try {
      protocol =
          avroSerializer.registerAvroProtocol(TestServerImp.BEANAME,
              "code/google/dsf/test/test.avpr");


    } catch (IOException e) {
      e.printStackTrace();
    }
    iniParamers();
  }

  /**
   * 准备测试数据
   */
  @SuppressWarnings("unused")
  private static void iniParamers() {
    testDTO = new TestDTO();
    testDTO.setItemid(1);
    testDTO.setLogid("186aea4e921f25fc512fc66612f9d789");
    testDTO.setImei("10086001");
    testDTO.setUserid(1);
    testDTO.setSid("111111");
    testDTO.setFlowSize(Double.valueOf(10));
    testDTO.setBeginTime(new Date());
    testDTO.setEndTime(new Date());
    testDTO.setHomecountry(1);
    testDTO.setVisitcountry(1);

    pbDTO =
        PbDTO.newBuilder().setItemid(testDTO.getItemid()).setLogid(testDTO.getLogid())
            .setImei(testDTO.getImei()).setUserid(testDTO.getUserid()).setSid(testDTO.getSid())
            .setFlowSize(testDTO.getFlowSize()).setBeginTime("2012-01-01 12:00:00")
            .setEndTime("2012-11-01 12:00:00").build();



    Message message = protocol.getMessages().get(methodName);
    avroparams = new GenericData.Record(message.getRequest());
    GenericRecord record = new GenericData.Record(message.getRequest().getField("data").schema());

    record.put("itemid", 1);
    record.put("logid", new Utf8(testDTO.getLogid()));
    record.put("imei", new Utf8(testDTO.getImei()));
    record.put("uid", testDTO.getUserid());
    record.put("sid", new Utf8(testDTO.getSid()));
    record.put("flowSize", testDTO.getFlowSize());
    record.put("beginTime", new Utf8("2012-01-01 12:00:00"));
    record.put("endTime", new Utf8("2012-11-01 12:00:00"));
    record.put("homecountry", 1);
    record.put("visitcountry", 1);
    avroparams.put("data", record);

    avrolist = new GenericData.Record(protocol.getMessages().get("testList_avro").getRequest());

    GenericData.Array<GenericRecord> listrecord =
        new GenericData.Array<GenericRecord>(listsize, protocol.getMessages().get("testList_avro")
            .getRequest().getField("data").schema());

    dtoList = new ArrayList<TestDTO>();
    Builder builder = (Builder)PbList.newBuilder();
    for (int i = 0; i < listsize; i++) {
      TestDTO dto = new TestDTO();
      dto.setItemid(i);
      dto.setLogid("186aea4e921f25fc512fc66612f9d789");
      dto.setImei("10086001");
      dto.setUserid(1);
      dto.setSid("111111");
      dto.setFlowSize(Double.valueOf(10));
      dto.setBeginTime(new Date());
      dto.setEndTime(new Date());
      dto.setHomecountry(1);
      dto.setVisitcountry(1);
      dtoList.add(dto);

      GenericRecord irecord =
          new GenericData.Record(message.getRequest().getField("data").schema());

      irecord.put("itemid", i);
      irecord.put("logid", new Utf8(testDTO.getLogid()));
      irecord.put("imei", new Utf8(testDTO.getImei()));
      irecord.put("uid", testDTO.getUserid());
      irecord.put("sid", new Utf8(testDTO.getSid()));
      irecord.put("flowSize", testDTO.getFlowSize());
      irecord.put("beginTime", new Utf8("2012-01-01 12:00:00"));
      irecord.put("endTime", new Utf8("2012-11-01 12:00:00"));
      irecord.put("homecountry", 1);
      irecord.put("visitcountry", 1);
      listrecord.add(irecord);
      
      //==pb
      PbDTO tmppbDTO =
        PbDTO.newBuilder().setItemid(testDTO.getItemid()).setLogid(testDTO.getLogid())
            .setImei(testDTO.getImei()).setUserid(testDTO.getUserid()).setSid(testDTO.getSid())
            .setFlowSize(testDTO.getFlowSize()).setBeginTime("2012-01-01 12:00:00")
            .setEndTime("2012-11-01 12:00:00").build();
      builder = builder.addDto(tmppbDTO);
    }
    pbList = builder.build();
    avrolist.put(0, listrecord);
    System.out.println(pbList);
  }

  public TestClient(String IP, int port) {
    client = new RPCClient(IP, port, transceiver);
  }

  @SuppressWarnings("unused")
  private void testReturnDTO_avro() {
    try {
      Object obj =
          this.client.invokeSync(TestServerImp.BEANAME, methodName, null,
              new Object[] {avroparams}, null, SerializerFactory.SERIALIZER_AVRO);
      System.out.println(obj);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  @SuppressWarnings("unused")
  private void testReturnDTO_avro_Async() {
    try {
      this.client.invokeAsync(TestServerImp.BEANAME, methodName, null, new Object[] {avroparams},
          null, SerializerFactory.SERIALIZER_AVRO, this);
    } catch (Exception e) {
      if (this.callBack != null) {
        this.callBack.handleError(e);
      }
    }
  }

  @SuppressWarnings("unused")
  private void testList_avro() {
    try {
      Object obj =
          this.client.invokeSync(TestServerImp.BEANAME, "testList_avro", null,
              new Object[] {avrolist}, null, SerializerFactory.SERIALIZER_AVRO);
      System.out.println(obj);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void testList_avro_Async() {
    try {
      this.client.invokeAsync(TestServerImp.BEANAME, "testList_avro", null,
          new Object[] {avrolist}, null, SerializerFactory.SERIALIZER_AVRO, this);
    } catch (Exception e) {
      e.printStackTrace();
      if (this.callBack != null) {
        this.callBack.handleError(e);
      }
    }
  }

  @SuppressWarnings("unused")
  private void testReturnDTO_Async_Other(byte contentType) {
    try {
      String methodname = "testReturnDTO";
      Object arg = null;
      if (contentType == SerializerFactory.SERIALIZER_PROTOBUF){
        arg = pbDTO;
        methodname = "testReturnDTO_protobuf";
      }
      else
        arg = testDTO;
      this.client.invokeAsync(TestServerImp.BEANAME, methodname, null, new Object[] {arg},
          TestDTO.class, contentType, this);
    } catch (Exception e) {
      if (this.callBack != null) {
        this.callBack.handleError(e);
      }
    }
  }

  @SuppressWarnings("unused")
  private void testReturnDTO_Other(byte contentType) {
    try {
      String methodname = "testReturnDTO";
      Object arg = null;
      if (contentType == SerializerFactory.SERIALIZER_PROTOBUF){
        arg = pbDTO;
        methodname = "testReturnDTO_protobuf";
      }
      else
        arg = testDTO;

      Object obj =
          this.client.invokeSync(TestServerImp.BEANAME, methodname, null, new Object[] {arg},
              testDTO.getClass(), contentType);
      System.out.println(obj);
    } catch (Exception e) {
      e.printStackTrace();
      if (this.callBack != null) {
        this.callBack.handleError(e);
      }
    }
  }

  public void testSimpletypes_Other(byte contentType) {
    try {
      Object obj =
          this.client.invokeSync(TestServerImp.BEANAME, "testSimpletypes", null, new Object[] {100,
              "hello"}, String.class, contentType);
      System.out.println(obj);
    } catch (Exception e) {
      e.printStackTrace();
      if (this.callBack != null) {
        this.callBack.handleError(e);
      }
    }
  }

  public void testNull_Other(byte contentType) {
    try {
      Object obj =
          this.client.invokeSync(TestServerImp.BEANAME, "testNull", null, null, null, contentType);
      System.out.println("is null " + (obj == null));
    } catch (Exception e) {
      e.printStackTrace();
      if (this.callBack != null) {
        this.callBack.handleError(e);
      }
    }
  }

  public void testSimpletypes_avro() throws Exception {
    String methodName = "testSimpletypes_avro";
    GenericRecord record =
        new GenericData.Record(protocol.getMessages().get(methodName).getRequest());
    record.put("id", 1000);
    record.put("str", new Utf8("hello"));
    Object obj =
        this.client.invokeSync(TestServerImp.BEANAME, methodName, null, new Object[] {record},
            null, SerializerFactory.SERIALIZER_AVRO);
    System.out.println(obj);
  }

  @SuppressWarnings("unused")
  private void testLis_Async_Other(byte contentType) {
    try {
      String methodname = "testLis";
      Object arg = null;
      if (contentType == SerializerFactory.SERIALIZER_PROTOBUF){
        arg = pbList;
        methodname = "testReturnList_protobuf";
      }
      else
        arg = dtoList;
      
      this.client.invokeAsync(TestServerImp.BEANAME, methodname, null, new Object[] {arg},
          dtoList.getClass(), contentType, this);
    } catch (Exception e) {
      if (this.callBack != null) {
        this.callBack.handleError(e);
      }
    }
  }

  private void testLis_Other(byte contentType) {
    try {
      String methodname = "testLis";
      Object arg = null;
      if (contentType == SerializerFactory.SERIALIZER_PROTOBUF){
        arg = pbList;
        methodname = "testReturnList_protobuf";
      }
      else
        arg = dtoList;
      Object obj =
          this.client.invokeSync(TestServerImp.BEANAME, methodname, null, new Object[] {arg},
              dtoList.getClass(), contentType);
      System.out.println(obj);
    } catch (Exception e) {
      e.printStackTrace();
      if (this.callBack != null) {
        this.callBack.handleError(e);
      }
    }
  }

  public static void main(String[] args) throws Exception {
    TestClient test = new TestClient("localhost", 7001);
    test.testReturnDTO_Other(SerializerFactory.SERIALIZER_PROTOBUF);
    test.testLis_Other(SerializerFactory.SERIALIZER_PROTOBUF);

    // test.testReturnDTO_avro();
   /* test.testList_avro();
    test.testList_avro_Async();

    test.testLis_Other(SerializerFactory.SERIALIZER_HESSIAN);
    test.testLis_Other(SerializerFactory.SERIALIZER_JAVA);
    test.testLis_Other(SerializerFactory.SERIALIZER_JSON);

    test.testSimpletypes_Other(SerializerFactory.SERIALIZER_HESSIAN);
    test.testSimpletypes_Other(SerializerFactory.SERIALIZER_JAVA);
    test.testSimpletypes_Other(SerializerFactory.SERIALIZER_JSON);

    test.testNull_Other(SerializerFactory.SERIALIZER_HESSIAN);
    test.testNull_Other(SerializerFactory.SERIALIZER_JAVA);
    test.testNull_Other(SerializerFactory.SERIALIZER_JSON);

    test.testSimpletypes_avro();
*/

  }


  /**
   * 性能测试测试接口
   */
  public void doAction() throws Exception {
    // this.testList_avro_Async();
    //this.testReturnDTO_avro_Async();
     //this.testLis_Async_Other(SerializerFactory.SERIALIZER_HESSIAN);
    this.testReturnDTO_Async_Other(SerializerFactory.SERIALIZER_PROTOBUF);
  }

}
