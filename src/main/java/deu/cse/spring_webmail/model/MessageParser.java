package deu.cse.spring_webmail.model;

import deu.cse.spring_webmail.PropertyReader;
import jakarta.activation.DataHandler;
import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.mail.internet.MimeUtility;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class MessageParser {

    @NonNull @Getter @Setter private Message message;
    @NonNull @Getter @Setter private String userid;

    @Getter @Setter private String toAddress;
    @Getter @Setter private String fromAddress;
    @Getter @Setter private String ccAddress;
    @Getter @Setter private String sentDate;
    @Getter @Setter private String subject;
    @Getter @Setter private String body;
    @Getter @Setter private String fileName;
    @Getter @Setter private String downloadTempDir = "C:/temp/download/";

    public MessageParser(Message message, String userid, HttpServletRequest request) {
        this.message = message;
        this.userid = userid;

        PropertyReader props = new PropertyReader();
        String downloadPath = props.getProperty("file.download_folder");
        downloadTempDir = request.getServletContext().getRealPath(downloadPath);

        File f = new File(downloadTempDir);
        if (!f.exists() && !f.mkdir()) {
            log.warn("다운로드 디렉터리 생성 실패: {}", downloadTempDir);
        }
    }

    public boolean parse(boolean parseBody) {
        try {
            extractEnvelope();
            if (parseBody) {
                processPart(message);
            }
            return true;
        } catch (Exception ex) {
            log.error("MessageParser.parse() - Exception : {}", ex.getMessage(), ex);
            return false;
        }
    }

    // 메일 헤더 정보 추출
    private void extractEnvelope() throws Exception {
        fromAddress = message.getFrom()[0].toString();
        toAddress = getAddresses(message.getRecipients(Message.RecipientType.TO));
        Address[] addr = message.getRecipients(Message.RecipientType.CC);
        ccAddress = (addr != null) ? getAddresses(addr) : "";
        subject = message.getSubject();
        sentDate = message.getSentDate().toString();
        if (sentDate.length() > 8) {
            sentDate = sentDate.substring(0, sentDate.length() - 8);
        }
    }

    // 메일 본문 또는 첨부파일 처리
    private void processPart(Part part) throws Exception {
        String disposition = part.getDisposition();

        if (isAttachment(disposition)) {
            handleAttachment(part);
        } else if (part.isMimeType("text/*")) {
            extractTextContent(part);
        } else if (part.isMimeType("multipart/*")) {
            processMultipart(part);
        }
    }

    // 첨부파일 여부 확인
    private boolean isAttachment(String disposition) {
        return disposition != null &&
                (disposition.equalsIgnoreCase(Part.ATTACHMENT) || disposition.equalsIgnoreCase(Part.INLINE));
    }

    // 첨부파일 저장 처리
    private void handleAttachment(Part part) throws Exception {
        fileName = MimeUtility.decodeText(part.getFileName());
        if (fileName == null) return;

        String tempUserDir = downloadTempDir + File.separator + userid;
        File dir = new File(tempUserDir);
        if (!dir.exists() && !dir.mkdir()) {
            log.warn("사용자 디렉터리 생성 실패: {}", tempUserDir);
        }

        String safeFileName = fileName.replace(" ", "_");
        try (FileOutputStream fos = new FileOutputStream(tempUserDir + File.separator + safeFileName)) {
            part.getDataHandler().writeTo(fos);
        }
    }

    // 텍스트 MIME 처리
    private void extractTextContent(Part part) throws Exception {
        body = (String) part.getContent();
        if (part.isMimeType("text/plain")) {
            body = body.replace("\r\n", " <br>");
        }
    }

    // 멀티파트 처리 (재귀 호출)
    private void processMultipart(Part part) throws Exception {
        Multipart mp = (Multipart) part.getContent();
        for (int i = 0; i < mp.getCount(); i++) {
            processPart(mp.getBodyPart(i));
        }
    }

    // 주소 배열을 문자열로 변환
    private String getAddresses(Address[] addresses) {
        StringBuilder buffer = new StringBuilder();
        for (Address address : addresses) {
            buffer.append(address.toString()).append(", ");
        }
        if (buffer.length() >= 2) {
            buffer.setLength(buffer.length() - 2);
        }
        return buffer.toString();
    }
}
