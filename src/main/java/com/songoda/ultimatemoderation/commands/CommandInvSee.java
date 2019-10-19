package com.songoda.ultimatemoderation.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.ultimatemoderation.UltimateModeration;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CommandInvSee extends AbstractCommand {

    private UltimateModeration instance;

    public CommandInvSee(UltimateModeration instance) {
        super(CommandType.PLAYER_ONLY, "InvSee");
        this.instance = instance;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        if (args.length != 1)
            return ReturnType.SYNTAX_ERROR;

        Player player = Bukkit.getPlayer(args[0]);

        if (player == null) {
            instance.getLocale().newMessage("That player does not exist or is not online").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }

        if (player.hasPermission("um.invsee.exempt")) {
            instance.getLocale().newMessage("You cannot invsee that player.").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }

        ((Player) sender).openInventory(player.getInventory());
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        if (args.length == 1) {
            List<String> players = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                players.add(player.getName());
            }
            return players;
        }
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "um.invsee";
    }

    @Override
    public String getSyntax() {
        return "/InvSee <player>";
    }

    @Override
    public String getDescription() {
        return "Allows you to see inside of a players inventory.";
    }
}
