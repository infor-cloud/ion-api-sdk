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