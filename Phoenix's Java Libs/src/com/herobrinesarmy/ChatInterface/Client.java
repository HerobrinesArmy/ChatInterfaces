package com.herobrinesarmy.ChatInterface;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import org.json.JSONException;

import com.herobrinesarmy.ChatInterface.Entities.*;

public class Client {
	
	//Encoding Format
	private String enc = "UTF-8";
	
	//Cookie Manager
	private CookieManager cookieManager = new CookieManager();
	
	//List of chats
	private LinkedHashMap<Integer, ChatRoom> chats = new LinkedHashMap<Integer, ChatRoom>();
	
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
		}
	}
	
	public boolean authenticate(String username, String password) throws Exception {
		 URL url = new URL("http://herobrinesarmy.com/auth");
	     HttpURLConnection conn = (HttpURLConnection) url.openConnection();
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
	
	public void connectChat(int chatChannel) throws JSONException, IOException {
		ChatRoom chat = new ChatRoom(chatChannel);
		chats.put(chat.getChannel(), chat);
	}
}
