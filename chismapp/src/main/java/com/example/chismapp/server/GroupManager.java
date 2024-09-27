package com.example.chismapp.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupManager {

    // A map that holds group names and their members (list of ClientHandler)
    private Map<String, List<ClientHandler>> groups;

    public GroupManager() {
        this.groups = new HashMap<>();
    }

    // Create a new group if it doesn't exist
    public void createGroup(String groupName) {
        if (!groups.containsKey(groupName)) {
            groups.put(groupName, new ArrayList<>());
            System.out.println("Group " + groupName + " created.");
        } else {
            System.out.println("Group " + groupName + " already exists.");
        }
    }

    // Add a client to a specific group
    public void addClientToGroup(String groupName, ClientHandler client) {
        List<ClientHandler> groupMembers = groups.get(groupName);
        if (groupMembers != null) {
            groupMembers.add(client);
            System.out.println("Client added to group " + groupName);
        } else {
            System.out.println("Group " + groupName + " does not exist.");
        }
    }

    // Send a message to all members of a specific group
    public void sendMessageToGroup(String groupName, String message) {
        List<ClientHandler> groupMembers = groups.get(groupName);
        if (groupMembers != null) {
            for (ClientHandler client : groupMembers) {
                client.sendMessage(message);  // Send message to all group members
            }
        } else {
            System.out.println("Group " + groupName + " does not exist.");
        }
    }
}
