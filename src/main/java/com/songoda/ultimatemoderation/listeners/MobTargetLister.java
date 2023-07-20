package com.songoda.ultimatemoderation.listeners;

import com.songoda.ultimatemoderation.commands.CommandVanish;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

public class MobTargetLister implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMobTagetEvent(EntityTargetLivingEntityEvent e) {
        if (!(e.getTarget() instanceof Player)) {
            return;
        }
        if (!(e.getEntity() instanceof Monster)) {
            return;
        }

        if (CommandVanish.isVanished(((Player) e.getTarget()).getPlayer())) {
            e.setCancelled(true);
        }
    }
}
