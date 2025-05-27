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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author jongmin
 */
@Slf4j
public class UserAdminAgent {

    private String server;
    private int port;
    Socket socket = null;
    InputStream is = null;
    OutputStream os = null;
    boolean isConnected = false;
    private String ADMIN_PASSWORD;
    private String ADMIN_ID;
    // private final String EOL = "\n";
    private final String EOL = "\r\n";

    private final String baseUrl = "http://localhost:8000/users";
    private final RestTemplate restTemplate = new RestTemplate();

  
    public UserAdminAgent(String server, int port,
            String admin_pass, String admin_id) {
        log.debug("UserAdminAgent created: server = " + server + ", port = " + port);
        this.server = server;  // 127.0.0.1
        this.port = port;  // 8000

        this.ADMIN_PASSWORD = admin_pass;
        this.ADMIN_ID = admin_id;
        

        log.debug("isConnected = {}, root.id = {}", isConnected, ADMIN_ID);
    }

    public boolean addUser(String userId, String password) {

        String url = getUserUrl(userId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String jsonBody = String.format("{\"password\": \"%s\"}", password);
        HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.PUT, request, String.class);
            log.info("Response Status Code: {}", response.getStatusCode());
            return response.getStatusCode() == HttpStatus.NO_CONTENT;
        }
        catch (HttpClientErrorException e) {
            log.error("HTTP 에러 발생");
        }
        catch (Exception e) {
            log.error("에러 발생 {}:",  e.toString());
        }
        return false;
    }
    
    // 이미 유저가 가입된 계정일 때
    public boolean existUser(String userId) {
        try {
            List<String> userList = getUserList();
            return userList.contains(userId);
        }
        catch (Exception e) {
            return false;
        }
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
            log.error("list에러 발생", e);
            return new LinkedList<>();
        }
    }

    private List<String> parseUserList(String message) {
        List<String> userList = new LinkedList<String>();

        // UNIX 형식을 윈도우 형식으로 변환하여 처리
        message = message.replace("\r\n", "\n");

        // 1: 줄 단위로 나누기
        String[] lines = message.split("\n");
        // 2: 첫 번째 줄에는 등록된 사용자 수에 대한 정보가 있음.
        //    예) Existing accounts 7
        String[] firstLine = lines[0].split(" ");
        int numberOfUsers = Integer.parseInt(firstLine[2]);

        // 3: 두 번째 줄부터는 각 사용자 ID 정보를 보여줌.
        //    예) user: admin
        for (int i = 1; i <= numberOfUsers; i++) {
            // 3.1: 한 줄을 구분자 " "로 나눔.
            String[] userLine = lines[i].split(" ");
            // 3.2 사용자 ID가 관리자 ID와 일치하는 지 여부 확인
            if (!userLine[1].equals(ADMIN_ID)) {
                userList.add(userLine[1]);
            }
        }
        return userList;
    } // parseUserList()

    
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
    public boolean quit() {
        byte[] messageBuffer = new byte[1024];
        boolean status = false;
        // quit
        try {
            // 1: quit 명령 송신
            String quitCommand = "quit" + EOL;
            os.write(quitCommand.getBytes());
            // 2: quit 명령에 대한 응답 수신
            java.util.Arrays.fill(messageBuffer, (byte) 0);
            //if (is.available() > 0) {
            is.read(messageBuffer);
            // 3: 메시지 분석
            String recvMessage = new String(messageBuffer);
            if (recvMessage.contains("closed")) {
                status = true;
            } else {
                status = false;
            }
        } catch (IOException ex) {
            log.error("quit() 예외: {}", ex);
        } 
            return status;
        
    }

    // 도메인 목록
    public List<String> getDomainList() {
        String url = String.format("http://%s:%d/domains", server, port);
        try {
            ResponseEntity<List<String>> response = restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<String>>() {});
            List<String> domainList = response.getBody();
            
            if (domainList == null) { // 도메인 목록이 존재하지 않을 때
                return new LinkedList<>(); // null이 아닌 비어있는 리스트 전달
            }
            return domainList;
        }
        catch (Exception e) {
            log.error("도메인 목록 가져오기 실패 : {}", e.getMessage());
            return new LinkedList<>();
        }
    }
    

    // 도메인 추가
    public String addDomain(String domain){
        String url = String.format("http://%s:%d/domains/%s", server, port, domain);

        // 이미 등록된 도메인인 경우
        List<String> domainList = getDomainList(); 
        if (domainList.contains(domain)) {
            log.info("이미 등록된 도메인입니다. : {}", domain);
            return "이미 등록된 도메인입니다.";
        }
            
        try {
            restTemplate.put(url,null);
            log.info("도메인 등록 성공: {}", domain);
            return "도메인 등록 성공";
        }
        catch (HttpClientErrorException e){
            log.error("도메인 등록 실패: {} {} {}", domain, e.getStatusCode(), e.getResponseBodyAsString());
            return "도메인 등록 실패";
        }
        catch (Exception e){
            log.error("도메인 등록 중 예외 발생 {}", e.toString());
            return "도메인 등록 중 오류 발생";
        }
    }
    
    // 도메인을 사용자가 사용하고 있는지 검사
    public boolean domainUse(String domain) {
        try {
            List<String> userList = getUserList();
            for (String user : userList) {
                if (user.endsWith("@" + domain)) {
                    return true;
                }
            }
        }
        catch (Exception e) {
            log.error("도메인 예외 발생 : {}" , e.toString());
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
    
    // 테스트코드를 위한 getter
    public String getServer() {
        return server;
    }
    
    public int getPort() {
        return port;
    }
}
