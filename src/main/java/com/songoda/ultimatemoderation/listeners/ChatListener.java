package com.songoda.ultimatemoderation.listeners;

import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.punish.AppliedPunishment;
import com.songoda.ultimatemoderation.punish.PunishmentType;
import com.songoda.ultimatemoderation.staffchat.StaffChannel;
import com.songoda.ultimatemoderation.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ChatListener implements Listener {

    private static boolean isChatToggled = true; // true means people can talk, false means muted
    private UltimateModeration instance;

    public ChatListener(UltimateModeration ultimateModeration) {
        this.instance = ultimateModeration;
    }

    public static void setChatToggled(boolean toggled) {
        isChatToggled = toggled;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

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
    }

}
