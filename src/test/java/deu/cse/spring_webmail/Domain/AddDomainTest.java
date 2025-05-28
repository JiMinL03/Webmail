/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.cse.spring_webmail.Domain;

import deu.cse.spring_webmail.model.UserAdminAgent;
import static org.junit.Assert.assertEquals;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author mskim
 */

// 임시로 도메인 값을 넣고 james 서버에 추가되었는지 확인하는 테스트
public class AddDomainTest {
    
    // 값 준비
    UserAdminAgent agent = new UserAdminAgent("localhost", 8000);
    String testDomain = "test.com";
    String url = "http://" + agent.getServer() + ":" + agent.getPort() + "/domains/" +  testDomain;
    
    // 테스트하기 전 테스트 값이 있다면 정리하기
    @BeforeEach
    void prepare() {
        agent.deleteDomain(new String[]{testDomain});
    }
       
    @Test
    void test() {
        
         // 실행
        String testAddDomain = agent.addDomain(testDomain);
        
        // 검증
        assertEquals("도메인 등록 성공", testAddDomain);
        ResponseEntity<Void> response = new RestTemplate().getForEntity(url, Void.class);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }
    
    // 테스트 후 테스트 값 정리하기
    @AfterEach
    void clean() {
        agent.deleteDomain(new String[]{testDomain});
    }
}
