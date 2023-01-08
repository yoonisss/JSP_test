package mvc.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import mvc.database.DBConnection;

public class BoardDAO {

	private static BoardDAO instance;
	
	private BoardDAO() {
		
	}

	public static BoardDAO getInstance() {
		if (instance == null)
			instance = new BoardDAO();
		return instance;
	}	
	//board 게시판의 총 게시물의 갯수를 가져오기.
	// 매개변수로 items : 검색조건, text : 검색어
	public int getListCount(String items, String text) {
		// 디비에 접근 하기위한 기본 객체들
		// 동적 쿼리 담을 객체 PreparedStatement
		// 조회 결과를 담을 객체 ResultSet
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		// 상태를 표시 위한. 기본값. 
		int x = 0;

		// 기본 sql 문장 표현하기위한 변수.
		String sql;
		
		// 해당 검색 조건이 없다면. 
		if (items == null && text == null)
			//count(*) sql에서 해당 행의 갯수를 세어줄 때 사용. 
			sql = "select  count(*) from board";
		else
			// where 조건절에서 items 가 해당 조건, like 속성을 통해서 해당 검색어를 디비에서 검색함. 
			sql = "SELECT   count(*) FROM board where " + items + " like '%" + text + "%'";
		
		try {
			conn = DBConnection.getConnection();
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();

			if (rs.next()) 
				x = rs.getInt(1);
			
		} catch (Exception ex) {
			System.out.println("getListCount() ����: " + ex);
		} finally {			
			try {				
				if (rs != null) 
					rs.close();							
				if (pstmt != null) 
					pstmt.close();				
				if (conn != null) 
					conn.close();												
			} catch (Exception ex) {
				throw new RuntimeException(ex.getMessage());
			}		
		}		
		return x;
	}
	//board 실제적으로 해당 페이지에 크기를 5개씩 불러오는 페이징 처리 실제로직. 
	// page : 현재 페이지(기본: 1페이지), limit : 초기에 설정한 현재 페이지에 불러올 갯수 : 5개
	public ArrayList<BoardDTO> getBoardList(int page, int limit, String items, String text) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		// 디비에서 불러온 게시글 총 갯수. = 예제) 11개
		int total_record = getListCount(items, text );
		// start = (1 -1 ) * 5 = 0
		int start = (page - 1) * limit;
		// index = 1
		int index = start + 1;

		String sql;

		if (items == null && text == null)
			sql = "select * from board ORDER BY num DESC";
		else
			sql = "SELECT  * FROM board where " + items + " like '%" + text + "%' ORDER BY num DESC ";

		
		// 디비에서 불러올 내용을 담을 임시 컬렉션 객체.
		ArrayList<BoardDTO> list = new ArrayList<BoardDTO>();

