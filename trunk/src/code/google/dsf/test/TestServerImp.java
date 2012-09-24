package code.google.dsf.test;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.avro.generic.GenericRecord;

public class TestServerImp {
  
  public static final String BEANAME= "/TEST/DTO";
  
  public TestDTO testReturnDTO(TestDTO dto) {
    return dto;
  }
  
  public GenericRecord testReturnDTO_avro(GenericRecord dto) {
    return dto;
  }
  
  public List<TestDTO> testLis(List<TestDTO> datas) {
	  return datas;
  }
  
  public List<GenericRecord> testList_avro(List<GenericRecord> datas) {
	  return datas;
  }
  
  public static void main(String[] args) throws SecurityException, NoSuchMethodException {
    Method methode = TestServerImp.class.getMethod("testLis", List.class);
   // methode.getParameterTypes()
    System.out.println(methode.getParameterTypes());
    System.out.println(methode.getGenericReturnType());
  }

}
