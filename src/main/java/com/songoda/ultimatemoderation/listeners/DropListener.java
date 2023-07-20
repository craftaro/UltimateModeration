package com.songoda.ultimatemoderation.listeners;

import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.moderate.moderations.FreezeModeration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

public class DropListener implements Listener {
    private final UltimateModeration instance;

    public DropListener(UltimateModeration ultimateModeration) {
        this.instance = ultimateModeration;
    }

    @EventHandler
    public void onMove(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (FreezeModeration.isFrozen(player)) {
            event.setCancelled(true);
            this.instance.getLocale().getMessage("command.freeze.nope").sendPrefixedMessage(player);
        }
    }
}
