package com.songoda.ultimatemoderation.listeners;

import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.moderate.moderations.FreezeModeration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryListener implements Listener {
    private final UltimateModeration plugin;

    public InventoryListener(UltimateModeration ultimateModeration) {
        this.plugin = ultimateModeration;
    }

    @EventHandler
    public void onMove(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        if (FreezeModeration.isFrozen(player)) {
            event.setCancelled(true);
            this.plugin.getLocale().getMessage("command.freeze.nope").sendPrefixedMessage(player);
        }
    }
}
