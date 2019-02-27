package com.songoda.ultimatemoderation.command.commands;

import com.songoda.ultimatemoderation.Locale;
import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.command.AbstractCommand;
import com.songoda.ultimatemoderation.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandToggleChat extends AbstractCommand {

    /*
     * Chat is enabled by default ;)
     */
    private boolean toggled = true;

    public CommandToggleChat() {
        super(null, false, "togglechat");
    }

    @Override
    protected ReturnType runCommand(UltimateModeration instance, CommandSender sender, String... args) {
        toggled = !toggled;
        String prefix = instance.getReferences().getPrefix();

        Locale locale = instance.getLocale();
        String strToggledOn = locale.getMessage("command.togglechat.toggledOn");
        String strToggledOff = locale.getMessage("command.togglechat.toggledOff");
        String messageToSend = prefix + Methods.formatText(toggled ? strToggledOn : strToggledOff);

        instance.getChatListener().setChatToggled(toggled);

        for (Player player : Bukkit.getOnlinePlayers()) {

            player.sendMessage(messageToSend);

            if (!player.hasPermission(getPermissionNode() + ".bypass"))
                continue;

            player.sendMessage(Methods.formatText(locale.getMessage("command.togglechat.bypass")));

        }

        if (!(sender instanceof Player))
            sender.sendMessage(messageToSend);

        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(UltimateModeration instance, CommandSender sender, String... args) {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "um.togglechat";
    }

    @Override
    public String getSyntax() {
        return "/ToggleChat";
    }

    @Override
    public String getDescription() {
        return "Toggle chat for the entire server";
    }
}
