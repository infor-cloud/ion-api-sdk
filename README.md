![Infor Logo](https://avatars0.githubusercontent.com/u/1702191?s=100&v=4) Infor ION API gateway SDK
===================
The Infor ION API gateway is a powerful API management tool. For more information contact us on Infor.com.  This SDK has been created to provide you the ability to:

 - Use previous configured ClientID and Secret to handshake with the Infor Authorization Server to obtain a valid OAuth 2.0 Bearer token that the Gateway expects all requests to include (unless API endpoint is configured to use the AnonymousInboundSecurity policy – which can and should be be used sparingly.)
 - Send and receive responses for HTTPS requests using the various methods such as GET, PUT, POST, DELETE and provide the appropriate headers and payload (body)
 - Handle errors indicated by HTTP status codes other than 200.

> Note that information on developing mobile client applications that authenticate through and access ION API gateway services can be found separately in the Infor Mobile SDK.

----------
**Table of Contents**

 - Choosing a grant type
 - Java Web Applications
 - Java Thick Clients
 - .Net Web Applications
 - .Net Thick Clients
 - Backend Applications (Java, .Net or Golang)

----------
Choosing a grant type
=====================

OAuth2 supports different flows to securely consume APIs for different access patterns. Of the various grants, ION API supports following grants -

 1. Authorization code Grant - suitable for Native mobile/desktop apps,
    Web Apps    
 2. Implicit Grant - suitable for single page/user agent based
    applications
 3. Resource Owner Grant - suitable for server to server access i.e.
    backend service client. In these cases user/resource owner is not
    present for authorization so service accounts are used for
    backchannel authentication and authorization.
 4. SAML Bearer Grant - suitable for applications plugged in with Infor
    Ming.le (i.e. apps that have SSO with Ming.le federation hub).

Based on your clients access pattern,  you need to implement the appropriate OAuth2 Grant. Here is a decision flow to help you choose an OAuth2 grant:

![Grant Flow](http://blogs.infor.com/technology/wp-content/uploads/sites/17/2017/11/ION_API_Choosing_OAuth2_grants.png)

Java web applications
=====================

ION API inbound security requires client application to use OAuth 2.0 tokens in order to access ION API CE resources. Web applications must implement OAuth 2.0 Authorization Code grant flow to obtain tokens from IFS CE and use the tokens to consume ION API CE. This document lists steps required for Java web applications to consume ION API CE resources and provides a sample implementation. To summarize, the process involves -
 1. Acquire OAuth Client 
 2. Obtain OAuth Token 
 3. Use OAuth Token to consume ION API

**Acquire OAuth Client**
In order to obtain and use OAuth Tokens to consume ION API, you need to acquire an OAuth Client specific to your application. The OAuth Client, specific to your app, is created while integrating your app with ION API. Your application should keep OAuth 2.0 client details, along with IFS authorization server endpoints.

**Obtain OAuth Token**
Once your app has OAuth Client and IFS Authorization Server details, OAuth tokens can be obtained using following steps -

 1. Send Authorization Code Request to IFS authorization server.

	Initiate the process of obtaining OAuth token by sending Authorization code request to IFS Authorization Server. This is a HTTP GET or POST request to authorization endpoint with client_id (OAuth Client specific to your app),  redirect_uri (the URL where IFS Authorization Server send the code upon user consent. Must be the same URL as registered in IFS during integration),  response_type=code (indicate IFS  authorization server to send authorization code upon user consent) parameters.

 2. Resource Owner (User) Authentication and Consent (IFS 
   functionality)

	IFS authorization server will work with IFS Federation Hub to authenticate the user/Resource Owner and get user consent to release the claims to your app. If the user approves sharing claims with your application, then IFS authorization server will release authorization code to your application.

 3. Exchanges the authorization code for an access token and refresh
   token

	Using token endpoint of IFS authorization server, exchange the authorization code for an OAuth access token and refresh token. Send following parameters as Content-Type "application/x-www-form-urlencoded" -

	 1. client_id (OAuth Client ID specific to your app)    
	 2. client_secret (OAuth Client secret received while acquiring OAuth Client details)   
	 3. grant_type=authorization_code (hint authorization server about the
	        grant type being used)
	 4. redirect_uri (The URL where authorization
        server will send access token. This URL must match the URL
        registered in ION API CE/IFS CE suing integration)
	 5. code (The
        authorization code sent by authorization server in previous step) In
        exchange, authorization server provides - token_type (Type of token
        issues. e.g. Bearer)

**Use OAuth Token to consume ION API**
Use the access token to consume ION API endpoints. You will need to send the access token in Authorization (HTTP) header. Please refer ION API inbound security documentation for details.

**Refresh access token**
By default, access token is valid for 2 hours, but the access token lifetime can be customized for each authorized app. If the access token is expired, a new access token can be obtained using refresh token. Following parameters are used to renew access token using IFS token endpoint

	 1. grant_type=refresh_token
	 2. refresh_token
	 3. client id - use as username for HTTP Basic authentication
	 4. client secret - user as password for HTTP Basic authentication

Typically, OAuth Client library will automatically handle refreshing expired tokens.

**Revoke Token**
Revoking the tokens prevents from 'orphan' grants so it is crucial to revoke the tokens. When to revoke the tokens will depend on the way your application is handling the refresh and access token. Token should be revoked before they are discarded by your application or you want the user/resource owner to reconfirm the grant. Once the refresh token is revoked, corresponding access token and grant is revoked as well. 
Use following parameters to revoke token using HTTP POST operation for IFS token endpoint.

	 1. token - refresh token
	 2. token_type_hint=refresh_token
	 3. client id - use as username for HTTP Basic authentication
	 4. client secret - user as password for HTTP Basic authentication

 

**Example Implementation**
--------------------------

You can use an OAuth client library to ease OAuth 2.0 adoption for your application. OAuth 2.0 client library will handle OAuth related low level functionality and provide a simple interface to implement steps documented in above section. http://oauth.net/2/ lists some popular OAuth 2.0 client libraries for Java.
A sample implementation based on Apache Oltu OAuth 2.0 Client is provided below. This implementation is a simple web application that integrates with Infor ION API and Infor IFS.
Code snippets to implement OAuth are as follows - 
**Request Authorization Code**

    OAuthClientRequest request = OAuthClientRequest
                    .authorizationProvider("https://mingledev01-sso.mingledev.infor.com:443/ACME_PRD/as/authorization.oauth2")
                    .setClientId("ACME_PRD~QxG91-i82CO4P7L5R1YR4YwdOyWw5caGh0UqkvqYrUY")
                    .setRedirectURI("http://sample-oauth2-client.infor.com:8080/SampleAppOAuth2/redirect"
                    .setResponseType("code")
                    .buildQueryMessage();
    servletResponse.sendRedirect(request.getLocationUri());

**Exchange authorization code for access token**

    OAuthClientRequest request = OAuthClientRequest
            .tokenLocation("https://mingledev01-sso.mingledev.infor.com:443/ACME_PRD/as/token.oauth2")
            .setGrantType(GrantType.AUTHORIZATION_CODE)
            .setClientId("ACME_PRD~QxG91-i82CO4P7L5R1YR4YwdOyWw5caGh0UqkvqYrUY")
            .setClientSecret("G1-DsyjDTlC6uzaelRKMZMDkfUU-3SUbs2zNdq-Rf9e0xE2G_mJhjqPCZXUPYHTqXQdMPKEqCwEO94rzmYleBg")
            .setRedirectURI("http://sample-oauth2-client.infor.com:8080/SampleAppOAuth2/redirect")
            .setCode(code)
            .buildQueryMessage();
    OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
    OAuthAccessTokenResponse oauthResponse = oAuthClient.accessToken(request);
    String accessToken = oAuthResponse.getAccessToken();
    String expiresIn = oAuthResponse.getExpiresIn();

**Use Access Token**

    OAuthClientRequest bearerClientRequest = new OAuthBearerClientRequest("https://mingledev01-ionapi.mingledev.infor.com/ACME_PRD/weather/geolookup/q/FL/32266.json")'+
            .setAccessToken(accessToken)'+
            .buildQueryMessage();'+
    OAuthResourceResponse resourceResponse = oAuthClient.resource(bearerClientRequest, OAuth.HttpMethod.GET, OAuthResourceResponse.class);

**Refresh Token**

    String reqParam = "refresh_token="+varRefreshToken+"&grant_type=refresh_token";
    OAuthClientRequest oauthrequest = OAuthClientRequest.tokenLocation(https://mingledev01-sso.mingledev.infor.com:443/ACME_PRD/as/token.oauth2+"?"+reqParam)
        .buildBodyMessage();
    oauthrequest.addHeader("Authorization", "Basic "+authStringEnc);//use client_id as username, client_secret as password
    OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
    OAuthResourceResponse resourceResponse = oAuthClient.resource(oauthrequest, OAuth.HttpMethod.POST, OAuthResourceResponse.class);';

**Revoke Token**

    String reqParam = "token="+varRefreshToken+"&token_type_hint=refresh_token";
    OAuthClientRequest oauthrequest = OAuthClientRequest.tokenLocation(https://mingledev01-sso.mingledev.infor.com:443/ACME_PRD/as/revoke_token.oauth2+"?"+reqParam)
        .buildBodyMessage();
    oauthrequest.addHeader("Authorization", "Basic "+authStringEnc);//use client_id as username, client_secret as password
    OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
    OAuthResourceResponse resourceResponse = oAuthClient.resource(oauthrequest, OAuth.HttpMethod.POST, OAuthResourceResponse.class);

**Sample Application**
----------------------

A sample Java web application is included in this SDK. By default the app uses userdetails endpoint of the Infor Ming.le API. To change the endpoint modify URL in bearerClientRequest of com/infor/ionapi/sample/client/web/OAuth2Servlet.java.
To run the source, extract the source and run mvn jetty:run to use embedded jetty or to deploy to your container -

 1. Extract the source and run - mvn package (maven 2 required)
 2. Deploy the war file to j2ee container.(redirect URL for your client app will change depending on your context root. The preregistered client has redirect url configured with redirect_url=http://sample-oauth2.infor.com:8080/RedirectServlet (assuming sample app will run at root context and port 8080)
 4. Add sample-oauth2.infor.com to the hosts file to point to the j2ee container IP address (windows hosts or /etc/hosts)
 5. Open the following URL in browser - http://sample-oauth2.infor.com:8080

Java thick clients
==================

Infor ION API inbound security requires client application to use OAuth 2.0 tokens in order to access Infor ION API resources. Thick Client applications must implement OAuth 2.0 Authorization Code grant flow to obtain tokens from Infor IFS and use the tokens to consume Infor ION API. This document lists steps required for Java based Thick Client applications to consume Infor ION API resources and provides a sample implementation. To summarize, the process involves -

 1. Acquire OAuth Client 
 2. Obtain OAuth Token
 3.  Use OAuth Token to consume ION API

**Acquire OAuth Client**

In order to obtain and use OAuth Tokens to consume ION API CE, you need to acquire an OAuth Client, specific to your application. The OAuth Client, specific to your app, is created while integrating your app with ION API CE. Your application should keep OAuth 2.0 client details, along with IFS CE authorization server endpoints.

**Obtain OAuth Token**
Once your app has OAuth Client and IFS CE Authorization Server details, OAuth tokens can be obtained using following steps -
	**Send Authorization Code Request to IFS CE authorization server.**
	Initiate the process of obtaining OAuth token by sending Authorization code request to IFS CE Authorization Server. This is a HTTP GET or POST request to authorization endpoint with:
	
 1. client_id (OAuth Client specific to your app)
 2. redirect_uri (the URL where IFS Authorization Server send the code upon user consent. Must be the same URL as registered in IFS during integration)
 3. response_type=code (indicate IFS authorization server to send authorization code upon user consent) parameters.

**Resource Owner (User) Authentication and Consent (IFS functionality)**
IFS authorization server will work with IFS Federation Hub to authenticate the user/Resource Owner and get user consent to release the claims to your app. If the user approves sharing claims with your application, then IFS authorization server will release authorization code to your application.
**Exchanges the authorization code for an access token and refresh token**
Using token endpoint, of IFS authorization server, to exchange the authorization code for an OAuth access token and refresh token. Send following parameters as Content-Type "application/x-www-form-urlencoded" -

 1. client_id (OAuth Client ID specific to your app)
 2. client_secret (OAuth Client secret received while acquiring OAuth Client details)
 3. grant_type=authorization_code (hint authorization server about the grant type being used)
 4. redirect_uri (The URL where authorization server will send access token. This URL must match the URL registered in ION API CE/IFS CE suing integration)
 5. code (The authorization code sent by authorization server in previous step)

In exchange, authorization server provides -
 1. token_type (Type of token issues. e.g. Bearer)
 2. expires_in (validity period of the access token)
 3. refresh_token (Refresh token to be used to renew expired access token)
 4. access_token (Token to be used for accessing protected resources)

**Use OAuth Token to consume ION API**
Use the access token to consume ION API endpoints. You will need to send the access token in Authorization (HTTP) header. Please refer ION API inbound security for details.

**Refresh access token**
Currently the access tokens are valid for 2 hours. If the access token is expired, a new access token can be obtained using refresh token. Following parameters are used to renew access token using IFS CE token endpoint

 1. grant_type=refresh_token 
 2. refresh_token
 3. client id - use as username for HTTP Basic authentication
 4. client secret - user as password for HTTP Basic authentication

Typically, OAuth Client library will automatically handle refreshing expired tokens.

**Revoke Token**
Revoking the tokens prevents from 'orphan' grants so it is crucial to revoke the tokens. When to revoke the tokens will depend on the way your application is handling the refresh and access token. Token should be revoked before they are discarded by your application or you want the user/resource owner to reconfirm the grant. Once the refresh token is revoked, corresponding access token and grant is revoked as well. 
Use following parameters to revoke token using HTTP POST operation for IFS CE token endpoint.

 1. token - refresh token
 2. token_type_hint=refresh_token
 3. client id - use as username for HTTP Basic authentication
 4. client secret - user as password for HTTP Basic authentication
 

**Example Implementation**
--------------------------

You can use an OAuth client library to ease OAuth 2.0 adoption for your application. OAuth 2.0 client library will handle OAuth related low level functionality and provide a simple interface to implement steps documented in above section. http://oauth.net/2/ lists some popular OAuth 2.0 client libraries for Java.
A sample implementation based on Apache Oltu OAuth 2.0 Client is provided below. This implementation is a simple Thick Client application that integrates with ION API and IFS.
Code snippets to implement OAuth are as follows - 

**Request Authorization Code**

    OAuthClientRequest request = OAuthClientRequest
                    .authorizationProvider("https://mingledev01-sso.mingledev.infor.com:443/ACME_PRD/as/authorization.oauth2")
                    .setClientId("ACME_PRD~QxG91-i82CO4P7L5R1YR4YwdOyWw5caGh0UqkvqYrUY")
                    .setRedirectURI("http://sample-oauth2-client.infor.com:8080/SampleAppOAuth2/redirect"
                    .setResponseType("code")
                    .buildQueryMessage();
    servletResponse.sendRedirect(request.getLocationUri());

**Exchange code for token**

    OAuthClientRequest request = OAuthClientRequest
            .tokenLocation("https://mingledev01-sso.mingledev.infor.com:443/ACME_PRD/as/token.oauth2")
            .setGrantType(GrantType.AUTHORIZATION_CODE)
            .setClientId("ACME_PRD~QxG91-i82CO4P7L5R1YR4YwdOyWw5caGh0UqkvqYrUY")
            .setClientSecret("G1-DsyjDTlC6uzaelRKMZMDkfUU-3SUbs2zNdq-Rf9e0xE2G_mJhjqPCZXUPYHTqXQdMPKEqCwEO94rzmYleBg")
            .setRedirectURI("http://sample-oauth2-client.infor.com:8080/SampleAppOAuth2/redirect")
            .setCode(code)
            .buildQueryMessage();
    OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
    OAuthAccessTokenResponse oauthResponse = oAuthClient.accessToken(request);
    String accessToken = oAuthResponse.getAccessToken();
    String expiresIn = oAuthResponse.getExpiresIn();

**Use Access Token**

    OAuthClientRequest bearerClientRequest = new OAuthBearerClientRequest("https://mingledev01-ionapi.mingledev.infor.com/ACME_PRD/weather/geolookup/q/FL/32266.json")'+
            .setAccessToken(accessToken)'+
            .buildQueryMessage();'+
    OAuthResourceResponse resourceResponse = oAuthClient.resource(bearerClientRequest, OAuth.HttpMethod.GET, OAuthResourceResponse.class);

**Refresh Token**

    String reqParam = "refresh_token="+varRefreshToken+"&grant_type=refresh_token";
    OAuthClientRequest oauthrequest = OAuthClientRequest.tokenLocation(https://mingledev01-sso.mingledev.infor.com:443/ACME_PRD/as/revoke_token.oauth2+"?"+reqParam)
        .buildBodyMessage();
    oauthrequest.addHeader("Authorization", "Basic "+authStringEnc);//use client_id as username, client_secret as password
    OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
    OAuthResourceResponse resourceResponse = oAuthClient.resource(oauthrequest, OAuth.HttpMethod.POST, OAuthResourceResponse.class);';

**Revoke Token**

    String reqParam = "token="+varRefreshToken+"&token_type_hint=refresh_token";
    OAuthClientRequest oauthrequest = OAuthClientRequest.tokenLocation(https://mingledev01-sso.mingledev.infor.com:443/ACME_PRD/as/revoke_token.oauth2+"?"+reqParam)
        .buildBodyMessage();
    oauthrequest.addHeader("Authorization", "Basic "+authStringEnc);//use client_id as username, client_secret as password
    OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
    OAuthResourceResponse resourceResponse = oAuthClient.resource(oauthrequest, OAuth.HttpMethod.POST, OAuthResourceResponse.class);

**Sample Application**
-----------------

A sample rich client java application is included in this SDK. 
To run the source -

 1. Extract the source and run dist/SampleThickClientOAuth2.jar
 2. Alternatively compile the source and run resulting jar.

.Net web applications
========================
ION API inbound security requires client application to use OAuth 2.0 tokens in order to access ION API resources. Web applications must implement OAuth 2.0 Authorization Code grant flow to obtain tokens from IFS CE and use the tokens to consume ION API. This document lists steps required for .Net based web applications to consume ION API resources and provides a sample implementation. The process involves -
 1. Acquire OAuth Client
 2. Obtain OAuth Token
 3. Use OAuth Token to consume ION API

**Acquire OAuth Client**
In order to obtain and use OAuth Tokens to consume ION API, you need to acquire an OAuth Client specific to your application. The OAuth Client, specific to your app, is created while integrating your app with ION API.
Your application should keep OAuth 2.0 client details, along with IFS authorization server endpoints.

**Obtain OAuth Token**
Once your app has OAuth Client and IFS Authorization Server details, OAuth tokens can be obtained using following steps -
	 - Send Authorization Code Request to IFS authorization server.
	 - Initiate the process of obtaining OAuth token by sending Authorization code request to IFS CE Authorization Server. This is a HTTP GET or POST request to authorization endpoint with:

 1. client_id (OAuth Client iD specific to your app)
 2. redirect_uri (the URL where IFS CE Authorization Server sends the code upon user consent, note: must be the same URL as registered in IFS CE during integration)
 3. response_type=code (tells the IFS CE authorization server to send an authorization code upon user consent).

**Resource Owner (User) Authentication and Consent (IFS CE functionality)**
IFS CE authorization server will work with IFS CE Federation Hub to authenticate the user/Resource Owner and get user consent to release the claims to your app. If the user approves sharing claims with your application, then IFS CE authorization server will release authorization code to your application.
**Exchanges the authorization code for an access token and refresh token**
Using token endpoint of IFS CE authorization server, exchange the authorization code for an OAuth access token and refresh token. Send following parameters as Content-Type "application/x-www-form-urlencoded" -

 1. client_id (OAuth Client ID specific to your app)
 2. client_secret (OAuth Client secret received while acquiring OAuth Client details)
 3. grant_type=authorization_code (hint authorization server about the grant type being used)
 4. redirect_uri (The URL where authorization server will send access token. This URL must match the URL registered in ION API CE/IFS suing integration)
 5. code (The authorization code sent by authorization server in previous step) In exchange, 
 
 authorization server provides -
 1. token_type (Type of token issues. e.g. Bearer)
 2. expires_in (validity period of the access token)
 3. refresh_token (Refresh token to be used to renew expired access token)
 4. access_token (Token to be used for accessing protected resources)

**Use OAuth Token to consume ION API CE**
Use the access token to consume ION API CE endpoints. You will need to send the access token in Authorization (HTTP) header. Please refer ION API CE inbound security for details.
**Refresh access token**
Currently the access tokens are valid for 2 hours. If the access token is expired, a new access token can be obtained using refresh token. Following parameters are used to renew access token using IFS CE token endpoint

   1. grant_type=refresh_token
   2. refresh_token
   3. client id - use as username for HTTP Basic authentication
   4. client secret - user as password for HTTP Basic authentication

Typically, OAuth Client library will automatically handle refreshing expired tokens.
**Revoke Token**
Revoking the tokens prevents from 'orphan' grants so it is crucial to revoke the tokens. When to revoke the tokens will depend on the way your application is handling the refresh and access token. Token should be revoked before they are discarded by your application or you want the user/resource owner to reconfirm the grant. Once the refresh token is revoked, corresponding access token and grant is revoked as well.
Use following parameters to revoke token using HTTP POST operation for IFS CE token endpoint.
 1. token - refresh token
 2. token_type_hint=refresh_token
 3. client id - use as username for HTTP Basic authentication
 4. client secret - user as password for HTTP Basic authentication

**Example Implementation**
--------------------------

You can use an OAuth client library to ease OAuth 2.0 adoption for your application. OAuth 2.0 client library will handle OAuth related low level functionality and provide a simple interface to implement steps documented in above section. http://oauth.net/2/ lists some popular OAuth 2.0 client libraries for .net.
A sample implementation, based on ThinkTecture IdentityServer3 Sample, is provided in this section. This implementation is a simple web application that integrates with ION API CE, IFS CE integration environment.
Code snippets to implement OAuth are as follows -
**Request Authorization Code**

    var state = Guid.NewGuid().ToString("N");
    var nonce = Guid.NewGuid().ToString("N");
    SetTempState(state, nonce);
    var client = new OAuth2Client(new Uri(Constants.AuthorizeEndpoint));
    var url = client.CreateCodeFlowUrl(
    clientId: Constants.ClientId,
    scope: scopes,
    redirectUri: Constants.RedirectUrl,
    state: state,
    nonce: nonce);
    return Redirect(url);

**Exchange code for token**

    var client = new OAuth2Client(
    new Uri(Constants.TokenEndpoint),
    Constants.ClientId,
    Constants.ClientSecret);
    var code = Request.QueryString["code"];
    var tempState = await GetTempStateAsync();
    Request.GetOwinContext().Authentication.SignOut("TempState");
    var response = await client.RequestAuthorizationCodeAsync(
    code, Constants.RedirectUrl);
    await ValidateResponseAndSignInAsync(response, tempState.Item2);
    return View("Token", response);

**Use Access Token**

    var principal = User as ClaimsPrincipal;
    var client = new HttpClient();
    client.SetBearerToken(principal.FindFirst("access_token").Value);
    var result = await client.GetStringAsync(Constants.AspNetWebApiSampleApi + Constants.AspNetWebApiSampleApiEndpoint);
    //"ACME_PRD/M3/m3api-rest/execute/CRS610MI/ChgFinancial?CUNO=Y30000&BLCD=0");
    return View((object)result);

**Refresh Token**

    var client = new OAuth2Client(
    new Uri(Constants.TokenEndpoint),
    Constants.ClientId,
    Constants.ClientSecret);
    var principal = User as ClaimsPrincipal;
    var refreshToken = principal.FindFirst("refresh_token").Value;
     Revoke Token
    var refreshToken = (User as ClaimsPrincipal).FindFirst("refresh_token").Value;
    var client = new HttpClient();
    client.SetBasicAuthentication(Constants.ClientId, Constants.ClientSecret);
    var postBody = new Dictionary<string, string>
    {
    { "token", refreshToken },
    { "token_type_hint", "refresh_token" }
    };
    var result = await client.PostAsync(Constants.TokenRevocationEndpoint, new FormUrlEncodedContent(Source

Sample Application
-------------

To run the source -
1. Build and Deploy the solution (Use port 443 and context root /SampleAppOAuth2).
2. Add sample-oauth2-client.infor.com to the hosts file to point to the IIS host ip address (windows hosts or /etc/hosts)
3. open following url in browser - https://sample-oauth2-client.infor.com/SampleAppOAuth2

.Net based thick clients
===========================

The suggested grant for thick clients would be Authorization Code. There are multiple OAuth2.0 libraries available but one that could be used is: http://www.nuget.org/packages/Thinktecture.IdentityModel.Client/
It provides a library with utility functions to implement the OAuth2.0 protocol. The client application can leverage the library to construct the correct url query parameters and the form post required as part of the interaction with the Authorization service. Sample application In order to facilitate the adoption of the Thinktecture.IdentityModel.Client application library a sample application has been created to showcase the different interactions with the Authorization Service in the OAuth2.0 protocol. The Sample application has been inspired on the samples from the Thinktecture team located at: https://github.com/IdentityServer/IdentityServer3.Samples/tree/master/source/Clients The application provides the functionality to obtain an AuthorizationCode ("Get Code") button.
After that the code can be exchange by an access_token with the ("Get Access Token with code") button. After a token is obtained if the client is configured to receive a refresh_token it is possible to obtain a new access_token with the ("Refresh Access Token") button. It is possible to call the ION API with the ("Call Service") button. When the application does not need the access_token or refresh_token those can be revoked using either ("Revoke Access Token") or ("Revoke Refresh Token"). The sample app tries to showcase the interaction of the client with the authorization service. This sample app does not treat the access_token or refresh_token securely. Maintaining the access_token and refresh_token secure is responsibility of the final application and should be secured as any other existing secret. 

Key interactions

 **Request Authorization Code**

    var state = Guid.NewGuid().ToString("N");
    var nonce = Guid.NewGuid().ToString("N");
    var client = new OAuth2Client(new Uri(OAuth2AuthorizationEndpoint));
    var startUrl = client.CreateCodeFlowUrl(
    clientId: ClientId,
    redirectUri: RedirectUri,
    state: state,
    nonce: nonce);
    LoginWebView webView = new LoginWebView();
    webView.Owner = this;
    webView.Done += _login_Done;
    webView.Show();
    webView.Start(new Uri(startUrl), new Uri(RedirectUri));

 The code above creates a new instance of the OAuth2Client passing the authorization end point. It also leverages the OAuth2Client to construct the correct url to obtain the AuthorizationCode. The constructed url has to be presented to a user through an user-agent for an example an embedded browser in the client. The user will have to authenticate and authorize providing the tokens to the thick client. LoginWebView in this case is a window that has a WebBrowser that navigates the user to the specified uri and checks the different urls the user is redirected to detecting when the user navigates to the RedirectUri.
**Obtain authorization code** 
After the user finishes the interaction with the Authorization Server the user is navigated to the RedirectUri. It is possible to inspect every redirect request handling the Navigating event on the WebBrowser like:

    private void webView_Navigating(object sender, NavigatingCancelEventArgs e)
    {
    if (e.Uri.ToString().StartsWith(_callbackUri.AbsoluteUri))
    {
    AuthorizeResponse = new AuthorizeResponse(e.Uri.AbsoluteUri);
    e.Cancel = true;
    this.Visibility = System.Windows.Visibility.Hidden;
    if (Done != null)
    {
    Done.Invoke(this, AuthorizeResponse);
    }
    }
    if (e.Uri.ToString().Equals("javascript:void(0)"))
    {
    e.Cancel = true;
    }
    } 

Please note the special case for "javascript:void(0)". While testing we detected that such navigation was making the browser irresponsive. As pointed out by Petr Novacek please be aware that the functionality above works when using WPF WebView as in the provided sample. Using Winforms browser may not provide all the events as expected. In this scenario we also leverage the Thinktecture library in order to parse the response url:
`AuthorizeResponse = new AuthorizeResponse(e.Uri.AbsoluteUri);` 
The AuthorizeResponse will include an error if there was a problem while obtaining the authorization code or an authorization code. 

**Obtain access_token and refresh_token**
With the Authorization code obtained in the previous step it is possible to obtain an access token

    var client = new OAuth2Client(new Uri(OAuth2TokenEndpoint), ClientId, ClientSecret);
    var response = client.RequestAuthorizationCodeAsync(this.CodeTextBox.Text, RedirectUri).Result;

 In the sample app this is normally done in two steps to show case the difference. Normally applications would obtain an access token directly out of the authorization code without user interaction. The response is of type TokenResponse. Among other properties it includes whether there was an error and if no error the access_token and refresh token. 
 
**Calling the service** 
With the access token it is possible to call the webservice passing the access token as a bearer token.

    var client = new HttpClient
    {
    BaseAddress = new Uri(IONAPIBaseUrl)
    };
    client.SetBearerToken(this.AccessTokenTextBox.Text);
    var response = client.GetAsync(this.WebServiceEndpoint.Text).Result;

 **Revoke access token** 
 When the token is not required anymore it should be revoked. When the token is not needed anymore it is recommended to revoke the access_token. Currently the Thinktecture library does not provide a method to revoke the token. But this can be achieved with a method like:

    private void _revokeToken(string token, string tokenType)
    {
    var client = new HttpClient();
    client.SetBasicAuthentication(ClientId, ClientSecret);
    var postBody = new Dictionary<string, string>
    {
    { "token", token },
    { "token_type_hint", tokenType }
    };
    var result = client.PostAsync(OAuth2TokenRevocationEndpoint, new FormUrlEncodedContent(postBody)).}

 In order to revoke the access token this can be achieved with the following call:

    _revokeToken(this.AccessTokenTextBox.Text, OAuth2Constants.AccessToken);

**Refresh token** 
As part of the access token it is possible to obtain a refresh token as well. The refresh token is a token with a longer expiration time that allows clients to obtain a new set of access_token and refresh_token without requiring the user to authenticate again. Refreshing the access token can be done with the following code:

    var client = new OAuth2Client(new Uri(OAuth2TokenEndpoint), ClientId, ClientSecret);
    TokenResponse response = client.RequestRefreshTokenAsync(this.RefreshTokenTextBox.Text).Result;

 **Revoke Refresh token**
If a refresh token is provided when the authorization from the user is not required anymore the refresh token should be revoked. This is done with the following call:

    _revokeToken(this.RefreshTokenTextBox.Text, OAuth2Constants.RefreshToken);


Backend Applications (Java, .Net or Golang)
===================================

For backend applications, those applications that do not have a user available to authenticate, the recommended grant to use is resource owner.  Given the disparity for the location of the Infor Ming.le identities a new set of credentials is being used for the resource owner grant.
Through the resource owner grant only service accounts will be authenticated. A Service Account can be associated to a user therefore making the call to the backend application on behalf of the user.  The administrator of the Infor ION API gateway will create the service account in IFS and register your application.

**Registering your Backend Application to Obtain an OAuth ClientID and Secret**
In addition to a Service Account to act as the "user" your backend Application will have to be registered.  During the process of registering your backend application an OAuth ClientID and Client-Secret will be generated for your application. You will need all of the following four pieces of information when contacting the Infor Authorization Server in order to obtain a OAuth 2.0 Bearer token that your backend application can you to make API requests via the ION API Gateway:

 1. Application ClientID 
 2. Application Client-Secret
 3. Service Account AccessKey 
 4. Service Account SecretKey

**Example HTTP Request for OAuth2 Resource Owner grant**
OAuth2 resource owner grant facilitates obtaining access token for backend services using backchannel HTTP POST request to auth server token endpoint (e.g. as/token or connect/token) using following params-

 1. grant_type = password (fixed) 
 2. username = service account accesskey
 3. password = service account secretkey
 4. client_id = authorized app
 5. client id client_secret = authorized app 
 6. client secret scope = oauth2 scope (Optional)

**.NET Applications**
---------------------

There are multiple OAuth2.0 libraries available but one that could be used is:
http://www.nuget.org/packages/Thinktecture.IdentityModel.Client/
It provides a library with utility functions to implement the OAuth2.0 protocol.
The client application can leverage the library to construct the correct url query parameters and the form post required as part of the interaction with the Authorization service.

**Sample Application**
----------------------

A .NET sample application is provided in this SDK and leverages the Thinktecture library in order to obtain/refresh/revoke tokens and call a webservice client with the token.
The sample client showcases the functionality available by the library.
The Sample application has been inspired on the samples from the Thinktecture team located at:
https://github.com/IdentityServer/IdentityServer3.Samples/tree/master/source/Clients

The sample app tries to showcase the interaction of the client with the authorization service. This sample app does not treat the access_token or refresh_token securely.
Maintaining the access_token and refresh_token secure is responsibility of the final application and should be secured as any other existing secret.  
Key interactions
**Create client**

    _oauth2 = new OAuth2Client(new Uri(OAuth2TokenEndpoint), ResourceOwnerClientId, ResourceOwnerClientSecret);

With the provided token_endpoint, ClientId and ClientSecret it is possible to construct a client to use in further interactions.
It will be able to make request authenticating using the ClientId and ClientSecret.

**Obtain access_token**

    _oauth2.RequestResourceOwnerPasswordAsync(ServiceAccountAccessKey, ServiceAccountSecretKey).Result;

With the service account accessKey and secretKey it is possible to request an access token.
The response will be of type TokenResponse. This will contain whether has been an error obtaining the token. If successful then it will include the access_token and if available for the client the refresh_token.
**Calling service**
With the token from the TokenResponse it is possible to call the service passing the access token as a bearer token.

    var client = new HttpClient
                {
                    BaseAddress = new Uri(IONAPIBaseUrl)
                };
                client.SetBearerToken(token);
                var response = client.GetAsync("M3/m3api-rest/execute/CRS610MI/ChgFinancial?CUNO=Y30000&BLCD=0").Result;

**Revoke access_token**
When the token is not needed anymore it is recommended to revoke the access_token. 
Currently the Thinktecture library does not provide a method to revoke the token. 
But this can be achieved with a method like:

    private static void RevokeToken(string token, string tokenType)
    {
        var client = new HttpClient();
        client.SetBasicAuthentication(ResourceOwnerClientId, ResourceOwnerClientSecret);
        var postBody = new Dictionary<string, string>
                {
                    { "token", token },
                    { "token_type_hint", tokenType }
                };
        var result = client.PostAsync(OAuth2TokenRevocationEndpoint, new FormUrlEncodedContent(postBody)).Result;
     
    }

In order to revoke an access_token it should be called with the following parameters:

    RevokeToken(token.AccessToken, OAuth2Constants.AccessToken);

**Refresh token**
If a refresh token is available as part of the response it is possible to obtain a new access_token and refresh_token without requiring the service account credentials.

    _oauth2.RequestRefreshTokenAsync(refreshToken).Result;

**Revoke refresh token**
If a refresh token is provided and there is no longer the need to make calls to the webservice without providing the service account credentials then the refresh token should be revoked.
Using the same method as the one provided to revoke access tokens it is possible to revoke refresh tokens as well.

    RevokeToken(token.RefreshToken, OAuth2Constants.RefreshToken);



Go applications
-------------------

Golang provides package golang.org/x/oauth2 to implement the OAuth2.0 protocol.
  
**Make OAuth 2.0 configuration**  
First step is to define a configuration. Reference to downloaded credentials properties will be used in all code examples.

    conf := &oauth2.Config{
           ClientID:     <Application ClientID>,
           ClientSecret: <Application Client-Secret>,
           Scopes: []string{
                  "openid profile",
           },
           Endpoint: oauth2.Endpoint{
                  AuthURL:  <pu> + <oa>,
                  TokenURL: <pu> + <ot>,
           },
    }

**Obtain tokens**  
Now it is ready to obtain tokens. Token struct in Go contains both access and refresh tokens.

    tok, err := conf.PasswordCredentialsToken(oauth2.NoContext, <Service Account AccessKey>, <Service Account SecretKey>)
    if err != nil {
           // handle error
    }

**Create HTTP client and make a request**  
OAuth 2.0 configuration struct has also a method to create HTTP client for you.

    client := conf.Client(oauth2.NoContext, tok)
      
    resp, err := client.Get(<Request URL>)
    if err != nil {
           // handle error
    }

_Note:_ you do not need to refresh token manually, client cares about and will do it [automatically.](https://godoc.org/golang.org/x/oauth2#Config.Client)  

**Revoke tokens**  
The package does not provide methods to revoke any token. You can do it, calling revoke service directly.

    resp, err := http.Get(<pu> + <or> + "?token=" + tok.AccessToken)
    if err != nil {
           // handle error
    }










