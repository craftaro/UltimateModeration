package com.songoda.ultimatemoderation.listeners;

import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.utils.Methods;
import com.songoda.ultimatemoderation.utils.SettingsManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.ArrayList;
import java.util.List;

public class CommandListener implements Listener {

    private UltimateModeration instance;

    public CommandListener(UltimateModeration ultimateModeration) {
        this.instance = ultimateModeration;
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        List<String> blockedCommands = SettingsManager.Setting.BLOCKED_COMMANDS.getStringList();

        for (String cmd : blockedCommands) {
            if (event.getMessage().toUpperCase().startsWith("/" + cmd.toUpperCase())
                    && !player.hasPermission("um.commandblock.bypass")) {
                event.setCancelled(true);
                event.setMessage("-");
                player.sendMessage(instance.getReferences().getPrefix() + instance.getLocale().getMessage("command.blocked"));
            }
        }
    }
}
