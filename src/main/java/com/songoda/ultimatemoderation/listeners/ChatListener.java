package com.songoda.ultimatemoderation.listeners;

import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.punish.AppliedPunishment;
import com.songoda.ultimatemoderation.punish.PunishmentType;
import com.songoda.ultimatemoderation.staffchat.StaffChannel;
import com.songoda.ultimatemoderation.utils.Methods;
import com.songoda.ultimatemoderation.settings.Settings;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!onChat(player, event.getMessage()))
            event.setCancelled(true);
    }

    public static boolean onChat(Player player, String message) {
        UltimateModeration instance = UltimateModeration.getInstance();

        long slowmode = slowModeOverride == 0 ? Methods.parseTime(Settings.SLOW_MODE.getString()) : slowModeOverride;

        if (!player.hasPermission("um.slowmode.bypass") && slowmode != 0) {
            List<Log> chats = chatLog.stream().filter(log -> log.player == player.getUniqueId()).collect(Collectors.toList());
            if (chats.size() != 0) {
                Log last = chats.get(chats.size() - 1);
                if ((System.currentTimeMillis() - last.sent) < slowmode) {
                    return false;
                }
            }
        }

        boolean isCancelled = false;

        for (StaffChannel channel : instance.getStaffChatManager().getChats().values()) {
            if (!channel.listMembers().contains(player.getUniqueId())) continue;
            isCancelled = true;
            channel.processMessage(message, player);
        }

        if (!isChatToggled && !player.hasPermission("um.togglechat.bypass")) {
            isCancelled = true;
            instance.getLocale().getMessage("command.togglechat.muted").sendPrefixedMessage(player);
        }

        List<AppliedPunishment> appliedPunishments = instance.getPunishmentManager().getPlayer(player).getActivePunishments(PunishmentType.MUTE);
        if (!appliedPunishments.isEmpty()) {
            appliedPunishments.get(0).sendMessage(player);
            isCancelled = true;
        }


        // Log chat.
        chatLog.add(new Log(player.getUniqueId(), System.currentTimeMillis(), message));

        return !isCancelled;
    }

    public static void setSlowModeOverride(long slowModeOverride) {
        ChatListener.slowModeOverride = slowModeOverride;
    }

    public static List<Log> getLogs() {
        return new ArrayList<>(chatLog);
    }

    public static class Log {

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
