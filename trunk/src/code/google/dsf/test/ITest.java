package code.google.dsf.test;

import java.util.List;

public interface ITest {
  
  public void testNull();
  
  public String testSimpletypes(int id,String what);
  
  public List<TestDTO> testLis(List<TestDTO> datas);
  
  public TestDTO testReturnDTO(TestDTO dto);

}
