package com.songoda.ultimatemoderation.tasks;

import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.listeners.ChatListener;
import com.songoda.ultimatemoderation.utils.Methods;
import com.songoda.ultimatemoderation.utils.ServerVersion;
import com.songoda.ultimatemoderation.utils.settings.Setting;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.stream.Collectors;

public class SlowModeTask extends BukkitRunnable {

    private static SlowModeTask instance;
    private static UltimateModeration plugin;

    private SlowModeTask(UltimateModeration plug) {
        plugin = plug;
    }

    public static SlowModeTask startTask(UltimateModeration plug) {
        plugin = plug;
        if (instance == null) {
            instance = new SlowModeTask(plugin);
            instance.runTaskTimer(plugin, 0, 1);
        }

        return instance;
    }

    @Override
    public void run() {
        long slowmode = ChatListener.getSlowModeOverride() == 0 ? Methods.parseTime(Setting.SLOW_MODE.getString()) : ChatListener.getSlowModeOverride();

        if (slowmode == 0) return;

        List<ChatListener.Log> logs = ChatListener.getLogs();

        Bukkit.getOnlinePlayers().forEach(player -> {
            if (player.hasPermission("um.slowmode.bypass")) return;

            List<ChatListener.Log> chats = logs.stream().filter(log -> log.getPlayer() == player.getUniqueId()).collect(Collectors.toList());
            if (chats.size() == 0) return;
            ChatListener.Log last = chats.get(chats.size() - 1);

            if ((System.currentTimeMillis() - last.getSent()) < (slowmode + 1000)) {
                int remaining = (int)((slowmode / 1000) - (System.currentTimeMillis() - last.getSent()) / 1000);
                if (plugin.isServerVersionAtLeast(ServerVersion.V1_9))
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(remaining == 0
                        ? plugin.getLocale().getMessage("event.slowmode.done").getMessage()
                        : plugin.getLocale().getMessage("event.slowmode.wait").processPlaceholder("delay", remaining).getMessage()));
            }

        });

    }

}