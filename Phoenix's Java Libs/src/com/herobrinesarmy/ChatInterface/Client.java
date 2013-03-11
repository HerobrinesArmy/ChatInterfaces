/**
 * Copyright (c) 2012 Phoenix, Herobrine and herobrinesarmy.com
 * YOU WILL FUCKING FREELY USE MY CODE HOWEVER YOU GODDAMN WANT TO. (sic) - Kitcat490
 */
package com.herobrinesarmy.ChatInterface;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;

import com.herobrinesarmy.ChatInterface.Entities.*;

/**
 * Client is the top level class, and only interacts with ChatRooms. Authentication to the server is handled here, 
 * as well as the cookies passed to and from the server, this is done by the 
 * <code>authenticate()</code> method that takes the users username and password as parameters. A separate method,
 * <code>checkAuth()</code> is used to check authentication by the <code>authenticate()</code> method, and it returns
 * a boolean value depending on the string returned by <code>checkAuth()</code>.
 * <p>
 * The only methods accessed from outside of this class should be <code>postMessage()</code> and <code>getNewMessages()</code>
 * in the ChatRoom class.
 * 
 * @author Phoenix
 * @version 1.0
 * @since 1.0
 * @see ChatRoom
 */

public class Client {
	
	//Encoding Format
	private String enc = "UTF-8";
	
	//Cookie Manager
	private CookieManager cookieManager = new CookieManager();

	private Scanner kbd = new Scanner(System.in);
	
	public static void main(String args[]) throws Exception {
		//TODO remove after testing
		new Client(args[0], args[1]);
	}
	
	public Client(String username, String password) throws Exception {
		CookieManager.setDefault(cookieManager);
		if(authenticate(username, password)) {
			//main chat 8613406
			ChatRoom chat = new ChatRoom(8613406);
			chat.getNewMessages();
			while(true) {
				chat.postMessage(kbd.nextLine());
			}
		}
		else {
			
		}
	}
	
	/**
	 * Sends a HTTP request to the authentication URL on <a href="herobrinesarmy.com">herobrinesarmy.com</a>
	 * takes the username and password of the user wishing to authenticate as parameters and sends them to the server 
	 * as an encoded array of bytes. Then uses the <code>checkAuth()</code> to see if authentication was successful.
	 * 
	 * @param username the username of the user wishing to authenticate
	 * @param password the password of the user wishing to authenticate
	 * @return true if authentication was successful
	 * @throws IOException if there is a general IO exception thrown
	 * @see checkAuth
	 */
	public boolean authenticate(String username, String password) throws IOException {
		 URL url = new URL("http://herobrinesarmy.com/auth");
		 HttpURLConnection conn = null;
		 conn = (HttpURLConnection) url.openConnection();
	     conn.setRequestMethod("POST");
	     conn.setDoOutput(true);
	     
	 	 String query = "user=" + URLEncoder.encode(username, enc) + "&pass=" + URLEncoder.encode(password, enc);
	     DataOutputStream outs = new DataOutputStream(conn.getOutputStream());
	     outs.writeBytes(query);
	     outs.close();
	     
	     //Receive a response from the server to enable auth.
	     @SuppressWarnings("unused")
		 int response = conn.getResponseCode();
	     
		 return checkAuth().equals("Yeah.");
	}
	
	/**
	 * Uses the authentication check URL on <a href="herobrinesarmy.com">herobrinesarmy.com</a> and returns the
	 * result on the page as a string.
	 * 
	 * @return the result of checking authentication
	 * @throws IOException if there is a general IO exception thrown
	 */
	private String checkAuth() throws IOException {
	    String result = "";
	    Scanner reader = null;
	        URL url = new URL("http://herobrinesarmy.com/amiauth");
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        conn.setRequestMethod("GET");
	        conn.setDoInput(true);
	        
	        reader = new Scanner(conn.getInputStream());
	        while(reader.hasNext()) {
	        	result += reader.nextLine();
	        }
	        return result;
	}
}