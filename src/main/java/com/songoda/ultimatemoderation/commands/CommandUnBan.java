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

public class CommandUnBan extends AbstractCommand {
    private final UltimateModeration plugin;

    public CommandUnBan(UltimateModeration plugin) {
        super(CommandType.CONSOLE_OK, "UnBan");
        this.plugin = plugin;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        if (args.length != 1) {
            return ReturnType.SYNTAX_ERROR;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);

        if (!this.plugin.getPunishmentManager().getPlayer(player).getActivePunishments()
                .stream().anyMatch(appliedPunishment -> appliedPunishment.getPunishmentType() == PunishmentType.BAN)) {
            this.plugin.getLocale().newMessage("That player isn't banned.").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }

        PlayerPunishData playerPunishData = this.plugin.getPunishmentManager().getPlayer(player);

        playerPunishData.expirePunishments(PunishmentType.BAN);

        this.plugin.getLocale().getMessage("event.unban.success")
                .processPlaceholder("player", player.getName()).sendPrefixedMessage(sender);
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
        return "um.ban";
    }

    @Override
    public String getSyntax() {
        return "/UnBan <player>";
    }

    @Override
    public String getDescription() {
        return "Allows you to unban players.";
    }
}
