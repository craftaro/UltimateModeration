package com.songoda.ultimatemoderation.listeners;

import com.songoda.ultimatemoderation.command.commands.CommandSpy;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.spigotmc.event.entity.EntityDismountEvent;

public class SpyingDismountListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDismountEvent(EntityDismountEvent e) {
        if (!(e.getDismounted() instanceof Player)) return;
        if (!(e.getEntity() instanceof Player)) return;
        if (CommandSpy.isSpying((((Player) e.getEntity()).getPlayer()))) {
            e.setCancelled(true);
        }
    }
}
