package com.songoda.ultimatemoderation.settings;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.configuration.Config;
import com.songoda.core.configuration.ConfigSetting;
import com.songoda.core.hooks.EconomyManager;
import com.songoda.ultimatemoderation.UltimateModeration;

import java.util.Arrays;
import java.util.stream.Collectors;

public class Settings {

    static final Config config = UltimateModeration.getInstance().getCoreConfig();

    public static final ConfigSetting VANISH_EFFECTS = new ConfigSetting(config, "Main.Enable Vanish Effects", true,
            "Show particles and play sound when going in and out of vanish.");

    public static final ConfigSetting VANISH_SOUND = new ConfigSetting(config, "Main.Vanish Sound", "ENTITY_GENERIC_EXPLODE",
            "Sound to be played when going into vanish.");

    public static final ConfigSetting VANISH_BATS = new ConfigSetting(config, "Main.Release Bats On Vanish", true,
            "Shows bats when entering vanish.");

    public static final ConfigSetting VANISH_PARTICLE = new ConfigSetting(config, "Main.Vanish Particle", "EXPLOSION_NORMAL",
            "Show particles when entering vanish.");

    public static final ConfigSetting SLOW_MODE = new ConfigSetting(config, "Main.SLOW_MODE", "0s",
            "Limits how often a player can send a chat message by the corresponding amount.");

    public static final ConfigSetting BLOCKED_COMMANDS = new ConfigSetting(config, "Main.Blocked Commands", Arrays.asList("Fly", "Op", "Plugins", "Pl"),
            "Prevents players from running the specified commands.");

    public static final ConfigSetting AUTOSAVE = new ConfigSetting(config, "Main.Auto Save Interval In Seconds", 15,
            "The amount of time in between saving to file.",
            "This is purely a safety function to prevent against unplanned crashes or",
            "restarts. With that said it is advised to keep this enabled.",
            "If however you enjoy living on the edge, feel free to turn it off.");

    public static final ConfigSetting STAFFCHAT_COLOR_CODE = new ConfigSetting(config, "Main.Staff Chat Color Code", 'b',
            "Color of messages sent in staff chat.");

    public static final ConfigSetting TICKET_TYPES = new ConfigSetting(config, "Main.Ticket Types", Arrays.asList("Grief", "Player Report", "Bug Report", "Suggestion", "Other"),
            "Types of tickets players can open.");

    public static final ConfigSetting MUTE_DISABLED_COMMANDS = new ConfigSetting(config, "Main.Mute Disabled Commands", Arrays.asList("minecraft:me", "minecraft:tell"),
            "Commands disabled when a player is muted.");

    public static final ConfigSetting GLASS_TYPE_1 = new ConfigSetting(config, "Interfaces.Glass Type 1", "GRAY_STAINED_GLASS_PANE");
    public static final ConfigSetting GLASS_TYPE_2 = new ConfigSetting(config, "Interfaces.Glass Type 2", "BLUE_STAINED_GLASS_PANE");
    public static final ConfigSetting GLASS_TYPE_3 = new ConfigSetting(config, "Interfaces.Glass Type 3", "LIGHT_BLUE_STAINED_GLASS_PANE");

    public static final ConfigSetting LANGUGE_MODE = new ConfigSetting(config, "System.Language Mode", "en_US",
            "The enabled language file.",
            "More language files (if available) can be found in the plugins data folder.");
    
    public static final ConfigSetting NOTIFY_BLOCK = new ConfigSetting(config, "Main.Notify Blocks", true, "Notify Staff on Block Break");
    
    public static final ConfigSetting NOTIFY_BLOCK_LIST = new ConfigSetting(config, "Main.Notify Blocks List", Arrays.asList("DIAMOND_ORE", "EMERALD_ORE"),
            "Blocks that will give a notification when mined.");

    public static final ConfigSetting MYSQL_ENABLED = new ConfigSetting(config, "MySQL.Enabled", false, "Set to 'true' to use MySQL instead of SQLite for data storage.");
    public static final ConfigSetting MYSQL_HOSTNAME = new ConfigSetting(config, "MySQL.Hostname", "localhost");
    public static final ConfigSetting MYSQL_PORT = new ConfigSetting(config, "MySQL.Port", 3306);
    public static final ConfigSetting MYSQL_DATABASE = new ConfigSetting(config, "MySQL.Database", "your-database");
    public static final ConfigSetting MYSQL_USERNAME = new ConfigSetting(config, "MySQL.Username", "user");
    public static final ConfigSetting MYSQL_PASSWORD = new ConfigSetting(config, "MySQL.Password", "pass");
    public static final ConfigSetting MYSQL_USE_SSL = new ConfigSetting(config, "MySQL.Use SSL", false);

    public static void setupConfig() {
        config.load();
        config.setAutoremove(true).setAutosave(true);

        // convert glass pane settings
        int color;
        if ((color = GLASS_TYPE_1.getInt(-1)) != -1) {
            config.set(GLASS_TYPE_1.getKey(), CompatibleMaterial.getGlassPaneColor(color).name());
        }
        if ((color = GLASS_TYPE_2.getInt(-1)) != -1) {
            config.set(GLASS_TYPE_2.getKey(), CompatibleMaterial.getGlassPaneColor(color).name());
        }
        if ((color = GLASS_TYPE_3.getInt(-1)) != -1) {
            config.set(GLASS_TYPE_3.getKey(), CompatibleMaterial.getGlassPaneColor(color).name());
        }

        config.saveChanges();
    }
}