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

![Grant Flow](https://raw.githubusercontent.com/infor-cloud/ion-api-sdk/master/ION%20API%20-%20Choosing%20OAuth2%20grants.png)

Java web applications
=====================

[Sample Java Web Application](https://github.com/infor-cloud/ion-api-sdk/tree/master/JavaWebClientOAuth2)

Java thick clients
==================

[Sample Java Thick Client](https://github.com/infor-cloud/ion-api-sdk/tree/master/JavaThickClientOAuth2)

.Net web applications
========================

[Sample .net Web Application](https://github.com/infor-cloud/ion-api-sdk/tree/master/DotNetWebClientOAuth2)

.Net Thick Client
===========================

[Sample .net Thick Client](https://github.com/infor-cloud/ion-api-sdk/tree/master/DotNetRichClientOAuth2)

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










