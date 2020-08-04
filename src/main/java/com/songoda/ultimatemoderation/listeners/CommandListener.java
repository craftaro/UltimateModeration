package com.songoda.ultimatemoderation.listeners;

import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.moderate.moderations.SpyModeration;
import com.songoda.ultimatemoderation.punish.AppliedPunishment;
import com.songoda.ultimatemoderation.punish.PunishmentType;
import com.songoda.ultimatemoderation.settings.Settings;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;

public class CommandListener implements Listener {

    private UltimateModeration instance;

    public CommandListener(UltimateModeration ultimateModeration) {
        this.instance = ultimateModeration;
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        String command = event.getMessage();

        List<AppliedPunishment> appliedPunishments = instance.getPunishmentManager().getPlayer(player).getActivePunishments(PunishmentType.MUTE);
        if (!appliedPunishments.isEmpty()) {
            if (Settings.MUTE_DISABLED_COMMANDS.getStringList().stream()
                    .anyMatch(s -> command.toUpperCase().startsWith("/" + s.toUpperCase())))
                event.setCancelled(true);

        }

        List<String> blockedCommands = Settings.BLOCKED_COMMANDS.getStringList();

        for (String cmd : blockedCommands) {
            if (command.toUpperCase().startsWith("/" + cmd.toUpperCase())
                    && (command.toUpperCase().endsWith(cmd.toUpperCase()) || (command.contains(" ") && command.split(" ")[0].toUpperCase().endsWith(cmd.toUpperCase())))
                    && !player.hasPermission("um.commandblock.bypass")) {
                event.setCancelled(true);
                event.setMessage("-");
                instance.getLocale().getMessage("event.command.blocked").sendPrefixedMessage(player);
            }
        }

        if (!player.hasPermission("um.commandspy.immune")) {
            for (Player pl : Bukkit.getOnlinePlayers()) {
                if (pl != player && pl.hasPermission("um.commandspy") && SpyModeration.isSpying(pl))
                    instance.getLocale().getMessage("command.commandspy.deny")
                            .processPlaceholder("player", player.getName())
                            .processPlaceholder("command", command)
                            .sendPrefixedMessage(pl);
            }
        }
    }
}
