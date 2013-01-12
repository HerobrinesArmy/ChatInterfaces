/**
 * 
 */
package com.herobrinesarmy.ChatInterface.Entities;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Scanner;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Phoenix
 *
 */
public class ChatRoom {
	
	private int channel;
	private int lastMessageID = 0;
	
	private String enc = "UTF-8";
	
	private LinkedHashMap<Integer, User> users = new LinkedHashMap<Integer, User>();
	private LinkedHashMap<Integer, Message> messages = new LinkedHashMap<Integer, Message>();
	
	@SuppressWarnings("unused")
	private Message[] messageList;
	
	public ChatRoom(int channel) {
		this.channel = channel;
	}
	
	public int getChannel() {
		return channel;
	}
	
	public void setChannel(int channel) {
		this.channel = channel;
	}
	
	public void postMessage(String message) throws IOException {
		URL url = new URL("http://herobrinesarmy.com/post_chat?o=1&c=" + this.channel
        		+ "&messageList=" + URLEncoder.encode(message, enc));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
//        Scanner reader = new Scanner(conn.getInputStream());
//        while(reader.hasNext()) {
//        	System.out.println(reader.nextLine());
//        }
        //Again to make sure that the server completes the request
        @SuppressWarnings("unused")
		int response = conn.getResponseCode();
	}
	
	public JSONObject poll() throws IOException, JSONException {
		URL url = new URL("http://herobrinesarmy.com/update_chat2?messageList=1&c=" + this.channel + "&l=" + this.lastMessageID);
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
	
	@SuppressWarnings("rawtypes")
	public void parseMessages() throws JSONException, IOException {
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
				//used for testing
				//System.out.println(message.formattedMessage());
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	public void parseUsers() throws JSONException, IOException {
		JSONObject jsonUsers = poll().getJSONObject("users");
		LinkedHashSet<Integer> onlineUsers = new LinkedHashSet<Integer>();			
		Iterator keys = jsonUsers.keys();
		while(keys.hasNext()) {
			String key = (String) keys.next();
			JSONObject jsonUser = jsonUsers.getJSONObject(key);
			int userID = jsonUser.getInt("user_id");
			User user = new User(userID);
			user.populateFromProfileString(jsonUser.getString("user"));
			user.setOnline(true);
			if(!users.containsKey(userID)) {
				users.put(userID, user);
			}
			for (User temp : users.values())
			{
				if (!onlineUsers.contains(temp.getUserID()))
				{
					temp.setOnline(false);
				}
			}
		}
	}	
	
	//orders messages and subsequently holds all messages from online session
	public Message[] getMessages() throws JSONException, IOException {
		parseMessages();
		Message[] messageList = messages.values().toArray(new Message[0]);
		//Message[] newMessageList = null;
//		Collection<Message> ml = messages.values();
//		for(Message m : ml) {
//			int i = 0;
//			if(m.getMsgID() > this.lastMessageID) {
//				messageList[i] = m;
//			}
//			i++;
//		}
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
		
//		for(int k = 0; k <= messageList.length-1; k++) {
//			int l = 0;
//			if(messageList[k].getMsgID() > this.lastMessageID) {
//				newMessageList[l] = messageList[k];
//			}
//			l++;
//		}
		
		this.lastMessageID = messageList[messageList.length-1].getMsgID();
		return messageList;
	}
	
	public User[] getUsers() {
		User[] userList = users.values().toArray(new User[0]);
		
		return userList;
	}
	
//	public void updateChannelMessageList(/*Message message*/) {
//		//Class lastMessage is used for getting messages from server, this is to make sure this list
//		//doesn't add messages that are already present.
//		for(int i = 0; i == getMessages().length; i++) {
//			Message temp = getMessages()[i];
//			if(temp.getMsgid() > lastMessageID) {
//				messageList[i] = temp;
//			}
//		}
//		lastMessageID = messageList[messageList.length].getMsgid();
//	}
	
//	private void updateChannelUsers(User user) {
//		//TODO pollUsers to update channel user list, or have "hidden"
//		//messages for libs to use to remove users from chan list
//	}
	
	//TODO Handle ALL THE EXCEPTIONS!
}