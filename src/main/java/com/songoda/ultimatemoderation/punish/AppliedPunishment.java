package com.songoda.ultimatemoderation.punish;

import java.util.UUID;

public class AppliedPunishment extends Punishment {
    private final UUID victim;
    private final UUID punisher;
    private long expiration;

    public AppliedPunishment(PunishmentType punishmentType, long duration, String reason, UUID victim, UUID punisher, long expiration, int id) {
        super(punishmentType, duration, reason, id);
        this.victim = victim;
        this.punisher = punisher;
        this.expiration = expiration;
    }

    public AppliedPunishment(PunishmentType punishmentType, long duration, String reason, UUID victim, UUID punisher, long expiration) {
        super(punishmentType, duration, reason);
        this.victim = victim;
        this.punisher = punisher;
        this.expiration = expiration;
    }

    public AppliedPunishment(Punishment punishment, UUID victim, UUID punisher, long expiration) {
        super(punishment);
        this.victim = victim;
        this.punisher = punisher;
        this.expiration = expiration;
    }

    public UUID getVictim() {
        return this.victim;
    }

    public UUID getPunisher() {
        return this.punisher;
    }

    public long getExpiration() {
        return this.expiration;
    }

    public void expire() {
        this.expiration = -1;
    }

    public long getTimeRemaining() {
        return this.expiration - System.currentTimeMillis();
    }
}
