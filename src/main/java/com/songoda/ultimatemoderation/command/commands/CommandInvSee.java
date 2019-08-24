package com.songoda.ultimatemoderation.command.commands;

import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.command.AbstractCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandInvSee extends AbstractCommand {

    public CommandInvSee() {
        super(true, true, "InvSee");
    }

    @Override
    protected ReturnType runCommand(UltimateModeration instance, CommandSender sender, String... args) {
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
    protected List<String> onTab(UltimateModeration instance, CommandSender sender, String... args) {
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
