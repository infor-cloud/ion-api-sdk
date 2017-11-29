using Newtonsoft.Json.Linq;
using Sample;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http;
using System.Security.Claims;
using System.Threading.Tasks;
using System.Web;
using System.Web.Mvc;
using Thinktecture.IdentityModel.Client;

namespace MvcCodeFlowClientManual.Controllers
{
    [Authorize]
    public class AppController : Controller
    {
        public ActionResult Index()
        {
            return View();
        }

        public async Task<ActionResult> CallService()
        {
            var principal = User as ClaimsPrincipal;

            var client = new HttpClient();
            client.SetBearerToken(principal.FindFirst("access_token").Value);
            var result = "";
            try
            {
                result = await client.GetStringAsync(Constants.AspNetWebApiSampleApi + Constants.AspNetWebApiSampleApiEndpoint);//"ACME_PRD/M3/m3api-rest/execute/CRS610MI/ChgFinancial?CUNO=Y30000&BLCD=0");

                //return View(JArray.Parse(result));

            }
            catch (Exception e)
            {
                result = "Error occured while calling ION API: "+e.Message;
            }
            return View((object)result);
        }

        public async Task<ActionResult> RefreshToken()
        {
            var client = new OAuth2Client(
                new Uri(Constants.TokenEndpoint),
                Constants.ClientId,
                Constants.ClientSecret);

            var principal = User as ClaimsPrincipal;
            var refreshToken = principal.FindFirst("refresh_token").Value;

            var response = await client.RequestRefreshTokenAsync(refreshToken);
            UpdateCookie(response);

            return RedirectToAction("Index");
        }

        public async Task<ActionResult> RevokeAccessToken()
        {
            var accessToken = (User as ClaimsPrincipal).FindFirst("access_token").Value;
            var client = new HttpClient();
            client.SetBasicAuthentication(Constants.ClientId, Constants.ClientSecret);

            var postBody = new Dictionary<string, string>
            {
                { "token", accessToken },
                { "token_type_hint", "access_token" }
            };

            var result = await client.PostAsync(Constants.TokenRevocationEndpoint, new FormUrlEncodedContent(postBody));

            return RedirectToAction("Index");
        }

        public async Task<ActionResult> RevokeRefreshToken()
        {
            var refreshToken = (User as ClaimsPrincipal).FindFirst("refresh_token").Value;
            var client = new HttpClient();
            client.SetBasicAuthentication(Constants.ClientId, Constants.ClientSecret);

            var postBody = new Dictionary<string, string>
            {
                { "token", refreshToken },
                { "token_type_hint", "refresh_token" }
            };

            var result = await client.PostAsync(Constants.TokenRevocationEndpoint, new FormUrlEncodedContent(postBody));

            return RedirectToAction("Index");
        }

        private void UpdateCookie(TokenResponse response)
        {

            var identity = (User as ClaimsPrincipal).Identities.First();
            var result = from c in identity.Claims
                         where c.Type != "access_token" &&
                               c.Type != "refresh_token" &&
                               c.Type != "expires_at"
                         select c;

            var claims = result.ToList();

            claims.Add(new Claim("access_token", string.IsNullOrEmpty(response.AccessToken) ? "" : response.AccessToken));
            claims.Add(new Claim("expires_at", response.ExpiresIn == 0? "" : (DateTime.UtcNow.ToEpochTime() + response.ExpiresIn).ToDateTimeFromEpoch().ToString()));
            claims.Add(new Claim("refresh_token", string.IsNullOrEmpty(response.RefreshToken) ? "" : response.RefreshToken));
            if (response.IsError)
            {
                //throw new Exception(response.Error);
                claims.Add(new Claim("Response", response.Error));
            }

            
            var newId = new ClaimsIdentity(claims, "Cookies");
            Request.GetOwinContext().Authentication.SignIn(newId);
        }
	}
}