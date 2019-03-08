package com.songoda.ultimatemoderation.command.commands;

import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.command.AbstractCommand;
import com.songoda.ultimatemoderation.listeners.DeathListener;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class CommandRevive extends AbstractCommand {

    public CommandRevive() {
        super(true, true, "Revive");
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

        if (!(revive(player, sender))) return ReturnType.FAILURE;

        player.sendMessage(instance.getReferences().getPrefix() + instance.getLocale().getMessage("command.revive.revived"));
        sender.sendMessage(instance.getReferences().getPrefix() + instance.getLocale().getMessage("command.revive.success", player.getName()));
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(UltimateModeration instance, CommandSender sender, String... args) {
        if (args.length == 1) {
            List<String> players = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                players.add(player.getName());
            }
            return players;
        }
        return null;
    }

    public static boolean revive(Player player, CommandSender sender) {
        UltimateModeration instance = UltimateModeration.getInstance();
        List<ItemStack> drops = DeathListener.getLastDrop(player);

        if (drops == null) {
            sender.sendMessage(instance.getReferences().getPrefix() + instance.getLocale().getMessage("command.revive.noloot"));
            return false;
        }

        ItemStack[] dropArr = new ItemStack[drops.size()];
        dropArr = drops.toArray(dropArr);

        HashMap<Integer, ItemStack> leftOver = player.getInventory().addItem(dropArr);

        for (ItemStack item : leftOver.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), item);
        }
        return true;
    }

    @Override
    public String getPermissionNode() {
        return "um.revive";
    }

    @Override
    public String getSyntax() {
        return "/Revive <player>";
    }

    @Override
    public String getDescription() {
        return "Allows you to revive a player.";
    }
}
