/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.cse.spring_webmail.model;

import static deu.cse.spring_webmail.model.MessageFormatter.MailType.ALL_MAIL;
import static deu.cse.spring_webmail.model.MessageFormatter.MailType.DRAFT;
import static deu.cse.spring_webmail.model.MessageFormatter.MailType.RECEIVED_MAIL;
import static deu.cse.spring_webmail.model.MessageFormatter.MailType.SENT_MAIL;
import static deu.cse.spring_webmail.model.MessageFormatter.MailType.SENT_TO_MYSELF;
import jakarta.mail.FetchProfile;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Store;
import java.util.Properties;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author skylo
 */
@Slf4j
@NoArgsConstructor        // 기본 생성자 생성
public class Pop3Agent {

    @Getter
    @Setter
    private String host;
    @Getter
    @Setter
    private String userid;
    @Getter
    @Setter
    private String password;
    @Getter
    @Setter
    private Store store;
    @Getter
    @Setter
    private String excveptionType;
    @Getter
    @Setter
    private HttpServletRequest request;

    // 220612 LJM - added to implement REPLY
    @Getter
    private String sender;
    @Getter
    private String subject;
    @Getter
    private String body;

    private static final String MAILBOX_INBOX = "INBOX";
    private static final String VALUE_FALSE = "false";
    private static final String VALUE_TRUE = "true";
    private static final String POP3_CONNECTION_FAILED = "POP3 connection failed!";

    public enum MailType {
        SENT_TO_MYSELF, // 내게 쓴 메일
        SENT_MAIL, // 내가 보낸 메일
        RECEIVED_MAIL, // 내가 받은 메일
        DRAFT, // 임시보관함
        ALL_MAIL
    }

    public Pop3Agent(String host, String userid, String password) {
        this.host = host;
        this.userid = userid;
        this.password = password;
    }

    public boolean validate() {
        boolean status = false;

        try {
            status = connectToStore();
            store.close();
        } catch (Exception ex) {
            log.error("Pop3Agent.validate() error : " + ex);
            status = false;  // for clarity
        }
        return status;
    }

    public boolean deleteMessage(int msgid, boolean really_delete) {
        boolean status = false;

        if (!connectToStore()) {
            return status;
        }

        try {
            // Folder 설정
//            Folder folder = store.getDefaultFolder();
            Folder folder = store.getFolder(MAILBOX_INBOX);
            folder.open(Folder.READ_WRITE);

            // Message에 DELETED flag 설정
            Message msg = folder.getMessage(msgid);
            msg.setFlag(Flags.Flag.DELETED, really_delete);

            // 폴더에서 메시지 삭제
            // Message [] expungedMessage = folder.expunge();
            // <-- 현재 지원 안 되고 있음. 폴더를 close()할 때 expunge해야 함.
            folder.close(true);  // expunge == true
            store.close();
            status = true;
        } catch (Exception ex) {
            log.error("deleteMessage() error: {}", ex.getMessage());
        }
        return status;
    }

    /*
     * 페이지 단위로 메일 목록을 보여주어야 함.
     */
    public int getMessageCount() { //페이징 기능 구현을 위한 전체 페이지 갯수 반혼
        if (!connectToStore()) {
            log.error(POP3_CONNECTION_FAILED);
            return 0; // 연결 실패 시 0 반환
        }

        try {
            Folder folder = store.getFolder(MAILBOX_INBOX);
            folder.open(Folder.READ_ONLY);
            int count = folder.getMessageCount();

            folder.close(false); // READ_ONLY일 경우 false
            store.close();

            return count;
        } catch (Exception ex) {
            log.error("Pop3Agent.getMessageCount() : exception = {}", ex.getMessage());
            return 0;
        }
    }

    public String getMessage(int n) {
        String result = "POP3  서버 연결이 되지 않아 메시지를 볼 수 없습니다.";

        if (!connectToStore()) {
            log.error(POP3_CONNECTION_FAILED);
            return result;
        }

        try {
            Folder folder = store.getFolder(MAILBOX_INBOX);
            folder.open(Folder.READ_ONLY);

            Message message = folder.getMessage(n);

            MessageFormatter formatter = new MessageFormatter(userid);
            formatter.setRequest(request);  // 210308 LJM - added
            result = formatter.getMessage(message);
            sender = formatter.getSender();  // 220612 LJM - added
            subject = formatter.getSubject();
            body = formatter.getBody();

            folder.close(true);
            store.close();
        } catch (Exception ex) {
            log.error("Pop3Agent.getMessageList() : exception = {}", ex);
            result = "Pop3Agent.getMessage() : exception = " + ex;
        }
        return result;
    }

