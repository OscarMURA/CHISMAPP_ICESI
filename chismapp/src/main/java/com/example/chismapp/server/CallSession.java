package com.example.chismapp.server;

/**
 * The `CallSession` class represents a call session between a caller and a recipient, with attributes
 * for caller, recipient, call acceptance status, and call activity status.
 */
public class CallSession {
    private String caller;
    private String recipient;
    private boolean accepted;
    private boolean active;

// This is a constructor method for the `CallSession` class in Java. When a new `CallSession` object is
// created, this constructor is called with the `caller` and `recipient` parameters provided. Inside
// the constructor, it initializes the `caller` and `recipient` attributes of the `CallSession` object
// with the values passed as parameters. It also sets the `accepted` and `active` attributes to `false`
// by default when a new call session is created.
    public CallSession(String caller, String recipient) {
        this.caller = caller;
        this.recipient = recipient;
        this.accepted = false;
        this.active = false;
    }

/**
 * The `getCaller` function in Java returns the caller.
 * 
 * @return The method `getCaller()` is returning the value of the variable `caller`.
 */
    public String getCaller() {
        return caller;
    }

/**
 * The `getRecipient` function in Java returns the recipient of a message.
 * 
 * @return The `recipient` variable is being returned.
 */
    public String getRecipient() {
        return recipient;
    }

/**
 * The `isAccepted` function in Java returns the value of a boolean variable `accepted`.
 * 
 * @return The method `isAccepted()` returns the value of the `accepted` variable.
 */
    public boolean isAccepted() {
        return accepted;
    }

/**
 * The function `setAccepted` in Java sets the value of a boolean variable `accepted`.
 * 
 * @param accepted The `accepted` parameter is a boolean variable that indicates whether a certain
 * condition or action has been accepted or not. It can have a value of `true` if accepted, or `false`
 * if not accepted. The `setAccepted` method is used to set the value of this parameter.
 */
    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

/**
 * The `isActive` function in Java returns the value of the `active` boolean variable.
 * 
 * @return The method `isActive()` is returning the value of the `active` variable.
 */
    public boolean isActive() {
        return active;
    }

/**
 * The above function sets the active status of an object in Java.
 * 
 * @param active The `active` parameter is a boolean variable that determines whether an object or
 * entity is currently active or inactive. When `active` is set to `true`, it indicates that the object
 * is active, and when set to `false`, it indicates that the object is inactive.
 */
    public void setActive(boolean active) {
        this.active = active;
    }

/**
 * The `endCall` function sets the `accepted` and `active` variables to false.
 */
    public void endCall() {
        this.accepted = false;
        this.active = false;
    }

}
