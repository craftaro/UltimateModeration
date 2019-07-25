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
            instance.getLocale().newMessage("That player does not exist or is not online.").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }

        if (!(revive(player, sender))) return ReturnType.FAILURE;

        instance.getLocale().getMessage("command.revive.revived").sendPrefixedMessage(player);
        instance.getLocale().getMessage("command.revive.success")
                .processPlaceholder("player", player.getName()).sendPrefixedMessage(sender);
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
            instance.getLocale().getMessage("command.revive.noloot").sendPrefixedMessage(sender);
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
