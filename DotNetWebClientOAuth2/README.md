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
