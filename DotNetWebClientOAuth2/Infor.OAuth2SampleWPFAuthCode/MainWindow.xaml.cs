using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;
using Thinktecture.IdentityModel.Client;

namespace Infor.OAuth2SampleWPFAuthCode
{
    /// <summary>
    /// Interaction logic for MainWindow.xaml
    /// </summary>
    public partial class MainWindow : Window
    {
        #region Environment properties

        private const string ClientId = "ionapi_thickclient_authcode_testclient";
        private const string ClientSecret = "HExs75rH";

        private const string OAuth2TokenEndpoint = "https://mingleinteg01-sso.mingledev.infor.com/ACME_AX1/as/token.oauth2";
        private const string OAuth2TokenRevocationEndpoint = "https://mingleinteg01-sso.mingledev.infor.com/ACME_AX1/as/revoke_token.oauth2";
        private const string OAuth2AuthorizationEndpoint = "https://mingleinteg01-sso.mingledev.infor.com/ACME_AX1/as/authorization.oauth2";

        private const string IONAPIBaseUrl = "https://mingleinteg01-ionapi.mingledev.infor.com/ACME_AX1/";

        #endregion

        #region Installation specific properties

        private const string RedirectUri = "oob://localhost/wpfclient";
        
        #endregion

        public MainWindow()
        {
            InitializeComponent();
        }

        /// <summary>
        /// Attempts to obtain an Authorization Code prompting the user if required.
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void GetCodeButton_Click(object sender, RoutedEventArgs e)
        {
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
        }

        void _login_Done(object sender, AuthorizeResponse response)
        {
            this.outputBox.AppendText("\n");
            this.outputBox.AppendText("Response from authorization code");
            this.outputBox.AppendText("\n");
            this.outputBox.AppendText(response.Raw);

            if (!String.IsNullOrWhiteSpace(response.Code))
            {
                this.CodeTextBox.Text = response.Code;
            }
        }

        private void GetAccessTokenButton_Click(object sender, RoutedEventArgs e)
        {
            var client = new OAuth2Client(new Uri(OAuth2TokenEndpoint), ClientId, ClientSecret);

            var response = client.RequestAuthorizationCodeAsync(this.CodeTextBox.Text, RedirectUri).Result;

            _handleTokenResponse(response);
        }

        private void CallServiceButton_Click(object sender, RoutedEventArgs e)
        {
            var client = new HttpClient
            {
                BaseAddress = new Uri(IONAPIBaseUrl)
            };

            client.SetBearerToken(this.AccessTokenTextBox.Text);
            var response = client.GetAsync(this.WebServiceEndpoint.Text).Result;

            this.outputBox.AppendText("\n");
            this.outputBox.AppendText("Call Service Response: ");
            this.outputBox.AppendText("\n");
            this.outputBox.AppendText(response.ToString());
            this.outputBox.AppendText("\n");
            this.outputBox.AppendText(response.Content.ReadAsStringAsync().Result);
        }

        private void RefreshTokenButton_Click(object sender, RoutedEventArgs e)
        {
            var client = new OAuth2Client(new Uri(OAuth2TokenEndpoint), ClientId, ClientSecret);
            TokenResponse response = client.RequestRefreshTokenAsync(this.RefreshTokenTextBox.Text).Result;
            _handleTokenResponse(response);
        }

        private void RevokeAccessTokenButton_Click(object sender, RoutedEventArgs e)
        {
            _revokeToken(this.AccessTokenTextBox.Text, OAuth2Constants.AccessToken);
        }

        private void RevokeRefreshTokenButton_Click(object sender, RoutedEventArgs e)
        {
            _revokeToken(this.RefreshTokenTextBox.Text, OAuth2Constants.RefreshToken);
        }

        private void _handleTokenResponse(TokenResponse tr)
        {
            this.outputBox.AppendText("\n");
            this.outputBox.AppendText("Response from get access token");
            this.outputBox.AppendText("\n");
            this.outputBox.AppendText(tr.Raw);

            if (!String.IsNullOrWhiteSpace(tr.AccessToken))
            {
                this.AccessTokenTextBox.Text = tr.AccessToken;
            }

            if (!String.IsNullOrWhiteSpace(tr.RefreshToken))
            {
                this.RefreshTokenTextBox.Text = tr.RefreshToken;
            }
        }

        private void _revokeToken(string token, string tokenType)
        {
            var client = new HttpClient();
            client.SetBasicAuthentication(ClientId, ClientSecret);

            var postBody = new Dictionary<string, string>
            {
                { "token", token },
                { "token_type_hint", tokenType }
            };

            var result = client.PostAsync(OAuth2TokenRevocationEndpoint, new FormUrlEncodedContent(postBody)).Result;

            if (result.IsSuccessStatusCode)
            {
                this.outputBox.AppendText("\n");
                this.outputBox.AppendText("Succesfully revoked token.");
            }
            else
            {
                this.outputBox.AppendText("\n");
                this.outputBox.AppendText("Error revoking token.");
            }

            this.outputBox.AppendText("\n");
            this.outputBox.AppendText(String.Format("{1}, {0}", token, tokenType));
        }

    }
}
