/**
 * Copyright (c) 2012 Phoenix, Herobrine and herobrinesarmy.com
 * YOU WILL FUCKING FREELY USE MY CODE HOWEVER YOU GODDAMN WANT TO. (sic) - Kitcat490
 */
package com.herobrinesarmy.ChatInterface.Entities;

import org.apache.commons.lang3.StringEscapeUtils;

/**
 * Message holds all the relative attributes of a message in a ChatRoom. It extracts the users username
 * and formats any HTML formatted characters into a string like you would see on <a href="herobrinesarmy.com">
 * herobrinesarmy.com</a>. Emotes and images (bbcode images) that are on the chat are also handled. Emotes
 * are handled in the way of being replaced by their name and images are replaced with the hyperlink to that 
 * image. Since this library is intended to not just support GUIs, it will be up to the client developer if/how they
 * will implement images in the place of emotes and images.
 * 
 * @author Phoenix
 * @version 1.0
 * @since 1.0
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
		this.messageText = handleBBCode(text);
	}
	
	private String handleBBCode(String text) {
		boolean contains = true;
		if(text.contains("\"bbcode_smiley\"") || text.contains("\"bbcode_img\"")) {
			while(contains == true) {
				if(text.contains("\"bbcode_smiley\"")) {
					String emote = text.substring(text.indexOf("\"",text.indexOf("alt=\""))+1,text.indexOf("\"",text.indexOf("\"",text.indexOf("alt=\""))+1));
					String regex = text.substring(text.indexOf("<img"),text.indexOf("/>")+2);
					String emoteText = text.replaceFirst(regex, emote);
					text = emoteText;
					if(!emoteText.contains("\"bbcode_smiley\"")) {
						contains = false;
					}
					else if(emoteText.contains(";")) {
						text = ":^^;:";
						contains = false;
					}
				}
				else if(text.contains("\"bbcode_img\"")) {
					String imgURL = text.substring(text.indexOf("\"",text.indexOf("src="))+1,text.indexOf("\"",text.indexOf("\"",text.indexOf("src="))+1));
					String regex = text.substring(text.indexOf("<img"),text.indexOf("/>")+2);
					String imgText = text.replaceFirst(regex, imgURL);
					text = imgText;
					if(!imgText.contains("\"bbcode_img\"")) {
						contains = false;
					}
				}
			}
		}
		return text;
	}
	
	protected String getMessage() {
		return StringEscapeUtils.unescapeHtml4("[" + timestamp + "] " + user + ": " + messageText);
	}
}
