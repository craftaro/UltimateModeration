package com.songoda.ultimatemoderation.staffchat;

import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StaffChannel {

    private final String channelName;
    private char chatChar = 'b';
    private final List<UUID> members = new ArrayList<>();

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
        for (UUID uuid : members) {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null) continue;
            p.sendMessage(Methods.formatText(channelName + " " + player.getDisplayName() + "&" + chatChar + " has just entered the channel."));
        }
    }

    public void removeMember(Player player) {
        members.remove(player.getUniqueId());
        for (UUID uuid : members) {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null) continue;
            p.sendMessage(Methods.formatText(channelName + " " + player.getDisplayName() + "&" + chatChar + " has just left the channel."));
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
