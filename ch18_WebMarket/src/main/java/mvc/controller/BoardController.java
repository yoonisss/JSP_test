package mvc.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.oreilly.servlet.MultipartRequest;
import com.oreilly.servlet.multipart.DefaultFileRenamePolicy;

import mvc.model.BoardDAO;
import mvc.model.BoardDTO;
import mvc.model.FileImageDTO;

public class BoardController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	// 해다 게시판의 페이징 처리하기위한 상수값, -> 목록에 보여주는 갯수.
	static final int LISTCOUNT = 5;
	// 처음에 해당 게시글을 작성시, 이미지 파일을 저장하는 테이블의 부모글이 처음에 없음.
	// 그래서, 임시로 해당 부모글의 갯수를 저장하는 변수를 공유 변수 형식으로 사용. 
	// 단점. 서버가 리로드 될때 마다 갱싱되어서, 작업이 불편함. -> 해당 테이블을 삭제후 생성을 반복. 
	// 테이블을 하나 만들어서 따로 분리해서 관리 할수도 있음. . 해당 게시글의 번호만 저장하는 역할. 
	static int boardNum = 0;

	// get 로 전송되어도, post 방식으로 다 처리하는 로직으로 예제 구성이 되어있음.
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

//보드 관련된 모든 처리를 다하는 로직이라서,
	// 게시판에 접속만 하더라도, 콘솔 상에서 확인 가능함.
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// RequestURI 의 주소 부분에서
		// contextPath 프로젝트명 부분를 자르기를 하고서
		// command : /BoardListAction.do 이런 형식으로 가져오기 위해서.

		String RequestURI = request.getRequestURI();
		String contextPath = request.getContextPath();
		String command = RequestURI.substring(contextPath.length());

		response.setContentType("text/html; charset=utf-8");
		request.setCharacterEncoding("utf-8");

		// 게시판을 클릭시, 여기 첫번째 조건문에서 처리하는 과정을 보자.
		if (command.equals("/BoardListAction.do")) {// ��ϵ� �� ��� ������ ����ϱ�
			// 게시판의 페이지 정보랑, 게시물 정보등을 불러와서,
			// 해당 request 객체에 담아두는역할.

			// 게시판의 목록에 관련된 비지니스 로직.
			requestBoardList(request);

			RequestDispatcher rd = request.getRequestDispatcher("./board/list.jsp");
			rd.forward(request, response);
		} else if (command.equals("/BoardWriteForm.do")) { // 글쓰기 폼
			requestLoginName(request);
			RequestDispatcher rd = request.getRequestDispatcher("./board/writeForm.jsp");
			rd.forward(request, response);
		} else if (command.equals("/BoardWriteAction.do")) {// 글쓰기 폼에서 입력 후 처리하는 로직.
			// 여기서 글쓰기작성시 필요한 로직
			// 여기안에 이미지를 등록하는 메서드를 추가 할 예정.
			requestBoardWrite(request);
			RequestDispatcher rd = request.getRequestDispatcher("/BoardListAction.do");
			rd.forward(request, response);
		} else if (command.equals("/BoardViewAction.do")) {// ���õ� �� �� ������ ��������
			requestBoardView(request);
			RequestDispatcher rd = request.getRequestDispatcher("/BoardView.do");
			rd.forward(request, response);
		} else if (command.equals("/BoardView.do")) { // �� �� ������ ����ϱ�
			RequestDispatcher rd = request.getRequestDispatcher("./board/view.jsp");
			rd.forward(request, response);
		} else if (command.equals("/BoardUpdateAction.do")) { // ���õ� ���� ��ȸ�� �����ϱ�
			requestBoardUpdate(request);
			RequestDispatcher rd = request.getRequestDispatcher("/BoardListAction.do");
			rd.forward(request, response);
		} else if (command.equals("/BoardDeleteAction.do")) { // ���õ� �� �����ϱ�
			requestBoardDelete(request);
			RequestDispatcher rd = request.getRequestDispatcher("/BoardListAction.do");
			rd.forward(request, response);
		}
	}

	// 해다 게시판 목록에 보여주는 비지니스 로직.
	public void requestBoardList(HttpServletRequest request) {

		// 임시로 , 게시판 목록 화면에 출력하기 위해서, 해당 정보들의 변수를 선언 및 재할당.
		// 만약. 이 정보를 계속 사용하겠다고 하면, 위에서 전역 또는 선언만 하고
		// 재할당해서 이용 가능.
		int listCount = BoardController.LISTCOUNT;
		String RequestURI = request.getRequestURI();
		String contextPath = request.getContextPath();
		String command = RequestURI.substring(contextPath.length());

		// 게시판에서 해당 DB에 접근을 하기위한 sql 문장이 모아져있다.
		// 싱글톤 패턴으로 하나의 객체를 이용을 하고 있음.
		// 게시판에 글쓰기, 수정하기, 삭제하기, 리스트 가져오기 등 여러 메서드들이
		// dao 라는 객체에 담아져 있다.
		BoardDAO dao = BoardDAO.getInstance();
		// 컬렉션: 게시판의 글들을 담아두는 역할.
		// 게시판에 하나의 글들은 각각 BoardDTO 타입의 객체입니다.
		// boardlist -> 게시판의 각각의 게시글을 담아두었다. 담아 둘 예정.
		// 디비에 연결해서, 해당 게시글 목록들을 받아둘 임시 저장 매체로서 사용.
		List<BoardDTO> boardlist = new ArrayList<BoardDTO>();

		int pageNum = 1;
		// 목록 게시판 보여줄 갯수 5개.
		int limit = LISTCOUNT;

		// 만약, 내장객체 request에 담겨진 페이지 정보가 null 아니면.
		// 해당 페이지 정보를 문자열 -> 정수로 변환하겠다.
		if (request.getParameter("pageNum") != null)
			pageNum = Integer.parseInt(request.getParameter("pageNum"));
		// items 는 게시판 화면 하단에 검색하는 창에서, 본문 검색, 글쓴이 검색, 작성자 검색 등. 조건.
		String items = request.getParameter("items");
		// text 해당 검색하기위한 검색어.
		String text = request.getParameter("text");

		// dao 게시판에 관련된 메서드들이 다 있고, 전달 할 때, 해당 검색어 항목, 검색할 내용등을 같이 전달.
		// 디비에 연결해서, 해당 게시글의 모든 갯수를 가져오는 역할.
		int total_record = dao.getListCount(items, text);

		// 실제적인 페이징 처리가 된 결과를 담을 컬렉션이라 보면 됩니다.
		boardlist = dao.getBoardList(pageNum, limit, items, text);

		// total_page 선언만 실제 페이지 계산의 아래에 있음.
		int total_page;

		if (total_record % limit == 0) {
			total_page = total_record / limit;
			Math.floor(total_page);
		} else {
			total_page = total_record / limit; // 11/5 = 2.2
			double total_page_test = Math.floor(total_page); // 2.2 -> 2 , 내림.
			System.out.println("total_page_test의 값 한번 찍어보기." + total_page_test);
			total_page = total_page + 1; // 2 + 1 -> 3
		}
		// 해당 RequestURI, contextPath, command 를 해당 뷰에 데이터를 전달함.
		// 해당 뷰에서, 키이름으로 해당 값을 불러와서 사용할 예정.
		request.setAttribute("RequestURI", RequestURI);
		request.setAttribute("contextPath", contextPath);
		request.setAttribute("command", command);

		request.setAttribute("listCount", listCount);
		request.setAttribute("pageNum", pageNum);
		request.setAttribute("total_page", total_page);
		request.setAttribute("total_record", total_record);
		request.setAttribute("boardlist", boardlist);
		request.setAttribute("boardNum", boardNum);
	}

	// 해당 로그인 아이디로
	public void requestLoginName(HttpServletRequest request) {

		String id = request.getParameter("id");

		BoardDAO dao = BoardDAO.getInstance();

		// 로그인하는 아이디가 디비에 있는지 검사하는 메서드.
		String name = dao.getLoginNameById(id);

		// 해당 아이디가 있다면, request 에 저장.
		request.setAttribute("name", name);
	}

	// 게시판 글쓰기 로직.
	// 추가로 이미지를 등록하는 메서드를 따로 분리해서 작업후 , 여기안에 해당 메서드를 호출 할 계획.
	public void requestBoardWrite(HttpServletRequest request) {

		
//		String realFolder = "C:\\upload"; //웹 어플리케이션상의 절대 경로
		// 해당 프로젝트의 특정 폴더의 위치를 절대경로로 알려줘서 상품 등록시 이미지의 저장경로.
		String realFolder = "C:\\JSP_Workspace1\\ch18_WebMarket\\src\\main\\webapp\\resources\\board_images"; // 웹
																												// 어플리케이션상의
																												// 절대 경로
		String encType = "utf-8"; // 인코딩 타입
		int maxSize = 10 * 1024 * 1024; // 최대 업로드될 파일의 크기10Mb

		MultipartRequest multi;

		try {
			multi = new MultipartRequest(request, realFolder, maxSize, encType, new DefaultFileRenamePolicy());
			//new DefaultFileRenamePolicy() : 파일 중복을 처리하는 기본 정책 : 
			//예) jsp.jpg, 2번째 파일명: jsp1.jpg, jsp2.jpg
			// multi 관련 객체 샘플로 사용하기위해서 가지고 옴.
			// String productId = multi.getParameter("productId");

			// dao 게시판에 관련된 crud 메서드들이 있다.
			// 싱글톤 패턴.
			BoardDAO dao = BoardDAO.getInstance();
			//getListCount 해당 메서드는 board 테이블의 몇개의 행이있는지 알려주는 메서드., 게시글이 몇개이냐? 카운트 세어주기.
			int test = dao.getListCount(null, null);
			// 자식 테이블(파일이미지 테이블) 여기에 값을 입력시, 부모글의 num 번호가 필요해요.
			// 게시글을 처음 작성시에, 부모글이 없습니다. -> 처리를 어떻게 할것인지.?
			// 예) 첫번째 글 , 운영자, 필독 공지사항 처럼 하나의 게시글임의로 작성
			// 예) 처음 게시글의 번호 여부를 처리하든지. -> 선택. 
			System.out.println("test 값확인 :"+ test);
			
			//게시판 첫 시작시, 임의의 이미지 없는 글 올리기 , 축하글 또는 공지사항등 등록.
			if(test == 0 ) { // 부모 글이 없다 -> 그래서, 기본값을 1로 설정.
				boardNum = 1;
				
			} else { // 부모글이 있고, 해당 파일이미지 테이블이 작성이 된다면.
				// 해당 부모글의 게시글 번호 카운트를 따라 감. = boardNum
				boardNum += 1;
			}
//			System.out.println("boardNum :"+ boardNum);

			// 사용자가 작성한 글의 내용을 담을 임시 객체.
			// 임시 객체는 해당 db에 전달할 형식(DTO)
			// 해당 게시글을 담기 위한 객체.
			BoardDTO board = new BoardDTO();
			// 파일의 이미지를 담을 객체.
			FileImageDTO fileDTO = new FileImageDTO();
			// 여러 파일 이미지 객체들을 담을 컬렉션 객체.
			ArrayList<FileImageDTO> fileLists = new ArrayList<FileImageDTO>();

			// num을 받아오는 작업이 없습니다.
			//
			// 사용자로부터 입력 받은 내용을 임시 객체에 담아두는 작업.
			// 일반 데이터를 받는 부분. -> 사용자가 입력된 글.
			// 일반 파일 이미지 업로드를 안하는 게시판에서는 받는 객체를 : request 사용 했다면.
			// 파일 이미지 업로드 가능한 게시판이므로 :MultipartRequest형식 multi 사용해야함. 
			board.setId(multi.getParameter("id"));
			board.setName(multi.getParameter("name"));
			board.setSubject(multi.getParameter("subject"));
			board.setContent(multi.getParameter("content"));

			// 콘솔 상에 출력하기(해당 값을 잘 받아 오고 있는지 여부를 확인하는 용도. )
			System.out.println(multi.getParameter("name"));
			System.out.println(multi.getParameter("subject"));
			System.out.println(multi.getParameter("content"));
			// 게시글의 등록 날짜 형식부분.
			java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyy/MM/dd");
			String regist_day = formatter.format(new java.util.Date());
			// 날짜.형식

			// 해당 게시글의 조회수 설정 0
			board.setHit(0);
			// 등록날짜
			board.setRegist_day(regist_day);
			// 등록아이피 설정.
			board.setIp(request.getRemoteAddr());
			//========================================================
			//파일 이미지를 업로드 하는 부분. Controller 라는 부분에 해당 기능(service) 코드가 섞여 있다. 
			// 유지보수 및 확장 불편함. -> service 역할 부분을 따로 빼어내서 해당 기능만 있는 파일을 만들기.
			// 인터페이스를 이용해서 작업.

			// 임시 객체 board(DTO) 사용자가 글쓰기시 입력한 정보와 보이지 않는 정보를 같이 전달함.
			// 임시 객체 board(DTO) 내용에는 num 의 정보가 없고, 기본 자동 생성 번호를 사용.
			// 글만 작성.
			//===================================================================
			// 글만 작성.
			dao.insertBoard(board);
			
			// 해당 이미지를 저장하는 메서드를 만들기.
			// 매개변수에는 해당 게시글의 번호를 넣을 예정.
			// 하나의 게시글에 첨부된 이미지들의 목록도 있음.

			// board 에서 이미지를 넣는 경우.
			// 1) 한개 2) 두개 이상이 들어갈수도 있음.
			// 3) 파일 이미지가 없는 경우.

			// 파일 데이터를 받아서 Enumeration 형식에 files 에 담기. 여러개 담았음. 
			Enumeration files = multi.getFileNames();
			
			// 반복문으로 Enumeration 안에 객체를 꺼내는 작업을 함.
			while (files.hasMoreElements()) {
				
				//파일명 중복 막기위해서, 파일명 앞에 붙은 랜덤한 숫자.
				// 예)4024bf24-4db3-458b-83b5-23399c8f4a72_bread2    .   jpg
				UUID uuid = UUID.randomUUID();
				// 반복문으로 해당파일을 하나씩 담기 위한 임시 객체.
				FileImageDTO fileDTO2 = new FileImageDTO();
				
				// fname -> file1, file2, file3 ..
				//파일을 첨부하는 뷰에서 file1, file2, file3 name 해당하는 부분.
				String fname = (String) files.nextElement();
				
//				System.out.println("fname" + fname);
				//원본 파일명 , 해당 name의 실제 파일명.
				String fileName = multi.getFilesystemName(fname);
				// 변경된 파일명 : 아래 형식으로 파일명 중복을 방지.
				// fileName : 실제 이미지 파일 명 : 라바1.jfif
				// 171f45c0-38fa-42fd-bd4c-63cb8c4847a1_라바1.jfif
				String uploadFileName = uuid.toString() + "_" + fileName;
				fileDTO2.setFileName(uploadFileName);
				fileDTO2.setRegist_day(regist_day);
				fileDTO2.setNum(boardNum);
				// 받아온 이미지를 임시 객체인 fileDTO2 에담아서, 여러 객체를 담을 컬렉션에 담는 작업. 
				fileLists.add(fileDTO2);
				// 업로드된 파일명을 변경하는 작업.
				// MultipartRequest 를 사용할 경우 파일 이름을 변경 하여 업로드 할 수 없다.
				//이유는 multi 생성시 바로 업로드.
				//그래서 업로드 후에 파일명을 변경 하는 방법.
			    
				// MultipartRequest 특징
				// 해당 객체를 생성하는 순간, 저장 경로에 해당 파일명으로 바로 생성됨. 
				// 그래서, 생성된 파일명을 제가 원하는 파일명으로 변경하는 작업. 
				if(!fileName.equals("")) {
					// fileName : 원본의 파일이름. 
				     // 원본이 업로드된 절대경로와 파일명를 구한다.
				 String fullFileName = realFolder + "/" + fileName;
				 // 파일 객체 생성
				     File f1 = new File(fullFileName);
				     if(f1.exists()) {     // 업로드된 파일명이 존재하면 Rename한다.
				    	 //변경하고 싶은 파일명, 해당 경로와 해당 파일 그리고 확장자 포함. 
				          File newFile = new File(realFolder + "/" + uploadFileName);
				          // 파일이름 변경.
				          f1.renameTo(newFile);   // rename...
				     }
				}
				// 테스트 하기위해서 콘솔에 찍어 본 내용입니다. 참고.
				System.out.println("uploadFileName : 반복문안에 파일명" + uploadFileName);
				System.out.println("해당 파일 위치 경로가 찍히는지 여부 : " + realFolder);
			}
			// 테스트 컬렉션 여러 이미지가 잘 담아지고, 잘 출력이 되는지 확인. 
//			for (int i = 0; i < fileLists.size(); i++) {
//				FileImageDTO ex = fileLists.get(i);
//				String ex2 = ex.getFileName();
//				System.out.println("ex2 밖에 반복문 테스트" + ex2);
//			}

			//파일이미지가 존재한다면, 해당 파일들을 디비 테이블에 추가함.
			if(fileLists != null) {
				// insertImage 제가 만든 메서드이고, 벤치 마킹, 기존에 글쓰기와, 상품등록 부분 참고.
			dao.insertImage(fileLists);
//			boardNum++;
			// 확인하는 샘플. 부모글 의 갯수와, 해당 내가 카운트하는 숫자가 일치하는 지 확인 하기 위해서.
			System.out.println("boardNum:" + boardNum);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// 해당 게시글의 상세 글보기 항목.
	public void requestBoardView(HttpServletRequest request) {

		// dao 디비 연결를 위한 객체 및 다수의 디비 연결 메소드.
		BoardDAO dao = BoardDAO.getInstance();

		// num : 해당 게시글의 글번호
		int num = Integer.parseInt(request.getParameter("num"));
		// pageNum : 페이징 처리에서 해당 페이지 번호.
		int pageNum = Integer.parseInt(request.getParameter("pageNum"));

		// 임시로 해당 게시글을 담은 객체(DTO)
		BoardDTO board = new BoardDTO();
		
		// 이미지 처리할 임시 객체들.
		ArrayList<FileImageDTO> fileLists = new ArrayList<FileImageDTO>();

		// dao 에서 해당 글번호의 내용을 가져오는 메서드.
		// 이 메서드 안에 조회수를 증가하는 메서드가 포함되어 있다.
		board = dao.getBoardByNum(num, pageNum);
		System.out.println("getBoardByNum 메서드 출력후" + board);
		System.out.println("num : " + num);
		fileLists = dao.getBoardImageByNum(num);
		System.out.println("getBoardImageByNum 메서드 출력후" + fileLists);
		
		//콘솔확인.
		for (int i = 0; i < fileLists.size(); i++) {
			FileImageDTO ex = fileLists.get(i);
			String ex2 = ex.getFileName();
			System.out.println("ex2 밖에 반복문 테스트" + ex2);
		}

		// 내장객체에 , 선택된 하나의 게시글의 번호인 num
		// board : 하나의 선택된 게시글의 객체.
		request.setAttribute("num", num);
		request.setAttribute("page", pageNum);
		request.setAttribute("board", board);
		// 해당 뷰에서 파일 이미지들의 목록을 확인하기 위해서 설정. 
		request.setAttribute("fileLists", fileLists);
	}

	// 게시판 수정하기.
	public void requestBoardUpdate(HttpServletRequest request) {

		// 문자열 형식의 게시글 번호를 int 형으로 변환하는 작업. parse
		int num = Integer.parseInt(request.getParameter("num"));
		int pageNum = Integer.parseInt(request.getParameter("pageNum"));

		// dao 디비 연결를 위한 객체 및 다수의 디비 연결 메소드.
		BoardDAO dao = BoardDAO.getInstance();

		// 임시로 해당 게시글을 담은 객체(DTO)
		BoardDTO board = new BoardDTO();

		board.setNum(num);
		board.setName(request.getParameter("name"));
		board.setSubject(request.getParameter("subject"));
		board.setContent(request.getParameter("content"));

		// 날짜 형식 지정하는 포맷을 잘 정리.
		java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyy/MM/dd(HH:mm:ss)");
		String regist_day = formatter.format(new java.util.Date());

		// 게시글을 수정시 조회수를 0으로 초기화함.
		board.setHit(0);
		board.setRegist_day(regist_day);
		board.setIp(request.getRemoteAddr());

		dao.updateBoard(board);
	}

	// 삭제 , 삭제를 생각했던. 디비에서 트리거 작업으로
	// 삭제된 회원, 게시글등 지운 내용을 새로운 테이블에 옮기는 작업도 가능.
	// 이부분은 디비 상에서 처리도 가능하고, 해당 서비스에도 따로 기능을 만들서 구현 가능.
	public void requestBoardDelete(HttpServletRequest request) {

		int num = Integer.parseInt(request.getParameter("num"));
		int pageNum = Integer.parseInt(request.getParameter("pageNum"));

		BoardDAO dao = BoardDAO.getInstance();

		dao.deleteBoard(num);
	}
}
