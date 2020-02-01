package com.songoda.ultimatemoderation.listeners;

import com.songoda.skyblock.api.event.player.PlayerIslandChatEvent;
import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.staffchat.StaffChatManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.List;

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
