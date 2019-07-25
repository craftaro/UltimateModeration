package com.songoda.ultimatemoderation.punish;

import com.songoda.ultimatemoderation.UltimateModeration;

public enum PunishmentType {

    ALL, BAN, KICK, WARNING, MUTE;

    private static PunishmentType[] vals = values();

    public PunishmentType next() {
        PunishmentType next = vals[(this.ordinal() != vals.length - 1 ? this.ordinal() + 1 : 0)];

        if (next == ALL)
            next = next.next();

        return next;
    }

    public PunishmentType nextFilter() {
        return vals[(this.ordinal() != vals.length - 1 ? this.ordinal() + 1 : 0)];
    }

    public String getTranslation() {
        return UltimateModeration.getInstance().getLocale().getMessage("gui.punishmenttypes." + this.name().toLowerCase()).getMessage();
    }
}
