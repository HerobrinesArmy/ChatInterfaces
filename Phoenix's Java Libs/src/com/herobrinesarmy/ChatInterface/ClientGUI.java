/**
 * Copyright (c) 2012 Phoenix, Herobrine and herobrinesarmy.com
 * YOU WILL FUCKING FREELY USE MY CODE HOWEVER YOU GODDAMN WANT TO. (sic) - Kitcat490
 */
package com.herobrinesarmy.ChatInterface;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.image.ImageObserver;
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

import javax.swing.*;

import org.json.JSONException;

import com.herobrinesarmy.ChatInterface.Entities.ChatRoom;
import com.herobrinesarmy.ChatInterface.Entities.Message;

/**
 * This class manages the GUI and only interacts with ChatRooms. It holds a <code>LinkedHashMap</code> of
 * all the ChatRooms that have been connected to by invoking the <code>connectChat()</code> method. Authentication
 * to the server is also handled here, as well as the cookies passed to and from the server, this is done by the 
 * <code>authenticate()</code> method that takes the users username and password as parameters. A separate method,
 * <code>checkAuth()</code> is used to check authentication by the <code>authenticate()</code> method, and it returns
 * a boolean value depending on the string returned by <code>checkAuth()</code>.
 * <p>
 * 
 * @author Phoenix
 * @version 1.0
 * @since 1.0
 * @see ChatRoom
 */

public class ClientGUI {
	//Encoding Format
	private static String enc = "UTF-8";
		
	//Cookie Manager
	private static CookieManager cookieManager = new CookieManager();
	
	//Collection for chats
	private LinkedHashMap<Integer, ChatRoom> chats = new LinkedHashMap<Integer, ChatRoom>();
	
	//The scheduler service for the getMessages() thread
	private final static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	
	//GUI Components
	private JFrame frame = new JFrame("HA Chat Client v0.1");
	private Container contentPane;
	private JMenuBar menuBar = new JMenuBar();
	private JMenu chatMenu = new JMenu();
	private JMenuItem auth = new JMenuItem("Authenticate");
	private JMenuItem quit = new JMenuItem("Quit");
	private static JTextArea chatArea = new JTextArea(40, 80);
	private JTextArea messageArea = new JTextArea(5, 80);
	private static JScrollPane scrollArea = new JScrollPane(chatArea);
	
	public ClientGUI() {		
		contentPane = frame.getContentPane();
		frame.setJMenuBar(menuBar);
		menuBar.add(chatMenu);
		chatMenu.add(auth);
		//TODO actionhandler
		chatMenu.add(quit);
		//TODO actionhandler
		chatArea.setEditable(false);
//		scrollArea.getVerticalScrollBar().addAdjustmentListener(new autoScroll());
		scrollArea.setAutoscrolls(true);
		scrollArea.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		frame.add(scrollArea);
		frame.pack();
		frame.setVisible(true);
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
	public static boolean authenticate(String username, String password) throws IOException {
		CookieManager.setDefault(cookieManager);
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
	private static String checkAuth() throws IOException {
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
	
	/**
	 * Creates a new ChatRoom and then stores it in the <code>LinkedHashMap</code> chats.
	 * 
	 * @param chatChannel the channel of the ChatRoom to be created
	 * @throws JSONException when "things are amiss"
	 * @throws IOException if there is a general IO error
	 */
	public void connectChat(int chatChannel) throws JSONException, IOException {
		ChatRoom chat = new ChatRoom(chatChannel);
		chats.put(chat.getChannel(), chat);
	}
	
	/**
	 * A simple thread management method. It creates the thread to run the printing of messages, then schedules
	 * this thread to run every second, getting message updates from the chat every second. If no messages are
	 * present, then a JSON exception is thrown, but not caught. This exception isn't harmful, so it can be ignored.
	 */
	public final static void getNewMessages(final ChatRoom chat) {
		final Runnable printMessages = new Runnable() { 
			public void run() {
				try {
					for(Message m : chat.getMessages()) {
						chatArea.append(m.getMessage() + "\n");
						chatArea.setCaretPosition(chatArea.getText().length());
						scrollArea.getVerticalScrollBar().setValue(chatArea.getCaretPosition());
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					//Maybe add increasing interval operation here.
					//WILL BE THROWN IF NO NEW MESSAGES
				}
			}
		};
		@SuppressWarnings("unused")
		final ScheduledFuture<?> poller = scheduler.scheduleWithFixedDelay(printMessages, 0, 1, SECONDS);
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		System.out.println(authenticate("Phoenix", "phoenix108"));
		new ClientGUI();
		ChatRoom chat = new ChatRoom(8613406);
		getNewMessages(chat);
	}
	
//	private class autoScroll implements AdjustmentListener
//	{
//		public void adjustmentValueChanged(AdjustmentEvent e) {
//			e.getAdjustable().;
//		}
//	}
}
