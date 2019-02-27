package com.songoda.ultimatemoderation.listeners;

import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.command.commands.CommandFreeze;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

public class InventoryListener implements Listener {

    private UltimateModeration instance;

    public InventoryListener(UltimateModeration ultimateModeration) {
        this.instance = ultimateModeration;
    }

    @EventHandler
    public void onMove(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player)event.getWhoClicked();
        if (CommandFreeze.isFrozen(player)) {
            event.setCancelled(true);
            player.sendMessage(instance.getReferences().getPrefix() + instance.getLocale().getMessage("command.freeze.nope"));
        }
    }
}
