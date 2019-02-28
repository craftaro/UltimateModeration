package com.songoda.ultimatemoderation.listeners;

import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.command.commands.CommandFreeze;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DeathListener implements Listener {

    private static Map<UUID, List<ItemStack>> playerDrops = new HashMap<>();

    private UltimateModeration instance;

    public DeathListener(UltimateModeration ultimateModeration) {
        this.instance = ultimateModeration;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        playerDrops.put(player.getUniqueId(), event.getDrops());
    }

    public static List<ItemStack> getLastDrop(Player player) {
        return playerDrops.get(player.getUniqueId());
    }
}
