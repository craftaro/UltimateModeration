package com.songoda.ultimatemoderation.command.commands;

import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.command.AbstractCommand;
import com.songoda.ultimatemoderation.punish.AppliedPunishment;
import com.songoda.ultimatemoderation.punish.PunishmentType;
import com.songoda.ultimatemoderation.punish.player.PlayerPunishData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CommandUnBan extends AbstractCommand {

    public CommandUnBan() {
        super(false, true, "UnBan");
    }

    @Override
    protected ReturnType runCommand(UltimateModeration instance, CommandSender sender, String... args) {
        if (args.length != 1)
            return ReturnType.SYNTAX_ERROR;

        OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);

        if (player == null) {
            sender.sendMessage(instance.getReferences().getPrefix() + "That player does not exist.");
            return ReturnType.FAILURE;
        }

        PlayerPunishData playerPunishData = instance.getPunishmentManager().getPlayer(player);

        playerPunishData.expirePunishments(PunishmentType.BAN);

        sender.sendMessage(instance.getReferences().getPrefix() + instance.getLocale().getMessage("event.unban.success"));
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(UltimateModeration instance, CommandSender sender, String... args) {
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
        return "Allows you to ban players.";
    }
}