		try {
			conn = DBConnection.getConnection();
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();

			while (rs.absolute(index)) {
				BoardDTO board = new BoardDTO();
				board.setNum(rs.getInt("num"));
				board.setId(rs.getString("id"));
				board.setName(rs.getString("name"));
				board.setSubject(rs.getString("subject"));
				board.setContent(rs.getString("content"));
				board.setRegist_day(rs.getString("regist_day"));
				board.setHit(rs.getInt("hit"));
				board.setIp(rs.getString("ip"));
				list.add(board);

				if (index < (start + limit) && index <= total_record)
					index++;
				else
					break;
			}
			return list;
		} catch (Exception ex) {
			System.out.println("getBoardList() ���� : " + ex);
		} finally {
			try {
				if (rs != null) 
					rs.close();							
				if (pstmt != null) 
					pstmt.close();				
				if (conn != null) 
					conn.close();
			} catch (Exception ex) {
				throw new RuntimeException(ex.getMessage());
			}			
		}
		return null;
	}
	//member 테이블에서 조회한 아이디가 있는 확인 
	public String getLoginNameById(String id) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;	

		String name=null;
		String sql = "select * from member where id = ? ";

		try {
			conn = DBConnection.getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, id);
			rs = pstmt.executeQuery();

			if (rs.next()) 
				name = rs.getString("name");	
			
			return name;
		} catch (Exception ex) {
			System.out.println("getBoardByNum() ���� : " + ex);
		} finally {
			try {				
				if (rs != null) 
					rs.close();							
				if (pstmt != null) 
					pstmt.close();				
				if (conn != null) 
					conn.close();
			} catch (Exception ex) {
				throw new RuntimeException(ex.getMessage());
			}		
		}
		return null;
	}

	//board 글쓰기 작업. board 사용자로 부터 입력받은 게시글 내용.
	// 디비 서버에 데이터를 전달하는 과정. 
	public void insertBoard(BoardDTO board)  {

		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			conn = DBConnection.getConnection();		

			String sql = "insert into board values(?, ?, ?, ?, ?, ?, ?, ?)";
		
			//동적 쿼리에 쿼리 담기.
			pstmt = conn.prepareStatement(sql);
			
			// 동적 쿼리 객체를 이용해서, 해당 디비에 각 항목의 값을 넣는 과정. 
			pstmt.setInt(1, board.getNum());
			pstmt.setString(2, board.getId());
			pstmt.setString(3, board.getName());
			pstmt.setString(4, board.getSubject());
			pstmt.setString(5, board.getContent());
			pstmt.setString(6, board.getRegist_day());
			pstmt.setInt(7, board.getHit());
			pstmt.setString(8, board.getIp());

			// 해당 담은 동적 쿼리 객체를 실행하는 메서드. 
			// executeUpdate() 호출해서 디비에 저장. 
			pstmt.executeUpdate();
			conn.commit();
			conn.setAutoCommit(true);
		} catch (Exception ex) {
			System.out.println("insertBoard() ���� : " + ex);
		} finally {
			try {
				// 역순으로 디비에 연결할 때 사용 했던 객체를 반납함. 
				if (pstmt != null) 
					pstmt.close();				
				if (conn != null) 
					conn.close();
			} catch (Exception ex) {
				throw new RuntimeException(ex.getMessage());
			}		
		}		
	} 
	
	//이미지를 등록하는 메서드. 매개변수: 파일 이미지들 객체를 담은 컬렉션이 필요함. 
	public void insertImage(ArrayList<FileImageDTO> fileLists)  {

		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			conn = DBConnection.getConnection();		
//			ArrayList<FileImageDTO> fileList = new ArrayList<FileImageDTO>();
//			fileList = board.getFileList();
			for(int i=0; i<fileLists.size(); i++) {
				FileImageDTO fileImageDTO = fileLists.get(i);
//				String fileName = fileImageDTO.getFileName();
				String sql = "insert into board_images values(?, ?, ?, ?)";
				
				//동적 쿼리에 쿼리 담기.
				pstmt = conn.prepareStatement(sql);
				
				// 동적 쿼리 객체를 이용해서, 해당 디비에 각 항목의 값을 넣는 과정. 
				//fnum , fileName, regist_day, num
				// 해당 멀티 이미지를 반복문으로 여러개를 입력 하는 로직 필요.
				
				//getFnum() 자동으로 숫자 증가.
				pstmt.setInt(1, fileImageDTO.getFnum());
				// 반복문으로 해당 목록에 들어가 있는 파일이름을 하나씩 가져올 계획. 
				pstmt.setString(2, fileImageDTO.getFileName());
				// 등록하는 날짜 형식은 시스템 날짜및 시간을 사용할 예정. 
				pstmt.setString(3, fileImageDTO.getRegist_day());
				// 부모 게시글을 입력할 예정. 
				pstmt.setInt(4, fileImageDTO.getNum());

				// 해당 담은 동적 쿼리 객체를 실행하는 메서드. 
				// executeUpdate() 호출해서 디비에 저장. 
				pstmt.executeUpdate();
			}
			
		} catch (Exception ex) {
			System.out.println("insertImage()  : " + ex);
		} finally {
			try {
				// 역순으로 디비에 연결할 때 사용 했던 객체를 반납함. 
				if (pstmt != null) 
					pstmt.close();				
				if (conn != null) 
					conn.close();
			} catch (Exception ex) {
				throw new RuntimeException(ex.getMessage());
			}		
		}		
	} 
	
	
	
	//해당 게시글을 조회 했을 경우, 조회수를 증가하는 메서드. 
	@SuppressWarnings("resource")
	public void updateHit(int num) {

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try {
			conn = DBConnection.getConnection();

			// 해당 게시글의 번호에 해당하는 히트(조회수) 조회.
			String sql = "select hit from board where num = ? ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, num);
			rs = pstmt.executeQuery();
			// 기본값.
			int hit = 0;

			//해당 결과값(디비에서 불러온 값)에서 해당 히트의 값을 불러와서 + 1 증가.
			if (rs.next())
				hit = rs.getInt("hit") + 1;
		
			// 조회된 조회수 숫자에 1카운트한 값을 다시 디비에 업데이트 수정하는 작업. 
			sql = "update board set hit=? where num=?";
			pstmt = conn.prepareStatement(sql);		
			pstmt.setInt(1, hit);
			pstmt.setInt(2, num);
			
			pstmt.executeUpdate();
			
		} catch (Exception ex) {
			System.out.println("updateHit() ���� : " + ex);
		} finally {
			try { // 디비연결에 사용했던 자원 반납. 역순. 
				if (rs != null) 
					rs.close();							
				if (pstmt != null) 
					pstmt.close();				
				if (conn != null) 
					conn.close();
			} catch (Exception ex) {
				throw new RuntimeException(ex.getMessage());
			}			
		}
	}
	
	
	
