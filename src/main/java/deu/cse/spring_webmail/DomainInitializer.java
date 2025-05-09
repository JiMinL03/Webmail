/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.cse.spring_webmail;

import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author mskim
 */
@Component
@Slf4j
public class DomainInitializer {
    
    @Value("${james.host}")
    private String host;
    
    @Value("${james.control.port}")
    private int port;
    
    public static final String domain = "user.com"; // 상수값으로 등록
    
    @Autowired
    private RestTemplate restTemplate;

    public String formatUrl(String host, int port, String domain){
        return String.format("http://%s:%d/domains/%s", host, port, domain);
    }
        
    @PostConstruct
    public void userDomain() {
        // String domain="user.com"; // 이거 하드코딩임. 수정:상수로 정의
        String url =  formatUrl(host,port,domain);
        
        try {
            restTemplate.put(url,null);
            // log.info("도메인 등록 성공");
        }
        catch(Exception e){
           log.error("도메인 오류가 발생하였습니다.",e);
        }
    }
}
