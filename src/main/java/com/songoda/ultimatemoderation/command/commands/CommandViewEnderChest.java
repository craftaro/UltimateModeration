package com.songoda.ultimatemoderation.command.commands;

import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.command.AbstractCommand;
import com.songoda.ultimatemoderation.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class CommandViewEnderChest extends AbstractCommand {

    public CommandViewEnderChest() {
        super(true, true,"ViewEnderChest");
    }

    @Override
    protected ReturnType runCommand(UltimateModeration instance, CommandSender sender, String... args) {

        if (args.length != 1)
            return ReturnType.SYNTAX_ERROR;

        Player player = Bukkit.getPlayer(args[0]);

        if (player == null) {
            sender.sendMessage(instance.getReferences().getPrefix() + "That player does not exist or is not online.");
            return ReturnType.FAILURE;
        }

        ((Player)sender).openInventory(player.getEnderChest());
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(UltimateModeration instance, CommandSender sender, String... args) {
        return null;
    }

    private boolean isForced(String[] args) {
        return args.length != 0 && args[0].equals("force");
    }

    @Override
    public String getPermissionNode() {
        return "um.viewenderchest";
    }

    @Override
    public String getSyntax() {
        return "/ViewEnderChest <player>";
    }

    @Override
    public String getDescription() {
        return "Allows you to see inside of a players enderchest.\n";
    }
}
