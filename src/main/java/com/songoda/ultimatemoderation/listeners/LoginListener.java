package com.songoda.ultimatemoderation.listeners;

import com.songoda.core.utils.TimeUtils;
import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.commands.CommandVanish;
import com.songoda.ultimatemoderation.punish.AppliedPunishment;
import com.songoda.ultimatemoderation.punish.PunishmentType;
import com.songoda.ultimatemoderation.punish.player.PlayerPunishData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.List;

public class LoginListener implements Listener {
    private final UltimateModeration instance;

    public LoginListener(UltimateModeration ultimateModeration) {
        this.instance = ultimateModeration;
    }

    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        PlayerPunishData playerPunishData = this.instance.getPunishmentManager().getPlayer(event.getUniqueId());

        List<AppliedPunishment> appliedPunishments = playerPunishData.getActivePunishments(PunishmentType.BAN);

        if (appliedPunishments.isEmpty()) {
            return;
        }

        AppliedPunishment appliedPunishment = playerPunishData.getActivePunishments(PunishmentType.BAN).get(0);

        event.setKickMessage(this.instance.getLocale().getMessage("event.ban.message")
                .processPlaceholder("reason", appliedPunishment.getReason() == null ? "" : appliedPunishment.getReason())
                .processPlaceholder("duration", TimeUtils.makeReadable(appliedPunishment.getTimeRemaining())).getMessage());

        event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_BANNED);

    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        CommandVanish.registerVanishedPlayers(player);
    }
}
