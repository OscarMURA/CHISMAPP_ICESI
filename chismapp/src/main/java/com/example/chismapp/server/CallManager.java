/**
 * The `CallManager` class in Java manages active and pending call sessions between users using
 * ConcurrentHashMaps.
 */
package com.example.chismapp.server;

import java.util.concurrent.ConcurrentHashMap;

public class CallManager {


    private ConcurrentHashMap<String, CallSession> activeCalls;


    private ConcurrentHashMap<String, CallSession> pendingCalls;

// The `public CallManager()` constructor in the `CallManager` class is initializing two
// `ConcurrentHashMap` objects named `activeCalls` and `pendingCalls`. It creates new instances of
// `ConcurrentHashMap` for both `activeCalls` and `pendingCalls` when a new `CallManager` object is
// created. This ensures that the `CallManager` object starts with empty maps for managing active and
// pending call sessions between users.
    public CallManager() {
        activeCalls = new ConcurrentHashMap<>();
        pendingCalls = new ConcurrentHashMap<>();
    }

/**
 * The `initiatePendingCall` function checks if either the caller or recipient is already in an active
 * or pending call, and if not, creates a new pending call session between them.
 * 
 * @param caller The `caller` parameter in the `initiatePendingCall` method represents the user who is
 * initiating the call.
 * @param recipient The `recipient` parameter in the `initiatePendingCall` method represents the user
 * who is being called or the intended recipient of the call. This method is used to initiate a pending
 * call between two users, the `caller` (the user initiating the call) and the `recipient` (the
 * @return The method `initiatePendingCall` returns a boolean value. It returns `true` if the call
 * initiation was successful and both the caller and recipient were not already in an active or pending
 * call. It returns `false` if either the caller or recipient is already in an active or pending call.
 */
    public synchronized boolean initiatePendingCall(String caller, String recipient) {
        if (activeCalls.containsKey(caller) || activeCalls.containsKey(recipient) ||
                pendingCalls.containsKey(caller) || pendingCalls.containsKey(recipient)) {
            return false; // Uno de los usuarios ya está en una llamada activa o pendiente
        }
        CallSession session = new CallSession(caller, recipient);
        pendingCalls.put(caller, session);
        pendingCalls.put(recipient, session);
        return true;
    }

    // Aceptar una llamada
/**
 * The `acceptCall` method in Java synchronously accepts a call between a recipient and a caller,
 * moving the call from pending to active status if the recipient matches the expected recipient for
 * the call session.
 * 
 * @param recipient Recipient is the person who is receiving the call.
 * @param caller The `caller` parameter in the `acceptCall` method represents the person who is making
 * the call.
 */
    public synchronized void acceptCall(String recipient, String caller) {
        CallSession session = pendingCalls.get(caller);
        if (session != null && session.getRecipient().equals(recipient)) {
            session.setAccepted(true);
            // Mover la llamada de "pendiente" a "activa"
            pendingCalls.remove(caller);
            pendingCalls.remove(recipient);
            activeCalls.put(caller, session);
            activeCalls.put(recipient, session);
        }
    }

    // Rechazar una llamada
/**
 * The `rejectCall` function removes a pending call session if the recipient and caller match the
 * provided parameters.
 * 
 * @param recipient Recipient is the person who is supposed to receive the call.
 * @param caller The `caller` parameter in the `rejectCall` method represents the person who initiated
 * the call that is being rejected.
 */
    public synchronized void rejectCall(String recipient, String caller) {
        CallSession session = pendingCalls.get(caller);
        if (session != null && session.getRecipient().equals(recipient)) {
            pendingCalls.remove(caller);
            pendingCalls.remove(recipient);
        }
    }

/**
 * The `endCall` function removes an active call session and related pending calls for a specified
 * user.
 * 
 * @param userName The `userName` parameter in the `endCall` method represents the user for whom the
 * call session is being ended. This method is used to end an active call session for the specified
 * user.
 */
    public synchronized void endCall(String userName) {
        CallSession session = activeCalls.get(userName);
        if (session != null) {
            activeCalls.remove(session.getCaller());
            activeCalls.remove(session.getRecipient());
            pendingCalls.remove(session.getCaller());
            pendingCalls.remove(session.getRecipient());
        }
    }

/**
 * The `removeCallSession` function removes the caller and recipient from both active and pending call
 * lists if the provided call session is not null.
 * 
 * @param session The `session` parameter in the `removeCallSession` method is an object of the
 * `CallSession` class.
 */
    public synchronized void removeCallSession(CallSession session) {
        if (session != null) {
            activeCalls.remove(session.getCaller());
            activeCalls.remove(session.getRecipient());
            pendingCalls.remove(session.getCaller());
            pendingCalls.remove(session.getRecipient());
        }
    }

/**
 * The `isInCall` function checks if a user is currently in an active call or has a pending call.
 * 
 * @param userName The `userName` parameter is a `String` representing the username of a user for whom
 * we want to check if they are currently in an active call or have a pending call.
 * @return The method `isInCall` is returning a boolean value indicating whether the user with the
 * given `userName` is currently in an active call or has a pending call. It checks if the
 * `activeCalls` map or the `pendingCalls` map contains the user's `userName` key and returns `true` if
 * either of them does, otherwise it returns `false`.
 */
    public synchronized boolean isInCall(String userName) {
        return activeCalls.containsKey(userName) || pendingCalls.containsKey(userName);
    }

/**
 * The function `getCallSession` retrieves the active call session for a specified user in a
 * synchronized manner.
 * 
 * @param user The `user` parameter in the `getCallSession` method is a String representing the user
 * for whom you want to retrieve the call session.
 * @return The `getCallSession` method is returning a `CallSession` object associated with the
 * specified `user` from the `activeCalls` map.
 */
    public synchronized CallSession getCallSession(String user) {
        return activeCalls.get(user);
    }

/**
 * The function `getPendingCallSession` retrieves a pending call session for a specific user in a
 * synchronized manner.
 * 
 * @param user The `user` parameter is a `String` representing the user for which the pending call
 * session needs to be retrieved.
 * @return A `CallSession` object corresponding to the specified `user` is being returned.
 */
    public synchronized CallSession getPendingCallSession(String user) {
        return pendingCalls.get(user);
    }
}
