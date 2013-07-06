package com.herobrinesarmy.javaclient.entities;

import org.apache.commons.lang3.StringEscapeUtils;

/**
 * A class for a message on herobrinesarmy.com, handles any bbcode tags, emoticons, and links within the message.
 *
 * @author Phoenix
 * @author LukeW4lker
 * @version 0.1
 */
public class Message {

    private int id;
    private String timestamp;
    private String user;
    private String messageText;

    protected Message(int id, String time, String user, String text) {
        this.id = id;
        this.timestamp = time;
        this.user = user;
        this.messageText = text;
    }

    public int getMessageID() {
        return id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getUser() {
        return user;
    }

    public String getMessageText() {
        return messageText;
    }

    public String toString() {
        if(messageText.startsWith("/me")) {
            messageText = messageText.substring(messageText.indexOf("/me") + 3);
            return StringEscapeUtils.unescapeHtml4("[" + timestamp + "] " + user + messageText);
        }
        else {
            return StringEscapeUtils.unescapeHtml4("[" + timestamp + "] " + user + ": " + messageText);
        }
    }
}
