/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package deu.cse.spring_webmail.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
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
    
    private final RestTemplate restTemplate = new RestTemplate();

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
    private String cwd;
    private final String baseUrl = "http://localhost:8000";

    public UserAdminAgent() {
    }

    public UserAdminAgent(String server, int port, String cwd,
              String admin_pass, String admin_id) {
        log.debug("UserAdminAgent created: server = " + server + ", port = " + port);
        this.server = server;  // 127.0.0.1
        this.port = port;  // 8000
        this.cwd = cwd;
        this.ADMIN_PASSWORD = admin_pass;
        this.ADMIN_ID = admin_id;

        log.debug("isConnected = {}, root.id = {}", isConnected, ADMIN_ID);

        //여기부터 다 지워도 상관없을듯
        /*try {
            socket = new Socket(server, port);
            System.out.println("UserAdminAgent소켓 연결 성공");
            is = socket.getInputStream();
            os = socket.getOutputStream();
        } catch (Exception e) {
            log.error("UserAdminAgent 생성자 예외: {}", e.getMessage());
        }*/
       /* isConnected = connect();
        System.out.println(isConnected);*/
    }

    /**
     *
     * @param userId
     * @param password
     * @return a boolean value as follows: - true: addUser operation successful
     * - false: addUser operation failed
     */
    // return value:
    //   - true: addUser operation successful
    //   - false: addUser operation failed
    public boolean addUser(String userId, String password) {
        String url = baseUrl + "/users/" + userId;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(password, headers);

        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                url, HttpMethod.PUT, request, Void.class);
            System.out.println("응답코드는 과연~~");
        log.info("Response Status Code: {}", response.getStatusCode());
        // 응답 코드가 204(No Content)인 경우 사용자 추가 성공
        if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
            return true; // 사용자 추가 성공
        }
        } catch (HttpClientErrorException.Conflict e) {
            log.warn("User already exists: {}", userId);
        } catch (HttpClientErrorException.BadRequest e) {
            log.warn("Invalid user creation request: {}", userId);
        } catch (Exception e) {
            log.error("Error adding user {}: {}", userId, e.getMessage());
        }
        return false;
    }
    
    public List<String> getUserList() {
        String url = baseUrl + "/users";
        try {
            ResponseEntity<String[]> response = restTemplate.getForEntity(url, String[].class);
            List<String> users = new LinkedList<>(Arrays.asList(response.getBody()));
            users.removeIf(user -> user.equalsIgnoreCase("root@localhost")); // 필터링
            return users;
        } catch (Exception e) {
            log.error("Error fetching user list: {}", e.getMessage());
            return new LinkedList<>();
        }
        /*List<String> userList = new LinkedList<String>();
        byte[] messageBuffer = new byte[1024];

        log.info("root.id = {}, root.password = {}", ROOT_ID, ADMIN_PASSWORD);

        if (!isConnected) {
            return userList;
        }

        try {
            // 1: "listusers" 명령 송신
            String command = "listusers " + EOL;
            os.write(command.getBytes());

            // 2: "listusers" 명령에 대한 응답 수신
            java.util.Arrays.fill(messageBuffer, (byte) 0);
            is.read(messageBuffer);

            // 3: 응답 메시지 처리
            String recvMessage = new String(messageBuffer);
            log.debug("recvMessage = {}", recvMessage);
            userList = parseUserList(recvMessage);

            quit();
        } catch (Exception ex) {
            log.error("getUserList(): 예외 = {}", ex.getMessage());
        } finally {
            return userList;
        }*/
    }   // getUserList()

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
            String url = baseUrl + "/users/" + user;
            try {
                restTemplate.delete(url);
                log.info("Deleted user: {}", user);
            } catch (Exception e) {
                log.error("Error deleting user {}: {}", user, e.getMessage());
                allSuccess = false;
            }
        }
        return allSuccess;
        /*byte[] messageBuffer = new byte[1024];
        String command;
        String recvMessage;
        boolean status = false;

        if (!isConnected) {
            return status;
        }

        try {
            for (String userId : userList) {
                // 1: "deluser" 명령 송신
                command = "deluser " + userId + EOL;
                os.write(command.getBytes());
                log.debug(command);

                // 2: 응답 메시지 수신
                java.util.Arrays.fill(messageBuffer, (byte) 0);
                is.read(messageBuffer);

                // 3: 응답 메시지 분석
                recvMessage = new String(messageBuffer);
                log.debug("recvMessage = {}", recvMessage);
                if (recvMessage.contains("deleted")) {
                    status = true;
                }
            }
            quit();
        } catch (Exception ex) {
            log.error("deleteUsers(): 예외 = {}", ex.getMessage());
        } finally {
            return status;
        }*/
    }  // deleteUsers()

    public boolean verify(String userId) {
        String url = baseUrl + "/users/" + userId;
        try {
            restTemplate.getForEntity(url, Void.class);
            return true; // 존재함
        } catch (HttpClientErrorException.NotFound e) {
            return false; // 존재하지 않음
        } catch (Exception e) {
            log.error("Error verifying user {}: {}", userId, e.getMessage());
            return false;
        }
    /*
        boolean status = false;
        byte[] messageBuffer = new byte[1024];

        try {
            // --> verify userid
            String verifyCommand = "verify " + userid;
            os.write(verifyCommand.getBytes());

            // read the result for verify command
            // <-- User userid exists   or
            // <-- User userid does not exist
            is.read(messageBuffer);
            String recvMessage = new String(messageBuffer);
            if (recvMessage.contains("exists")) {
                status = true;
            }

            quit();  // quit command
        } catch (IOException ex) {
            log.error("verify(): 예외 = {}", ex.getMessage());
        } finally {
            return status;
        }*/
    }

    /*private boolean connect() {
        byte[] messageBuffer = new byte[1024];
        boolean returnVal = false;
        String sendMessage;
        String recvMessage;

        log.info("connect() : root.id = {}, root.password = {}", ROOT_ID, ADMIN_PASSWORD);

        // root 인증: id, passwd - default: root
        // 1: Login Id message 수신
        
        try {
        log.debug("Step 1: 서버로부터 로그인 메시지 수신 대기");
        is.read(messageBuffer);
        recvMessage = new String(messageBuffer);
            System.out.println("receive message " + recvMessage + "123");
        log.debug("받은 메시지: {}", recvMessage.trim());

        log.debug("Step 2: root ID 전송");
        sendMessage = ROOT_ID + EOL;
        os.write(sendMessage.getBytes());

        log.debug("Step 3: 비밀번호 입력 메시지 수신 대기");
        Arrays.fill(messageBuffer, (byte) 0);
        is.read(messageBuffer);

        log.debug("Step 4: root PASSWORD 전송");
        sendMessage = ADMIN_PASSWORD + EOL;
        os.write(sendMessage.getBytes());

        log.debug("Step 5: Welcome 메시지 수신 대기");
        Arrays.fill(messageBuffer, (byte) 0);
        is.read(messageBuffer);
        recvMessage = new String(messageBuffer);
        log.debug("받은 메시지: {}", recvMessage.trim());
        
        if (recvMessage.contains("Welcome")) {
        System.out.println("connect true");
        returnVal = true;
         } else {
        System.out.println("connect false");
         }
        } catch (Exception e) {
        log.error("connect() 예외 발생", e);
         }


        return returnVal;
    }  // connect()*/

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
        } finally {
            return status;
        }
    }
}
