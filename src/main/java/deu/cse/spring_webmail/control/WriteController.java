package deu.cse.spring_webmail.control;

import deu.cse.spring_webmail.model.SmtpAgent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@PropertySource("classpath:/system.properties")
@Slf4j
public class WriteController {

    @Value("${file.upload_folder}")
    private String uploadFolder;

    @Value("${file.max_size}")
    private String maxSize;

    @Autowired
    private ServletContext ctx;

    @Autowired
    private HttpSession session;

    @GetMapping("/write_mail")
    public String writeMail() {
        log.debug("GET /write_mail called...");
        session.removeAttribute("sender");  // 임시 저장된 값이 있으면 삭제
        return "write_mail/write_mail";  // 메일 작성 화면 반환
    }

    @PostMapping("/write_mail.do")
    public String writeMailDo(@RequestParam String to,
                              @RequestParam String cc,
                              @RequestParam String subj,
                              @RequestParam String body,
                              @RequestParam(name = "file1", required = false) MultipartFile upFile,
                              @RequestParam String action,
                              RedirectAttributes attrs) {

        log.debug("POST /write_mail.do - to: {}, cc: {}, subj: {}, file: {}, action: {}",
                to, cc, subj, (upFile != null ? upFile.getOriginalFilename() : "없음"), action);

        if ("save".equals(action)) {
            // 임시 저장: 세션에 값 저장
            session.setAttribute("draft_to", to);
            session.setAttribute("draft_cc", cc);
            session.setAttribute("draft_subj", subj);
            session.setAttribute("draft_body", body);
            attrs.addFlashAttribute("msg", "임시 저장이 완료되었습니다.");
            return "redirect:/write_mail";
        }

        // 파일이 존재하는 경우 업로드 처리
        if (upFile != null && !upFile.isEmpty()) {
            String basePath = ctx.getRealPath(uploadFolder);
            File dir = new File(basePath);
            if (!dir.exists()) {
                dir.mkdirs();  // 폴더 없으면 생성
            }

            File file = new File(dir, upFile.getOriginalFilename());
            try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {
                bos.write(upFile.getBytes());  // 파일 업로드
            } catch (IOException e) {
                log.error("File upload failed: {}", e.getMessage());
            }
        }

        // 메일 전송 처리
        boolean sendSuccessful = sendMessage(to, cc, subj, body, upFile);
        if (sendSuccessful) {
            attrs.addFlashAttribute("msg", "메일 전송에 성공했습니다.");

            // 메일 전송 성공 시 임시 저장된 내용 삭제
            session.removeAttribute("draft_to");
            session.removeAttribute("draft_cc");
            session.removeAttribute("draft_subj");
            session.removeAttribute("draft_body");

        } else {
            attrs.addFlashAttribute("msg", "메일 전송에 실패했습니다.");
        }

        return "redirect:/main_menu";  // 메인 메뉴로 리다이렉트
    }

    private boolean sendMessage(String to, String cc, String subject, String body, MultipartFile upFile) {
        String host = (String) session.getAttribute("host");
        String userid = (String) session.getAttribute("userid");

        SmtpAgent agent = new SmtpAgent(host, userid);
        agent.setTo(to);
        agent.setCc(cc);
        agent.setSubj(subject);
        agent.setBody(body);

        // 파일이 첨부되었다면 첨부파일 경로 설정
        if (upFile != null) {
            String fileName = upFile.getOriginalFilename();
            if (fileName != null && !fileName.isEmpty()) {
                File file = new File(ctx.getRealPath(uploadFolder), fileName);
                agent.setFile1(file.getAbsolutePath());
            }
        }

        return agent.sendMessage();  // 메일 전송
    }
}
