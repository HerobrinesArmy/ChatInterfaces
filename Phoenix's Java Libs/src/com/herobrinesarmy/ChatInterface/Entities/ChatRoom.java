/**
 * Copyright (c) 2012 Phoenix, Herobrine and herobrinesarmy.com
 * YOU WILL FUCKING FREELY USE MY CODE HOWEVER YOU GODDAMN WANT TO. (sic) - Kitcat490
 */
package com.herobrinesarmy.ChatInterface.Entities;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * ChatRoom is, obviously, an entity that has been made to represent a chat room found on
 * <a href="http://herobrinesarmy.com">herobrinesarmy.com</a>. It holds: 
 * <ul>
 * <li>The channel of the chat room.
 * <li>Chat room messages.
 * <li>Chat room users.
 * </ul>
 * <p>
 * It also parses the JSON output for the chat room from <a href="http://herobrinesarmy.com">herobrinesarmy.com</a>. 
 * For some reason, when putting each <code>JSON message object</code> in the messages <code>
 * LinkedHashMap</code> so there is a bubble sort method, <code>getMessages()</code>
 * that not only sorts the messages, but also stores the last 50 messages, which is the maximum
 * amount of messages that is returned by an <code>update_chat2.php</code> request.
 * <p>
 * <b><u>IMPORTANT:</u></b>
 * <ul>
 * <li>It is advised that you only use the two regular chat channels
 * when creating a chat room, normal chat <code>8613406</code> or meeting room <code>3</code> 
 * as using a different channel produces blank JSON results. It is being looked into and I'm 
 * hoping that it will be a feature that is included in future iterations of this library.
 * <li>Muting isn't yet a feature of this library, but will most definitely be included in the 
 * next iteration.
 * <li>Parsing of BBCode (this includes, images, emoticons etc.) hasn't been implemented yet, and will 
 * simply appear as if someone has messaged a line of HTML. I'm considering leaving this to client developers
 * as it will be easier to handle there.
 * </ul>
 * 
 * @author 		Phoenix
 * @version		1.0
 * @since		1.0
 */
public class ChatRoom {
	
	//The channel of the ChatRoom
	private int channel;
	
	//The ID of the last message in the ChatRoom
	private int lastMessageID = 0;
	
	//The format of encoding the ChatRoom uses for URLs
	private String enc = "UTF-8";
	
	//The interval length for polling chat.
	private int INTERVAL = 1;
	
	/*
	 * Holds all of the users in an online session with their user ID as a key,
	 * and the user as the even if they go offline, they will remain here
	 */
	private LinkedHashMap<Integer, User> users = new LinkedHashMap<Integer, User>();
	
	//Holds all of the messages in an online session
	private LinkedHashMap<Integer, Message> messages = new LinkedHashMap<Integer, Message>();
	
	//The scheduler service for the getMessages() thread
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
	
	//TODO TESTING PURPOSES
	Scanner kbd = new Scanner(System.in);
	
	//Holds online users only, sized at the main users list
	//private User[] onlineUserList = new User[users.size()];
	
	/**
	 * Constructs a ChatRoom from a passed chat channel.
	 * @param channel The channel this ChatRoom uses
	 */
	public ChatRoom(int channel) {
		this.channel = channel;
	}
	
	/**
	 * @return the channel number this channel is using
	 */
	public int getChannel() {
		return channel;
	}
	
	/**
	 * Write the passed parameter to this ChatRooms channel. It encodes the message in 
	 * UTF-8 format to ensure that it is URL compliant. It is for this reason that unicode 
	 * characters in users usernames will appear as a '?'.
	 * <p>
	 * I am in the process of deciding how to handle these characters.
	 *  
	 * @param message the message that is being sent
	 * @throws UnsupportedEncodingException if the Encoding format of choice is invalid
	 * @throws MalformedURLException if the requested URL is invalid
	 * @throws IOException if openConnection() or getResponseCode() throw an exception
	 */
	public void postMessage(String message) throws MalformedURLException, UnsupportedEncodingException, IOException {
		URL url = new URL("http://herobrinesarmy.com/post_chat?o=1&c=" + this.channel
        		+ "&m=" + URLEncoder.encode(message, enc));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        //Again to make sure that the server completes the request
        @SuppressWarnings("unused")
		int response = conn.getResponseCode();
	}
	
	/**
	 * Polls the server for a JSON response containing the last 50 messages and
	 * listed users in this ChatRoom. The JSON response from the server isn't actually
	 * JSONObject compatible, so the response is formatted and then returned as a 
	 * JSONObject.
	 * 
	 * @return the JSON object that is returned by calling update_chat2.php
	 * @throws MalformedURLException if the requested URL is invalid
	 * @throws IOException if getInputStream() or openConnection() throw an exception
	 * @throws JSONException if there is a syntax error in the source string or a duplicated key
	 */
	private JSONObject poll() throws MalformedURLException, IOException, JSONException {
		URL url = new URL("http://herobrinesarmy.com/update_chat2?&c=" + this.channel + "&l=" + this.lastMessageID);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        Scanner reader = new Scanner(conn.getInputStream());
        String result = "";
        while(reader.hasNext()) {
        	result += reader.nextLine();
        }
		return new JSONObject(result.substring(1, result.length()));
	}
	
