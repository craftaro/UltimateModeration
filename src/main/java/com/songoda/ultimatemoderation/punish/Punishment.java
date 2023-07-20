package com.songoda.ultimatemoderation.punish;

import com.songoda.core.utils.TextUtils;
import com.songoda.core.utils.TimeUtils;
import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.punish.player.PlayerPunishData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Punishment {
    private int id;

    private final PunishmentType punishmentType;
    private final long duration;
    private final String reason;

    public Punishment(PunishmentType punishmentType, long duration, String reason, int id) {
        this.punishmentType = punishmentType;
        this.duration = duration;
        this.reason = reason;
        this.id = id;
    }

    public Punishment(PunishmentType punishmentType, long duration, String reason) {
        this.punishmentType = punishmentType;
        this.duration = duration;
        this.reason = reason;
    }

    public Punishment(PunishmentType punishmentType, String reason) {
        this.punishmentType = punishmentType;
        this.duration = 0;
        this.reason = reason;
    }

    protected Punishment(Punishment punishment) {
        this.punishmentType = punishment.getPunishmentType();
        this.duration = punishment.getDuration();
        this.reason = punishment.getReason();
    }

    public void execute(CommandSender punisher, OfflinePlayer victim) {
        UltimateModeration plugin = UltimateModeration.getInstance();

        if (!punisher.hasPermission("Um." + this.punishmentType)) {
            plugin.getLocale().getMessage("event.general.nopermission").sendPrefixedMessage(punisher);
            return;
        }

        PlayerPunishData playerPunishData = plugin.getPunishmentManager().getPlayer(victim);
        switch (this.punishmentType) {
            case BAN:
                if (!playerPunishData.getActivePunishments(PunishmentType.BAN).isEmpty()) {
                    plugin.getLocale().getMessage("event.ban.already").sendPrefixedMessage(punisher);
                    return;
                }
                if (victim.isOnline()) {
                    Bukkit.getScheduler().runTask(plugin, () -> victim.getPlayer().kickPlayer(plugin.getLocale()
                            .getMessage("event.ban.message")
                            .processPlaceholder("reason", this.reason == null ? "" : this.reason)
                            .processPlaceholder("duration", TimeUtils.makeReadable(this.duration)).getMessage()));
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
                    Bukkit.getScheduler().runTask(plugin, () -> victim.getPlayer().kickPlayer(plugin.getLocale()
                            .getMessage("event.kick.message")
                            .processPlaceholder("reason", this.reason == null ? "" : this.reason).getMessage()));
                }
                break;
            case WARNING:
                sendMessage(victim);
                break;
        }

        String punishSuccess = plugin.getLocale()
                .getMessage("event." + this.punishmentType.name().toLowerCase() + ".success")
                .processPlaceholder("player", victim.getName())
                .getPrefixedMessage();

        if (this.reason != null) {
            punishSuccess += plugin.getLocale().getMessage("event.punish.reason")
                    .processPlaceholder("reason", this.reason).getMessage();
        }

        if (this.duration != -1 && this.duration != 0) {
            punishSuccess += plugin.getLocale().getMessage("event.punish.theirduration")
                    .processPlaceholder("duration", TimeUtils.makeReadable(this.duration)).getMessage();
        }

        punisher.sendMessage(punishSuccess + TextUtils.formatText("&7."));

        AppliedPunishment appliedPunishment = apply(victim, punisher);
        if (this.duration != 0) {
            playerPunishData.addPunishment(appliedPunishment);
        } else {
            appliedPunishment.expire();
            playerPunishData.addExpiredPunishment(appliedPunishment);
        }
        plugin.getDataManager().createAppliedPunishment(appliedPunishment);
    }

    public void sendMessage(OfflinePlayer offlineVictim) {
        if (!offlineVictim.isOnline()) {
            return;
        }
        Player victim = offlineVictim.getPlayer();
        UltimateModeration plugin = UltimateModeration.getInstance();

        String punishSuccess = plugin.getLocale()
                .getMessage("event." + this.punishmentType.name().toLowerCase() + ".message").getPrefixedMessage();

        if (this.reason != null) {
            punishSuccess += plugin.getLocale().getMessage("event.punish.reason")
                    .processPlaceholder("reason", this.reason).getMessage();
        }

        if (this.duration != -1) {
            punishSuccess += plugin.getLocale().getMessage("event.punish.yourduration")
                    .processPlaceholder("duration", TimeUtils.makeReadable(this.duration)).getMessage();
        }

        victim.sendMessage(punishSuccess + TextUtils.formatText("&7."));
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
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
                punisher == null ? null : punisher instanceof OfflinePlayer ? ((OfflinePlayer) punisher).getUniqueId() : null, System.currentTimeMillis() + this.duration);
    }

}
