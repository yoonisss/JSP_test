<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>


<!-- 작업에 필요한 패키지들을 추가했음. -->
<%@ page import="mvc.model.BoardDTO"%>
<%@ page import="mvc.model.FileImageDTO"%>
<%@ page import="java.util.ArrayList"%>

<%
	BoardDTO notice = (BoardDTO) request.getAttribute("board");
// 해당 뷰에 작업 하기 위해서, 컨트롤러에서 설정한 파일 이미지들 전체를 담는 컬렉션을 가져오는 역할. 
@SuppressWarnings("unchecked")	
 ArrayList<FileImageDTO> fileLists = (ArrayList<FileImageDTO>) request.getAttribute("fileLists");

// 기존 일반글 작성시 필요한 해당 게시글 번호 및 페이지 번호를 작업.
	int num = ((Integer) request.getAttribute("num")).intValue();
	int nowpage = ((Integer) request.getAttribute("page")).intValue();
%>
<html>
<head>
<link rel="stylesheet" href="./resources/css/bootstrap.min.css" />
<title>Board</title>
</head>
<body>
	<jsp:include page="../menu.jsp" />
	<div class="jumbotron">
		<div class="container">
			<h1 class="display-3">게시판</h1>
		</div>
	</div>

	<div class="container">
		<form name="newUpdate"
			action="BoardUpdateAction.do?num=<%=notice.getNum()%>&pageNum=<%=nowpage%>"
			class="form-horizontal" method="post">
			<div class="form-group row">
				<label class="col-sm-2 control-label" >성명</label>
				<div class="col-sm-3">
					<input name="name" class="form-control"	value=" <%=notice.getName()%>">
				</div>
			</div>
			<div class="form-group row">
				<label class="col-sm-2 control-label" >제목</label>
				<div class="col-sm-5">
					<input name="subject" class="form-control"	value=" <%=notice.getSubject()%>" >
				</div>
			</div>
			<div class="form-group row">
				<label class="col-sm-2 control-label" >내용</label>
				<div class="col-sm-8" style="word-break: break-all;">
					<textarea name="content" class="form-control" cols="50" rows="5"> <%=notice.getContent()%></textarea>
				</div>
			</div>
			 <!-- 반복문으로 컬렉션에 있는 파일 이미지 객체를 하나씩 꺼내서 가져오는 작업. -->
			<% for (int i = 0; i < fileLists.size(); i++) {
				FileImageDTO fileImageDTO = new FileImageDTO();
				fileImageDTO = fileLists.get(i);
				String image = fileImageDTO.getFileName();
				
				%>
				<div class="col-md-4">
				<%--  <img src="C:/JSP_Workspace1/ch18_WebMarket/src/main/webapp/resources/board_images/<%= image %>" style="width: 70%">  --%>
				<img src="../resources/board_images/<%= image %>" style="width: 70%"> 
				<%-- <img src="./resources/images/<%=rs.getString("p_fileName")%>" style="width: 100%"> --%>
				<%= image %>
				</div>
				<%
				}
				 %>
			<div class="form-group row">
				<div class="col-sm-offset-2 col-sm-10 ">
					<c:set var="userId" value="<%=notice.getId()%>" />
					<c:if test="${sessionId==userId}">
						<p>
							<a	href="./BoardDeleteAction.do?num=<%=notice.getNum()%>&pageNum=<%=nowpage%>"	class="btn btn-danger"> 삭제</a> 
							<input type="submit" class="btn btn-success" value="수정 ">
					</c:if>
					<a href="./BoardListAction.do?pageNum=<%=nowpage%>"		class="btn btn-primary"> 목록</a>
				</div>
			</div>
		</form>
		<hr>
	</div>
	<jsp:include page="../footer.jsp" />
</body>
</html>


