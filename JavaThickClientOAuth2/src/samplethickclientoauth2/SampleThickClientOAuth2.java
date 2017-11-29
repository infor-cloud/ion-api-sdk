/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package samplethickclientoauth2;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthAccessTokenResponse;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import samplethickclientoauth2.util.OAuth2ClientConfig;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;

/**
 *
 * @author vkhalipe
 */
public class SampleThickClientOAuth2 extends Application {

    Button btnReqAuthCode;
    Button btnExchangeAuthCodeForTokens;
    Button btnCallApi;
    Button btnRefreshTokens;
    Button btnRevokeTokens;
    TextField tfAuthCode;
    TextField tfAccessTOken;
    TextField tfRefreshToken;
    public static Stage primaryStage;
    private TextArea taAPiReply;
    private Object JSONSerializer;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        OAuth2ClientConfig.InitProperties();
        BorderPane border = new BorderPane();
        HBox hbox = addTopHBox();
        border.setTop(hbox);
        border.setLeft(addLeftVBox());
        HBox centerHBox = addCenterHBox();
        border.setCenter(centerHBox);
        HBox bottomHBox = addBottomHBox();
        border.setBottom(bottomHBox);
        //addStackPane(hbox);         // Add stack to HBox in top region

        Scene scene = new Scene(border, 1300, 800);
        //Scene scene = new Scene(border);