// 상세글에서 해당 이미지 불러오는 메서드.	
public ArrayList<FileImageDTO> getBoardImageByNum(int num) {
		
		// 디비 연결를 위한 세트.
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		ArrayList<FileImageDTO> fileLists = new ArrayList<FileImageDTO>();				
		// 해당 게시글 번호로 다시 디비에서 조회하는 작업. 
		String sql = "select * from board_images where num = ? ";

		try {
			// 임시 담을 객체. 게시글 번호로 인한 하나의 게시글을 담을 임시 객체.
			
		
			conn = DBConnection.getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, num);
			
			// 선택된 게시글 번호로 조회된 이미지 여러개를 가져와서. 
			// 임시 객체에 역할로 담는 작업. 
			rs = pstmt.executeQuery();

			while (rs.next()) {
				FileImageDTO fileDTO = new FileImageDTO();
				fileDTO.setFnum(rs.getInt("Fnum"));
				fileDTO.setFileName(rs.getString("fileName"));
				fileDTO.setRegist_day(rs.getString("regist_day"));
				fileDTO.setNum(rs.getInt("num"));
				
				fileLists.add(fileDTO);
			}
			System.out.println("fileLists의 갯수 : " + fileLists.size());
			return fileLists;
		} catch (Exception ex) {
			System.out.println("getBoardByNum() 예외 : " + ex);
		} finally {
			try {
				if (rs != null) 
					rs.close();							
				if (pstmt != null) 
					pstmt.close();				
				if (conn != null) 
					conn.close();
			} catch (Exception ex) {
				throw new RuntimeException(ex.getMessage());
			}		
		}
		return null;
	}
	
	//글 목록에서 , 해당 게시글 하나 클릭시 상세 글보기.
	// 해당 게시글 번호로 해당 글 하나를 가져오는 역할. 
	public BoardDTO getBoardByNum(int num, int page) {
		
		// 디비 연결를 위한 세트.
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
				
		// 임시 담을 객체. 게시글 번호로 인한 하나의 게시글을 담을 임시 객체.
		BoardDTO board = null;

		// 해당 게시글 클릭시, 조회수 증가하는 메서드. 
		updateHit(num);
		// 해당 게시글 번호로 다시 디비에서 조회하는 작업. 
		String sql = "select * from board where num = ? ";

		try {
			conn = DBConnection.getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, num);
			
			// 선택된 게시글 번호로 조회된 게시글 하나를 가져와서. 
			// 임시 객체에 역으로 담는 작업. 
			rs = pstmt.executeQuery();

			if (rs.next()) {
				board = new BoardDTO();
				board.setNum(rs.getInt("num"));
				board.setId(rs.getString("id"));
				board.setName(rs.getString("name"));
				board.setSubject(rs.getString("subject"));
				board.setContent(rs.getString("content"));
				board.setRegist_day(rs.getString("regist_day"));
				board.setHit(rs.getInt("hit"));
				board.setIp(rs.getString("ip"));
			}
			
			return board;
		} catch (Exception ex) {
			System.out.println("getBoardByNum() ���� : " + ex);
		} finally {
			try {
				if (rs != null) 
					rs.close();							
				if (pstmt != null) 
					pstmt.close();				
				if (conn != null) 
					conn.close();
			} catch (Exception ex) {
				throw new RuntimeException(ex.getMessage());
			}		
		}
		return null;
	}
	//게시글을 수정하는 작업.
	public void updateBoard(BoardDTO board) {

		Connection conn = null;
		PreparedStatement pstmt = null;
	
		try {
			String sql = "update board set name=?, subject=?, content=? where num=?";

			conn = DBConnection.getConnection();
			pstmt = conn.prepareStatement(sql);
			
			// 업데이트 수정시 자동 커밋을 false
			conn.setAutoCommit(false);

			pstmt.setString(1, board.getName());
			pstmt.setString(2, board.getSubject());
			pstmt.setString(3, board.getContent());
			pstmt.setInt(4, board.getNum());

			// 변경 내용을 디비에 저장을 하는 메소드.
			pstmt.executeUpdate();	
			// 그 후에 저장하는 작업.
			conn.commit();
			// 트랜잭션. all or nothing, 스프링에 가면 트랜잭션 부분을 설정을 통해서 따로 보게됨. 

		} catch (Exception ex) {
			System.out.println("updateBoard() ���� : " + ex);
		} finally {
			try {										
				if (pstmt != null) 
					pstmt.close();				
				if (conn != null) 
					conn.close();
			} catch (Exception ex) {
				throw new RuntimeException(ex.getMessage());
			}		
		}
	} 
	//게시글 삭제하는 방법. 해당 게시글 번호로 삭제.
	public void deleteBoard(int num) {
		Connection conn = null;
		PreparedStatement pstmt = null;		

		String sql = "delete from board where num=?";	

		try {
			conn = DBConnection.getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, num);
			
			pstmt.executeUpdate();

		} catch (Exception ex) {
			System.out.println("deleteBoard() ���� : " + ex);
		} finally {
			try {										
				if (pstmt != null) 
					pstmt.close();				
				if (conn != null) 
					conn.close();
			} catch (Exception ex) {
				throw new RuntimeException(ex.getMessage());
			}		
		}
	}	
}
