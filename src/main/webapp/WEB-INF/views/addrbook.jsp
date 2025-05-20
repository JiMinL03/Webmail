<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>주소록</title>
    <link rel="stylesheet" href="css/main_style.css" />

    <script>
        <c:if test="${not empty msg}">
            alert("${msg}");
        </c:if>
    </script>
</head>
<body>
<%@ include file="header.jspf" %>

<div id="sidebar">
    <jsp:include page="sidebar_previous_menu.jsp" />
</div>

<div id="main">
    <div style="display: flex; gap: 40px; align-items: flex-start;">
        
        <!-- 주소록 추가 폼 (왼쪽) -->
        <div id="addrbook-form" style="flex: 1;">
            <h2>주소록 추가</h2>
            <form method="post" action="addrbook/add">
                <p>
                    이메일: <input type="concatEmail" name="concatEmail" required />
                </p>
                <button type="submit">추가</button>
            </form>
        </div>

        <!-- 주소록 목록 (오른쪽) -->
        <div id="addrbook-list" style="flex: 2;">
            <h2>주소록 목록</h2>
            <table border="1">
                <thead>
                <tr>
                    <th>이메일</th>
                    <th>삭제</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="entry" items="${addrbookList}">
                    <tr>
                                                <td>${entry.concatEmail}</td>
                        
                        <td>
                            <form method="post" action="addrbook/delete">
                                <input type="hidden" name="id" value="${entry.id}" />
                                <button type="submit">삭제</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>

    </div>
</div>

<%@ include file="footer.jspf" %>
</body>
</html>
