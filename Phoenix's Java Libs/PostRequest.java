package chat;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;

public class PostRequest {
	
	public PostRequest(String user, String pass) throws IOException {
		authenticate(user, pass);
	}
	
	public void authenticate(String username, String password) throws IOException {
		 URL url = new URL("http://herobrinesarmy.com/auth");
	     HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	     conn.setReadTimeout(10000);
	     conn.setConnectTimeout(15000);
	     conn.setRequestMethod("POST");
	     conn.setDoInput(true);
	     conn.setDoOutput(true);
		
	     OutputStream os = conn.getOutputStream();
	     BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
	     writer.write("user="+URLEncoder.encode(username, "UTF-8")+"&pass="+URLEncoder.encode(password, "UTF-8"));
	     writer.close();
	     os.close();

	     conn.connect();
	     int response = conn.getResponseCode();
	     System.out.println(response);
	     Scanner reader = new Scanner(conn.getInputStream());
	     while(reader.hasNext()) {
	    	 System.out.println(reader.nextLine());
	     }
	     System.out.println(checkAuth());
	}
	
	private String checkAuth() throws IOException {
	    String result = "";
	    Scanner reader = null;
	        URL url = new URL("http://herobrinesarmy.com/amiauth");
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        conn.setReadTimeout(10000);
	        conn.setConnectTimeout(15000);
	        conn.setRequestMethod("GET");
	        conn.setDoInput(true);
	        conn.connect();
	        reader = new Scanner(conn.getInputStream());
	        while(reader.hasNext()) {
	        	result += reader.nextLine(); 
	        }
	        return result;
	}
	
	/** 
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
			new PostRequest("USERNAME","PASSWORD");
	}
}