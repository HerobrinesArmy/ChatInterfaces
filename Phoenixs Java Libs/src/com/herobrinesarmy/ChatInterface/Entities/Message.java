/**
 * 
 */
package com.herobrinesarmy.ChatInterface.Entities;

import org.apache.commons.lang3.StringEscapeUtils;

/**
 * @author Phoenix
 *
 */
public class Message {
	
	private int messageID;
	private String timestamp;
	private String user;
	private String messageText;
	
	protected Message(int messageID) {
		this.messageID = messageID;
	}
	
	protected int getMsgID() {
		return messageID;
	}
	
	protected void setMsgid(int msgid) {
		this.messageID = msgid;
	}
	
	protected String getTimestamp() {
		return timestamp;
	}
	
	protected void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	
	protected String getUser() {
		return user;
	}
	
	protected void setUser(String user) {
		this.user = user.substring(user.indexOf("'>")+2,user.indexOf("</"));
	}
	
	protected String getText() {
		return messageText;
	}
	
	protected void setText(String text) {
		this.messageText = text;
	}
	
	protected String getMessage() {
		return StringEscapeUtils.unescapeHtml4("[" + timestamp + "] " + user + ": " + messageText);
	}
}
