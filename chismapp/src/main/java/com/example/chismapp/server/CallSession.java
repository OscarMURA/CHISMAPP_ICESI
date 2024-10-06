package com.example.chismapp.server;

public class CallSession {
    private String caller;
    private String recipient;
    private boolean accepted;
    private boolean active;

    public CallSession(String caller, String recipient) {
        this.caller = caller;
        this.recipient = recipient;
        this.accepted = false;
        this.active = false;
    }

    public String getCaller() {
        return caller;
    }

    public String getRecipient() {
        return recipient;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void endCall() {
        this.accepted = false;
        this.active = false;
    }

}
