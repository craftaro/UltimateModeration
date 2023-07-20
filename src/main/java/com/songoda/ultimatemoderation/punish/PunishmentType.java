package com.songoda.ultimatemoderation.punish;

import com.songoda.ultimatemoderation.UltimateModeration;

public enum PunishmentType {
    ALL, BAN, KICK, WARNING, MUTE;

    public PunishmentType next() {
        PunishmentType next = values()[(this.ordinal() != values().length - 1 ? this.ordinal() + 1 : 0)];

        if (next == ALL) {
            next = next.next();
        }

        return next;
    }

    public PunishmentType nextFilter() {
        return values()[(this.ordinal() != values().length - 1 ? this.ordinal() + 1 : 0)];
    }

    public String getTranslation() {
        return UltimateModeration.getPlugin(UltimateModeration.class)
                .getLocale()
                .getMessage("gui.punishmenttypes." + this.name().toLowerCase())
                .getMessage();
    }
}
