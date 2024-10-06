package com.example.chismapp.server;

import java.util.concurrent.ConcurrentHashMap;

public class CallManager {


    private ConcurrentHashMap<String, CallSession> activeCalls;


    private ConcurrentHashMap<String, CallSession> pendingCalls;

    public CallManager() {
        activeCalls = new ConcurrentHashMap<>();
        pendingCalls = new ConcurrentHashMap<>();
    }

    // Iniciar una llamada pendiente
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
    public synchronized void rejectCall(String recipient, String caller) {
        CallSession session = pendingCalls.get(caller);
        if (session != null && session.getRecipient().equals(recipient)) {
            pendingCalls.remove(caller);
            pendingCalls.remove(recipient);
        }
    }

    // Método para finalizar una llamada
    public synchronized void endCall(String userName) {
        CallSession session = activeCalls.get(userName);
        if (session != null) {
            activeCalls.remove(session.getCaller());
            activeCalls.remove(session.getRecipient());
            pendingCalls.remove(session.getCaller());
            pendingCalls.remove(session.getRecipient());
        }
    }


    // Método para eliminar la sesión de llamada
    public synchronized void removeCallSession(CallSession session) {
        if (session != null) {
            activeCalls.remove(session.getCaller());
            activeCalls.remove(session.getRecipient());
            pendingCalls.remove(session.getCaller());
            pendingCalls.remove(session.getRecipient());
        }
    }

    // Método para verificar si un usuario está en una llamada (activa o pendiente)
    public synchronized boolean isInCall(String userName) {
        return activeCalls.containsKey(userName) || pendingCalls.containsKey(userName);
    }

    // Obtener la sesión de llamada de un usuario
    public synchronized CallSession getCallSession(String user) {
        return activeCalls.get(user);
    }

    // Obtener la sesión de llamada pendiente de un usuario
    public synchronized CallSession getPendingCallSession(String user) {
        return pendingCalls.get(user);
    }
}
