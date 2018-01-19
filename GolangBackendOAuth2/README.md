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

_Note:_ you do not need refresh token manually, client cares about and will do it [automatically.](https://godoc.org/golang.org/x/oauth2#Config.Client)
Revoke tokens
The package doesn't provide methods to revoke any token. You can do it, calling revoke service directly.

    resp, err := http.Get(<pu> + <or> + "?token=" + tok.AccessToken)
    if err != nil {
           // handle error
    }










