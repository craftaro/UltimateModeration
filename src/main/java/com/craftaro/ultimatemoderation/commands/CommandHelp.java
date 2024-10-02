package com.craftaro.ultimatemoderation.commands;

import com.craftaro.core.commands.AbstractCommand;
import com.craftaro.core.utils.TextUtils;
import com.craftaro.ultimatemoderation.UltimateModeration;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CommandHelp extends AbstractCommand {
    private final UltimateModeration plugin;

    public CommandHelp(UltimateModeration plugin) {
        super(CommandType.CONSOLE_OK, "help");
        this.plugin = plugin;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        sender.sendMessage("");
        this.plugin.getLocale().getMessage("&7Version " + this.plugin.getDescription().getVersion() + " Created with <3 by &5&l&oSongoda")
                .sendPrefixedMessage(sender);
        sender.sendMessage("");
        sender.sendMessage(TextUtils.formatText("&7Welcome to UltimateModeration! To get started try using the /um command to access the moderation panel."));
        sender.sendMessage("");
        sender.sendMessage(TextUtils.formatText("&6Commands:"));
        for (AbstractCommand command : this.plugin.getCommandManager().getAllCommands()) {
            if (command.getPermissionNode() == null || sender.hasPermission(command.getPermissionNode())) {
                sender.sendMessage(TextUtils.formatText("&8 - &a" + command.getSyntax() + "&7 - " + command.getDescription()));
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
