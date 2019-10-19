package com.songoda.ultimatemoderation.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.ultimatemoderation.UltimateModeration;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CommandCommandSpy extends AbstractCommand {

    private UltimateModeration instance;
    private static List<UUID> inSpy = new ArrayList<>();

    public CommandCommandSpy(UltimateModeration instance) {
        super(CommandType.CONSOLE_OK, "CommandSpy");
        this.instance = instance;
    }

    public static boolean isSpying(Player player) {
        return !inSpy.contains(player.getUniqueId());
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
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
    protected List<String> onTab(CommandSender sender, String... args) {
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
