package com.songoda.ultimatemoderation.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DeathListener implements Listener {
    private static final Map<UUID, List<ItemStack>> playerDrops = new HashMap<>();

    public static List<ItemStack> getLastDrop(Player player) {
        return playerDrops.get(player.getUniqueId());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        playerDrops.put(player.getUniqueId(), event.getDrops());
    }
}
