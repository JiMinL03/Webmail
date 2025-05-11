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
import java.util.Arrays;
import java.util.stream.Collectors;

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
        this.downloadTempDir = request.getServletContext().getRealPath(downloadPath);

        createDirectoryIfNotExists(downloadTempDir);
    }

    public boolean parse(boolean parseBody) {
        try {
            extractEnvelope();
            if (parseBody) {
                processPart(message);
            }
            return true;
        } catch (Exception ex) {
            log.error("메일 파싱 실패: {}", ex.getMessage(), ex);
            return false;
        }
    }

    private void extractEnvelope() throws Exception {
        fromAddress = addressToString(message.getFrom());
        toAddress = addressToString(message.getRecipients(Message.RecipientType.TO));
        ccAddress = addressToString(message.getRecipients(Message.RecipientType.CC));
        subject = message.getSubject();
        sentDate = trimDate(message.getSentDate().toString());
    }

    private void processPart(Part part) throws Exception {
        if (part.isMimeType("multipart/*")) {
            processMultipart(part);
        } else if (part.isMimeType("text/*")) {
            extractTextContent(part);
        } else if (isAttachment(part.getDisposition())) {
            handleAttachment(part);
        }
    }

    private boolean isAttachment(String disposition) {
        return disposition != null &&
               (Part.ATTACHMENT.equalsIgnoreCase(disposition) || Part.INLINE.equalsIgnoreCase(disposition));
    }

    private void handleAttachment(Part part) throws Exception {
        String originalFileName = part.getFileName();
        if (originalFileName == null) return;

        fileName = MimeUtility.decodeText(originalFileName).replace(" ", "_");
        String userDir = downloadTempDir + File.separator + userid;
        createDirectoryIfNotExists(userDir);

        File file = new File(userDir, fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            part.getDataHandler().writeTo(fos);
        }
    }

    private void extractTextContent(Part part) throws Exception {
        body = (String) part.getContent();
        if (part.isMimeType("text/plain")) {
            body = body.replace("\r\n", " <br>");
        }
    }

    private void processMultipart(Part part) throws Exception {
        Multipart multipart = (Multipart) part.getContent();
        for (int i = 0; i < multipart.getCount(); i++) {
            processPart(multipart.getBodyPart(i));
        }
    }

    private String addressToString(Address[] addresses) {
        if (addresses == null || addresses.length == 0) return "";
        return Arrays.stream(addresses)
                .map(Address::toString)
                .collect(Collectors.joining(", "));
    }

    private String trimDate(String dateStr) {
        return (dateStr.length() > 8) ? dateStr.substring(0, dateStr.length() - 8) : dateStr;
    }

    private void createDirectoryIfNotExists(String path) {
        File dir = new File(path);
        if (!dir.exists() && !dir.mkdirs()) {
            log.warn("디렉터리 생성 실패: {}", path);
        }
    }
}
