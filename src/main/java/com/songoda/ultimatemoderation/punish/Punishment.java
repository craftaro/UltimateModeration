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
            punisher.sendMessage(plugin.getReferences().getPrefix() + plugin.getLocale().getMessage("event.general.nopermission"));
            return;
        }

        PlayerPunishData playerPunishData = plugin.getPunishmentManager().getPlayer(victim);
        switch (punishmentType) {
            case BAN:
                if (!playerPunishData.getActivePunishments(PunishmentType.BAN).isEmpty()) {
                    punisher.sendMessage(plugin.getReferences().getPrefix()
                            + plugin.getLocale().getMessage("event.ban.already"));
                    return;
                }
                if (victim.isOnline()) {
                    victim.getPlayer().kickPlayer(plugin.getLocale().getMessage("event.ban.message",
                            reason == null ? "" : reason,
                            Methods.makeReadable(duration)));
                }
                break;
            case MUTE:
                if (!playerPunishData.getActivePunishments(PunishmentType.MUTE).isEmpty()) {
                    punisher.sendMessage(plugin.getReferences().getPrefix()
                            + plugin.getLocale().getMessage("event.mute.already"));
                    return;
                }
                sendMessage(victim);
                break;
            case KICK:
            case WARNING:
                sendMessage(victim);
                break;
        }

        String punishSuccess = plugin.getReferences().getPrefix()
                + plugin.getLocale().getMessage("event." + punishmentType.name().toLowerCase() + ".success", victim.getName());

        if (reason != null)
            punishSuccess += plugin.getLocale().getMessage("event.punish.reason", reason);

        if (duration != -1)
            punishSuccess += plugin.getLocale().getMessage("event.punish.theirduration", Methods.makeReadable(duration));

        punisher.sendMessage(punishSuccess + Methods.formatText("&7."));

        playerPunishData.addPunishment(apply(victim, punisher));
    }

    public void sendMessage(OfflinePlayer offlineVictim) {
        if (!offlineVictim.isOnline()) return;
        Player victim = offlineVictim.getPlayer();
        UltimateModeration plugin = UltimateModeration.getInstance();

        String punishSuccess = plugin.getReferences().getPrefix()
                + plugin.getLocale().getMessage("event." + punishmentType.name().toLowerCase() + ".message");

        if (reason != null)
            punishSuccess += plugin.getLocale().getMessage("event.punish.reason", reason);

        if (duration != -1)
            punishSuccess += plugin.getLocale().getMessage("event.punish.yourduration", Methods.makeReadable(duration));

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
