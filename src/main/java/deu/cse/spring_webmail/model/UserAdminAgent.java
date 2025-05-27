/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package deu.cse.spring_webmail.model;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
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

    private String server;
    private int port;
    private final RestTemplate restTemplate = new RestTemplate();
    private final String baseUrl = "http://localhost:8000/users";

    public UserAdminAgent(String server, int port) {
        this.server = server;
        this.port = port;
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
        } catch (HttpClientErrorException e) {
            log.error("HTTP 에러 발생");
        } catch (Exception e) {
            log.error("에러 발생 {}:", e.toString());
        }
        return false;
    }

    // 이미 유저가 가입된 계정일 때
    public boolean existUser(String userId) {
        try {
            List<String> userList = getUserList();
            return userList.contains(userId);
        } catch (Exception e) {
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
            ResponseEntity<List<String>> response = restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<String>>() {
            });
            List<String> domainList = response.getBody();

            if (domainList == null) { // 도메인 목록이 존재하지 않을 때
                return new LinkedList<>(); // null이 아닌 비어있는 리스트 전달
            }
            return domainList;
        } catch (Exception e) {
            log.error("도메인 목록 가져오기 실패 : {}", e.getMessage());
            return new LinkedList<>();
        }
    }

    // 도메인 추가
    public String addDomain(String domain) {
        String url = String.format("http://%s:%d/domains/%s", server, port, domain);

        // 이미 등록된 도메인인 경우
        List<String> domainList = getDomainList();
        if (domainList.contains(domain)) {
            log.info("이미 등록된 도메인입니다. : {}", domain);
            return "이미 등록된 도메인입니다.";
        }

        try {
            restTemplate.put(url, null);
            log.info("도메인 등록 성공: {}", domain);
            return "도메인 등록 성공";
        } catch (HttpClientErrorException e) {
            log.error("도메인 등록 실패: {} {} {}", domain, e.getStatusCode(), e.getResponseBodyAsString());
            return "도메인 등록 실패";
        } catch (Exception e) {
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
        } catch (Exception e) {
            log.error("도메인 예외 발생 : {}", e.toString());
        }
        return false;
    }

    // 도메인 삭제
    public boolean deleteDomain(String[] domainList) {
        boolean success = true;

        for (String domain : domainList) {
            try {
                String url = String.format("http://%s:%d/domains/%s", server, port, domain);
                restTemplate.delete(url);
                log.info("도메인 삭제: {}", domain);
            } catch (Exception e) {
                log.error("도메인 삭제 중 예외 발생 {}", e.toString());
                success = false;
            }
        }
        return success;
    }

    private String getBaseUrl() {
        return baseUrl;
    }

    private String getUserUrl(String userId) {
        return baseUrl + "/" + userId;
    }

    // 테스트코드를 위한 getter
    public String getServer() {
        return server;
    }

    public int getPort() {
        return port;
    }
}
