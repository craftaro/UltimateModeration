package com.craftaro.ultimatemoderation.settings;

import com.craftaro.core.compatibility.CompatibleMaterial;
import com.craftaro.core.configuration.Config;
import com.craftaro.core.configuration.ConfigSetting;
import com.craftaro.ultimatemoderation.UltimateModeration;

import java.util.Arrays;

public class Settings {
    static final Config CONFIG = UltimateModeration.getPlugin(UltimateModeration.class).getCoreConfig();

    public static final ConfigSetting VANISH_EFFECTS = new ConfigSetting(CONFIG, "Main.Enable Vanish Effects", true,
            "Show particles and play sound when going in and out of vanish.");

    public static final ConfigSetting VANISH_SOUND = new ConfigSetting(CONFIG, "Main.Vanish Sound", "ENTITY_GENERIC_EXPLODE",
            "Sound to be played when going into vanish.");

    public static final ConfigSetting VANISH_BATS = new ConfigSetting(CONFIG, "Main.Release Bats On Vanish", true,
            "Shows bats when entering vanish.");

    public static final ConfigSetting VANISH_PARTICLE = new ConfigSetting(CONFIG, "Main.Vanish Particle", "EXPLOSION_NORMAL",
            "Show particles when entering vanish.");

    public static final ConfigSetting SLOW_MODE = new ConfigSetting(CONFIG, "Main.SLOW_MODE", "0s",
            "Limits how often a player can send a chat message by the corresponding amount.");

    public static final ConfigSetting BLOCKED_COMMANDS = new ConfigSetting(CONFIG, "Main.Blocked Commands", Arrays.asList("Fly", "Op", "Plugins", "Pl"),
            "Prevents players from running the specified commands.");

    public static final ConfigSetting AUTOSAVE = new ConfigSetting(CONFIG, "Main.Auto Save Interval In Seconds", 15,
            "The amount of time in between saving to file.",
            "This is purely a safety function to prevent against unplanned crashes or",
            "restarts. With that said it is advised to keep this enabled.",
            "If however you enjoy living on the edge, feel free to turn it off.");

    public static final ConfigSetting STAFFCHAT_COLOR_CODE = new ConfigSetting(CONFIG, "Main.Staff Chat Color Code", 'b',
            "Color of messages sent in staff chat.");

    public static final ConfigSetting TICKET_TYPES = new ConfigSetting(CONFIG, "Main.Ticket Types", Arrays.asList("Grief", "Player Report", "Bug Report", "Suggestion", "Other"),
            "Types of tickets players can open.");

    public static final ConfigSetting MUTE_DISABLED_COMMANDS = new ConfigSetting(CONFIG, "Main.Mute Disabled Commands", Arrays.asList("minecraft:me", "minecraft:tell"),
            "Commands disabled when a player is muted.");

    public static final ConfigSetting GLASS_TYPE_1 = new ConfigSetting(CONFIG, "Interfaces.Glass Type 1", "GRAY_STAINED_GLASS_PANE");
    public static final ConfigSetting GLASS_TYPE_2 = new ConfigSetting(CONFIG, "Interfaces.Glass Type 2", "BLUE_STAINED_GLASS_PANE");
    public static final ConfigSetting GLASS_TYPE_3 = new ConfigSetting(CONFIG, "Interfaces.Glass Type 3", "LIGHT_BLUE_STAINED_GLASS_PANE");

    public static final ConfigSetting LANGUGE_MODE = new ConfigSetting(CONFIG, "System.Language Mode", "en_US",
            "The enabled language file.",
            "More language files (if available) can be found in the plugins data folder.");

    public static final ConfigSetting NOTIFY_BLOCK = new ConfigSetting(CONFIG, "Main.Notify Blocks", true, "Notify Staff on Block Break");

    public static final ConfigSetting NOTIFY_BLOCK_LIST = new ConfigSetting(CONFIG, "Main.Notify Blocks List", Arrays.asList("DIAMOND_ORE", "EMERALD_ORE"),
            "Blocks that will give a notification when mined.");

    public static final ConfigSetting DATA_UPDATE_INTERVAL = new ConfigSetting(CONFIG, "Main.Data Update Interval", 20,
            "The amount of time in between updating the data from the database in ticks.");

    public static void setupConfig() {
        CONFIG.load();
        CONFIG.setAutoremove(true).setAutosave(true);

        // convert glass pane settings
        int color;
        if ((color = GLASS_TYPE_1.getInt(-1)) != -1) {
            CONFIG.set(GLASS_TYPE_1.getKey(), CompatibleMaterial.getGlassPaneForColor(color).name());
        }
        if ((color = GLASS_TYPE_2.getInt(-1)) != -1) {
            CONFIG.set(GLASS_TYPE_2.getKey(), CompatibleMaterial.getGlassPaneForColor(color).name());
        }
        if ((color = GLASS_TYPE_3.getInt(-1)) != -1) {
            CONFIG.set(GLASS_TYPE_3.getKey(), CompatibleMaterial.getGlassPaneForColor(color).name());
        }

        CONFIG.saveChanges();
    }
}
