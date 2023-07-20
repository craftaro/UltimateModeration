package com.songoda.ultimatemoderation.staffchat;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class StaffChatManager {
    private final Map<String, StaffChannel> chats = new HashMap<>();

    public Map<String, StaffChannel> getChats() {
        return Collections.unmodifiableMap(this.chats);
    }

    public StaffChannel getChat(String channel) {
        return this.chats.computeIfAbsent(formatName(channel), k -> new StaffChannel(formatName(channel)));
    }

    public void removeChat(String channel) {
        this.chats.remove(formatName(channel));
    }

    private String formatName(String name) {
        if (name == null) {
            return null;
        }
        name = name.toUpperCase().trim();
        name = name.replace(" ", "_");
        return name;
    }
}