    private boolean connectToStore() {
        boolean status = false;
        Properties props = System.getProperties();
        // https://jakarta.ee/specifications/mail/2.1/apidocs/jakarta.mail/jakarta/mail/package-summary.html
        props.setProperty("mail.pop3.host", host);
        props.setProperty("mail.pop3.user", userid);
        props.setProperty("mail.pop3.apop.enable", VALUE_FALSE);
        props.setProperty("mail.pop3.disablecapa", VALUE_TRUE);  // 200102 LJM - added cf. https://javaee.github.io/javamail/docs/api/com/sun/mail/pop3/package-summary.html
        props.setProperty("mail.debug", VALUE_FALSE);
        props.setProperty("mail.pop3.debug", VALUE_FALSE);

        Session session = Session.getInstance(props);
        session.setDebug(false);

        try {
            store = session.getStore("pop3");
            store.connect(host, userid, password);
            status = true;
        } catch (Exception ex) {
            log.error("connectToStore 예외: {}", ex.getMessage());
        }
        return status;
    }

    // 공통된 메서드로 메시지를 가져오는 로직 처리
    private String getMessages(String mailboxType, MessageFormatter.MailType mailType, int start, int end) {
        String result = "";
        Message[] messages = null;

        if (!connectToStore()) {
            log.error(POP3_CONNECTION_FAILED);
            return "POP3 연결이 되지 않아 메일 목록을 볼 수 없습니다.";
        }

        try {
            Folder folder = store.getFolder(mailboxType);  // mailBoxType에 따라 다른 메일함을 열 수 있음
            folder.open(Folder.READ_ONLY);

            messages = folder.getMessages(end, start);
            FetchProfile fp = new FetchProfile();
            fp.add(FetchProfile.Item.ENVELOPE);
            folder.fetch(messages, fp);

            // 공통된 MessageFormatter 사용
            MessageFormatter formatter = new MessageFormatter(userid);
            switch (mailType) {
                case SENT_TO_MYSELF:
                    result = formatter.getSentToMyselfMessages(messages, userid);   // 내게 쓴 메일
                    break;
                case SENT_MAIL:
                    result = formatter.getSentMailMessages(messages, userid);   // 내가 보낸 메일
                    break;
                case RECEIVED_MAIL:
                    result = formatter.getReceivedMessages(messages, userid);   // 내가 받은 메일
                    break;
                case DRAFT:
                    result = formatter.getDraftMessages(messages, userid);   // 임시보관함
                    break;
                case ALL_MAIL:
                    result = formatter.getAllMailMessages(messages, userid);
                    break;
                default:
                    result = "잘못된 메일 유형입니다.";
                    break;
            }

            folder.close(true);  // 메일 폴더 닫기
            store.close();       // POP3 연결 종료
        } catch (Exception ex) {
            log.error("Pop3Agent.getMessages() : exception = {}", ex.getMessage());
            result = "Pop3Agent.getMessages() : exception = " + ex.getMessage();
        }

        return result;
    }

    public int countPage(MessageFormatter.MailType mailType) {
        Message[] messages = null;
        int count = 0;

        if (!connectToStore()) {
            log.error(POP3_CONNECTION_FAILED);
            return 0;
        }

        try {
            Folder folder = store.getFolder(MAILBOX_INBOX);
            folder.open(Folder.READ_ONLY);

            messages = folder.getMessages();
            FetchProfile fp = new FetchProfile();
            fp.add(FetchProfile.Item.ENVELOPE);
            folder.fetch(messages, fp);

            MessageFormatter formatter = new MessageFormatter(userid);

            switch (mailType) {
                case SENT_TO_MYSELF:
                    count = formatter.countIncludedMessages(messages, userid, MessageFormatter.MailType.SENT_TO_MYSELF);
                    break;
                case SENT_MAIL:
                    count = formatter.countIncludedMessages(messages, userid, MessageFormatter.MailType.SENT_MAIL);
                    break;
                case RECEIVED_MAIL:
                    count = formatter.countIncludedMessages(messages, userid, MessageFormatter.MailType.RECEIVED_MAIL);
                    break;
                case DRAFT:
                    count = formatter.countIncludedMessages(messages, userid, MessageFormatter.MailType.DRAFT);
                    break;
                case ALL_MAIL:
                    count = formatter.countIncludedMessages(messages, userid, MessageFormatter.MailType.ALL_MAIL);
                    break;
                default:
                    log.warn("알 수 없는 mailType입니다: {}", mailType);
                    break;
            }

            folder.close(true);
            store.close();

        } catch (Exception ex) {
            log.error("Pop3Agent.countPage() : 예외 발생 = {}", ex.getMessage(), ex);
        }

        return count;
    }

    public String getOldMessage(int start, int end) {
        return getMessages(MAILBOX_INBOX, MessageFormatter.MailType.DRAFT, start, end);
    }

    public String getMyMail(int start, int end) {
        return getMessages(MAILBOX_INBOX, MessageFormatter.MailType.SENT_TO_MYSELF, start, end);
    }

    public String getReceivedMessage(int start, int end) {
        return getMessages(MAILBOX_INBOX, MessageFormatter.MailType.RECEIVED_MAIL, start, end);
    }

    public String getSendMail(int start, int end) {
        return getMessages(MAILBOX_INBOX, MessageFormatter.MailType.SENT_MAIL, start, end);
    }

    public String getMessageList(int start, int end) {
        return getMessages(MAILBOX_INBOX, MessageFormatter.MailType.ALL_MAIL, start, end);
    }
}
