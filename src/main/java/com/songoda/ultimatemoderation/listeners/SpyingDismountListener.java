package com.songoda.ultimatemoderation.listeners;

import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.command.commands.CommandSpy;
import com.songoda.ultimatemoderation.command.commands.CommandVanish;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpyingDismountListener implements Listener {

    private Map<UUID, GameMode> gamemodes = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDismountEvent(EntityDismountEvent event) {
        if (!(event.getDismounted() instanceof Player)) return;
        if (!(event.getEntity() instanceof Player)) return;
        if (CommandSpy.isSpying((((Player) event.getEntity()).getPlayer()))) {
            Player player = (Player) event.getEntity();
            gamemodes.put(player.getUniqueId(), player.getGameMode());
            player.setGameMode(GameMode.SPECTATOR);
            Bukkit.getScheduler().scheduleSyncDelayedTask(UltimateModeration.getInstance(), () ->
                    player.setSpectatorTarget(event.getDismounted()), 5L);
        }
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (player.isSneaking() || !CommandSpy.isSpying(player) || player.getGameMode() != GameMode.SPECTATOR) return;
        CommandSpy.Spy spyingEntry = CommandSpy.getSpying().remove(player.getUniqueId());
        player.teleport(spyingEntry.getLastLocation());
        if (spyingEntry.isVanishApplied() && CommandVanish.isVanished(player))
            CommandVanish.vanish(player);
        player.setGameMode(gamemodes.get(player.getUniqueId()));

        UltimateModeration.getInstance().getLocale().getMessage("command.spy.returned").sendPrefixedMessage(player);
    }
}
