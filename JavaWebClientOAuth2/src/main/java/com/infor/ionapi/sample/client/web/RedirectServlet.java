package com.infor.ionapi.sample.client.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.oltu.oauth2.client.response.OAuthAuthzResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;

import com.infor.ionapi.sample.client.util.OAuth2ClientConfig;

/**
 * Servlet implementation class RedirectServlet
 */
public class RedirectServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * Default constructor. 
     */
    public RedirectServlet() {
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("Inside Redirect Servlet doGet");
		try {
			OAuthAuthzResponse oar;
			oar = OAuthAuthzResponse.oauthCodeAuthzResponse(request);
			String code = oar.getCode();
			OAuth2ClientConfig.setAuthorization_code(code);
			OAuth2ClientConfig.setAccess_token(null);
			OAuth2ClientConfig.setRefresh_token(null);
			OAuth2ClientConfig.setApi_reply("Received aothorization code: "+code);
		} catch (OAuthProblemException e) {
			OAuth2ClientConfig.setAuthorization_code(null);
			OAuth2ClientConfig.setAccess_token(null);
			OAuth2ClientConfig.setRefresh_token(null);
			OAuth2ClientConfig.setApi_reply(e.getMessage());
			e.printStackTrace();
		}
		//request.getRequestDispatcher("/index.jsp").forward(request, response);
		response.sendRedirect(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
