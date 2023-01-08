<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.net.URLEncoder"%>
<%
	request.setCharacterEncoding("UTF-8");

// 설정.
// 해당 배송정보들의 값을 받아와서 쿠키로 생성하는 로직. 
	Cookie cartId = new Cookie("Shipping_cartId", URLEncoder.encode(request.getParameter("cartId"), "utf-8"));
	Cookie name = new Cookie("Shipping_name", URLEncoder.encode(request.getParameter("name"), "utf-8"));
	Cookie shippingDate = new Cookie("Shipping_shippingDate", URLEncoder.encode(request.getParameter("shippingDate"), "utf-8"));
	Cookie country = new Cookie("Shipping_country",	URLEncoder.encode(request.getParameter("country"), "utf-8"));
	Cookie zipCode = new Cookie("Shipping_zipCode", URLEncoder.encode(request.getParameter("zipCode"), "utf-8"));
	Cookie addressName = new Cookie("Shipping_addressName", URLEncoder.encode(request.getParameter("addressName"), "utf-8"));

	//해당 쿠키 유효 시간 365일
	cartId.setMaxAge(365 * 24 * 60 * 60);
	name.setMaxAge(365 * 24 * 60 * 60);
	zipCode.setMaxAge(365 * 24 * 60 * 60);
	country.setMaxAge(365 * 24 * 60 * 60);
	addressName.setMaxAge(365 * 24 * 60 * 60);

	// 반드시 적용.
	// response 내장객체 해당 쿠키들을 저장해둠. 
	response.addCookie(cartId);
	response.addCookie(name);
	response.addCookie(shippingDate);
	response.addCookie(country);
	response.addCookie(zipCode);
	response.addCookie(addressName);

	// orderConfirmation 주문정보 확인 페이지로 이동. 
	response.sendRedirect("orderConfirmation.jsp");
%>
