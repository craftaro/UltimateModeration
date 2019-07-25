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
        super(true, false, "CommandSpy");
    }

    public static boolean isSpying(Player player) {
        return !inSpy.contains(player.getUniqueId());
    }

    @Override
    protected ReturnType runCommand(UltimateModeration instance, CommandSender sender, String... args) {
        Player player = ((Player) sender);

        if (inSpy.contains(player.getUniqueId())) {
            inSpy.remove(player.getUniqueId());
            instance.getLocale().getMessage("command.commandspy.toggleOn").sendPrefixedMessage(player);
        } else {
            inSpy.add(player.getUniqueId());
            instance.getLocale().getMessage("command.commandspy.toggleOff").sendPrefixedMessage(player);
        }
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(UltimateModeration instance, CommandSender sender, String... args) {
        return null;
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
