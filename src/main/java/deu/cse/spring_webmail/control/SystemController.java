/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.cse.spring_webmail.control;

import deu.cse.spring_webmail.model.Pop3Agent;
import deu.cse.spring_webmail.model.UserAdminAgent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import javax.imageio.ImageIO;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 초기 화면과 관리자 기능(사용자 추가, 삭제)에 대한 제어기
 *
 * @author skylo
 */
@Controller
@PropertySource("classpath:/system.properties")
@Slf4j
public class SystemController {

    @Autowired
    private ServletContext ctx;
    @Autowired
    private HttpSession session;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private UserAdminAgent userAdminAgent;

    @Value("${admin.id}")
    private String ADMINISTRATOR;
    @Value("${james.control.port}")
    private Integer JAMES_CONTROL_PORT;//8000
    @Value("${james.host}")
    private String JAMES_HOST;//127.0.0.1

    private static final String REDIRECT_ADMIN_MENU = "redirect:/admin_menu";
    private static final String SESSION_USERID = "userid";

    private static final String REDIRECT_ROOT = "redirect:/";
    private static final String REDIRECT_DOMAIN_ROOT = "redirect:/domain_menu";
    private static final String SIGNUP_USER_ROOT = "admin/sign_up_user";

    @GetMapping("/")
    public String index() {
        log.debug("index() called...");
        session.setAttribute("host", JAMES_HOST);
        session.setAttribute("debug", "false");

        return "/index";
    }

    @RequestMapping(value = "/login.do", method = {RequestMethod.GET, RequestMethod.POST})
    public String loginDo(@RequestParam Integer menu, RedirectAttributes attrs) {
        String url = "";
        log.debug("로그인 처리: menu = {}", menu);
        switch (menu) {
            case CommandType.LOGIN: // 로그인 요청

                String host = (String) request.getSession().getAttribute("host");
                String userid = request.getParameter(SESSION_USERID);
                String password = request.getParameter("passwd");

                Pop3Agent pop3Agent = new Pop3Agent(host, userid, password);
                boolean isLoginSuccess = pop3Agent.validate();

                if (isLoginSuccess) {
                    if (isAdmin(userid)) {
                        session.setAttribute(SESSION_USERID, userid);
                        attrs.addFlashAttribute("msg", "관리자 로그인이 맞습니까?");
                        url = REDIRECT_ADMIN_MENU; // admin_menu.jsp 이동
                    } else {
                        session.setAttribute(SESSION_USERID, userid);
                        session.setAttribute("password", password);
                        attrs.addFlashAttribute("msg", "사용자 로그인이 맞습니까?");
                        url = "redirect:/main_menu";

                    }
                } else {
                    url = "redirect:/login_fail";
                }
                break;

            case CommandType.LOGOUT: // 로그아웃 요청
                session.invalidate(); // 현재 세션 무효화
                url = REDIRECT_ROOT;  // redirect: 반드시 넣어야만 컨텍스트 루트로 갈 수 있음 => "/webmail"로 이동
                break;
            default:
                break;
        }
        return url;
    }

    @GetMapping("/login_fail")
    public String loginFail() {
        return "login_fail";
    }

    protected boolean isAdmin(String userid) {
        boolean status = false;

        if (userid.equals(this.ADMINISTRATOR)) {
            status = true;
        }

        return status;
    }

    @GetMapping("/admin_menu")
    public String adminMenu(Model model) {
        String userid = (String) session.getAttribute(SESSION_USERID);

        if (userid == null || userid.isEmpty()) {
            log.warn("비로그인 상태로 admin_menu 접근 시도");
            return "redirect:/login.do";
        }

        model.addAttribute("userList", getUserList());
        return "admin/admin_menu";
    }

    // 도메인 목록 보여주는 메서드 ( 재사용 많이 하는거라서 메서드로 따로 뺌 )
    public void domainListModel(Model model) {
        UserAdminAgent agent = new UserAdminAgent(JAMES_HOST, JAMES_CONTROL_PORT);
        List<String> domainList = agent.getDomainList();
        model.addAttribute("domainList", domainList);
    }

    // 도메인 메뉴 (도메인 목록 보여주기)
    @GetMapping("/domain_menu")
    public String domainManage(Model model) {

        domainListModel(model);

        return "admin/domain/domain_menu";
    }

    // 도메인 추가
    @GetMapping("/add_domain")
    public String addDomain() {
        return "admin/domain/add_domain";
    }

    @PostMapping("/add_domain.do")
    public String addDomainDo(@RequestParam("domain") String domainName, RedirectAttributes attrs) {

        UserAdminAgent agent = new UserAdminAgent(JAMES_HOST, JAMES_CONTROL_PORT);
        String message = agent.addDomain(domainName);

        try {
            attrs.addFlashAttribute("msg", message);

        } catch (Exception e) {
            attrs.addFlashAttribute("msg", message);
        }
        return REDIRECT_DOMAIN_ROOT;
    }

    // 도메인 삭제 => 삭제할 도메인 목록 보여주기
    @GetMapping("/delete_domain")
    public String deleteDomain(Model model) {

        domainListModel(model);

        return "admin/domain/delete_domain";
    }

