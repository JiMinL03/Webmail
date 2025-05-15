/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package deu.cse.spring_webmail.model;

import jakarta.mail.Message;
import jakarta.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author skylo
 */
@Slf4j
@RequiredArgsConstructor
public class MessageFormatter {

    @NonNull
    private String userid;  // 파일 임시 저장 디렉토리 생성에 필요
    private HttpServletRequest request = null;

    // 220612 LJM - added to implement REPLY
    @Getter
    private String sender;
    @Getter
    private String subject;
    @Getter
    private String body;

    final String BR = " <br>";

    public enum MailType {
        SENT_TO_MYSELF, // 내게 쓴 메일
        SENT_MAIL, // 내가 보낸 메일
        RECEIVED_MAIL, // 내가 받은 메일
        DRAFT, // 임시보관함
        ALL_MAIL
    }

    public String getMessageTable(Message[] messages, String userid, String tableTitle, MailType mailType) {
        StringBuilder buffer = new StringBuilder();
        String tableHeader = "<table border='1'>"
                + "<tr><th>보낸 사람</th><th>제목</th><th>날짜</th><th>삭제</th></tr>";

        buffer.append("<h2>").append(tableTitle).append("</h2>").append(tableHeader);

        for (int i = messages.length - 1; i >= 0; i--) {
            MessageParser parser = new MessageParser(messages[i], userid);
            parser.parse(false);  // envelope 정보만 필요

            if (shouldIncludeMessage(parser, userid, mailType)) {
                buffer.append(createRow(i, parser));
            }
        }

        buffer.append("</table>");
        return buffer.toString();
    }

    private boolean shouldIncludeMessage(MessageParser parser, String userid, MailType mailType) {
        String from = parser.getFromAddress(); //보낸 사람
        String to = parser.getToAddress(); //받은 사람
        System.out.println("받은 사람: " + to);

        switch (mailType) {
            case SENT_TO_MYSELF:
                return userid.equals(from) && userid.equals(to);
            case SENT_MAIL:
                return userid.equals(from) && !userid.equals(to);
            case RECEIVED_MAIL:
                return userid.equals(to) && !userid.equals(from);
            case DRAFT:
                return isThirtyDaysOld(parser.getSentDate());
            case ALL_MAIL:
            default:
                return true;
        }
    }

    private String createRow(int index, MessageParser parser) {
        return "<tr> "
                + "<td id=sender>" + parser.getFromAddress() + "</td> "
                + "<td id=subject><a href=show_message?msgid=" + (index + 1) + " title=\"메일 보기\">"
                + parser.getSubject() + "</a></td> "
                + "<td id=date>" + parser.getSentDate() + "</td> "
                + "<td id=delete><a href=\"javascript:void(0);\" onclick=\"confirmDelete(" + (index + 1) + ")\">삭제</a></td> "
                + "</tr>";
    }

    public String getAllMailMessages(Message[] messages, String userid) {
        return getMessageTable(messages, userid, "전체 메일", MailType.ALL_MAIL);
    }

    // 내가 보낸 메일
    public String getSentMailMessages(Message[] messages, String userid) {
        return getMessageTable(messages, userid, "내가 보낸 메일", MailType.SENT_MAIL);
    }

// 내게 쓴 메일
    public String getSentToMyselfMessages(Message[] messages, String userid) {
        return getMessageTable(messages, userid, "내게 쓴 메일", MailType.SENT_TO_MYSELF);
    }

// 내가 받은 메일
    public String getReceivedMessages(Message[] messages, String userid) {
        return getMessageTable(messages, userid, "내가 받은 메일", MailType.RECEIVED_MAIL);
    }

// 임시보관함
    public String getDraftMessages(Message[] messages, String userid) {
        return getMessageTable(messages, userid, "임시보관함", MailType.DRAFT);
    }

    public String getMessage(Message message) {
        StringBuilder buffer = new StringBuilder();

        // MessageParser parser = new MessageParser(message, userid);
        MessageParser parser = new MessageParser(message, userid, request);
        parser.parse(true);

        sender = parser.getFromAddress();
        subject = parser.getSubject();
        body = parser.getBody();

        buffer.append("보낸 사람: " + parser.getFromAddress() + BR);
        buffer.append("받은 사람: " + parser.getToAddress() + BR);
        buffer.append("Cc &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; : " + parser.getCcAddress() + BR);
        buffer.append("보낸 날짜: " + parser.getSentDate() + BR);
        buffer.append("제 &nbsp;&nbsp;&nbsp;  목: " + parser.getSubject() + BR + " <hr>");

        buffer.append(parser.getBody());

        String attachedFile = parser.getFileName();
        if (attachedFile != null) {
            buffer.append("<br> <hr> 첨부파일: <a href=download"
                    + "?userid=" + this.userid
                    + "&filename=" + attachedFile.replace(" ", "%20")
                    + " target=_top> " + attachedFile + "</a> <br>");
        }

        return buffer.toString();
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public boolean isThirtyDaysOld(String date) {
        Date d1 = new Date(); // 현재 날짜

        // 날짜 형식에 맞는 SimpleDateFormat 객체 생성
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss", Locale.ENGLISH);

        // 입력 날짜에서 불필요한 공백을 제거
        date = date.replaceAll("\\s+", " ").trim();

        try {
            // 날짜 문자열을 Date 객체로 변환
            Date inputDate = sdf.parse(date);

            // Date 객체의 밀리초 값
            long inputDateMillis = inputDate.getTime();

            // 30일을 밀리초로 계산
            long thirtyDaysInMillis = 30L * 24 * 60 * 60 * 1000; // 30일을 밀리초로 변환

            // 현재 날짜와 입력된 날짜의 차이를 계산
            long differenceInMillis = Math.abs(d1.getTime() - inputDateMillis);

            // 차이가 정확히 30일이면 true, 아니면 false
            return differenceInMillis == thirtyDaysInMillis;

        } catch (ParseException e) {
            // 날짜 형식이 맞지 않으면 예외 처리
            e.printStackTrace();
            return false;
        }
    }
}
