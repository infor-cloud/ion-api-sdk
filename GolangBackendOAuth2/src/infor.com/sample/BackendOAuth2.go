package main

import (
	"golang.org/x/oauth2"
	"log"
	"net/http"
	"io/ioutil"
)

const mingleEndpoint string = "https://mingleinteg01-ionapi.mingledev.infor.com/AWSION_DEV/Mingle/"
const mingleSocial string = mingleEndpoint + "SocialService.SVC"

func main() {

	println("Getting OAuth 2.0 token...")
	conf := &oauth2.Config{
		ClientID:     "AWSION_DEV~k0UIGQzDF0g0OCK37MGd_Dt-3WKELV1d1rP-OKv1DWc",
		ClientSecret: "A_9Sz4zwZzGkYCFkUKr9IjHT_87gi4fsmipW1-_77f5jLyaXT800GvKFFLVs40jvTm-rY95ZycahP0WeT_pD2Q",
		Scopes:       []string{""},
		Endpoint: oauth2.Endpoint{
			AuthURL:  "https://mingleinteg01-sso.mingledev.infor.com/AWSION_DEV/as/authorization.oauth2",
			TokenURL: "https://mingleinteg01-sso.mingledev.infor.com/AWSION_DEV/as/token.oauth2",
		},
	}

	tok, err := conf.PasswordCredentialsToken(oauth2.NoContext,
		"AWSION_DEV#Bhqfl0x3ec9AWOx5_JBY96KD9fcVbQKREMe83ozDAxc3qrgXLk49eeFSMUSOND6AN2D8b6MqmltN4VbAfW24Nw",
		"f7pFGxW-pXRC9R1DpZM0MM33ZiNxDgqRUesCbemorUO0hKlatzJqkrZOrlQgLtjen-S1RYhfXOwfXvY9etfHyg")
	if err != nil {
		log.Fatal(err)
	}
	println("Received token: ", tok.TokenType, tok.AccessToken)

	println("Making Ming.le API test query...")
	req, err := http.NewRequest("GET", mingleSocial+"/User/Detail/", nil)
	if err != nil {
		log.Fatal(err)
	}
	req.Header.Add("Accept", "application/json")

	clientMingle := conf.Client(oauth2.NoContext, tok)
	respMingle, err := clientMingle.Do(req)
	if err != nil {
		log.Fatal(err)
	} else {
		data, err := readAllData(respMingle)
		if err != nil {
			log.Fatal(err)
		}
		println("Test response: ", len(string(data)))
	}

	println("Done.")
}

func readAllData(resp *http.Response) ([]byte, error) {
	defer resp.Body.Close()
	return ioutil.ReadAll(resp.Body)
}