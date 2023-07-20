package com.songoda.ultimatemoderation.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.ultimatemoderation.UltimateModeration;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandRandomPlayer extends AbstractCommand {
    private final UltimateModeration plugin;

    public CommandRandomPlayer(UltimateModeration plugin) {
        super(CommandType.PLAYER_ONLY, "RandomPlayer");
        this.plugin = plugin;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        Collections.shuffle(players);
        players.remove(sender);

        if (players.size() == 0) {
            this.plugin.getLocale().newMessage("&cYou are the only one online!").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }

        ((Player) sender).teleport(players.get(0).getLocation());
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "um.randomplayer";
    }

    @Override
    public String getSyntax() {
        return "/RandomPlayer";
    }

    @Override
    public String getDescription() {
        return "Allows you to randomly teleport to a player on the server.";
    }
}
