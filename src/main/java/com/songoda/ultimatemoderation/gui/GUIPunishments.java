package com.songoda.ultimatemoderation.gui;

import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.punish.AppliedPunishment;
import com.songoda.ultimatemoderation.punish.PunishmentType;
import com.songoda.ultimatemoderation.punish.player.PlayerPunishData;
import com.songoda.ultimatemoderation.utils.Methods;
import com.songoda.ultimatemoderation.utils.gui.AbstractGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GUIPunishments extends AbstractGUI {

    private final UltimateModeration plugin;
    private final Player toModerate;

    private Activity currentActivity = Activity.BOTH;
    private PunishmentType punishmentType = PunishmentType.ALL;

    public GUIPunishments(UltimateModeration plugin, Player toModerate, Player player) {
        super(player);
        this.plugin = plugin;
        this.toModerate = toModerate;

        init(plugin.getLocale().getMessage("gui.punishments.title", toModerate.getName()), 54);
    }

    @Override
    protected void constructGUI() {
        inventory.clear();
        resetClickables();
        registerClickables();

        createButton(8, Material.OAK_DOOR, plugin.getLocale().getMessage("gui.general.back"));

        createButton(1, Material.ARROW, plugin.getLocale().getMessage("gui.general.previous"));

        createButton(3, Material.APPLE, Methods.formatText("&6" + currentActivity.getTranslation()));
        createButton(4, Material.DIAMOND_SWORD, Methods.formatText("&6" + punishmentType.name()));

        createButton(6, Material.ARROW, plugin.getLocale().getMessage("gui.general.next"));

        for (int i = 0; i < 9; i++)
            createButton(9 + i, Material.GRAY_STAINED_GLASS_PANE, "&1");

        PlayerPunishData playerPunishData = plugin.getPunishmentManager().getPlayer(toModerate);

        Map<AppliedPunishment, Activity> punishments = new HashMap<>();

        if (currentActivity == Activity.ACTIVE || currentActivity == Activity.BOTH) {
            for (AppliedPunishment punishment : playerPunishData.getActivePunishments()) {
                if (punishmentType != PunishmentType.ALL) {
                    if (punishment.getPunishmentType() != punishmentType)
                        continue;
                }
                punishments.put(punishment, Activity.ACTIVE);
            }
        }

        if (currentActivity == Activity.EXPIRED || currentActivity == Activity.BOTH) {
            for (AppliedPunishment punishment : playerPunishData.getExpiredPunishments()) {
                if (punishmentType != PunishmentType.ALL) {
                    if (punishment.getPunishmentType() != punishmentType)
                        continue;
                }
                punishments.put(punishment, Activity.EXPIRED);
            }
        }

        int currentSlot = 18;
        for (Map.Entry<AppliedPunishment, Activity> entry : punishments.entrySet()) {
            AppliedPunishment appliedPunishment = entry.getKey();
            Activity activity = entry.getValue();

            ArrayList<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(plugin.getLocale().getMessage("gui.punishments.reason"));
            lore.add("&7" + appliedPunishment.getReason());
            if (appliedPunishment.getPunishmentType() != PunishmentType.KICK) {
                lore.add("");
                lore.add(plugin.getLocale().getMessage("gui.punishments.duration"));
                lore.add("&7" + Methods.makeReadable(appliedPunishment.getDuration()));
                lore.add("");
                lore.add(plugin.getLocale().getMessage("gui.punishments.punisher"));
                lore.add("&7" + (appliedPunishment.getPunisher() == null ? "Console" : Bukkit.getOfflinePlayer(appliedPunishment.getPunisher()).getName()));
                if (activity == Activity.ACTIVE) {
                    lore.add("");
                    lore.add(plugin.getLocale().getMessage("gui.punishments.remaining"));
                    lore.add("&7" + Methods.makeReadable(appliedPunishment.getTimeRemaining()));
                    lore.add("");
                    lore.add(plugin.getLocale().getMessage("gui.punishments.click"));

                    registerClickable(currentSlot, ((player1, inventory1, cursor, slot, type) -> {
                        appliedPunishment.expire();
                        constructGUI();
                    }));
                }
            }
            lore.add("");
            createButton(currentSlot, Material.MAP,
                    "&6&l" + appliedPunishment.getPunishmentType().getTranslation() + " - &7&l" + activity.getTranslation(), lore);

            currentSlot++;
        }

    }

    @Override
    protected void registerClickables() {
        registerClickable(8, ((player1, inventory1, cursor, slot, type) ->
                new GUIPlayer(plugin, toModerate, player)));

        registerClickable(3, ((player1, inventory1, cursor, slot, type) -> {
            this.currentActivity = currentActivity.next();
            constructGUI();
        }));

        registerClickable(4, ((player1, inventory1, cursor, slot, type) -> {
            this.punishmentType = punishmentType.nextFilter();
            constructGUI();
        }));
    }

    @Override
    protected void registerOnCloses() {
    }

    private enum Activity {

        BOTH, ACTIVE, EXPIRED;

        private static Activity[] vals = values();

        public Activity next() {
            return vals[(this.ordinal() != vals.length - 1 ? this.ordinal() + 1 : 0)];
        }

        public String getTranslation() {
            return UltimateModeration.getInstance().getLocale().getMessage("gui.punishments.activity." + this.name().toLowerCase());
        }
    }
}
