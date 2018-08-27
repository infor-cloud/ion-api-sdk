<%@ page import="com.infor.ionapi.sample.client.util.*"%>
<%
	if (OAuth2ClientConfig.getClient_id() == null) {
		OAuth2ClientConfig.InitProperties();
	}
	String auth_code = (OAuth2ClientConfig.getAuthorization_code() == null || OAuth2ClientConfig
			.getAuthorization_code().trim().isEmpty())
			? "Not Available"
			: OAuth2ClientConfig.getAuthorization_code();
	String access_token = (OAuth2ClientConfig.getAccess_token() == null || OAuth2ClientConfig
			.getAccess_token().trim().isEmpty())
			? "Not Available"
			: OAuth2ClientConfig.getAccess_token();
	String refresh_token = (OAuth2ClientConfig.getRefresh_token() == null || OAuth2ClientConfig
			.getRefresh_token().trim().isEmpty())
			? "Not Available"
			: OAuth2ClientConfig.getRefresh_token();
%>
<html>
<head>
<link rel="stylesheet" type="text/css" href="css/style.css">
<link rel="stylesheet" type="text/css" href="css/selector.css">
<link rel="stylesheet" type="text/css" href="css/button.css">
<link href='http://fonts.googleapis.com/css?family=Open+Sans'
	rel='stylesheet' type='text/css'>
<script src="js/show_source.js"></script>
<title>ION API CE OAuth 2.0 Client Demo for Java Web App</title>
</head>
<body>
	<div class="blended_grid">
		<div class="pageHeader">
			<h3>ION API CE OAuth 2.0 Client Demo for Java Web App</h3>
		</div>
		<div class="pageLeftMenu">
			<!--form name="OAuth2Servlet" action="OAuth2Servlet" method="post"-->
			<form id="OAuth2Servlet" action="OAuth2Servlet" method="post">
				<br>
				<div id="tenantDiv" class="styled-select">
					Tenant: <select name="tenant">
						<option>ACME_PRD</option>
						<option>ACME_AX1</option>
						<option>Infor</option>
					</select>
				</div>
				<br>
				<div id="apiDiv" class="styled-select">
					Resource: <select>
						<option>/mingle/socialservice.svc/user/detail</option>
					</select>
				</div>
				<br>
				<div>
					<INPUT name="request_code" TYPE="submit"
						VALUE="Step 1-Request Authorization Code" class="button1"
						onmouseover="req_auth_source()" onmouseout="show_tokens()" />
					<!-- button>Request Authorization Code</button-->
				</div>
				<br>
				<div>
					<INPUT name="exchange_code" TYPE="submit"
						VALUE="Step 2-Exchange Auth Code for Access Token" class="button1"
						onmouseover="exchange_code_for_token()" onmouseout="show_tokens()" />
				</div>
				<br>
				<div>
					<INPUT name="use_token" TYPE="submit"
						VALUE="Step 3(Loop)-Use Access Token to consume ION API"
						class="button1" onmouseover="use_access_token()"
						onmouseout="show_tokens()" />
				</div>
				<br>
				<div>
					<INPUT name="revoke_token" TYPE="submit"
						VALUE="Finally!-Revoke Token" class="button1"
						onmouseover="revoke_oauth_token()" onmouseout="show_tokens()" />
				</div>
				<script> 
				function show_tokens(){
					document.getElementById('source').value = 'Authorization Code: <%=auth_code%>\nAccess Token: <%=access_token%>\nRefresh Token: <%=refresh_token%>';
				}
				function hide_buttons(){
					alert('hide_buttons');
					if("<%=auth_code%>"==="Not Available" && "<%=access_token%>"==="Not Available" && "<%=refresh_token%>"==="Not Available"){
						document.getElementById("OAuth2Servlet").elements["exchange_code"].disabled = true;
						document.getElementById("OAuth2Servlet").elements["use_token"].disabled = true;
						document.getElementById("OAuth2Servlet").elements["revoke_token"].disabled = true;
					}
				}
			</script>
			</FORM>
		</div>
		<div class="pageContent">
			<br>
			<textarea id="source" name="source" class=ta>Authorization Code: <%=auth_code%>&#13;&#10;Access Token: <%=access_token%>&#13;&#10;Refresh Token: <%=refresh_token%></textarea>
		</div>
		<div class="pageFooter">
			<label>API Reply:</label> <br>
			<textarea id="ion_api_reply" class="ta"><%=OAuth2ClientConfig.getApi_reply()%></textarea>
			<script> 
				function show_api_reply(){
					document.getElementById('ion_api_reply').value = '<%=OAuth2ClientConfig.getApi_reply()%>';
				}
			</script>
		</div>
	</div>
</body>
</html>
