package com.songoda.ultimatemoderation.listeners;

import com.songoda.skyblock.api.event.player.PlayerIslandChatEvent;
import com.songoda.ultimatemoderation.UltimateModeration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SkyBlockListener implements Listener {

    private UltimateModeration instance;

    public SkyBlockListener(UltimateModeration ultimateModeration) {
        this.instance = ultimateModeration;
    }

    @EventHandler
    public void onIslandChat(PlayerIslandChatEvent event) {
        if (!ChatListener.onChat(event.getPlayer(), event.getMessage()))
            event.setCancelled(true);
    }

}
