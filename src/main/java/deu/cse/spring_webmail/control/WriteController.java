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
        session.removeAttribute("sender");
        return "write_mail/write_mail";
    }

    @PostMapping("/write_mail.do")
    public String writeMailDo(@RequestParam String to,
                              @RequestParam String cc,
                              @RequestParam String subj,
                              @RequestParam String body,
                              @RequestParam(name = "file1") MultipartFile upFile,
                              RedirectAttributes attrs) {

        log.debug("POST /write_mail.do - to: {}, cc: {}, subj: {}, file: {}",
                to, cc, subj, upFile.getOriginalFilename());

        // 파일 업로드 처리
        if (!upFile.isEmpty()) {
            String basePath = ctx.getRealPath(uploadFolder);
            File dir = new File(basePath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File file = new File(dir, upFile.getOriginalFilename());
            try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {
                bos.write(upFile.getBytes());
            } catch (IOException e) {
                log.error("File upload failed: {}", e.getMessage());
            }
        }

        // 이메일 전송
        boolean sendSuccessful = sendMessage(to, cc, subj, body, upFile);
        if (sendSuccessful) {
            attrs.addFlashAttribute("msg", "메일 전송에 성공했습니다.");
        } else {
            attrs.addFlashAttribute("msg", "메일 전송에 실패했습니다.");
        }

        return "redirect:/main_menu";
    }

    private boolean sendMessage(String to, String cc, String subject, String body, MultipartFile upFile) {
        String host = (String) session.getAttribute("host");
        String userid = (String) session.getAttribute("userid");

        SmtpAgent agent = new SmtpAgent(host, userid);
        agent.setTo(to);
        agent.setCc(cc);
        agent.setSubj(subject);
        agent.setBody(body);

        String fileName = upFile.getOriginalFilename();
        if (fileName != null && !fileName.isEmpty()) {
            File file = new File(ctx.getRealPath(uploadFolder), fileName);
            agent.setFile1(file.getAbsolutePath());
        }

        return agent.sendMessage();
    }
}
