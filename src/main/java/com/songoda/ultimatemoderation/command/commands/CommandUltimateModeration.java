package com.songoda.ultimatemoderation.command.commands;

import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.command.AbstractCommand;
import com.songoda.ultimatemoderation.gui.GUIPlayers;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandUltimateModeration extends AbstractCommand {

    public CommandUltimateModeration() {
        super(true, false, "UltimateModeration");
    }

    @Override
    protected ReturnType runCommand(UltimateModeration instance, CommandSender sender, String... args) {
        new GUIPlayers(instance, (Player) sender);

        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(UltimateModeration instance, CommandSender sender, String... args) {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "um.moderate";
    }

    @Override
    public String getSyntax() {
        return "/um";
    }

    @Override
    public String getDescription() {
        return "Displays this page.";
    }
}
