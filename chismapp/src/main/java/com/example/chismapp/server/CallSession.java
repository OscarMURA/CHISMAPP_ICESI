package com.example.chismapp.server;

public class CallSession {
    private String caller;
    private String recipient;
    private boolean accepted;

    public CallSession(String caller, String recipient) {
        this.caller = caller;
        this.recipient = recipient;
        this.accepted = false;
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
}
