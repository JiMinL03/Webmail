package deu.cse.spring_webmail.model;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.internet.MimeUtility;
import jakarta.mail.internet.MimeMessage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * SMTP 서버를 통해 메일을 전송하는 클래스
 */
@Slf4j
public class SmtpAgent {

    @Getter @Setter private String host;     // SMTP 서버 주소
    @Getter @Setter private String userid;   // 송신자 이메일 주소
    @Getter @Setter private String to;       // 수신자 이메일 주소
    @Getter @Setter private String cc;       // 참조 이메일 주소
    @Getter @Setter private String subj;     // 메일 제목
    @Getter @Setter private String body;     // 메일 본문
    @Getter @Setter private String file1;    // 첨부파일 경로

    public SmtpAgent(String host, String userid) {
        this.host = host;
        this.userid = userid;
    }

    /**
     * 메일을 구성하고 전송하는 메소드
     */
    public boolean sendMessage() {
        Properties props = System.getProperties();
        props.put("mail.debug", false);
        props.put("mail.smtp.host", this.host);
        log.debug("SMTP host: {}", props.get("mail.smtp.host"));

        Session session = Session.getDefaultInstance(props, null);
        session.setDebug(false);

        try {
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(this.userid));  // 송신자 설정

            // 수신자/참조 주소 설정 (세미콜론을 쉼표로 변환)
            msg.setRecipients(Message.RecipientType.TO, sanitizeAddress(this.to));
            if (this.cc != null && this.cc.length() > 1) {
                msg.setRecipients(Message.RecipientType.CC, sanitizeAddress(this.cc));
            }

            msg.setSubject(this.subj);  // 제목 설정
            msg.setHeader("User-Agent", "LJM-WM/0.1");

            // 본문 설정
            MimeBodyPart bodyPart = new MimeBodyPart();
            bodyPart.setText(this.body);

            Multipart mp = new MimeMultipart();
            mp.addBodyPart(bodyPart);

            // 첨부파일이 있으면 추가
            if (this.file1 != null) {
                MimeBodyPart attachment = createAttachmentPart(this.file1);
                mp.addBodyPart(attachment);
            }

            msg.setContent(mp);  // 본문 + 첨부 조합 설정
            Transport.send(msg); // 메일 전송

            deleteUploadedFile();  // 업로드된 임시파일 삭제

            return true;
        } catch (Exception ex) {
            log.error("sendMessage() error: {}", ex.getMessage(), ex);
            return false;
        }
    }

    /**
     * 주소 문자열에서 세미콜론을 쉼표로 변환하여 여러 수신자 처리
     */
    private String sanitizeAddress(String address) {
        return address.replace(";", ",");
    }

    /**
     * 첨부파일을 MimeBodyPart 형태로 생성
     */
    private MimeBodyPart createAttachmentPart(String filepath) throws Exception {
        MimeBodyPart attachment = new MimeBodyPart();
        DataSource src = new FileDataSource(filepath);
        attachment.setDataHandler(new DataHandler(src));

        // 첨부파일 이름 인코딩
        String fileName = new File(filepath).getName();
        attachment.setFileName(MimeUtility.encodeText(fileName, "UTF-8", "B"));
        return attachment;
    }

    /**
     * 업로드된 첨부파일을 삭제
     */
    private void deleteUploadedFile() {
        if (this.file1 == null) return;

        try {
            Files.deleteIfExists(new File(this.file1).toPath());
            log.debug("File deleted: {}", this.file1);
        } catch (IOException e) {
            log.error("Failed to delete file {}: {}", this.file1, e.getMessage(), e);
        }
    }
}
