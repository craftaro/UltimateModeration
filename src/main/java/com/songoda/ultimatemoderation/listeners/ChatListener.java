package com.songoda.ultimatemoderation.listeners;

import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.utils.Methods;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    private UltimateModeration instance;
    private boolean isChatToggled = true; // true means people can talk, false means muted

    public ChatListener(UltimateModeration ultimateModeration) {
        this.instance = ultimateModeration;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!isChatToggled && !player.hasPermission("um.togglechat.bypass")) {
            event.setCancelled(true);
            player.sendMessage(instance.getReferences().getPrefix() + Methods.formatText(instance.getLocale().getMessage("command.togglechat.muted")));
        }
    }

    public void setChatToggled(boolean toggled) {
        this.isChatToggled = toggled;
    }

}
