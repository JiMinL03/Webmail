<%-- 
    Document   : main_menu
    Created on : 2022. 6. 10., 오후 3:15:45
    Author     : skylo
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<!DOCTYPE html>

<!-- 제어기에서 처리하면 로직 관련 소스 코드 제거 가능!
<jsp:useBean id="pop3" scope="page" class="deu.cse.spring_webmail.model.Pop3Agent" />
<%
            pop3.setHost((String) session.getAttribute("host"));
            pop3.setUserid((String) session.getAttribute("userid"));
            pop3.setPassword((String) session.getAttribute("password"));
%>
-->

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>주메뉴 화면</title>
        <link type="text/css" rel="stylesheet" href="css/main_style.css" />
        <script>
            <c:if test="${!empty msg}">
            alert("${msg}");
            </c:if>
        </script>
    </head>
    <body>
        <%@include file="header.jspf"%>

        <div id="sidebar">
            <jsp:include page="sidebar_menu.jsp" />
        </div>

        <div id="main">
            <form class="search-container" action="search_result" method="get">
                <input type="text" id="searchInput" name="keyword" placeholder="검색어를 입력하세요">
                <button type="submit">검색</button>
            </form>
            <!-- 메시지 목록 -->
            <c:forEach var="message" items="${messageList}">
                <div>
                    ${message}
                    <hr/>
                </div>
            </c:forEach>
        </div>
        <script type="text/javascript">
            function confirmDelete(msgId) {
                var confirmation = confirm("정말 이 메시지를 삭제하시겠습니까?");
                if (confirmation) {
                    // 사용자가 '확인'을 누르면 삭제 요청을 보내도록 처리
                    window.location.href = 'delete_mail.do?msgid=' + msgId;
                }
            }
        </script>
        <%@include file="footer.jspf"%>
    </body>
</html>
