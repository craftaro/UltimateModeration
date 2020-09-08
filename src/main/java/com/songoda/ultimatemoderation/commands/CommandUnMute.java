package com.songoda.ultimatemoderation.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.punish.PunishmentType;
import com.songoda.ultimatemoderation.punish.player.PlayerPunishData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CommandUnMute extends AbstractCommand {

    private final UltimateModeration plugin;

    public CommandUnMute(UltimateModeration plugin) {
        super(CommandType.CONSOLE_OK, "UnMute");
        this.plugin = plugin;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        if (args.length != 1)
            return ReturnType.SYNTAX_ERROR;

        OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);

        if (!player.hasPlayedBefore()) {
            plugin.getLocale().newMessage("That player does not exist.").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }

        if (!plugin.getPunishmentManager().getPlayer(player).getActivePunishments()
                .stream().anyMatch(appliedPunishment -> appliedPunishment.getPunishmentType() == PunishmentType.MUTE)) {
            plugin.getLocale().newMessage("That player isn't muted.").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }

        PlayerPunishData playerPunishData = plugin.getPunishmentManager().getPlayer(player);

        playerPunishData.expirePunishments(PunishmentType.MUTE);

        plugin.getLocale().newMessage(plugin.getLocale().getMessage("event.unmute.success")
                .processPlaceholder("player", player.getName()).getMessage()).sendPrefixedMessage(sender);
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
        return "um.mute";
    }

    @Override
    public String getSyntax() {
        return "/UnMute <player>";
    }

    @Override
    public String getDescription() {
        return "Allows you to unmute players.";
    }
}
