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
                if (confirm("${msg}")) {
                    // 창 그대로 이동
                }
                else {
                    location.href = "${pageContext.request.contextPath}/";
                }
            </c:if>
        </script>
    </head>
    <body>
        <%@include file="header.jspf"%>

        <div id="sidebar">
            <jsp:include page="sidebar_menu.jsp" />
        </div>

        <!-- 메시지 삭제 링크를 누르면 바로 삭제되어 실수할 수 있음. 해결 방법은? -->
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

        <div class="pagination">
            <!-- 이전 페이지 -->
            <c:if test="${currentPage > 1}">
                <a href="?page=${currentPage - 1}">이전</a>
            </c:if>

            <!-- 페이지 번호 -->
            <c:forEach begin="1" end="${totalPages}" var="i">
                <c:choose>
                    <c:when test="${i == currentPage}">
                        <strong>[${i}]</strong> <!-- 현재 페이지는 강조 -->
                    </c:when>
                    <c:otherwise>
                        <a href="?page=${i}" class="page-link">[${i}]</a> <!-- 페이지 링크 -->
                    </c:otherwise>
                </c:choose>
            </c:forEach>

            <!-- 다음 페이지 -->
            <c:if test="${currentPage < totalPages}">
                <a href="?page=${currentPage + 1}">다음</a>
            </c:if>
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
