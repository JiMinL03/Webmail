package deu.cse.spring_webmail.admin;

import deu.cse.spring_webmail.model.UserAdminAgent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import org.junit.jupiter.api.AfterEach;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserAdminAgentTest {

    @Autowired
    private UserAdminAgent userAdminAgent;

    @Test
    public void testAddsUser() {
        String userId = "adduser@user.com";
        String password = "1234";

        // 사용자 추가
        boolean result = userAdminAgent.addUser(userId, password);
        assertTrue(result, "사용자 추가 실패");

        // 사용자 리스트 확인
        List<String> userList = userAdminAgent.getUserList();
        assertTrue(userList.contains(userId), "추가된 사용자가 리스트에 없음");
    }

    @Test
    public void testDeletesUser() {
        String userId = "deleteuser@user.com";
        String password = "1234";

        // 먼저 사용자 추가
        userAdminAgent.addUser(userId, password);

        // 삭제
        boolean result = userAdminAgent.deleteUsers(new String[]{userId});
        assertTrue(result, "사용자 삭제 실패");

        // 리스트에서 삭제되었는지 확인
        List<String> userList = userAdminAgent.getUserList();
        assertFalse(userList.contains(userId), "삭제된 사용자가 여전히 리스트에 존재");
    }

    @Test
    public void testGetUserList() {
        List<String> userList = userAdminAgent.getUserList();

        assertNotNull(userList, "userList가 null임");
        assertTrue(userList.size() > 0, "userList가 비어 있음");  
    }
    
    @AfterEach
    public void cleanUp() {
        List<String> userList = userAdminAgent.getUserList();
        if (userList.contains("adduser@user.com")) {
            userAdminAgent.deleteUsers(new String[]{"adduser@user.com"});
        }
    }
}
