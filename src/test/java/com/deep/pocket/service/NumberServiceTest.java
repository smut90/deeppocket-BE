//package com.deep.pocket.service;
//
//import com.deep.pocket.dao.NumberDao;
//import com.deep.pocket.model.response.NumberResponse;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import static org.junit.Assert.assertNotNull;
//
//@RunWith(SpringRunner.class)
//@SpringBootTest
//public class NumberServiceTest {
//
//    @Autowired
//    private NumberService numberService;
//
//    @MockBean
//    private NumberDao numberDao;
//
//    @Test
//    public void testNumberGenerationLogic() {
//        NumberResponse response = numberService.generateNumbers(4);
//
//        assertNotNull(response);
//    }
//
//}
