<%-- 
    Document   : add_domain
    Created on : 2025. 5. 19., 오후 11:48:28
    Author     : mskim
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>도메인 관리 화면</title>
        <link type="text/css" rel="stylesheet" href="css/main_style.css">
    </head>
    <body>
        <%@ include file="../../header.jspf" %>
                   
        <div id="sidebar">
            <jsp:include page="sidebar_domain_previous_menu.jsp" />
        </div>
        
        <div id="main">
            등록할 도메인을 입력해주시기 바랍니다. <br> <br>
            
            <form name="AddDoamin" action="add_domain.do" method="POST">
                <table border="0" align="left">
                    <tr>
                        <th scope="row">도메인</th>
                        <td> <input type="text" name="domain" value="" /> </td>
                    </tr>
                    <tr>
                        <td colspan="2">
                            <input type="submit" value="등록" name="register" />
                            <input type="reset" value="초기화" name="reset" />
                        </td>
                    </tr>
                </table>
            </form>
           
        </div>
        
        <%@include file="../../footer.jspf" %>
    </body>
</html>
