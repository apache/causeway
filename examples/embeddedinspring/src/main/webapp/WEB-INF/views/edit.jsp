<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<html>
<head>
	<title>Home</title>
</head>
<body>
<h1>
	Editing Person ${person.id} - ${person.firstName}  ${person.lastName} 
</h1>
<form:form commandName="person" style="padding:8px">
    ID - ${person.id}<br/>
    <p>
        First Name<br/>
        <form:input path="firstName"/>
    </p>
    <p>
        Last Name<br/>
        <form:input path="lastName"/>
    </p>
    <input type="submit" value="Save"/>
</form:form>
</body>
</html>
