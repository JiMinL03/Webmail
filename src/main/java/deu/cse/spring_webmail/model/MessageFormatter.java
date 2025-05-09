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

    public String getMessageTable(Message[] messages, String userid) {
        //StringBuilder buffer = new StringBuilder();
        StringBuilder receivedBuffer = new StringBuilder();
        StringBuilder myselfBuffer = new StringBuilder();
        StringBuilder sendBuffer = new StringBuilder();
        // 메시지 제목 보여주기
        String tableHeader = "<table border='1'>"
                + "<tr><th>No</th><th>보낸 사람</th><th>제목</th><th>날짜</th><th>삭제</th></tr>";

        myselfBuffer.append("<h2>내게 쓴 메일</h2>").append(tableHeader);
        receivedBuffer.append("<h2>내가 받은 메일</h2>").append(tableHeader);
        sendBuffer.append("<h2>내가 보낸 메일</h2>").append(tableHeader);

        for (int i = messages.length - 1; i >= 0; i--) {
            MessageParser parser = new MessageParser(messages[i], userid);
            parser.parse(false);  // envelope 정보만 필요
            // 메시지 헤더 포맷
            // 추출한 정보를 출력 포맷 사용하여 스트링으로 만들기
            String row = "<tr> "
                    + "<td id=no>" + (i + 1) + "</td> "
                    + "<td id=sender>" + parser.getFromAddress() + "</td> "
                    + "<td id=subject><a href=show_message?msgid=" + (i + 1) + " title=\"메일 보기\">"
                    + parser.getSubject() + "</a></td> "
                    + "<td id=date>" + parser.getSentDate() + "</td> "
                    + "<td id=delete><a href=\"javascript:void(0);\" onclick=\"confirmDelete(" + (i + 1) + ")\">삭제</a></td> "
                    + "</tr>";

            if (userid.equals(parser.getFromAddress()) && userid.equals(parser.getToAddress())) {
                myselfBuffer.append(row);  // "Sent to Myself" (myself)
            } else if (userid.equals(parser.getFromAddress())) {
                sendBuffer.append(row);    // "Sent Mail"
            } else if (userid.equals(parser.getToAddress())) {
                receivedBuffer.append(row);  // "Received Mail"
            }
        }
        // 테이블 마무리
        myselfBuffer.append("</table>");
        receivedBuffer.append("</table>");
        sendBuffer.append("</table>");
        return receivedBuffer.toString() + "<br><br>" + sendBuffer.toString() + "<br><br>" + myselfBuffer.toString();
//        return "MessageFormatter 테이블 결과";
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
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss "); // 날짜 형식에 맞게 지정

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
    
    public String oldMessageTable(Message[] messages, String userid){
        StringBuilder oldMessageBuffer = new StringBuilder();
        // 메시지 제목 보여주기
        String tableHeader = "<table border='1'>"
                + "<tr><th>No</th><th>보낸 사람</th><th>제목</th><th>날짜</th><th>삭제</th></tr>";

        oldMessageBuffer.append("<h2>임시보관함</h2>").append(tableHeader);

        for (int i = messages.length - 1; i >= 0; i--) {
            MessageParser parser = new MessageParser(messages[i], userid);
            parser.parse(false); 
            String row = "<tr> "
                    + "<td id=no>" + (i + 1) + "</td> "
                    + "<td id=sender>" + parser.getFromAddress() + "</td> "
                    + "<td id=subject><a href=show_message?msgid=" + (i + 1) + " title=\"메일 보기\">"
                    + parser.getSubject() + "</a></td> "
                    + "<td id=date>" + parser.getSentDate() + "</td> "
                    + "<td id=delete><a href=\"javascript:void(0);\" onclick=\"confirmDelete(" + (i + 1) + ")\">삭제</a></td> "
                    + "</tr>";

            if (isThirtyDaysOld(parser.getSentDate())) {
                oldMessageBuffer.append(row);  // "Sent to Myself" (myself)
            }
        }
        // 테이블 마무리
        oldMessageBuffer.append("</table>");
        return oldMessageBuffer.toString();
    }
}
