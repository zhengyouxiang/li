<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>Insert title here</title>
	</head>
	<body>
		<div>
			<a href="?pn=${page.pageNumber}&lang=en_US">English</a>
			<a href="?pn=${page.pageNumber}&lang=zh_CN">中文</a>
		</div>
		<br/>
		<div>
			${lang["welcome"]}
			${lang["name"]}
			${lang["sex"]}
		</div>
		<div>
			<br /> 
			<a href="${root}?pn=${page.pageNumber}">INDEX</a>
			<a href="account_list?pn=${page.pageNumber}">LIST</a> 
			<a href="account_add?pn=${page.pageNumber}">ADD</a> 
			<br />
		</div>
		<div>
			<br /> 
			<a href="account_list?pn=1">首页</a> / 
			<a href="account_list?pn=${page.previous}">上一页</a> / 
			<a href="account_list?pn=${page.next}">下一页</a> / 
			<a href="account_list?pn=${page.pageCount}">末页</a> /
			第${page.pageNumber}页/共${page.pageCount}页 
			<br />
		</div>
		<div>
			<br />
			<table width="960px">	
			<c:forEach items="${accounts}" var="account" varStatus="status">
				<tr>
					<td width="8%"><a href="account_edit?id=${account.id}&pn=${page.pageNumber}">EDIT</a></td>
					<td width="8%">
						<a href="javascript:if(confirm('确认删除?')){location.href='account_delete?pn=${page.pageNumber}&id=${account.id}';}">DELETE</a>
					</td>
					<td width="8%">${account.id}</td>
					<td>${account.username}</td>
					<td>${account.password}</td>
					<td>${account.email}</td>
				</tr>
			</c:forEach>
			</table>
		</div>
	</body>
</html>