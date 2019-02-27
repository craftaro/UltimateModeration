package com.songoda.ultimatemoderation;

public class References {

    private String prefix;

    public References() {
        prefix = UltimateModeration.getInstance().getLocale().getMessage("general.nametag.prefix") + " ";
    }

    public String getPrefix() {
        return this.prefix;
    }
}
