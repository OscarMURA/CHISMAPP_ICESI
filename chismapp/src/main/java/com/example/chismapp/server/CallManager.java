package com.example.chismapp.server;

import java.util.concurrent.ConcurrentHashMap;

public class CallManager {

    // Mapa que mantiene el estado de las llamadas activas
    private ConcurrentHashMap<String, CallSession> activeCalls;

    public CallManager() {
        activeCalls = new ConcurrentHashMap<>();
    }

    // Iniciar una llamada
    public synchronized boolean initiateCall(String caller, String recipient) {
        if (activeCalls.containsKey(caller) || activeCalls.containsKey(recipient)) {
            return false; // Uno de los usuarios ya está en una llamada
        }
        CallSession session = new CallSession(caller, recipient);
        activeCalls.put(caller, session);
        activeCalls.put(recipient, session);
        return true;
    }

    // Aceptar una llamada
    public synchronized void acceptCall(String recipient, String caller) {
        CallSession session = activeCalls.get(caller);
        if (session != null && session.getRecipient().equals(recipient)) {
            session.setAccepted(true);
        }
    }

    // Rechazar una llamada
    public synchronized void rejectCall(String recipient, String caller) {
        CallSession session = activeCalls.get(caller);
        if (session != null && session.getRecipient().equals(recipient)) {
            activeCalls.remove(caller);
            activeCalls.remove(recipient);
        }
    }

    // Finalizar una llamada
    public synchronized void endCall(String user) {
        CallSession session = activeCalls.get(user);
        if (session != null) {
            String caller = session.getCaller();
            String recipient = session.getRecipient();
            activeCalls.remove(caller);
            activeCalls.remove(recipient);
        }
    }

    // Verificar si un usuario está en una llamada
    public boolean isInCall(String user) {
        return activeCalls.containsKey(user);
    }

    // Obtener la sesión de llamada de un usuario
    public CallSession getCallSession(String user) {
        return activeCalls.get(user);
    }
}
