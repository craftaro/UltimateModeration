package com.songoda.ultimatemoderation.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.gui.MainGui;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandUltimateModeration extends AbstractCommand {

    private UltimateModeration instance;

    public CommandUltimateModeration(UltimateModeration instance) {
        super(CommandType.PLAYER_ONLY, "UltimateModeration");
        this.instance = instance;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        instance.getGuiManager().showGUI((Player) sender, new MainGui(instance, (Player) sender));
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
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
        return "Displays the moderation panel.";
    }
}
