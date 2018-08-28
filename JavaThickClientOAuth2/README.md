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
