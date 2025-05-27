<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>메일 쓰기 화면</title>
    <link type="text/css" rel="stylesheet" href="css/main_style.css" />

    <script>
        <c:if test="${not empty msg}">
            alert("${fn:escapeXml(msg)}");
        </c:if>

        function selectEmail(email) {
            const toField = document.getElementsByName("to")[0];
            if (toField.value) {
                toField.value += ", " + email;
            } else {
                toField.value = email;
            }
        }
    </script>
</head>
<body>
<%@ include file="../header.jspf" %>

<div id="sidebar">
    <jsp:include page="../sidebar_previous_menu.jsp" />
</div>

<div id="main">
    <!-- 전체 레이아웃 구성: 좌측 메일 작성, 우측 주소록 -->
    <div style="display: flex; gap: 40px; align-items: flex-start;">
        
        <!-- 메일 작성 폼 (좌측) -->
        <div style="flex: 2;">
            <form enctype="multipart/form-data" method="POST" action="write_mail.do">
                <table>
                    <caption>메일 작성 양식</caption>
                    <tr>
                        <th scope="col">수신</th>
                        <td>
                            <input type="text" name="to" size="80"
                                   value="${sessionScope.draft_to != null ? sessionScope.draft_to : ''}" />
                        </td>
                    </tr>
                    <tr>
                        <th scope="col">참조</th>
                        <td>
                            <input type="text" name="cc" size="80"
                                   value="${sessionScope.draft_cc != null ? sessionScope.draft_cc : ''}" />
                        </td>
                    </tr>
                    <tr>
                        <th scope="col">메일 제목</th>
                        <td>
                            <input type="text" name="subj" size="80"
                                   value="${sessionScope.draft_subj != null ? sessionScope.draft_subj : ''}" />
                        </td>
                    </tr>
                    <tr>
                        <th scope="col" colspan="2">본문</th>
                    </tr>
                    <tr>
                        <td colspan="2">
                            <textarea rows="15" name="body" cols="80">${sessionScope.draft_body != null ? sessionScope.draft_body : ''}</textarea>
                        </td>
                    </tr>
                    <tr>
                        <th scope="col">첨부 파일</th>
                        <td><input type="file" name="file1" size="80" /></td>
                    </tr>
                    <tr>
                        <td colspan="2">
                            <button type="submit" name="action" value="send">메일 보내기</button>
                            <button type="submit" name="action" value="save">임시 저장</button>
                            <input type="reset" value="다시 입력" />
                        </td>
                    </tr>
                </table>
            </form>
        </div>

        <!-- 주소록 패널 (우측) -->
        <div style="flex: 1; border: 1px solid #ccc; padding: 15px; border-radius: 5px;">
            <h3>주소록</h3>
            <ul style="list-style: none; padding-left: 0;">
                <c:forEach var="entry" items="${addrbookList}">
                    <li style="margin-bottom: 10px;">
                        <a href="#" onclick="selectEmail('${entry.concatEmail}'); return false;">
                            ${entry.name} (${entry.concatEmail})
                        </a>
                    </li>
                </c:forEach>
            </ul>
        </div>

    </div>
</div>

<%@ include file="../footer.jspf" %>
</body>
</html>
