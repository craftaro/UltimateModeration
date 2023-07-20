package com.songoda.ultimatemoderation.listeners;

import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.moderate.moderations.SpyModeration;
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
    private static final Map<UUID, GameMode> gamemodes = new HashMap<>();

    private final UltimateModeration plugin;

    public SpyingDismountListener(UltimateModeration plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDismountEvent(EntityDismountEvent event) {
        if (!(event.getDismounted() instanceof Player)) {
            return;
        }
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        if (SpyModeration.isSpying((((Player) event.getEntity()).getPlayer()))) {
            Player player = (Player) event.getEntity();
            gamemodes.put(player.getUniqueId(), player.getGameMode());
            player.setGameMode(GameMode.SPECTATOR);
            Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, () -> {
                if (player.getGameMode() == GameMode.SPECTATOR) {
                    player.setSpectatorTarget(event.getDismounted());
                }
            }, 5L);
        }
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (player.isSneaking() || !SpyModeration.isSpying(player) || player.getGameMode() != GameMode.SPECTATOR) {
            return;
        }

        SpyModeration.spy(null, player);
    }

    public static Map<UUID, GameMode> getGamemodes() {
        return Collections.unmodifiableMap(gamemodes);
    }
}
