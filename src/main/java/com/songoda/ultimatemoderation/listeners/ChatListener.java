package com.songoda.ultimatemoderation.listeners;

import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.punish.AppliedPunishment;
import com.songoda.ultimatemoderation.punish.PunishmentType;
import com.songoda.ultimatemoderation.staffchat.StaffChannel;
import com.songoda.ultimatemoderation.utils.Methods;
import com.songoda.ultimatemoderation.utils.settings.Setting;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.*;
import java.util.stream.Collectors;

public class ChatListener implements Listener {

    private static long slowModeOverride = 0;

    private static boolean isChatToggled = true; // true means people can talk, false means muted
    private UltimateModeration instance;

    public ChatListener(UltimateModeration ultimateModeration) {
        this.instance = ultimateModeration;
    }

    public static void setChatToggled(boolean toggled) {
        isChatToggled = toggled;
    }

    private static List<Log> chatLog = new ArrayList<>();

    public static long getSlowModeOverride() {
        return slowModeOverride;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        long slowmode = slowModeOverride == 0 ? Methods.parseTime(Setting.SLOW_MODE.getString()) : slowModeOverride;

        if (!player.hasPermission("um.slowmode.bypass") && slowmode != 0) {
            List<Log> chats = chatLog.stream().filter(log -> log.player == player.getUniqueId()).collect(Collectors.toList());
            if (chats.size() != 0) {
                Log last = chats.get(chats.size() - 1);
                if ((System.currentTimeMillis() - last.sent) < slowmode) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        for (StaffChannel channel : instance.getStaffChatManager().getChats().values()) {
            if (!channel.listMembers().contains(player.getUniqueId())) continue;
            event.setCancelled(true);
            channel.processMessage(event.getMessage(), player);
        }

        if (!isChatToggled && !player.hasPermission("um.togglechat.bypass")) {
            event.setCancelled(true);
            player.sendMessage(instance.getReferences().getPrefix() + Methods.formatText(instance.getLocale().getMessage("command.togglechat.muted")));
        }

        List<AppliedPunishment> appliedPunishments = instance.getPunishmentManager().getPlayer(player).getActivePunishments(PunishmentType.MUTE);
        if (!appliedPunishments.isEmpty()) {
            appliedPunishments.get(0).sendMessage(player);
            event.setCancelled(true);
        }


        // Log chat.
        chatLog.add(new Log(player.getUniqueId(), System.currentTimeMillis(), event.getMessage()));

    }

    public static void setSlowModeOverride(long slowModeOverride) {
        ChatListener.slowModeOverride = slowModeOverride;
    }

    public static List<Log> getLogs() {
        return new ArrayList<>(chatLog);
    }

    public class Log {

        private UUID player;
        private long sent;
        private String message;

        Log(UUID player, long sent, String message) {
            this.player = player;
            this.sent = sent;
            this.message = message;
        }

        public UUID getPlayer() {
            return player;
        }

        public long getSent() {
            return sent;
        }

        public String getMessage() {
            return message;
        }
    }

}
