package com.craftaro.ultimatemoderation.punish.player;

import com.craftaro.ultimatemoderation.UltimateModeration;
import com.craftaro.ultimatemoderation.punish.AppliedPunishment;
import com.craftaro.ultimatemoderation.punish.PunishmentNote;
import com.craftaro.ultimatemoderation.punish.PunishmentType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayerPunishData {
    private final UUID player;

    private final List<AppliedPunishment> activePunishments = new ArrayList<>();
    private final List<AppliedPunishment> expiredPunishments = new ArrayList<>();

    private final List<PunishmentNote> punishmentNotes = new ArrayList<>();

    public PlayerPunishData(UUID player) {
        this.player = player;
    }

    public PlayerPunishData(UUID player, List<AppliedPunishment> punishments) {
        this.player = player;
        this.activePunishments.addAll(punishments);
    }

    public UUID getPlayer() {
        return this.player;
    }

    public List<AppliedPunishment> getActivePunishments() {
        audit();
        return new ArrayList<>(this.activePunishments);
    }

    public List<AppliedPunishment> getActivePunishments(PunishmentType type) {
        audit();
        return this.activePunishments.stream().filter(punishment -> punishment.getPunishmentType() == type).collect(Collectors.toList());
    }

    public List<AppliedPunishment> getExpiredPunishments() {
        audit();
        return new ArrayList<>(this.expiredPunishments);
    }

    public List<AppliedPunishment> getExpiredPunishments(PunishmentType type) {
        audit();
        return this.expiredPunishments.stream().filter(punishment -> punishment.getPunishmentType() == type).collect(Collectors.toList());
    }

    public AppliedPunishment[] addPunishment(AppliedPunishment... appliedPunishments) {
        this.activePunishments.addAll(Arrays.asList(appliedPunishments));
        return appliedPunishments;
    }

    public AppliedPunishment[] addExpiredPunishment(AppliedPunishment... appliedPunishments) {
        this.expiredPunishments.addAll(Arrays.asList(appliedPunishments));
        return appliedPunishments;
    }

    public AppliedPunishment[] removePunishment(AppliedPunishment... appliedPunishments) {
        this.activePunishments.removeAll(Arrays.asList(appliedPunishments));
        return appliedPunishments;
    }

    public AppliedPunishment[] removeExpiredPunishment(AppliedPunishment... appliedPunishments) {
        this.activePunishments.removeAll(Arrays.asList(appliedPunishments));
        return appliedPunishments;
    }

    public List<PunishmentNote> getNotes() {
        return new ArrayList<>(this.punishmentNotes);
    }

    public PunishmentNote[] addNotes(PunishmentNote... notes) {
        this.punishmentNotes.addAll(Arrays.asList(notes));
        return notes;
    }

    public PunishmentNote[] removeNotes(PunishmentNote... notes) {
        this.punishmentNotes.removeAll(Arrays.asList(notes));
        return notes;
    }

    public void audit() {
        audit(false, PunishmentType.ALL);
    }

    private void audit(boolean forced, PunishmentType punishmentType) {
        List<AppliedPunishment> expired = this.activePunishments.stream().filter(appliedPunishment ->
                (appliedPunishment.getDuration() != -1 || forced || appliedPunishment.getExpiration() == -1)
                        && (appliedPunishment.getPunishmentType() == punishmentType || punishmentType == PunishmentType.ALL)
                        && appliedPunishment.getExpiration() <= System.currentTimeMillis()).collect(Collectors.toList());

        this.expiredPunishments.addAll(expired);
        this.activePunishments.removeAll(expired);
    }

    public void expirePunishments(PunishmentType type) {
        List<AppliedPunishment> toAudit = new ArrayList<>();
        this.activePunishments.stream().filter(appliedPunishment ->
                type == appliedPunishment.getPunishmentType()).forEach(appliedPunishment -> {
            appliedPunishment.expire();
            UltimateModeration.getInstance().getDataHelper().updateAppliedPunishment(appliedPunishment);
            toAudit.add(appliedPunishment);
        });
        toAudit.forEach(appliedPunishment -> this.audit(true, type));
    }
}
