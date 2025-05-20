<%-- 
    Document   : domain_manage
    Created on : 2025. 5. 12., 오후 4:04:42
    Author     : mskim
--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>도메인 관리 메뉴</title>
        <link type="text/css" rel="stylesheet" href="css/main_style.css">
        <script>
            <c:if test="${!empty msg}">
            alert("${msg}");
            </c:if>
        </script>
    </head>
    <body>
        <%@ include file = "../../header.jspf" %>
        
        <div id="sidebar">
            <jsp:include page="../sidebar_admin_previous_menu.jsp" />
            
            <p><a href="add_domain">도메인 추가</a></p>
            <p><a href ="delete_domain">도메인 삭제</a></p>
        </div>
        
        
        <div id="domain_list">
            <h2>도메인 목록</h2>
            
            <ul>
                <c:forEach items="${domainList}" var="domain">
                    <li> ${domain} </li>
                </c:forEach>
            </ul>
        </div>
        
        <%@ include file="../../footer.jspf" %>
    </body>
    
</html>