	/**
	 * Handles the parsing of the "messages" object found in the JSONObject returned by poll().
	 * It loads each message key into an iterator and if the message isn't found in the "messages"
	 * LinkedHashMap then, the attributes of the single message object are used to construct a Message 
	 * object which is then stored in the "messages" LinkedHashMap with the "message_id" as the key.
	 * 
	 * @see Message
	 * @throws IOException if getInputStream() or openConnection() throw an exception
	 * @throws JSONException if there is a syntax error in the source JSONObject or a duplicated key
	 */
	@SuppressWarnings("rawtypes")
	private void parseMessages() throws JSONException, IOException {
		JSONObject jsonMessages = poll().getJSONObject("messages");
		Iterator keys = jsonMessages.keys();
		while(keys.hasNext()) {
			String key = (String) keys.next();
			JSONObject jsonMessage = jsonMessages.getJSONObject(key);
			int messageID = jsonMessage.getInt("message_id");
			if(!messages.containsKey(messageID)) {
				Message message = new Message(messageID);
				message.setText(jsonMessage.getString("message"));
				message.setTimestamp(jsonMessage.getString("time"));
				message.setUser(jsonMessage.getString("user"));
				messages.put(messageID, message);
			}
		}
	}
	
	/**
	 * Similar to parseMessages() in that it parses the "users" object found in the JSONObject
	 * returned by poll(). Again, it loads each user key into an iterator and if the user isn't already
	 * in the "users" LinkedHashMap, then the attributes of the user JSONObject are used to construct
	 * a user object which is then put in the LinkedHashMap.
	 * <p>
	 * <b><u>IMPORTANT</u></b><br>
	 * 
	 * @see User
	 * @throws IOException if getInputStream() or openConnection() throw an exception
	 * @throws JSONException if there is a syntax error in the source JSONObject or a duplicated key
	 */
	@SuppressWarnings({ "rawtypes" })
	private void parseUsers() throws JSONException, IOException {
		JSONObject jsonUsers = poll().getJSONObject("users");
		Iterator keys = jsonUsers.keys();
		while(keys.hasNext()) {
			String key = (String) keys.next();
			JSONObject jsonUser = jsonUsers.getJSONObject(key);
			int userID = jsonUser.getInt("user_id");
			if(!users.containsKey(userID)) {
				User user = new User(userID);
				user.populateFromProfileString(jsonUser.getString("user"));
				user.setOnline(true);
				users.put(userID, user);
			}
		}
	}	
	
	/**
	 * Calls <code>parseMessages()</code> to update messages <code>LinkedHashMap</code> and then puts the values
	 * of messages into a <code>Collection</code>. Loops through the collection and if the message id is greater than 
	 * <code>lastMessageID</code> then it is put into a newly created <code>LinkedHashMap</code>. The latest messages
	 * are then put into a primitive <code>Message</code> array, which is then ordered by a bubble sort using message IDs.
	 * <code>lastMessageID</code> is then set using the last message captured and the <code>Message</code> array is returned.
	 * 
	 * @see Message
	 * @return the most recent messages in order as a primitive Message array
	 * @throws IOException if parseMessages()
	 * @throws JSONException if there is a syntax error in the source JSONObject or a duplicated key
	 */
	public Message[] getMessages() throws JSONException, IOException {
		parseMessages();
		Collection<Message> ml = messages.values();
		LinkedHashMap<Integer, Message> newMessages = new LinkedHashMap<Integer, Message>();
		for(Message m : ml) {
			if( m.getMsgID() > this.lastMessageID) {
				newMessages.put(m.getMsgID(), m);
			}
		}
		Message[] messageList = newMessages.values().toArray(new Message[0]);
		Message t;
		int i, j;
		int n = messageList.length;
		for(i = 0; i < n; i++){
			for(j = 1; j < (n-i); j++){
				if(messageList[j-1].getMsgID() > messageList[j].getMsgID()){
					 t = messageList[j-1];
					 messageList[j-1] = messageList[j];
					 messageList[j] = t;
				 }
			 }
		}
		this.lastMessageID = messageList[messageList.length-1].getMsgID();
		return messageList;
	}
	
	//TESTING
	private void printNewMessages() throws IOException, JSONException {
		//TODO decide method of output for final libs
		//ONLY FOR TESTING
		for(Message m : getMessages()) {
			System.out.println(m.getMessage());
		}
	}
	
	/**
	 * A simple thread management method. It creates the thread to run the printing of messages, then schedules
	 * this thread to run every second, getting message updates from the chat every second. If no messages are
	 * present, then a JSON exception is thrown, but not caught. This exception isn't harmful, so it can be ignored.
	 */
	public final void getNewMessages() {
		final Runnable printMessages = new Runnable() { 
			public void run() {
				try {
					printNewMessages();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					//Maybe add increasing interval operation here.
					//WILL BE THROWN IF NO NEW MESSAGES
				}
			}
		};
		@SuppressWarnings("unused")
		final ScheduledFuture<?> poller = scheduler.scheduleWithFixedDelay(printMessages, 0, INTERVAL, SECONDS);
	}
	
	public Collection<User> getUsers() throws JSONException, IOException {
		parseUsers();
		Collection<User> ul = users.values();
		LinkedHashMap<Integer, User> onlineUsers = new LinkedHashMap<Integer, User>();
		for(User u : ul) {
			//if(!onlineUsers.containsKey(u.getUserID())) {
				onlineUsers.put(u.getUserID(), u);
			//}
		}
//		User[] userList = onlineUsers.values().toArray(new User[0]);
		return ul;
	}
	
//	private void updateChannelUsers(User user) {
//		//TODO pollUsers to update channel user list, or have "hidden"
//		//messages for libs to use to remove users from chan list
//	}
	
	//TODO Handle ALL THE EXCEPTIONS!
}