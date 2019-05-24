package com.songoda.ultimatemoderation.utils.settings;

import com.songoda.ultimatemoderation.UltimateModeration;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum Setting {

    VANISH_EFFECTS("Main.Enable Vanish Effects", true,
            "Show particles and play sound when going in and out of vanish."),

    VANISH_SOUND("Main.Vanish Sound", "ENTITY_GENERIC_EXPLODE",
            "Sound to be played when going into vanish."),

    VANISH_BATS("Main.Release Bats On Vanish", true,
            "Shows bats when entering vanish."),

    VANISH_PARTICLE("Main.Vanish Particle", "EXPLOSION_NORMAL",
            "Show particles when entering vanish."),

    SLOW_MODE("Main.SLOW_MODE", "0s",
            "Limits how often a player can send a chat message by the corresponding amount."),

    BLOCKED_COMMANDS("Main.Blocked Commands", Arrays.asList("Fly", "Op", "Plugins", "Pl"),
            "Prevents players from running the specified commands."),

    AUTOSAVE("Main.Auto Save Interval In Seconds", 15,
            "The amount of time in between saving to file.",
            "This is purely a safety function to prevent against unplanned crashes or",
            "restarts. With that said it is advised to keep this enabled.",
            "If however you enjoy living on the edge, feel free to turn it off."),

    STAFFCHAT_COLOR_CODE("Main.Staff Chat Color Code", 'b',
            "Color of messages sent in staff chat."),

    TICKET_TYPES("Main.Ticket Types", Arrays.asList("Grief", "Player Report", "Bug Report", "Suggestion", "Other"),
            "Types of tickets players can open."),

    MUTE_DISABLED_COMMANDS("Main.Mute Disabled Commands", Arrays.asList("minecraft:me", "minecraft:tell"),
            "Commands disabled when a player is muted."),

    GLASS_TYPE_1("Interfaces.Glass Type 1", 7),
    GLASS_TYPE_2("Interfaces.Glass Type 2", 11),
    GLASS_TYPE_3("Interfaces.Glass Type 3", 3),

    DATABASE_SUPPORT("Database.Activate Mysql Support", false,
            "Should MySQL be used for data storage?"),

    DATABASE_IP("Database.IP", "127.0.0.1",
            "MySQL IP"),

    DATABASE_PORT("Database.Port", 3306,
            "MySQL Port"),

    DATABASE_NAME("Database.Database Name", "UltimateModeration",
            "The database you are inserting data into."),

    DATABASE_PREFIX("Database.Prefix", "US-",
            "The prefix for tables inserted into the database."),

    DATABASE_USERNAME("Database.Username", "PUT_USERNAME_HERE",
            "MySQL Username"),

    DATABASE_PASSWORD("Database.Password", "PUT_PASSWORD_HERE",
            "MySQL Password"),

    LANGUGE_MODE("System.Language Mode", "en_US",
            "The enabled language file.",
            "More language files (if available) can be found in the plugins data folder.");

    private String setting;
    private Object option;
    private String[] comments;

    Setting(String setting, Object option, String... comments) {
        this.setting = setting;
        this.option = option;
        this.comments = comments;
    }

    Setting(String setting, Object option) {
        this.setting = setting;
        this.option = option;
        this.comments = null;
    }

    public static Setting getSetting(String setting) {
        List<Setting> settings = Arrays.stream(values()).filter(setting1 -> setting1.setting.equals(setting)).collect(Collectors.toList());
        if (settings.isEmpty()) return null;
        return settings.get(0);
    }

    public String getSetting() {
        return setting;
    }

    public Object getOption() {
        return option;
    }

    public String[] getComments() {
        return comments;
    }

    public List<String> getStringList() {
        return UltimateModeration.getInstance().getConfig().getStringList(setting);
    }

    public boolean getBoolean() {
        return UltimateModeration.getInstance().getConfig().getBoolean(setting);
    }

    public int getInt() {
        return UltimateModeration.getInstance().getConfig().getInt(setting);
    }

    public long getLong() {
        return UltimateModeration.getInstance().getConfig().getLong(setting);
    }

    public String getString() {
        return UltimateModeration.getInstance().getConfig().getString(setting);
    }

    public char getChar() {
        return UltimateModeration.getInstance().getConfig().getString(setting).charAt(0);
    }

    public double getDouble() {
        return UltimateModeration.getInstance().getConfig().getDouble(setting);
    }
}