package com.example.chismapp.model;

import java.time.LocalDateTime;
public class Message {
    private String content;
    private User sender;
    private String recipient;  // Could be a username or a groupName
    private LocalDateTime timestamp;

    public Message(String content, User sender, String recipient) {
        this.content = content;
        this.sender = sender;
        this.recipient = recipient;
        this.timestamp = LocalDateTime.now();  // Sets the current time as the timestamp
    }


    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "[" + timestamp + "] " + sender.getUsername() + ": " + content;
    }
}