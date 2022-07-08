<%@ page import="java.io.PrintWriter" %><%--
  Created by IntelliJ IDEA.
  User: samon
  Date: 7/8/2022
  Time: 3:09 AM
  To change this template use File | Settings | File Templates.
--%>
<%
    String textBox = (String) session.getAttribute("textBox");
    String result = (String) session.getAttribute("result");
    if(result == null){
        result = "";
    }
    if(textBox == null){
        textBox = "";
    }
%>

<%
    String execute = (String) session.getAttribute("execute");
%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en" >
<head>
    <meta charset="UTF-8">
    <title>Root-user</title>
    <link rel="stylesheet" href="./root/style.css">
    <title>Root user</title>
</head>
<body>
<html>
<div>
    <h1 style="color:#fecd05;">Welcome to the Enterprise
        </br>Database System
    </h1>
    <h2 style="color:#e2dce4;">A Servlet/JSP-based multi-tiered enterprise application using Tomcat container</h2>
</div>

<div class = "connectionContainer">
    <p class = "connectionText">You are connected to the Project 3 Enterprise System Database as a root-level user</p>
</div>

<body style="background-color: #222b45;">
<div class = "Hbox">
    <form action="hello" method="post">
        <div class>
            <br/>
            <textarea type = "text" name="textBox" id="textBox" class = "textarea" placeholder = "Type your SQL commands here" rows="4" cols="50"><%= textBox %></textarea>
        </div>

        <div class = "container">
            <input type = "submit" value = "Reset Table" name="button_clicked" id="resetButton"  class = "resetButton">
            <input type = "submit" value = "Clear"  name="button_clicked" id="clearButton"  class = "clearButton">
            <input type = "submit" value = "Execute"  name="button_clicked" id="executeButton" class = "executeButton">
        </div>
    </form>
</div>

<%= execute %>


<div class = "tableResult">
    <%-- jsp statement with out sql response--%>
    <%= result %>
</div>


<script  src="./sorttable.js"></script>
</body>
</html>

