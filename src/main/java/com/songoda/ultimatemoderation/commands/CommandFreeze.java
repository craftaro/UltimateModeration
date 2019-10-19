package com.songoda.ultimatemoderation.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.ultimatemoderation.UltimateModeration;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CommandFreeze extends AbstractCommand {

    private UltimateModeration instance;
    private static List<UUID> frozen = new ArrayList<>();

    public CommandFreeze(UltimateModeration instance) {
        super(CommandType.CONSOLE_OK, "Freeze");
        this.instance = instance;
    }

    public static void freeze(OfflinePlayer player, CommandSender sender) {
        UltimateModeration instance = UltimateModeration.getInstance();
        if (frozen.contains(player.getUniqueId())) {
            frozen.remove(player.getUniqueId());
            instance.getLocale().getMessage("command.freeze.remove")
                    .processPlaceholder("player", player.getPlayer().getDisplayName()).sendPrefixedMessage(sender);
            instance.getLocale().getMessage("command.freeze.alertremove").sendPrefixedMessage(sender);
        } else {
            frozen.add(player.getUniqueId());
            instance.getLocale().getMessage("command.freeze.add")
                    .processPlaceholder("player", player.getPlayer().getDisplayName()).sendPrefixedMessage(sender);
            instance.getLocale().getMessage("command.freeze.alertadd").sendPrefixedMessage(player.getPlayer());
        }
    }

    public static boolean isFrozen(OfflinePlayer player) {
        return frozen.contains(player.getUniqueId());
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        if (args.length != 1)
            return ReturnType.SYNTAX_ERROR;

        Player player = Bukkit.getPlayer(args[0]);

        if (player == null) {
            instance.getLocale().newMessage("That player does not exist or is not online.").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }

        if (sender instanceof Player && player.hasPermission("um.freeze.exempt")) {
            instance.getLocale().newMessage("That player cannot be frozen.").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }

        freeze(player, sender);

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
