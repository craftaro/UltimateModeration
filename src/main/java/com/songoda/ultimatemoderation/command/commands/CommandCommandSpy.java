package com.songoda.ultimatemoderation.command.commands;

import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.command.AbstractCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CommandCommandSpy extends AbstractCommand {

    private static List<UUID> inSpy = new ArrayList<>();

    public CommandCommandSpy() {
        super(true, false,"CommandSpy");
    }

    @Override
    protected ReturnType runCommand(UltimateModeration instance, CommandSender sender, String... args) {
        Player player = ((Player)sender);

        if (inSpy.contains(player.getUniqueId())) {
            inSpy.remove(player.getUniqueId());
            player.sendMessage(instance.getReferences().getPrefix() + instance.getLocale().getMessage("command.commandspy.toggleOn"));
        } else {
            inSpy.add(player.getUniqueId());
            player.sendMessage(instance.getReferences().getPrefix() + instance.getLocale().getMessage("command.commandspy.toggleOff"));
        }
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(UltimateModeration instance, CommandSender sender, String... args) {
        return null;
    }

    public static boolean isSpying(Player player) {
        return !inSpy.contains(player.getUniqueId());
    }

    @Override
    public String getPermissionNode() {
        return "Um.commandspy";
    }

    @Override
    public String getSyntax() {
        return "/Commandspy";
    }

    @Override
    public String getDescription() {
        return "Allows you to see inside of a players enderchest.";
    }
}
