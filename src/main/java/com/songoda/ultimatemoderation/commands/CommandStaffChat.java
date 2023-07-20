package com.songoda.ultimatemoderation.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.staffchat.StaffChannel;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CommandStaffChat extends AbstractCommand {
    private final UltimateModeration plugin;

    public CommandStaffChat(UltimateModeration plugin) {
        super(CommandType.PLAYER_ONLY, "StaffChat");
        this.plugin = plugin;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        if (args.length != 1) {
            return ReturnType.SYNTAX_ERROR;
        }

        String channelName = args[0];
        Player player = (Player) sender;

        if (channelName.trim().equalsIgnoreCase("leave")) {
            for (StaffChannel channel : this.plugin.getStaffChatManager().getChats().values()) {
                if (!channel.listMembers().contains(player.getUniqueId())) {
                    continue;
                }
                channel.removeMember(player);
                this.plugin.getLocale().getMessage("event.staffchat.leave")
                        .processPlaceholder("channel", channel.getChannelName()).sendPrefixedMessage(player);
                return ReturnType.SUCCESS;
            }
            this.plugin.getLocale().getMessage("event.staffchat.nochannels").sendPrefixedMessage(player);
            return ReturnType.FAILURE;
        }

        this.plugin.getLocale().getMessage("event.staffchat.join")
                .processPlaceholder("channel", channelName).sendPrefixedMessage(player);
        this.plugin.getStaffChatManager().getChat(channelName).addMember(player);
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        return new ArrayList<>(this.plugin.getStaffChatManager().getChats().keySet());
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
