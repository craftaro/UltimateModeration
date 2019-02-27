package com.songoda.ultimatemoderation.command.commands;

import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.command.AbstractCommand;
import com.songoda.ultimatemoderation.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CommandFreeze extends AbstractCommand {

    private static List<UUID> frozen = new ArrayList<>();

    public CommandFreeze() {
        super(true, true,"Freeze");
    }

    @Override
    protected ReturnType runCommand(UltimateModeration instance, CommandSender sender, String... args) {
        if (args.length != 1)
            return ReturnType.SYNTAX_ERROR;

        Player player = Bukkit.getPlayer(args[0]);

        if (player == null) {
            sender.sendMessage(instance.getReferences().getPrefix() + "That player does not exist or is not online.");
            return ReturnType.FAILURE;
        }

        if (frozen.contains(player.getUniqueId())) {
            frozen.remove(player.getUniqueId());
            sender.sendMessage(instance.getReferences().getPrefix() + instance.getLocale().getMessage("command.freeze.remove", player.getDisplayName()));
            player.sendMessage(instance.getReferences().getPrefix() + instance.getLocale().getMessage("command.freeze.alertremove"));
        } else {
            frozen.add(player.getUniqueId());
            sender.sendMessage(instance.getReferences().getPrefix() + instance.getLocale().getMessage("command.freeze.add", player.getDisplayName()));
            player.sendMessage(instance.getReferences().getPrefix() + instance.getLocale().getMessage("command.freeze.alertadd"));
        }

        return ReturnType.SUCCESS;
    }

    public static boolean isFrozen(Player player) {
        return frozen.contains(player.getUniqueId());
    }

    @Override
    protected List<String> onTab(UltimateModeration instance, CommandSender sender, String... args) {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "um.freeze";
    }

    @Override
    public String getSyntax() {
        return "/Freeze <player>";
    }

    @Override
    public String getDescription() {
        return "Allows you to freeze a player.";
    }
}
