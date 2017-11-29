/**
 * 
 */
function req_auth_source(){
	var newtext = document.getElementById('source').value;
	document.getElementById('source').value = newtext+'\n\ne.g.\nOAuthClientRequest request = OAuthClientRequest'+ 
		   '.authorizationProvider("https://mingleinteg01-sso.mingledev.infor.com:443/ACME_PRD/as/authorization.oauth2")'+ 
		   '\n\t\t.setClientId("ACME_PRD~QxG91-i82CO4P7L5R1YR4YwdOyWw5caGh0UqkvqYrUY")'+
		   '\n\t\t.setRedirectURI("http://sample-oauth2-client.infor.com:8080/SampleAppOAuth2/redirect")'+
		   '\n\t\t.setResponseType("code")'+
		   '\n\t\t.buildQueryMessage();'+
		   '\nservletResponse.sendRedirect(request.getLocationUri());';
}
function exchange_code_for_token(){
	var newtext = document.getElementById('source').value;
	document.getElementById('source').value = newtext+'\n\ne.g.\nOAuthClientRequest request = OAuthClientRequest'+ 
	   '.tokenLocation("https://mingleinteg01-sso.mingledev.infor.com:443/ACME_PRD/as/token.oauth2")'+
	   '\n\t\t.setGrantType(GrantType.AUTHORIZATION_CODE)'+
	   '\n\t\t.setClientId("ACME_PRD~QxG91-i82CO4P7L5R1YR4YwdOyWw5caGh0UqkvqYrUY")'+
	   '.setClientSecret("G1-DsyjDTlC6uzaelRKMZMDkfUU-3SUbs2zNdq-Rf9e0xE2G_mJhjqPCZXUPYHTqXQdMPKEqCwEO94rzmYleBg")'+
	   '\n\t\t.setRedirectURI("http://sample-oauth2-client.infor.com:8080/SampleAppOAuth2/redirect")'+
	   '\n\t\t.setCode(code)'+
	   '.buildQueryMessage();'+
	   '\nOAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());'+
	   '\nOAuthAccessTokenResponse oauthResponse = oAuthClient.accessToken(request);'+
	   '\nString accessToken = oAuthResponse.getAccessToken();'+
	   '\nString expiresIn = oAuthResponse.getExpiresIn();';
}
function use_access_token(){
	var newtext = document.getElementById('source').value;
	document.getElementById('source').value = newtext+'\n\ne.g.\nOAuthClientRequest bearerClientRequest = new OAuthBearerClientRequest("https://mingleinteg01-ionapi.mingledev.infor.com/ACME_PRD/weather/geolookup/q/FL/32266.json")'+ 
	   '\n\t\t.setAccessToken(accessToken)'+
	   '\n\t\t.buildQueryMessage();'+
	   '\n//bearerClientRequest.setHeader(OAuth.HeaderType.CONTENT_TYPE, ...);'+
	   '\n//bearerClientRequest.setBody(...);'+
	   '\nOAuthResourceResponse resourceResponse = oAuthClient.resource(bearerClientRequest, OAuth.HttpMethod.GET, OAuthResourceResponse.class);';
}
function revoke_oauth_token(){
	var newtext = document.getElementById('source').value;
	document.getElementById('source').value = newtext+'\n\ne.g.\nString reqParam = "token="+varRefreshToken+"&token_type_hint=refresh_token";'+
	'\nOAuthClientRequest oauthrequest = OAuthClientRequest.tokenLocation(https://mingleinteg01-sso.mingledev.infor.com:443/ACME_PRD/as/revoke_token.oauth2+"?"+reqParam)'+
	'\n\t\t.buildBodyMessage();'+
	'\noauthrequest.addHeader("Authorization", "Basic "+authStringEnc);//use client_id as username, client_secret as password'+
	'\nOAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());'+
	'\nOAuthResourceResponse resourceResponse = oAuthClient.resource(oauthrequest, OAuth.HttpMethod.POST, OAuthResourceResponse.class);';
}
