package com.example.chismapp.server;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GroupManager {

    private final Map<String, Set<ClientHandler>> groups;

    public GroupManager() {
        this.groups = new HashMap<>();
    }

    public void createGroup(String groupName, ClientHandler clientHandler) {
        groups.putIfAbsent(groupName, new HashSet<>());
        groups.get(groupName).add(clientHandler);
    }

    public void sendMessageToGroup(String groupName, String message) {
        Set<ClientHandler> groupMembers = groups.get(groupName);
        if (groupMembers != null) {
            for (ClientHandler member : groupMembers) {
                member.sendMessage(message);
            }
        }
    }

    public boolean isGroup(String groupName) {
        return groups.containsKey(groupName);
    }
}
