package com.songoda.ultimatemoderation.command.commands;

import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.command.AbstractCommand;
import com.songoda.ultimatemoderation.listeners.DeathListener;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class CommandRevive extends AbstractCommand {

    private static List<UUID> frozen = new ArrayList<>();

    public CommandRevive() {
        super(true, true,"Revive");
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

        List<ItemStack> drops = DeathListener.getLastDrop(player);

        if (drops == null) {
            sender.sendMessage(instance.getReferences().getPrefix() + instance.getLocale().getMessage("command.revive.noloot"));
            return ReturnType.FAILURE;
        }

        ItemStack[] dropArr = new ItemStack[drops.size()];
        dropArr = drops.toArray(dropArr);

        HashMap<Integer, ItemStack> leftOver = player.getInventory().addItem(dropArr);

        for (ItemStack item : leftOver.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), item);
        }

        player.sendMessage(instance.getReferences().getPrefix() + instance.getLocale().getMessage("command.revive.noloot"));
        sender.sendMessage(instance.getReferences().getPrefix() + instance.getLocale().getMessage("command.revive.success", player.getName()));
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(UltimateModeration instance, CommandSender sender, String... args) {
        return null;
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