    @PostMapping("/delete_domain.do")
    public String deleteDomainDo(@RequestParam("domain") String[] domainList, RedirectAttributes attrs) {

        UserAdminAgent agent = new UserAdminAgent(JAMES_HOST, JAMES_CONTROL_PORT);

        // 삭제할 수 없는 도메인 보관
        List<String> blocked = new ArrayList<>();

        for (String domain : domainList) {
            if (agent.domainUse(domain)) {
                blocked.add(domain);
            }
        }

        if (!blocked.isEmpty()) {
            attrs.addFlashAttribute("msg", "사용자가 사용하고 있는 도메인입니다. 삭제하실 수 없습니다.");
            return REDIRECT_DOMAIN_ROOT;
        }

        boolean success = agent.deleteDomain(domainList);

        if (success) {
            attrs.addFlashAttribute("msg", "도메인 삭제 성공");
        } else {
            attrs.addFlashAttribute("msg", "도메인 삭제 실패");
        }
        return REDIRECT_DOMAIN_ROOT;
    }

    // 사용자 회원가입
    @GetMapping("/sign_up")
    public String addUser(Model model) {
        domainListModel(model);
        return SIGNUP_USER_ROOT;
    }

    @PostMapping("/add_user.do")
    public String addUserDo(@RequestParam String id, @RequestParam String password, @RequestParam String confirmPassword, @RequestParam String domain, Model model,
            RedirectAttributes attrs) {

        // 비밀번호 검증
        if (!password.equals(confirmPassword)) {
            model.addAttribute("msg", "비밀번호와 비밀번호 확인이 다릅니다. 다시 입력해주세요");
            domainListModel(model);
            return SIGNUP_USER_ROOT;
        }

        String fullId = id + "@" + domain;

        try {

            UserAdminAgent agent = new UserAdminAgent(JAMES_HOST, JAMES_CONTROL_PORT);

            // 중복 가입자 확인
            if (agent.existUser(fullId)) {
                attrs.addFlashAttribute("msg", "이미 가입되어 있는 계정입니다.");
                return REDIRECT_ROOT;
            } else if (agent.addUser(fullId, password)) {
                attrs.addFlashAttribute("msg", String.format("사용자 회원가입(%s)을 성공하였습니다.", fullId));
                return REDIRECT_ROOT;
            } else {
                attrs.addFlashAttribute("msg", String.format("사용자 회원가입(%s)을 실패하였습니다.", fullId));
                return REDIRECT_ROOT;

            }
        } catch (Exception ex) {
            log.error("add_user.do: 시스템 접속에 실패했습니다. 예외 = {}", ex.getMessage());

        }

        return REDIRECT_ROOT;
    }

    @GetMapping("/delete_user")
    public String deleteUser(Model model) {
        log.debug("delete_user called");
        model.addAttribute("userList", getUserList());
        return "admin/delete_user";
    }

    /**
     *
     * @param selectedUsers <input type=checkbox> 필드의 선택된 이메일 ID. 자료형: String[]
     * @param attrs
     * @return
     */
    @PostMapping("delete_user.do")
    public String deleteUserDo(@RequestParam String[] selectedUsers, RedirectAttributes attrs) {
        log.debug("delete_user.do: selectedUser = {}", List.of(selectedUsers));

        try {

            userAdminAgent.deleteUsers(selectedUsers);  // 수정!!!
        } catch (Exception ex) {
            log.error("delete_user.do : 예외 = {}", ex);
        }

        return REDIRECT_ADMIN_MENU;
    }

    private List<String> getUserList() {

        List<String> userList = userAdminAgent.getUserList();
        log.debug("userList = {}", userList);

        //(주의) root.id와 같이 '.'을 넣으면 안 됨.
        userList.sort((e1, e2) -> e1.compareTo(e2));//알파벳 순으로 정렬
        return userList;
    }

    @GetMapping("/img_test")
    public String imgTest() {
        return "img_test/img_test";
    }

    /**
     * https://34codefactory.wordpress.com/2019/06/16/how-to-display-image-in-jsp-using-spring-code-factory/
     *
     * @param imageName
     * @return
     */
    @RequestMapping(value = "/get_image/{imageName}")
    @ResponseBody
    public byte[] getImage(@PathVariable String imageName) {
        try {
            String folderPath = ctx.getRealPath("/WEB-INF/views/img_test/img");
            return getImageBytes(folderPath, imageName);
        } catch (Exception e) {
            log.error("/get_image 예외: {}", e.getMessage());
        }
        return new byte[0];
    }

    private byte[] getImageBytes(String folderPath, String imageName) {
        ByteArrayOutputStream byteArrayOutputStream;
        BufferedImage bufferedImage;
        byte[] imageInByte;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            bufferedImage = ImageIO.read(new File(folderPath + File.separator + imageName));
            String format = imageName.substring(imageName.lastIndexOf(".") + 1);
            ImageIO.write(bufferedImage, format, byteArrayOutputStream);
            byteArrayOutputStream.flush();
            imageInByte = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();
            return imageInByte;
        } catch (FileNotFoundException e) {
            log.error("getImageBytes 예외: {}", e.getMessage());
        } catch (Exception e) {
            log.error("getImageBytes 예외: {}", e.getMessage());
        }
        return null;
    }

}
