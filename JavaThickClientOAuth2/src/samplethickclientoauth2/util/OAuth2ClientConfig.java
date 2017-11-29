/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package samplethickclientoauth2.util;

import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OAuth2ClientConfig {

    private final static Logger LOGGER = Logger.getLogger(OAuth2ClientConfig.class.getName());

    private static String authorization_endpoint;
    private static String token_endpoint;
    private static String revocation_endpoint;
    private static String client_id;
    private static String client_secret;
    private static String redirect_url;

    private static String authorization_code;
    private static String access_token;
    private static String refresh_token;
    private static String api_reply;
    private static String ion_api_url;

    public static void InitProperties() {
        authorization_code = null;
        access_token = null;
        refresh_token = null;
        try {
            System.out.println("Initializing properties");
            Properties prop = new Properties();
            //InputStream is = ClassLoader.class.getResourceAsStream("/ifsceoauth.properties");
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("ifsceoauth.properties");
            prop.load(is);
            setAuthorization_endpoint(prop.getProperty("authorization_endpoint"));
            setToken_endpoint(prop.getProperty("token_endpoint"));
            setRevocation_endpoint(prop.getProperty("revocation_endpoint"));
            setClient_id(prop.getProperty("client_id"));
            setClient_secret(prop.getProperty("client_secret"));
            setRedirect_url(prop.getProperty("redirect_url"));
            setIon_api_url(prop.getProperty("ion_api_url"));
            setApi_reply("");
            LOGGER.log(Level.INFO, "Set properties from file:{0}", Thread.currentThread().getContextClassLoader().getResource("ifsceoauth.properties").getPath());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error reading properties. Setting default values");
            setAuthorization_endpoint("https://mingleinteg01-sso.mingledev.infor.com:443/{TENANT_ID}/as/authorization.oauth2");
            setToken_endpoint("https://mingleinteg01-sso.mingledev.infor.com:443/{TENANT_ID}/as/token.oauth2");
            setRevocation_endpoint("https://mingleinteg01-sso.mingledev.infor.com:443/{TENANT_ID}/as/revoke_token.oauth2");
            setClient_id("ACME_PRD~yJPOo80cO59KXl8dqne6UGShRIP5K1OTld7wJaJzDtA");
            setClient_secret("IucCjwBRyj_SHOvrpd3FVgVDPbJQ8Dm56DNstSTdFo8R1nnHRdOSc3YM8wrtC_hAe5Fd4lVYGvGV59xJ0K__Lg");
            //setRedirect_url("sample-oauth2-app://auth");//http://sample-oauth2-client.infor.com:8080/SampleAppOAuth2/RedirectServlet");
            setRedirect_url("http://sample-oauth2-client.infor.com:8080/SampleAppOAuth2/RedirectServlet");
            setIon_api_url("https://mingleinteg01-ionapi.mingledev.infor.com/{TENANT_ID}/{RESOURCE}");
        }
        System.out.println("END: Initializing properties");
    }

    public static String getAuthorization_endpoint(String tenant) {
        return authorization_endpoint.replace("{TENANT_ID}", tenant);
    }

    public static void setAuthorization_endpoint(String authorization_endpoint) {
        OAuth2ClientConfig.authorization_endpoint = authorization_endpoint;
    }

    public static String getToken_endpoint(String tenant) {
        return token_endpoint.replace("{TENANT_ID}", tenant);
    }

    public static void setToken_endpoint(String token_endpoint) {
        OAuth2ClientConfig.token_endpoint = token_endpoint;
    }

    public static String getRevocation_endpoint(String tenant) {
        return revocation_endpoint.replace("{TENANT_ID}", tenant);
    }

    public static void setRevocation_endpoint(String revocation_endpoint) {
        OAuth2ClientConfig.revocation_endpoint = revocation_endpoint;
    }

    public static String getClient_id() {
        return client_id;
    }

    public static void setClient_id(String client_id) {
        OAuth2ClientConfig.client_id = client_id;
    }

    public static String getClient_secret() {
        return client_secret;
    }

    public static void setClient_secret(String client_secret) {
        OAuth2ClientConfig.client_secret = client_secret;
    }

    public static String getAuthorization_code() {
        return authorization_code;
    }

    public static void setAuthorization_code(String authorization_code) {
        OAuth2ClientConfig.authorization_code = authorization_code;
    }

    public static String getAccess_token() {
        return access_token;
    }

    public static void setAccess_token(String access_token) {
        OAuth2ClientConfig.access_token = access_token;
    }

    public static String getRefresh_token() {
        return refresh_token;
    }

    public static void setRefresh_token(String refresh_token) {
        OAuth2ClientConfig.refresh_token = refresh_token;
    }

    public static String getRedirect_url() {
        return redirect_url;
    }

    public static void setRedirect_url(String redirect_url) {
        OAuth2ClientConfig.redirect_url = redirect_url;
    }

    public static String getApi_reply() {
        return api_reply;
    }

    public static void setApi_reply(String api_reply) {
        OAuth2ClientConfig.api_reply = api_reply;
    }

    public static String getIon_api_url() {
        return ion_api_url;
    }

    public static void setIon_api_url(String ion_api_url) {
        OAuth2ClientConfig.ion_api_url = ion_api_url;
    }

}
