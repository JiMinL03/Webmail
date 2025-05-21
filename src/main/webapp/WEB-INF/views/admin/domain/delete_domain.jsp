<%-- 
    Document   : delete_domain
    Created on : 2025. 5. 20., 오전 12:35:37
    Author     : mskim
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>도메인 제거 화면</title>
        <link type="text/css" rel="stylesheet" href="css/main_style.css" />
        <script>
            function getConfirmResult() {
                var result = confirm("사용자를 정말로 삭제하시겠습니까?");
                return result;
            }
        </script>
    </head>
    <body>
        <%@ include file="../../header.jspf" %>
                   
        <div id="sidebar">
            <jsp:include page="sidebar_domain_previous_menu.jsp" />
        </div>
        
        <div id="main">
            <h2> 삭제할 도메인을 선택해 주세요 </h2> <br>
            
            <form name="DeleteDomain" action="delete_domain.do" method="POST">
                <%
                    for (String domain : (java.util.List<String>) request.getAttribute("domainList")) {
                    out.print("<label><input type=checkbox name=\"domain\" "
                        + "value=\"" + domain + "\" />");
                    out.println(domain+"</label> <br>");
                    }
                %>
                <br>
                <input type="submit" value="도메인 제거" name="delete_domain"
                       onClick="return getConfirmResult()" />
                <input type ="reset" value="선택 전부 취소" />
            </form>
        </div>
                
        <%@include file="../../footer.jspf" %>
    </body>
</html>
