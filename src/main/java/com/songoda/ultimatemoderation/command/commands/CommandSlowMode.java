package com.songoda.ultimatemoderation.command.commands;

import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.command.AbstractCommand;
import com.songoda.ultimatemoderation.listeners.ChatListener;
import com.songoda.ultimatemoderation.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CommandSlowMode extends AbstractCommand {

    public CommandSlowMode() {
        super(true, true, "Slowmode");
    }

    @Override
    protected ReturnType runCommand(UltimateModeration instance, CommandSender sender, String... args) {
        if (args.length == 0) {
            ChatListener.setSlowModeOverride(0);
            sender.sendMessage(instance.getReferences().getPrefix() + instance.getLocale().getMessage("event.slowmode.disabled"));
            return ReturnType.SUCCESS;
        } else if (args.length != 1)
            return ReturnType.SYNTAX_ERROR;

        long delay = Methods.parseTime(args[0]);

        ChatListener.setSlowModeOverride(delay);

        Bukkit.getOnlinePlayers().forEach(player ->
                player.sendMessage(instance.getReferences().getPrefix() +
                        instance.getLocale().getMessage("event.slowmode.enabled", Methods.makeReadable(delay))));

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
        return "um.slowmode";
    }

    @Override
    public String getSyntax() {
        return "/slowmode [delay in seconds]";
    }

    @Override
    public String getDescription() {
        return "Allows you to slow down the chat. Accepts time in the format of 1s 5s 10s.";
    }
}
