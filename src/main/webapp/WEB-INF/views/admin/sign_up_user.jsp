<%-- 
    Document   : sign_up_user
    Created on : 2025. 5. 20., 오후 8:48:09
    Author     : mskim
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>사용자 추가 화면</title>
        <link type="text/css" rel="stylesheet" href="css/main_style.css" />
        <script>
            <c:if test="${!empty msg}">
                alert("${msg}");
            </c:if>
        </script>
    </head>
    <body>
        <%@ include file="../header.jspf" %>

        <div id="main">
            사용자 ID와 암호를 입력해 주시기 바랍니다. <br> <br>

            <form name="AddUser" action="add_user.do" method="POST">
                <table border="0" align="left">
                    <tr>
                        <td>ID</td>
                        <td>
                            <input type="text" name="id" value="" size="20" />
                            @
                            <select name="domain" required>
                                <option value="" disabled selected>-------</option>
                                <c:forEach var="domain" items="${domainList}">
                                    <option value="${domain}">${domain}</option>
                                </c:forEach>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td>비밀번호</td>
                        <td> <input type="password" name="password" value="" /> </td>
                    </tr>
                    <tr>
                        <td>비밀번호 확인</td>
                        <td>
                            <input type="password" name="confirmPassword" value="" />
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2">
                            <input type="submit" value="회원가입" name="register" />
                            <input type="reset" value="초기화" name="reset" />
                            <button type="button" onclick="history.back();">뒤로 가기</button>
                        </td>
                    </tr>
                </table>
            </form>
        </div>

        <%@include file="../footer.jspf" %>
    </body>
</html>
