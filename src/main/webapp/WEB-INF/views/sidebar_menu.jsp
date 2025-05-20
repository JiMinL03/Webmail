<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="deu.cse.spring_webmail.control.CommandType" %>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>웹메일 시스템 메뉴</title>
        <!-- 외부 CSS 파일 링크 -->
        <link rel="stylesheet" type="text/css" href="css/sidebar_menu.css">
    </head>
    <body>

        <!-- 사용자 정보 표시 -->
        <div class="user-info">
            <span>사용자: <%= session.getAttribute("userid") %> </span>
        </div>

        <!-- 메뉴 항목 -->
        <p><a href="main_menu">전체 메일</a>
            <span class="total-count">${totalCount}</span>
        </p>
        <p> <a href="addrbook"> 주소록 </a> </p>
        <p> <a href="send_mail"> 내가 보낸 메일 </a> </p>
        <p> <a href="received_mail"> 내가 받은 메일 </a> </p>
        <p> <a href="my_mail"> 내게 쓴 메일 </a> </p>
        <p> <a href="mail_box"> 임시보관함 </a> </p>
        <p> <a href="write_mail"> 메일 쓰기 </a> </p>
        <p><a href="login.do?menu=<%= CommandType.LOGOUT %>">로그아웃</a></p>

    </body>
</html>
