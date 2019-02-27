package com.songoda.ultimatemoderation.listeners;

import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.command.commands.CommandFreeze;
import com.songoda.ultimatemoderation.command.commands.CommandVanish;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class MoveListener implements Listener {

    private UltimateModeration instance;

    public MoveListener(UltimateModeration ultimateModeration) {
        this.instance = ultimateModeration;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (CommandFreeze.isFrozen(player)) {
            event.setCancelled(true);
            player.sendMessage(instance.getReferences().getPrefix() + instance.getLocale().getMessage("command.freeze.nope"));
        }
    }
}
