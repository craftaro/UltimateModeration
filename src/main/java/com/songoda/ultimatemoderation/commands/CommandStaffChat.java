package com.songoda.ultimatemoderation.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.staffchat.StaffChannel;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CommandStaffChat extends AbstractCommand {

    private UltimateModeration instance;

    public CommandStaffChat(UltimateModeration instance) {
        super(CommandType.PLAYER_ONLY, "StaffChat");
        this.instance = instance;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        if (args.length != 1)
            return ReturnType.SYNTAX_ERROR;

        String channelName = args[0];
        Player player = (Player) sender;

        if (channelName.trim().equalsIgnoreCase("leave")) {
            for (StaffChannel channel : instance.getStaffChatManager().getChats().values()) {
                if (!channel.listMembers().contains(player.getUniqueId())) continue;
                channel.removeMember(player);
                instance.getLocale().getMessage("event.staffchat.leave")
                        .processPlaceholder("channel", channel.getChannelName()).sendPrefixedMessage(player);
                return ReturnType.SUCCESS;
            }
            instance.getLocale().getMessage("event.staffchat.nochannels").sendPrefixedMessage(player);
            return ReturnType.FAILURE;
        }

        instance.getLocale().getMessage("event.staffchat.join")
                .processPlaceholder("channel", channelName).sendPrefixedMessage(player);
        instance.getStaffChatManager().getChat(channelName).addMember(player);
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
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
