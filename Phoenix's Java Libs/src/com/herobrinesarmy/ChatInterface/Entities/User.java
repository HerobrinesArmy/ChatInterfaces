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
	
	public User(int ID) {
		this.userID = ID;
	}
	
	public int getUserID() {
		return userID;
	}
	
	public String getUsername() {
		return username;
	}
	
	public int getUserTag() {
		return userTag;
	}
	
	public boolean isOnline() {
		return online;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public void setUserTag(int userTag) {
		this.userTag = userTag;
	}
	
	public void setOnline(boolean online) {
		this.online = online;
	}
	
	protected void populateFromProfileString(String string) {
		if (string.contains("'element_username tag-"))
			{
				this.userTag = Integer.parseInt(string.substring(string.indexOf("tag-")+4,string.lastIndexOf("'>")));
			}
			this.username = string.substring(string.lastIndexOf("'>")+2,string.lastIndexOf("</"));
	}
}