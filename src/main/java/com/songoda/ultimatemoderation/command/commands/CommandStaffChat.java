package com.songoda.ultimatemoderation.command.commands;

import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.command.AbstractCommand;

import com.songoda.ultimatemoderation.staffchat.StaffChannel;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CommandStaffChat extends AbstractCommand {

    public CommandStaffChat() {
        super(true, true, "StaffChat");
    }

    @Override
    protected ReturnType runCommand(UltimateModeration instance, CommandSender sender, String... args) {
        if (args.length != 1)
            return ReturnType.SYNTAX_ERROR;

        String channelName = args[0];
        Player player = (Player)sender;

        if (channelName.trim().equalsIgnoreCase("leave")) {
            for (StaffChannel channel : instance.getStaffChatManager().getChats().values()) {
                if (!channel.listMembers().contains(player.getUniqueId())) continue;
                channel.removeMember(player);
                player.sendMessage("You left " + channel.getChannelName() + " successfully.");
                return ReturnType.SUCCESS;
            }
            player.sendMessage("You are not in any channels.");
            return ReturnType.FAILURE;
        }

        instance.getStaffChatManager().getChat(channelName).addMember(player);
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(UltimateModeration instance, CommandSender sender, String... args) {
        return new ArrayList<>(instance.getStaffChatManager().getChats().keySet());
    }

    @Override
    public String getPermissionNode() {
        return "um.staffchat";
    }

    @Override
    public String getSyntax() {
        return "/sc <channel/leave>";
    }

    @Override
    public String getDescription() {
        return "Opens a staff chat channel.";
    }
}
