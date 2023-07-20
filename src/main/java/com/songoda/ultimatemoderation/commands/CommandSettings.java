package com.songoda.ultimatemoderation.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.core.configuration.editor.PluginConfigGui;
import com.songoda.core.gui.GuiManager;
import com.songoda.ultimatemoderation.UltimateModeration;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandSettings extends AbstractCommand {
    private final UltimateModeration plugin;
    private final GuiManager guiManager;

    public CommandSettings(UltimateModeration plugin, GuiManager manager) {
        super(CommandType.PLAYER_ONLY, "settings");
        this.plugin = plugin;
        this.guiManager = manager;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        this.guiManager.showGUI((Player) sender, new PluginConfigGui(this.plugin));
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "um.admin";
    }

    @Override
    public String getSyntax() {
        return "/um settings";
    }

    @Override
    public String getDescription() {
        return "Edit UltimateModeration Settings.";
    }
}
