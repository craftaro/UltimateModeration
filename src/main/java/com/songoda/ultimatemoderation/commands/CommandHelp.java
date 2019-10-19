package com.songoda.ultimatemoderation.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.utils.Methods;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CommandHelp extends AbstractCommand {

    private UltimateModeration instance;

    public CommandHelp(UltimateModeration instance) {
        super(CommandType.CONSOLE_OK, "help");
        this.instance = instance;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        sender.sendMessage("");
        instance.getLocale().getMessage("&7Version " + instance.getDescription().getVersion() + " Created with <3 by &5&l&oSongoda")
                .sendPrefixedMessage(sender);
        sender.sendMessage("");
        sender.sendMessage(Methods.formatText("&7Welcome to UltimateModeration! To get started try using the /um command to access the moderation panel."));
        sender.sendMessage("");
        sender.sendMessage(Methods.formatText("&6Commands:"));
        for (AbstractCommand command : instance.getCommandManager().getAllCommands()) {
            if (command.getPermissionNode() == null || sender.hasPermission(command.getPermissionNode())) {
                sender.sendMessage(Methods.formatText("&8 - &a" + command.getSyntax() + "&7 - " + command.getDescription()));
            }
        }
        sender.sendMessage("");

        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return null;
    }

    @Override
    public String getSyntax() {
        return "/um help";
    }

    @Override
    public String getDescription() {
        return "Displays this page.";
    }
}
