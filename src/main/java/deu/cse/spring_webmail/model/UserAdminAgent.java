/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package deu.cse.spring_webmail.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author jongmin
 */
@Slf4j
@Component
public class UserAdminAgent {
    @Value("${user.base-url}")
    private String baseUrl;

    private String server;
    private int port;
    boolean isConnected = false;
    private String ADMIN_PASSWORD;
    private String ADMIN_ID;
    private final RestTemplate restTemplate = new RestTemplate();

  
    public UserAdminAgent() { }

    public boolean addUser(String userId, String password) {

        String url = getUserUrl(userId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String jsonBody = String.format("{\"password\": \"%s\"}", password);
        HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);

        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                    url, HttpMethod.PUT, request, Void.class);
            log.info("Response Status Code: {}", response.getStatusCode());
            return response.getStatusCode() == HttpStatus.NO_CONTENT;
        } catch (HttpClientErrorException e) {
            log.error("HTTP error when adding user {}: {} - Body: {}",
                    userId, e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Unexpected error adding user {}: {}", userId, e.toString());
        }
        return false;
    }
    

    public List<String> getUserList() {
        String url = getBaseUrl();
        try {
            // 응답을 List<Map<String, String>> 형태로 받기
            ResponseEntity<List<Map<String, String>>> response = restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<Map<String, String>>>() {
            });
            List<Map<String, String>> body = response.getBody();

            if (body == null) {
                log.warn("응답 본문이 null입니다.");
                return new LinkedList<>();
            }

            // "username" 키에 해당하는 값들을 리스트로 추출
            List<String> users = new LinkedList<>();
            for (Map<String, String> user : body) {
                String username = user.get("username");
                if (username != null && !username.equalsIgnoreCase("admin@admin.com")) {
                    users.add(username);
                }
            }

            users.sort(String::compareTo);  // 알파벳 순으로 정렬
            return users;
        } catch (Exception e) {
            log.error("Error fetching user list", e);
            return new LinkedList<>();
        }
    }
    
    public boolean deleteUsers(String[] userList) {
        boolean allSuccess = true;
        for (String user : userList) {
            String url = getUserUrl(user);
            try {
                restTemplate.delete(url);
                log.info("Deleted user: {}", user);
            } catch (Exception e) {
                log.error("Error deleting user {}: {}", user, e.getMessage());
                allSuccess = false;
            }
        }
        return allSuccess;
    }

    public boolean verify(String userId) {
        String url = getUserUrl(userId);
        try {
            restTemplate.getForEntity(url, Void.class);
            return true; // 존재함
        } catch (HttpClientErrorException.NotFound e) {
            return false; // 존재하지 않음
        } catch (Exception e) {
            log.error("Error verifying user {}: {}", userId, e.getMessage());
            return false;
        }
    }
    
    public List<String> getDomainList() {
        String url = String.format("http://%s:%d/domains", server, port);
        try {
            ResponseEntity<List<String>> response = restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<String>>() {});
            List<String> domainList = response.getBody();
            
            if (domainList == null) {
                log.info("도메인 목록이 비어 있습니다.");
                return new LinkedList<>();
            }
            return domainList;
        }
        catch (Exception e) {
            log.error("도메인 목록 가져오기 실패 : {}", e.getMessage());
            return new LinkedList<>();
        }
    }
    

    // 도메인 추가
    public boolean addDomain(String domain){
        String url = String.format("http://%s:%d/domains/%s", server, port, domain);
        
        try {
            restTemplate.put(url,null);
            log.info("도메인 등록 성공: {}", domain);
            return true;
        }
        catch (HttpClientErrorException e){
            log.error("도메인 등록 실패: {} {} {}", domain, e.getStatusCode(), e.getResponseBodyAsString());
        }
        catch (Exception e){
            log.error("도메인 등록 중 예외 발생 {}", e.toString());
        }
        return false;
    }
    
    // 도메인 삭제
    public boolean deleteDomain(String [] domainList) {
        boolean success = true;
        
        for (String domain : domainList) {
            try {
                String url = String.format("http://%s:%d/domains/%s", server, port, domain);
                restTemplate.delete(url);
                log.info("도메인 삭제: {}", domain);
            }
            catch (Exception e) {
                log.error("도메인 삭제 중 예외 발생 {}", e.toString());
                success = false;
            }
        }
        return success;
    }
    
    private String getBaseUrl() {
        return baseUrl;
    }
    private String getUserUrl(String userId){
        return baseUrl+"/"+userId;
    }
}
