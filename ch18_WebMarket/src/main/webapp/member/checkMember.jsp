<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="sql" uri="http://java.sun.com/jsp/jstl/sql"%>
<%
	String id = request.getParameter("id");
%>

<sql:setDataSource var="dataSource"
	url="jdbc:mysql://localhost:3306/WebMarketDB"
	driver="com.mysql.jdbc.Driver" user="root" password="k404" />


  <!--  디비에서 입력한 값 id로 검색 -->
<sql:query dataSource="${dataSource}" var="resultSet">
   SELECT * FROM MEMBER WHERE ID=?  
   <sql:param value="<%=id%>" />
</sql:query>

<!--  같은 값이 있으면 result값이 1, 값이 없으면 0 표시 -->
<c:set var="result" value="0" />
<c:forEach var="row" items="${resultSet.rows}">
	<c:set var="result" value="1" />
</c:forEach>

<html>
<head>
<link rel="stylesheet"
	href="<c:url value="/resources/css/bootstrap.min.css"/>" />
<title>중복체크</title>
</head>
<body>
	<br>
	<div class="container" align="center">
		<h6 class="alert alert-danger">
			<c:if test="${result == 0}"> 아이디를 사용할 수 있습니다 </c:if>
			<c:if test="${result == 1}"> 이미 사용 중인 아이디 입니다	</c:if>
		</h6>
	</div>

	<div class="container" align="center">
		<input type="button" onclick="window.close()"
			class="btn btn-secondary" value="닫기 ">

	</div>
</body>
</html>


