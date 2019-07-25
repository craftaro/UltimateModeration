package com.songoda.ultimatemoderation.command.commands;

import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.command.AbstractCommand;
import com.songoda.ultimatemoderation.punish.Punishment;
import com.songoda.ultimatemoderation.punish.PunishmentType;
import com.songoda.ultimatemoderation.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandKick extends AbstractCommand {

    public CommandKick() {
        super(false, true, "Kick");
    }

    @Override
    protected ReturnType runCommand(UltimateModeration instance, CommandSender sender, String... args) {
        if (args.length < 1)
            return ReturnType.SYNTAX_ERROR;

        StringBuilder reasonBuilder = new StringBuilder();
        if (args.length > 1) {
            for (int i = 1; i < args.length; i++) {
                String line = args[i];
                reasonBuilder.append(line).append(" ");

            }
        }
        String reason = reasonBuilder.toString().trim();

        OfflinePlayer player = Bukkit.getPlayer(args[0]);

        if (player == null) {
            instance.getLocale().newMessage("That player does not exist or is not online.").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }

        new Punishment(PunishmentType.KICK, reason.equals("") ? null : reason)
                .execute(sender, player);

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
        } else if (args.length == 2) {
            return Collections.singletonList("For being bad");
        }
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "um.kick";
    }

    @Override
    public String getSyntax() {
        return "/Kick <player> [reason]";
    }

    @Override
    public String getDescription() {
        return "Allows you to kick players.";
    }
}
