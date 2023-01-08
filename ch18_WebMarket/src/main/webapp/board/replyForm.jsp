<%@ page contentType="text/html; charset=utf-8" isELIgnored="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%
	String name = (String) request.getAttribute("name");
	
%>
<html>
<head>
<link rel="stylesheet" href="./resources/css/bootstrap.min.css" />
<title>reply</title>
</head>
<script  src="http://code.jquery.com/jquery-latest.min.js"></script>
<script type="text/javascript">
	function checkForm() {
		if (!document.newWrite.name.value) {
			alert("성명을 입력하세요.");
			return false;
		}
		if (!document.newWrite.subject.value) {
			alert("제목을 입력하세요.");
			return false;
		}
		if (!document.newWrite.content.value) {
			alert("내용을 입력하세요.");
			return false;
		}		
	}
	
	/* 파일첨부 추가하는 함수 */
	 var cnt=1;
	  function addFile(){
		  $("#d_file").append("<br>"+"<input type='file' name='file"+cnt+"' />");
		  cnt++;
	  }  
	
</script>
<body>
	<jsp:include page="../menu.jsp" />
	<div class="jumbotron">
		<div class="container">
		<!-- 	<h1 class="display-3">게시판</h1> -->
		</div>
	</div>


	 <br>
	<b><font size="6" color="gray">답글 작성</font></b>
	<br>
	<br>
	
	
	<form method="post" action="BoardReplyAction.bo?page=${pageNum}" name="boardForm">
	<!-- 부모글 정보를 넘긴다. -->
	<input type="hidden" name="board_id" value="${sessionScope.sessionID}">
	<input type="hidden" name="board_num" value="${board.board_num}"/>
	<input type="hidden" name="board_re_ref" value="${board.board_re_ref}"/>
 

<div class="container">

		<form name="newWrite" action="./BoardWriteAction.do"
			class="form-horizontal" method="post" onsubmit="return checkForm()"
			enctype="multipart/form-data">
			
			<input name="id" type="hidden" class="form-control"
				value="${sessionId}">
			<div class="form-group row">
				<label class="col-sm-2 control-label" >성명</label>
				<div class="col-sm-3">
					<input name="name" type="text" class="form-control" value="<%=name %>"
						placeholder="name">
				</div>
			</div>
			<div class="form-group row">
				<label class="col-sm-2 control-label" >제목</label>
				<div class="col-sm-5">

					<input name="subject" type="text" class="form-control"
						placeholder="subject">
				</div>
			</div>
			<div class="form-group row">
				<label class="col-sm-2 control-label" >내용</label>
				<div class="col-sm-8">
					<textarea name="content" cols="50" rows="5" class="form-control"
						placeholder="content"></textarea>
				</div>
			</div>
		
		


		<div class="form-group row">
				<div class="col-sm-offset-2 col-sm-10 ">
				 <input type="submit" class="btn btn-primary " value="등록 ">				
					 <input type="reset" class="btn btn-primary " value="취소 ">
				<!-- 	  <input type="button" class="btn btn-primary " value="목록 "> -->
				<input type="button" value="목록" onclick="javascript:history.go(-1)">			
			</div>
			</td>
		</tr>
	</table>	
	</form>
			<hr>
	</div>
	<jsp:include page="../footer.jsp" />
</body>
</html>