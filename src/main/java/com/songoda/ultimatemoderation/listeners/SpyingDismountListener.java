package com.songoda.ultimatemoderation.listeners;

import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.commands.CommandSpy;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpyingDismountListener implements Listener {

    private static Map<UUID, GameMode> gamemodes = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDismountEvent(EntityDismountEvent event) {
        if (!(event.getDismounted() instanceof Player)) return;
        if (!(event.getEntity() instanceof Player)) return;
        if (CommandSpy.isSpying((((Player) event.getEntity()).getPlayer()))) {
            Player player = (Player) event.getEntity();
            gamemodes.put(player.getUniqueId(), player.getGameMode());
            player.setGameMode(GameMode.SPECTATOR);
            Bukkit.getScheduler().scheduleSyncDelayedTask(UltimateModeration.getInstance(), () -> {

                if (player.getGameMode() == GameMode.SPECTATOR)
                    player.setSpectatorTarget(event.getDismounted());
            }, 5L);
        }
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (player.isSneaking() || !CommandSpy.isSpying(player) || player.getGameMode() != GameMode.SPECTATOR) return;
        CommandSpy.spy(null, player);
    }

    public static Map<UUID, GameMode> getGamemodes() {
        return Collections.unmodifiableMap(gamemodes);
    }
}
