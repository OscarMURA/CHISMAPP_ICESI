package com.example.chismapp.server;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The `GroupManager` class manages groups of `ClientHandler` objects, allowing for group creation,
 * message sending, group existence checking, and removing users from all groups.
 */
public class GroupManager {

    private final Map<String, Set<ClientHandler>> groups;

// The `public GroupManager()` constructor in the `GroupManager` class is initializing the `groups`
// field with a new instance of `HashMap`. This ensures that when a new `GroupManager` object is
// created, it will have an empty map ready to store group names and corresponding sets of
// `ClientHandler` objects.
    public GroupManager() {
        this.groups = new HashMap<>();
    }

/**
 * The `createGroup` function adds a client handler to a group with the specified group name in a
 * synchronized manner.
 * 
 * @param groupName The `groupName` parameter is a `String` representing the name of the group that is
 * being created.
 * @param clientHandler The `clientHandler` parameter in the `createGroup` method is an instance of the
 * `ClientHandler` class. It is being added to a group identified by the `groupName` parameter in the
 * `groups` map. The `ClientHandler` class likely represents a client connection or session handler in
 */
    public synchronized void createGroup(String groupName, ClientHandler clientHandler) {
        groups.putIfAbsent(groupName, new HashSet<>());
        groups.get(groupName).add(clientHandler);
    }

/**
 * The `sendMessageToGroup` function sends a message to all members of a specified group in a
 * synchronized manner.
 * 
 * @param groupName The `groupName` parameter is a `String` representing the name of the group to which
 * the message will be sent.
 * @param message The `message` parameter in the `sendMessageToGroup` method is a String that
 * represents the message that you want to send to all members of a specific group.
 */
    public synchronized void sendMessageToGroup(String groupName, String message) {
        Set<ClientHandler> groupMembers = groups.get(groupName);
        if (groupMembers != null) {
            for (ClientHandler member : groupMembers) {
                member.sendMessage(message);
            }
        }
    }

/**
 * The function checks if a specified group exists in a synchronized manner.
 * 
 * @param groupName The `groupName` parameter is a `String` representing the name of a group.
 * @return The method is returning a boolean value indicating whether the `groupName` exists in the
 * `groups` map.
 */
    public synchronized boolean isGroup(String groupName) {
        return groups.containsKey(groupName);
    }

/**
 * The function removes a specific client handler from all groups in a synchronized manner.
 * 
 * @param clientHandler The `clientHandler` parameter is an object of type `ClientHandler`.
 */
    public synchronized void removeUserFromAllGroups(ClientHandler clientHandler) {
        for (Set<ClientHandler> members : groups.values()) {
            members.remove(clientHandler);
        }
    }
}
