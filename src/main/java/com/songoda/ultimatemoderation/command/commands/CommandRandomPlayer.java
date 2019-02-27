package com.songoda.ultimatemoderation.command.commands;

import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.command.AbstractCommand;
import com.songoda.ultimatemoderation.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class CommandRandomPlayer extends AbstractCommand {

    public CommandRandomPlayer() {
        super(true, false,"RandomPlayer");
    }

    @Override
    protected ReturnType runCommand(UltimateModeration instance, CommandSender sender, String... args) {
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        Collections.shuffle(players);
        players.remove(sender);

        if (players.size() == 0) {
            sender.sendMessage(Methods.formatText(instance.getReferences().getPrefix() + "&c You are the only one online!"));
            return ReturnType.FAILURE;
        }

        ((Player)sender).teleport(players.get(0).getLocation());
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(UltimateModeration instance, CommandSender sender, String... args) {
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
        return "Allows you to randomly teleport to  a player on the server.";
    }
}
