package com.songoda.ultimatemoderation.staffchat;

import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.settings.Settings;
import com.songoda.ultimatemoderation.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StaffChannel {

    private final String channelName;
    private char chatChar = Settings.STAFFCHAT_COLOR_CODE.getChar();
    private final List<UUID> members = new ArrayList<>();
    private final List<String> chatLog = new ArrayList<>();

    public StaffChannel(String channelName) {
        this.channelName = channelName;
    }

    public List<UUID> listMembers() {
        return new ArrayList<>(members);
    }

    public void addMember(Player player) {
        if (members.contains(player.getUniqueId())) return;
        messageAll(UltimateModeration.getInstance().getLocale()
                .getMessage("event.staffchat.alljoin")
                .processPlaceholder("player", player.getName()).getMessage(), player);

        UltimateModeration.getInstance().getStaffChatManager().getChats().values().stream().forEach(members1 -> {
            if (members1.listMembers().contains(player.getUniqueId())) {
                members1.removeMember(player);
            }
        });
        members.add(player.getUniqueId());
        if (chatLog.size() > 5) {
            chatLog.stream().skip(chatLog.size() - 3).forEach(message -> player.sendMessage(Methods.formatText(message)));
        }
    }

    public void removeMember(Player player) {
        members.remove(player.getUniqueId());
        messageAll(UltimateModeration.getInstance().getLocale()
                .getMessage("event.staffchat.allleave")
                .processPlaceholder("player", player.getName()).getMessage(), player);
    }

    public void processMessage(String message, Player player) {
        messageAll(UltimateModeration.getInstance().getLocale()
                .getMessage("event.staffchat.format")
                .processPlaceholder("color", chatChar)
                .processPlaceholder("channel", channelName)
                .processPlaceholder("player", player.getDisplayName())
                .processPlaceholder("message", message).getMessage());
    }

    public void messageAll(String message) {
        messageAll(message, null);
    }

    public void messageAll(String message, Player exempt) {
        chatLog.add(message);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (exempt != null && player == exempt) continue;
            if (!members.contains(player.getUniqueId()) && !player.hasPermission("um.staffchat.spy")) continue;
            player.sendMessage(Methods.formatText(message));
        }
    }

    public String getChannelName() {
        return channelName;
    }

    public char getChatChar() {
        return chatChar;
    }

    public void setChatChar(char chatChar) {
        this.chatChar = chatChar;
    }
}
