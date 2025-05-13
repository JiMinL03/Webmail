/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.cse.spring_webmail.user;

import deu.cse.spring_webmail.DomainInitializer;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;


/**
 *
 * @author mskim
 */
// 테스트코드
@ExtendWith(MockitoExtension.class)
public class DomainInitializerTest {
    
    private DomainInitializer domainInitializer = new DomainInitializer();
    
    @Mock
    private RestTemplate restTemplate;
    
    // 테스트에 사용될 값
    private final String host = "127.0.0.1";
    private final int port = 8000;
    private final String domain = "user.com";
    
    private String testUrl(){
        return "http://"+host+":"+port+"/domains/"+domain;
    }
    
    @BeforeEach
    void settingData(){
        ReflectionTestUtils.setField(domainInitializer, "host", host);
        ReflectionTestUtils.setField(domainInitializer, "port", port);
        ReflectionTestUtils.setField(domainInitializer, "restTemplate", restTemplate);
    }
    
    // FormatUrl 테스트코드
    // system.properties에 있는 값 잘 들고 왔는지 테스트
    @Test
    void testFormatUrl(){
        String runUrl = domainInitializer.formatUrl(host, port, domain);
        assertThat(runUrl).isEqualTo(testUrl());
    }
 
    // UserDomain 테스트코드
    // James 서버에 도메인 등록 요청이 잘 갔는지 테스트
    @Test
    void testUserDomain(){
        domainInitializer.userDomain();
        verify(restTemplate, times(1)).put(eq(testUrl()), eq(null));
    }
}
