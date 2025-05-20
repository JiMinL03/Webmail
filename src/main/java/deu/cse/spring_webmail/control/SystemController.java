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
import org.springframework.web.client.RestTemplate;
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
    
    private RestTemplate restTemplate = new RestTemplate();

    @Value("${admin.password}")
    private String ADMIN_PASSWORD;
    @Value("${admin.id}")
    private String ADMINISTRATOR;
    @Value("${james.control.port}")
    private Integer JAMES_CONTROL_PORT;//8000
    @Value("${james.host}")
    private String JAMES_HOST;//127.0.0.1
    
    private static final String REDIRECT_ADMIN_MENU = "redirect:/admin_menu";
    private static final String SESSION_USERID = "userid";

    @GetMapping("/")
    public String index() {
        log.debug("index() called...");
        session.setAttribute("host", JAMES_HOST);
        session.setAttribute("debug", "false");

        return "/index";
    }

    @RequestMapping(value = "/login.do", method = {RequestMethod.GET, RequestMethod.POST})
    public String loginDo(@RequestParam Integer menu, RedirectAttributes attrs) { // [변경 부분] RedirectAttributes attrs 추가
        String url = "";
        log.debug("로그인 처리: menu = {}", menu);
        switch (menu) { 
            case CommandType.LOGIN: // 로그인 요청
                
                String host = (String) request.getSession().getAttribute("host");
                String userid = request.getParameter(SESSION_USERID);
                String password = request.getParameter("passwd");
                
                // Pop3Agent 모델 클래스를 이용해서 로그인 정보가 유효한지 확인하라
                // Pop3Agent 모델 클래스에서 로그인 유효성 검사를 실시해라

                Pop3Agent pop3Agent = new Pop3Agent(host, userid, password);
                boolean isLoginSuccess = pop3Agent.validate();
                
                // Now call the correct page according to its validation result.
                if (isLoginSuccess) {                 
                    if (isAdmin(userid)) {
                        // HttpSession 객체에 userid를 등록해 둔다.
                        session.setAttribute(SESSION_USERID, userid);
                        attrs.addFlashAttribute("msg", "관리자 로그인이 맞습니까?"); // [변경 부분]
                        // response.sendRedirect("admin_menu.jsp");
                        url = REDIRECT_ADMIN_MENU; // admin_menu.jsp 이동
                    }
                    else{
                        // HttpSession 객체에 userid와 password를 등록해 둔다.
                        session.setAttribute(SESSION_USERID, userid);
                        session.setAttribute("password", password);
                        // response.sendRedirect("main_menu.jsp");
                        attrs.addFlashAttribute("msg", "사용자 로그인이 맞습니까?"); // [변경 부분]
                        url = "redirect:/main_menu";  // URL이 http://localhost:8080/webmail/main_menu 이와 같이 됨. // main_menu.jsp로 이동
                        // url = "/main_menu";  // URL이 http://localhost:8080/webmail/login.do?menu=91 이와 같이 되어 안 좋음
                    
                }} else {
                    // RequestDispatcher view = request.getRequestDispatcher("login_fail.jsp");
                    // view.forward(request, response);
                    url = "redirect:/login_fail";
                }
                break;
                
            case CommandType.LOGOUT: // 로그아웃 요청
                session.invalidate(); // 현재 세션 무효화
                url = "redirect:/";  // redirect: 반드시 넣어야만 컨텍스트 루트로 갈 수 있음 => "/webmail"로 이동
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
        log.debug("root.password = {}, admin.id = {}",
                ADMIN_PASSWORD, ADMINISTRATOR);

        model.addAttribute("userList", getUserList());
        return "admin/admin_menu";
    }
    

    // 도메인 메뉴 (도메인 목록 보여주기)
    @GetMapping("/domain_menu")
    public String domainManage(Model model) {
        UserAdminAgent domain = new UserAdminAgent(JAMES_HOST, JAMES_CONTROL_PORT,
                    ADMIN_PASSWORD, ADMINISTRATOR);
        List<String> domainList = domain.getDomainList();
        model.addAttribute("domainList", domainList);
        return "admin/domain/domain_menu";
    }
    
    // 도메인 추가
    @GetMapping("/add_domain")
    public String addDomain() {
        return "admin/domain/add_domain";
    }
    
    @PostMapping("/add_domain.do")
    public String addDomainDo(@RequestParam("domain") String domainName, RedirectAttributes attrs) {
        UserAdminAgent domain = new UserAdminAgent(JAMES_HOST, JAMES_CONTROL_PORT,
                    ADMIN_PASSWORD, ADMINISTRATOR);
        boolean success = domain.addDomain(domainName);
        if (success) {
            attrs.addFlashAttribute("msg", "도메인 등록 성공");
        }
        else {
            attrs.addFlashAttribute("msg", "도메인 등록 실패");
        }
        return "redirect:/domain_menu";
    }
    
    // 도메인 삭제 => 삭제할 도메인 목록 보여주기
    @GetMapping("/delete_domain")
    public String deleteDomain(Model model) {
        UserAdminAgent domain = new UserAdminAgent(JAMES_HOST, JAMES_CONTROL_PORT,
                    ADMIN_PASSWORD, ADMINISTRATOR);
        List<String> domainList = domain.getDomainList();
        model.addAttribute("domainList", domainList);
        return "admin/domain/delete_domain";
    }
    
    @PostMapping("/delete_domain.do")
    public String deleteDomainDo(@RequestParam("domain") String[] domainList, RedirectAttributes attrs) {
        UserAdminAgent domain = new UserAdminAgent(JAMES_HOST, JAMES_CONTROL_PORT,
                    ADMIN_PASSWORD, ADMINISTRATOR);
        boolean success = domain.deleteDomain(domainList);
        if (success) {
            attrs.addFlashAttribute("msg", "도메인 삭제 성공");
        }
        else {
            attrs.addFlashAttribute("msg", "도메인 삭제 실패");
        }
        return "redirect:/domain_menu";
    }

    // 사용자 회원가입
    @GetMapping("/sign_up")
    public String addUser() {
        return "admin/sign_up_user";
    }

    @PostMapping("/add_user.do")
    public String addUserDo(@RequestParam String id, @RequestParam String password,@RequestParam String confirmPassword, Model model,
            RedirectAttributes attrs) {
        
        if (!password.equals(confirmPassword)) {
            model.addAttribute("msg", "비밀번호와 비밀번호 확인이 다릅니다. 다시 입력해주세요");
            return "admin/sign_up_user";
        }

        try {

            UserAdminAgent agent = new UserAdminAgent(JAMES_HOST, JAMES_CONTROL_PORT,
                    ADMIN_PASSWORD, ADMINISTRATOR);

            // if (addUser successful)  사용자 등록 성공 팝업창
            // else 사용자 등록 실패 팝업창
            if (agent.addUser(id, password)) {
                attrs.addFlashAttribute("msg", String.format("사용자 회원가입(%s) 추가를 성공하였습니다.", id));
            } else {
                attrs.addFlashAttribute("msg", String.format("사용자 회원가입(%s) 추가를 실패하였습니다.", id));
            }
        } catch (Exception ex) {
            log.error("add_user.do: 시스템 접속에 실패했습니다. 예외 = {}", ex.getMessage());
        }

        return "index";
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
            
            UserAdminAgent agent = new UserAdminAgent(JAMES_HOST, JAMES_CONTROL_PORT,
                    ADMIN_PASSWORD, ADMINISTRATOR);
            agent.deleteUsers(selectedUsers);  // 수정!!!
        } catch (Exception ex) {
            log.error("delete_user.do : 예외 = {}", ex);
        }

        return REDIRECT_ADMIN_MENU;
    }

    private List<String> getUserList() {

        UserAdminAgent agent = new UserAdminAgent(JAMES_HOST, JAMES_CONTROL_PORT,
                ADMIN_PASSWORD, ADMINISTRATOR);
        List<String> userList = agent.getUserList();
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
