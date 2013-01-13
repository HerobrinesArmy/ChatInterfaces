/**
 * 
 */
package com.herobrinesarmy.ChatInterface.Entities;

/**
 * @author Phoenix
 *
 */
public class Message {
	
	private int messageID;
	private String timestamp;
	private String user;
	private String messageText;
	
	public Message(int messageID) {
		this.messageID = messageID;
	}
	
	public int getMsgID() {
		return messageID;
	}
	
	public void setMsgid(int msgid) {
		this.messageID = msgid;
	}
	
	public String getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	
	public String getUser() {
		return user;
	}
	
	public void setUser(String user) {
		this.user = user.substring(user.indexOf("'>")+2,user.indexOf("</"));
	}
	
	public String getText() {
		return messageText;
	}
	
	public void setText(String text) {
		this.messageText = text;
	}
	
	public String getMessage() {
		return "[" + timestamp + "] " + user + ": " + messageText;
	}
}
