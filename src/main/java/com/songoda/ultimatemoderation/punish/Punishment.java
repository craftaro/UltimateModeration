package com.songoda.ultimatemoderation.punish;

import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.punish.player.PlayerPunishData;
import com.songoda.ultimatemoderation.utils.Methods;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Punishment {

    private final UUID uuid;

    private final PunishmentType punishmentType;
    private final long duration;
    private final String reason;

    public Punishment(PunishmentType punishmentType, long duration, String reason, UUID uuid) {
        this.punishmentType = punishmentType;
        this.duration = duration;
        this.reason = reason;
        this.uuid = uuid;
    }

    public Punishment(PunishmentType punishmentType, long duration, String reason) {
        this.punishmentType = punishmentType;
        this.duration = duration;
        this.reason = reason;
        this.uuid = UUID.randomUUID();
    }

    public Punishment(PunishmentType punishmentType, String reason) {
        this.punishmentType = punishmentType;
        this.duration = -1;
        this.reason = reason;
        this.uuid = UUID.randomUUID();
    }

    protected Punishment(Punishment punishment) {
        this.punishmentType = punishment.getPunishmentType();
        this.duration = punishment.getDuration();
        this.reason = punishment.getReason();
        this.uuid = punishment.getUUID();
    }

    public void execute(CommandSender punisher, OfflinePlayer victim) {
        UltimateModeration plugin = UltimateModeration.getInstance();

        if (!punisher.hasPermission("Um." + punishmentType)) {
            plugin.getLocale().getMessage("event.general.nopermission").sendPrefixedMessage(punisher);
            return;
        }

        PlayerPunishData playerPunishData = plugin.getPunishmentManager().getPlayer(victim);
        switch (punishmentType) {
            case BAN:
                if (!playerPunishData.getActivePunishments(PunishmentType.BAN).isEmpty()) {
                    plugin.getLocale().getMessage("event.ban.already").sendPrefixedMessage(punisher);
                    return;
                }
                if (victim.isOnline()) {
                    victim.getPlayer().kickPlayer(plugin.getLocale()
                            .getMessage("event.ban.message")
                            .processPlaceholder("reason", reason == null ? "" : reason)
                            .processPlaceholder("duration", Methods.makeReadable(duration)).getMessage());
                }
                break;
            case MUTE:
                if (!playerPunishData.getActivePunishments(PunishmentType.MUTE).isEmpty()) {
                    plugin.getLocale().getMessage("event.mute.already").sendPrefixedMessage(punisher);
                    return;
                }
                sendMessage(victim);
                break;
            case KICK:
                if (victim.isOnline()) {
                    victim.getPlayer().kickPlayer(plugin.getLocale()
                            .getMessage("event.kick.message")
                            .processPlaceholder("reason", reason == null ? "" : reason)
                            .processPlaceholder("duration", Methods.makeReadable(duration)).getMessage());
                }
                break;
            case WARNING:
                sendMessage(victim);
                break;
        }

        String punishSuccess = plugin.getLocale()
                .getMessage("event." + punishmentType.name().toLowerCase() + ".success")
                .processPlaceholder("player", victim.getName())
                .getPrefixedMessage();

        if (reason != null)
            punishSuccess += plugin.getLocale().getMessage("event.punish.reason")
                    .processPlaceholder("reason", reason).getMessage();

        if (duration != -1)
            punishSuccess += plugin.getLocale().getMessage("event.punish.theirduration")
                    .processPlaceholder("duration", Methods.makeReadable(duration)).getMessage();

        punisher.sendMessage(punishSuccess + Methods.formatText("&7."));

        playerPunishData.addPunishment(apply(victim, punisher));
    }

    public void sendMessage(OfflinePlayer offlineVictim) {
        if (!offlineVictim.isOnline()) return;
        Player victim = offlineVictim.getPlayer();
        UltimateModeration plugin = UltimateModeration.getInstance();

        String punishSuccess = plugin.getLocale()
                .getMessage("event." + punishmentType.name().toLowerCase() + ".message").getPrefixedMessage();

        if (reason != null)
            punishSuccess += plugin.getLocale().getMessage("event.punish.reason")
                    .processPlaceholder("reason", reason).getMessage();

        if (duration != -1)
            punishSuccess += plugin.getLocale().getMessage("event.punish.yourduration")
                    .processPlaceholder("duration", Methods.makeReadable(duration)).getMessage();

        victim.sendMessage(punishSuccess + Methods.formatText("&7."));
    }

    public UUID getUUID() {
        return uuid;
    }

    public PunishmentType getPunishmentType() {
        return this.punishmentType;
    }

    public long getDuration() {
        return this.duration;
    }

    public String getReason() {
        return this.reason;
    }

    private AppliedPunishment apply(OfflinePlayer player, CommandSender punisher) {
        return new AppliedPunishment(this, player.getUniqueId(),
                punisher == null ? null : punisher instanceof OfflinePlayer ? ((OfflinePlayer)punisher).getUniqueId() : null, System.currentTimeMillis() + this.duration);
    }

}
