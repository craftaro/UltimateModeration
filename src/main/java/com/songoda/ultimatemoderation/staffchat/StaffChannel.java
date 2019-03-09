package com.songoda.ultimatemoderation.staffchat;

import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.utils.Methods;
import com.songoda.ultimatemoderation.utils.SettingsManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class StaffChannel {

    private final String channelName;
    private char chatChar = SettingsManager.Setting.STAFFCHAT_COLOR_CODE.getChar();
    private final List<UUID> members = new ArrayList<>();
    private final List<String> chatLog = new ArrayList<>();

    public StaffChannel(String channelName) {
        this.channelName = channelName;
    }

    public List<UUID> listMembers() {
        return new ArrayList<>(members);
    }

    public void addMember(Player player) {
        UltimateModeration.getInstance().getStaffChatManager().getChats().values().stream().forEach(members1 -> {
            if (members1.listMembers().contains(player.getUniqueId())) {
                members1.removeMember(player);
            }
        });
        members.add(player.getUniqueId());
        if (chatLog.size() > 5) {
            chatLog.stream().skip(chatLog.size() - 3).forEach(message -> player.sendMessage(Methods.formatText(message)));
        }
        messageAll(UltimateModeration.getInstance().getLocale().getMessage("event.staffchat.format.join", chatChar, channelName, player.getDisplayName()));
    }

    public void removeMember(Player player) {
        members.remove(player.getUniqueId());
        messageAll(UltimateModeration.getInstance().getLocale().getMessage("event.staffchat.format.leave", chatChar, channelName, player.getDisplayName()));
    }

    public void processMessage(String message, Player player) {
        messageAll(UltimateModeration.getInstance().getLocale().getMessage("event.staffchat.format", chatChar, channelName, player.getDisplayName(), chatChar, message));
    }

    private void messageAll(String message) {
        chatLog.add(message);
        for (Player player : Bukkit.getOnlinePlayers()) {
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
