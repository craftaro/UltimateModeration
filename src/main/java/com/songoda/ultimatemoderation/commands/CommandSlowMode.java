package com.songoda.ultimatemoderation.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.core.utils.TimeUtils;
import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.listeners.ChatListener;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CommandSlowMode extends AbstractCommand {
    private final UltimateModeration plugin;

    public CommandSlowMode(UltimateModeration plugin) {
        super(CommandType.CONSOLE_OK, "Slowmode");
        this.plugin = plugin;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        if (args.length == 0) {
            ChatListener.setSlowModeOverride(0);
            this.plugin.getLocale().getMessage("event.slowmode.disabled").sendPrefixedMessage(sender);
            return ReturnType.SUCCESS;
        } else if (args.length != 1) {
            return ReturnType.SYNTAX_ERROR;
        }

        long delay = TimeUtils.parseTime(args[0]);

        ChatListener.setSlowModeOverride(delay);

        Bukkit.getOnlinePlayers().forEach(player ->
                this.plugin.getLocale().getMessage("event.slowmode.enabled")
                        .processPlaceholder("delay", TimeUtils.makeReadable(delay)).sendPrefixedMessage(player));

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