        primaryStage.setTitle("ION API: Sample OAuth2 Client");
        primaryStage.setScene(scene);
        //primaryStage.setFullScreen(true);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    /*Request Authorization Code from IFS CE Auth Server*/
    private void RequestAuthCode() {
        System.out.println("Requesting Auth Code!");
        System.out.println("Auth URL " + OAuth2ClientConfig.getAuthorization_endpoint("ACME_PRD"));

        OAuthClientRequest oauthrequest;
        try {

            oauthrequest = OAuthClientRequest
                    .authorizationLocation(OAuth2ClientConfig.getAuthorization_endpoint("ACME_PRD"))
                    .setClientId(OAuth2ClientConfig.getClient_id())
                    .setRedirectURI(OAuth2ClientConfig.getRedirect_url())
                    .setResponseType("code")
                    .buildQueryMessage();
            System.out.println("Redirecting to: " + oauthrequest.getLocationUri());

            final Stage stage = new Stage();
            stage.setTitle("Requesting Auth Code");
            stage.setWidth(900);
            stage.setHeight(800);

            VBox root = new VBox();

            final WebView browser = new WebView();
            final WebEngine webEngine = browser.getEngine();

            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setPrefWidth(600);
            scrollPane.setPrefHeight(500);
            scrollPane.setContent(browser);
            webEngine.load(oauthrequest.getLocationUri());
            webEngine.setOnStatusChanged(new EventHandler<WebEvent<String>>() {
                public void handle(WebEvent<String> event) {
                    //System.out.println("Handling status change of webengine.");
                    if (event.getSource() instanceof WebEngine) {
                        WebEngine we = (WebEngine) event.getSource();
                        String location = we.getLocation();
                        //System.out.println("Location: " + location);
                        if (location.startsWith(OAuth2ClientConfig.getRedirect_url()) && location.contains("code")) {
                            try {
                                URL url = new URL(location);
                                System.out.println("Query Param: " + url.getQuery());
                                String[] params = url.getQuery().split("&");
                                Map<String, String> map = new HashMap<String, String>();
                                for (String param : params) {
                                    String name = param.split("=")[0];
                                    String value = param.split("=")[1];
                                    map.put(name, value);
                                    System.out.println("name: " + name + " Value: " + value);
                                }
                                System.out.println("The Authorization Code: " + map.get("code"));
                                OAuth2ClientConfig.setAuthorization_code(map.get("code"));
                                OAuth2ClientConfig.setAccess_token("");
                                OAuth2ClientConfig.setRefresh_token("");
                                //ExchangeAuthCodeForAccessToken();
                                //File f = new File("redirect.html");
                                //we.load(f.toURI().toURL().toString());
                                stage.hide();
                                OAuth2ClientConfig.setApi_reply("Received Authorization Code: " + OAuth2ClientConfig.getAuthorization_code());
                                btnReqAuthCode.setDisable(true);
                                btnExchangeAuthCodeForTokens.setDisable(false);
                                btnCallApi.setDisable(true);
                                btnRevokeTokens.setDisable(true);
                                btnRefreshTokens.setDisable(true);
                                
                                refreshUI();
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
            root.getChildren().addAll(scrollPane);
            Scene scene = new Scene(browser);
            //scene.setRoot(root);

            stage.setScene(scene);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(primaryStage);
            stage.show();
        } catch (OAuthSystemException ex) {
            OAuth2ClientConfig.setAuthorization_code(null);
            OAuth2ClientConfig.setAccess_token(null);
            OAuth2ClientConfig.setRefresh_token(null);
            OAuth2ClientConfig.setApi_reply(ex.getMessage());
            Logger.getLogger(SampleThickClientOAuth2.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally{
            refreshUI();
        }
    }

    /*Exchange Authorization Code For Access & Refresh Token*/
    private void ExchangeAuthCodeForAccessToken() {
        System.out.println("Exchanging Auth Code for access token!");
        OAuthAccessTokenResponse oauthResponse;
        try {
            OAuthClientRequest oauthrequest = OAuthClientRequest
                    .tokenLocation(OAuth2ClientConfig.getToken_endpoint("ACME_PRD"))
                    .setGrantType(GrantType.AUTHORIZATION_CODE)
                    .setClientId(OAuth2ClientConfig.getClient_id())
                    .setClientSecret(OAuth2ClientConfig.getClient_secret())
                    .setRedirectURI(OAuth2ClientConfig.getRedirect_url())
                    .setCode(OAuth2ClientConfig.getAuthorization_code())
                    .buildQueryMessage();
            System.out.println("Sending Request to: "+oauthrequest.getLocationUri());
            //create OAuth client that uses custom http client under the hood
            OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
            OAuthResourceResponse resourceResponse = oAuthClient.resource(oauthrequest, OAuth.HttpMethod.POST, OAuthResourceResponse.class);
            if(resourceResponse.getResponseCode() == 200)
            {
                JSONObject  json = new JSONObject(resourceResponse.getBody());
                OAuth2ClientConfig.setAccess_token(json.getString("access_token"));
                OAuth2ClientConfig.setRefresh_token(json.getString("refresh_token"));
                OAuth2ClientConfig.setAuthorization_code(null);                
                btnReqAuthCode.setDisable(true);
                btnExchangeAuthCodeForTokens.setDisable(true);
                btnCallApi.setDisable(false);
                btnRevokeTokens.setDisable(false);
                btnRefreshTokens.setDisable(false);
            }else{
                OAuth2ClientConfig.setAccess_token(null);
                OAuth2ClientConfig.setRefresh_token(null);
                OAuth2ClientConfig.setAuthorization_code(null);
                btnReqAuthCode.setDisable(false);
                btnExchangeAuthCodeForTokens.setDisable(true);
                btnCallApi.setDisable(true);
                btnRevokeTokens.setDisable(true);
                btnRefreshTokens.setDisable(true);
            }
            //oauthResponse = oAuthClient.accessToken(oauthrequest);
            OAuth2ClientConfig.setAuthorization_code(null);
            //OAuth2ClientConfig.setApi_reply("Received Content type: " + resourceResponse.getContentType());
            OAuth2ClientConfig.setApi_reply("Authorization Server Reply : Response Code: "+ resourceResponse.getResponseCode()+"\nBody : \n"+resourceResponse.getBody());
        } catch (Exception e) {
            //OAuth2ClientConfig.setAuthorization_code(null);
            OAuth2ClientConfig.setAccess_token(null);
            OAuth2ClientConfig.setRefresh_token(null);
            OAuth2ClientConfig.setApi_reply("Error Exchanging auth code for token: " + e.getMessage());
            e.printStackTrace();
        }
        refreshUI();
    }

    /*Call API using the access Token*/
    private void CallApi() {
        System.out.println("Calling API!");
        OAuthClientRequest bearerClientRequest;
        try {
            bearerClientRequest = new OAuthBearerClientRequest("https://mingleinteg01-ionapi.mingledev.infor.com/ACME_PRD/weather/geolookup/q/FL/32266.json")
                    .setAccessToken(OAuth2ClientConfig.getAccess_token()).buildQueryMessage();

            OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
            OAuthResourceResponse resourceResponse = oAuthClient.resource(bearerClientRequest, OAuth.HttpMethod.GET, OAuthResourceResponse.class);
            System.out.println("ION API Reply: Code-" + resourceResponse.getResponseCode() + " Body- " + resourceResponse.getBody());
            OAuth2ClientConfig.setApi_reply("Response Code: " + resourceResponse.getResponseCode() + "\n Body:\n\t" + resourceResponse.getBody());
            //taAPiReply.setText(OAuth2ClientConfig.getApi_reply());
        } catch (Exception e) {
            //OAuth2ClientConfig.setAuthorization_code(null);
            //OAuth2ClientConfig.setAccess_token(null);
            //OAuth2ClientConfig.setRefresh_token(null);
            OAuth2ClientConfig.setApi_reply("Error consuming ION API using token: " + e.getMessage());
            e.printStackTrace();
        }
        refreshUI();

    }

    /*Refresh Acccess Token*/
    private void RefreshAccessToken() {
        System.out.println("Refreshing Tokens!");
        try {
            String reqParam = "refresh_token=" + OAuth2ClientConfig.getRefresh_token() + "&grant_type=refresh_token";
            OAuthClientRequest oauthrequest = OAuthClientRequest
                    .tokenLocation(OAuth2ClientConfig.getToken_endpoint("ACME_PRD") + "?" + reqParam)
                    .buildBodyMessage();
            String authString = OAuth2ClientConfig.getClient_id() + ":" + OAuth2ClientConfig.getClient_secret();
            byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
            String authStringEnc = new String(authEncBytes);
            oauthrequest.addHeader("Authorization", "Basic " + authStringEnc);
            OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
            //OAuthAccessTokenResponse oauthResponse = oAuthClient.accessToken(oauthrequest);
            System.out.println("Sending Refresh Token Request to: "+oauthrequest.getLocationUri());
            OAuthResourceResponse resourceResponse = oAuthClient.resource(oauthrequest, OAuth.HttpMethod.POST, OAuthResourceResponse.class);
            if(resourceResponse.getResponseCode() == 200)
            {
                JSONObject  json = new JSONObject(resourceResponse.getBody());
                OAuth2ClientConfig.setAccess_token(json.getString("access_token"));
                OAuth2ClientConfig.setRefresh_token(json.getString("refresh_token"));
                OAuth2ClientConfig.setAuthorization_code(null);                
                btnReqAuthCode.setDisable(true);
                btnExchangeAuthCodeForTokens.setDisable(true);
                btnCallApi.setDisable(false);
                btnRevokeTokens.setDisable(false);
                btnRefreshTokens.setDisable(false);
            }else{
                OAuth2ClientConfig.setAccess_token(null);
                OAuth2ClientConfig.setRefresh_token(null);
                OAuth2ClientConfig.setAuthorization_code(null);
                btnReqAuthCode.setDisable(false);
                btnExchangeAuthCodeForTokens.setDisable(true);
                btnCallApi.setDisable(true);
                btnRevokeTokens.setDisable(true);
                btnRefreshTokens.setDisable(true);
            }
            //OAuthResourceResponse resourceResponse = oAuthClient.resource(oauthrequest, OAuth.HttpMethod.POST, OAuthResourceResponse.class);
            OAuth2ClientConfig.setApi_reply("Authorization Server Reply : Response Code: "+ resourceResponse.getResponseCode()+"\nBody : \n"+resourceResponse.getBody());
        } catch (Exception e) {
            OAuth2ClientConfig.setAuthorization_code(null);
            //OAuth2ClientConfig.setAccess_token(null);
            //OAuth2ClientConfig.setRefresh_token(null);
            OAuth2ClientConfig.setApi_reply("Error Refreshing token: " + e.getMessage());
            e.printStackTrace();
        }
        refreshUI();
    }

    /*Revoke Refresh Token*/
    private void RevokeTokens() {
        System.out.println("Revoking Tokens!");
        try {
            String reqParam = "token=" + OAuth2ClientConfig.getRefresh_token() + "&token_type_hint=refresh_token";
            OAuthClientRequest oauthrequest = OAuthClientRequest
                    .tokenLocation(OAuth2ClientConfig.getRevocation_endpoint("ACME_PRD") + "?" + reqParam)
                    .buildBodyMessage();
            String authString = OAuth2ClientConfig.getClient_id() + ":" + OAuth2ClientConfig.getClient_secret();
            byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
            String authStringEnc = new String(authEncBytes);
            oauthrequest.addHeader("Authorization", "Basic " + authStringEnc);
            OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
            OAuthResourceResponse resourceResponse = oAuthClient.resource(oauthrequest, OAuth.HttpMethod.POST, OAuthResourceResponse.class);
            OAuth2ClientConfig.setApi_reply("Response Code: " + resourceResponse.getResponseCode() + "\nToken Revoked\n\t" + resourceResponse.getBody());
            if (resourceResponse.getResponseCode() == 200) {
                System.out.println("Token revoked. Cleaning up local tokens");
            }
        } catch (Exception e) {
            OAuth2ClientConfig.setApi_reply("Error Revoking token: " + e.getMessage());
            e.printStackTrace();
        }
        OAuth2ClientConfig.setAuthorization_code(null);
        OAuth2ClientConfig.setAccess_token(null);
        OAuth2ClientConfig.setRefresh_token(null);
        btnReqAuthCode.setDisable(false);
        btnExchangeAuthCodeForTokens.setDisable(true);
        btnCallApi.setDisable(true);
        btnRevokeTokens.setDisable(true);
        btnRefreshTokens.setDisable(true);
        refreshUI();
    }
    
    /*Top Horizontal bar with command buttons*/
    private HBox addTopHBox() {
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15, 12, 15, 12));
        hbox.setSpacing(10);
        hbox.setPrefHeight(150);
        hbox.setStyle("-fx-background-color: #adc8e4;");

        btnReqAuthCode = new Button();
        btnReqAuthCode.setText("Request Authorizarion Code");
        btnReqAuthCode.setStyle("-fx-font: 18 arial;");
        btnReqAuthCode.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                RequestAuthCode();
            }
        });

        btnExchangeAuthCodeForTokens = new Button();
        btnExchangeAuthCodeForTokens.setText("Exchange Authorizarion Code for Access Token");
        btnExchangeAuthCodeForTokens.setStyle("-fx-font: 18 arial;");
        btnExchangeAuthCodeForTokens.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                ExchangeAuthCodeForAccessToken();
            }
        });

        btnCallApi = new Button();
        btnCallApi.setText("Call ION API");
        btnCallApi.setStyle("-fx-font: 18 arial;");
        btnCallApi.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                CallApi();
            }
        });

        btnRefreshTokens = new Button();
        btnRefreshTokens.setText("Refresh Access Token");
        btnRefreshTokens.setStyle("-fx-font: 18 arial;");
        btnRefreshTokens.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                RefreshAccessToken();
            }
        });

        btnRevokeTokens = new Button();
        btnRevokeTokens.setText("Revoke Tokens");
        btnRevokeTokens.setStyle("-fx-font: 18 arial;");
        btnRevokeTokens.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                RevokeTokens();
            }
        });
        hbox.setAlignment(Pos.CENTER);
        final Tooltip tooltip = new Tooltip();
        tooltip.setText(
            "These steps are broken down for explaining OAuth 2.0 Authorization code grant flow.\n" +
            "In real application these steps will happen in background and remain transparent to end user.\n");

        btnReqAuthCode.setTooltip(tooltip);
        btnExchangeAuthCodeForTokens.setTooltip(tooltip);
        btnCallApi.setTooltip(tooltip);
        btnRefreshTokens.setTooltip(tooltip);
        btnRevokeTokens.setTooltip(tooltip);
        hbox.getChildren().addAll(btnReqAuthCode, btnExchangeAuthCodeForTokens, btnCallApi, btnRefreshTokens, btnRevokeTokens);
        btnReqAuthCode.setDisable(false);
        btnExchangeAuthCodeForTokens.setDisable(true);
        btnCallApi.setDisable(true);
        btnRevokeTokens.setDisable(true);
        btnRefreshTokens.setDisable(true);
        return hbox;
    }

    /*Left Vertical bar with OAuth codes,tokens etc*/
    public VBox addLeftVBox() {
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(10));
        vbox.setSpacing(8);
        vbox.setPrefWidth(350);

        Text scenetitle = new Text("OAuth Data");
        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        vbox.getChildren().add(scenetitle);

        HBox hbAuthCode = new HBox();
        hbAuthCode.setAlignment(Pos.CENTER_LEFT);
        hbAuthCode.setSpacing(8);
        hbAuthCode.setPrefWidth(340);
        Label lblAuthCode = new Label("Auth Code:");
        lblAuthCode.setPrefWidth(80);
        hbAuthCode.getChildren().add(lblAuthCode);
        tfAuthCode = new TextField();
        tfAuthCode.setEditable(false);
        tfAuthCode.setText(OAuth2ClientConfig.getAuthorization_code());
        tfAuthCode.setPrefWidth(250);
        hbAuthCode.getChildren().add(tfAuthCode);
        vbox.getChildren().add(hbAuthCode);

        HBox hbAccTkn = new HBox();
        hbAccTkn.setAlignment(Pos.CENTER_LEFT);
        hbAccTkn.setSpacing(8);
        hbAccTkn.setPrefWidth(340);
        Label lblAccessToken = new Label("Access Token:");
        lblAccessToken.setPrefWidth(80);
        hbAccTkn.getChildren().add(lblAccessToken);
        tfAccessTOken = new TextField();
        tfAccessTOken.setEditable(false);
        tfAccessTOken.setText(OAuth2ClientConfig.getAccess_token());
        tfAccessTOken.setPrefWidth(250);
        hbAccTkn.getChildren().add(tfAccessTOken);
        vbox.getChildren().add(hbAccTkn);

        HBox hbRfsTkn = new HBox();
        hbRfsTkn.setAlignment(Pos.CENTER_LEFT);
        hbRfsTkn.setSpacing(8);
        hbAccTkn.setPrefWidth(340);
        Label lblRefreshToken = new Label("Refresh Token:");
        lblRefreshToken.setPrefWidth(80);
        hbRfsTkn.getChildren().add(lblRefreshToken);
        tfRefreshToken = new TextField();
        tfRefreshToken.setEditable(false);
        tfRefreshToken.setText(OAuth2ClientConfig.getRefresh_token());
        tfRefreshToken.setPrefWidth(250);
        hbRfsTkn.getChildren().add(tfRefreshToken);
        vbox.getChildren().add(hbRfsTkn);

        return vbox;
    }

    /*Botton horizontal bar for source*/
    private HBox addBottomHBox() {
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15, 12, 15, 12));
        hbox.setSpacing(10);
        hbox.setStyle("-fx-background-color: #336699;");
        hbox.setPrefHeight(150);
        Label lblSOurce = new Label("Source");

        hbox.getChildren().addAll(lblSOurce);
        return hbox;
    }

    /*Center Section for API result*/
    private HBox addCenterHBox() {
        HBox hbox = new HBox();
        hbox.setAlignment(Pos.TOP_LEFT);

        taAPiReply = new TextArea();
        taAPiReply.setWrapText(true);
        taAPiReply.setText("API Reply goes here");
        taAPiReply.setEditable(false);
        taAPiReply.setPrefWidth(800);

        hbox.getChildren().addAll(taAPiReply);

        return hbox;
    }
    
    private void refreshUI() {
            System.out.println("Refreshing UI");
            tfAuthCode.setText(OAuth2ClientConfig.getAuthorization_code());
            tfAccessTOken.setText(OAuth2ClientConfig.getAccess_token());
            tfRefreshToken.setText(OAuth2ClientConfig.getRefresh_token());
            taAPiReply.setText(OAuth2ClientConfig.getApi_reply());
        }    
}
