package code.google.dsf.test;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.avro.generic.GenericRecord;
import org.apache.avro.util.Utf8;

import code.google.dsf.test.protobuf.Test.PbDTO;

/**
 * 测试服务对象
 * @author taohuifei
 *
 */
public class TestServerImp {
  
  public static final String BEANAME= "/TEST/DTO";
  
  public TestDTO testReturnDTO(TestDTO dto) {
    return dto;
  }
  
  /**
   * Avro DTO 测试
   * @param dto
   * @return
   */
  public GenericRecord testReturnDTO_avro(GenericRecord dto) {
    return dto;
  }
  
  /**
   * Avro DTO 测试
   * @param dto
   * @return
   */
  public PbDTO testReturnDTO_protobuf(PbDTO dto) {
    return dto;
  }
  
  
  /**
   * List 测试
   * @param datas
   * @return
   */
  public List<TestDTO> testLis(List<TestDTO> datas) {
	  return datas;
  }
  
  
  /**
   * avro List测试
   * @param datas
   * @return
   */
  public List<GenericRecord> testList_avro(List<GenericRecord> datas) {
	  return datas;
  }
  
  /**
   * 简单类型测试
   * @param id
   * @param what
   * @return
   */
  public String testSimpletypes(int id,String what) {
    return id+":"+what;
  }
  
  /**
   * 简单类型测试
   * @param id
   * @param what
   * @return
   */
  public Utf8 testSimpletypes_avro(int id,Utf8 what) {
    return new Utf8(id+":"+what);
  }
  
  /**
   * Null值测试
   */
  public void testNull() {
    return ;
  }
  
  public static void main(String[] args) throws SecurityException, NoSuchMethodException {
    Method methode = TestServerImp.class.getMethod("testLis", List.class);
   // methode.getParameterTypes()
    System.out.println(methode.getParameterTypes());
    System.out.println(methode.getGenericReturnType());
  }

}
