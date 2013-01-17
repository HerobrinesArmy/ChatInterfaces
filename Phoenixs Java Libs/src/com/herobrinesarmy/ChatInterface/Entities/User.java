/**
 * Copyright (c) 2012 Phoenix, Herobrine and herobrinesarmy.com
 * YOU WILL FUCKING FREELY USE MY CODE HOWEVER YOU GODDAMN WANT TO. (sic) - Kitcat490
 */
package com.herobrinesarmy.ChatInterface.Entities;

/**
 * @author Phoenix
 *
 */
public class User {
	
	private int userID;
	private String username;
	private int userTag;
	private boolean online;
	
	protected User(int ID) {
		this.userID = ID;
	}
	
	protected int getUserID() {
		return userID;
	}
	
	protected String getUsername() {
		return username;
	}
	
	protected int getUserTag() {
		return userTag;
	}
	
	protected boolean isOnline() {
		return online;
	}
	
	protected void setUsername(String username) {
		this.username = username;
	}
	
	protected void setUserTag(int userTag) {
		this.userTag = userTag;
	}
	
	protected void setOnline(boolean online) {
		this.online = online;
	}
	
	protected void populateFromProfileString(String string) {
		if (string.contains("'element_username tag-"))
			{
				this.userTag = Integer.parseInt(string.substring(string.indexOf('-')+1,string.indexOf("'>")));
			}
			this.username = string.substring(string.indexOf("'>")+2,string.indexOf("</"));
	}
}