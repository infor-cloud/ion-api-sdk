namespace Sample
{
    public static class Constants
    {
        //public const string BaseAddress = "https://localhost:44333/core";
        public const string BaseAddress = "https://mingleinteg01-sso.mingledev.infor.com/ACME_PRD/";

        //public const string AuthorizeEndpoint = BaseAddress + "/connect/authorize";
        public const string AuthorizeEndpoint = BaseAddress + "/as/authorization.oauth2";
        //public const string LogoutEndpoint = BaseAddress + "/connect/endsession";
        
        //public const string TokenEndpoint = BaseAddress + "/connect/token";
        public const string TokenEndpoint = BaseAddress + "/as/token.oauth2";
        public const string UserInfoEndpoint = BaseAddress + "/connect/userinfo";
        public const string IdentityTokenValidationEndpoint = BaseAddress + "/connect/identitytokenvalidation";
        //public const string TokenRevocationEndpoint = BaseAddress + "/connect/revocation";
        public const string TokenRevocationEndpoint = BaseAddress + "/as/revoke_token.oauth2";

        public const string AspNetWebApiSampleApi = "https://mingleinteg01-ionapi.mingledev.infor.com/";
        public const string AspNetWebApiSampleApiEndpoint = "ACME_PRD/ionapi-testapp-rest/basic/customers";

        public const string ClientId = "ACME_PRD~yJPOo80cO59KXl8dqne6UGShRIP5K1OTld7wJaJzDtA";
        public const string ClientSecret = "IucCjwBRyj_SHOvrpd3FVgVDPbJQ8Dm56DNstSTdFo8R1nnHRdOSc3YM8wrtC_hAe5Fd4lVYGvGV59xJ0K__Lg";
        public const string RedirectUrl = "https://sample-oauth2-client.infor.com:443/SampleAppOAuth2/callback";

    }
}