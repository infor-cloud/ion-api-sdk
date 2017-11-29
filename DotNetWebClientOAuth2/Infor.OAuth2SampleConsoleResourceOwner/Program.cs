using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http;
using System.Text;
using System.Threading.Tasks;
using Newtonsoft.Json.Linq;
using Thinktecture.IdentityModel.Client;
using Thinktecture.IdentityModel.Extensions;

namespace Infor.OAuth2SampleConsoleResourceOwner
{
    /// <summary>
    /// Console application that obtains an access_token and makes a rest call to the service.
    /// 
    /// It is also able to get a new access_token out of the refresh token.
    /// 
    /// When the grant is not needed anymore it revokes the refresh token.
    /// 
    /// The implementation is inspired on the client projects from Thinktecture:
    /// https://github.com/IdentityServer/IdentityServer3.Samples
    /// 
    /// The implementation relies on the Thinktecture.IdentityModel.Client to do most of the hard lifting. 
    /// 
    /// </summary>
    class Program
    {
        #region Environment properties

        private const string ResourceOwnerClientId = "ionapi_ro_testclient";
        private const string ResourceOwnerClientSecret = "Wj13gxGD";

        private const string OAuth2TokenEndpoint = "https://mingleinteg01-sso.mingledev.infor.com/ACME_AX1/as/token.oauth2";
        private const string OAuth2TokenRevocationEndpoint = "https://mingleinteg01-sso.mingledev.infor.com/ACME_AX1/as/revoke_token.oauth2";
        private const string OAuth2AuthorizationEndpoint = "https://mingleinteg01-sso.mingledev.infor.com/ACME_AX1/as/authorization.oauth2";

        private const string IONAPIBaseUrl = "https://mingleinteg01-ionapi.mingledev.infor.com/ACME_AX1/";

        #endregion

        #region User Properties

        private const string ServiceAccountAccessKey = "ACME_AX1#WHRenCSZHAr9ToHv5zqOP8kREO25YuAj9080pp20H7b6C0fORXKdEUahIvIzTmcfbVx-TQRTObEjt_vurKbBRQ";
        private const string ServiceAccountSecretKey = "C54-BSqd9sXHdTdqrOXr4-uIWchc_lrzGk6vdVdqaZQmk0SPdHjASZIq4PJ_4KsROA7H5WSn5K28MAPgJQlZKw";

        #endregion

        private static OAuth2Client _oauth2;

        static void Main(string[] args)
        {
            _oauth2 = new OAuth2Client(
                new Uri(OAuth2TokenEndpoint),
                    ResourceOwnerClientId,
                    ResourceOwnerClientSecret);

            //Request a token with the provided ServiceAccountAccessKey and ServiceAccountSecretKey
            TokenResponse token = RequestToken();

            ShowResponse(token);

            if (!token.IsError)
            {
                //Use the access_token to make a call to ION API
                CallService(token.AccessToken);

                //If a refresh token is available the application can obtain new access_token after those have expired.
                if (token.RefreshToken != null)
                {
                    token = RefreshToken(token.RefreshToken);

                    //It should be possible to continue calling the service with the new token.

                    if (!token.IsError)
                    {
                        CallService(token.AccessToken);
                    }
                }

                //When there is no need for the token it should be revoked so no further access is allowed.
                RevokeToken(token.AccessToken, OAuth2Constants.AccessToken);

                //If the refresh token is provided is recommended to revoke the refresh token.
                if (token.RefreshToken != null)
                {
                    RevokeToken(token.RefreshToken, OAuth2Constants.RefreshToken);
                }

                //It is not possible to use the access_token anymore...
                CallService(token.AccessToken);

                //It should not be possible to refresh the token again...
                token = RefreshToken(token.RefreshToken);

                ShowResponse(token);
            }

            Console.ReadLine();
        }

        static void CallService(string token)
        {
            var client = new HttpClient
            {
                BaseAddress = new Uri(IONAPIBaseUrl)
            };

            client.SetBearerToken(token);
            var response = client.GetAsync("M3/m3api-rest/execute/CRS610MI/ChgFinancial?CUNO=Y30000&BLCD=0").Result;

            if (response.IsSuccessStatusCode)
            {
                "\n\nWebSerivce call response.".ConsoleGreen();
            }
            else
            {
                "\n\nWebService failed".ConsoleRed();
            }

            Console.WriteLine(response);
            Console.WriteLine(response.Content.ReadAsStringAsync().Result);
        }

        private static TokenResponse RequestToken()
        {
            return _oauth2.RequestResourceOwnerPasswordAsync
                (ServiceAccountAccessKey, ServiceAccountSecretKey).Result;
        }

        private static TokenResponse RefreshToken(string refreshToken)
        {
            "\nUsing refresh token:".ConsoleGreen();
            Console.WriteLine(refreshToken);

            return _oauth2.RequestRefreshTokenAsync(refreshToken).Result;
        }

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

            if (result.IsSuccessStatusCode)
            {
                "Succesfully revoked token.".ConsoleGreen();
            }
            else
            {
                "Error revoking token.".ConsoleRed();
            }

            Console.WriteLine("{1}, {0}", token, tokenType);
        }

        private static void ShowResponse(TokenResponse response)
        {
            if (!response.IsError)
            {
                "\nToken response:".ConsoleGreen();
                Console.WriteLine(response.Json);

                "\nAccess Token:".ConsoleGreen();

                Console.WriteLine(response.AccessToken);
            }
            else
            {
                if (response.IsHttpError)
                {
                    "HTTP error: ".ConsoleRed();
                    Console.WriteLine(response.HttpErrorStatusCode);
                    "HTTP error reason: ".ConsoleRed();
                    Console.WriteLine(response.HttpErrorReason);
                }
                else
                {
                    "Protocol error response:".ConsoleRed();
                    Console.WriteLine(response.Json);
                }
            }
        }
    }
}
