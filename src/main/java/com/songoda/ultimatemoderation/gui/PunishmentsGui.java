package com.songoda.ultimatemoderation.gui;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.gui.Gui;
import com.songoda.core.gui.GuiUtils;
import com.songoda.core.utils.TextUtils;
import com.songoda.core.utils.TimeUtils;
import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.punish.AppliedPunishment;
import com.songoda.ultimatemoderation.punish.PunishmentType;
import com.songoda.ultimatemoderation.punish.player.PlayerPunishData;
import com.songoda.ultimatemoderation.settings.Settings;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PunishmentsGui extends Gui {
    private final UltimateModeration plugin;
    private final OfflinePlayer toModerate;

    private Activity currentActivity = Activity.BOTH;
    private PunishmentType punishmentType = PunishmentType.ALL;

    public PunishmentsGui(UltimateModeration plugin, OfflinePlayer toModerate) {
        super(6);
        setDefaultItem(null);
        this.plugin = plugin;
        this.toModerate = toModerate;

        setTitle(plugin.getLocale().getMessage("gui.punishments.title")
                .processPlaceholder("toModerate", toModerate.getName()).getMessage());

        showPage();
    }

    protected void showPage() {
        if (this.inventory != null) {
            this.inventory.clear();
        }
        setActionForRange(0, 53, null);


        setNextPage(0, 5, GuiUtils.createButtonItem(CompatibleMaterial.ARROW, this.plugin.getLocale().getMessage("gui.general.next").getMessage()));
        setPrevPage(0, 1, GuiUtils.createButtonItem(CompatibleMaterial.ARROW, this.plugin.getLocale().getMessage("gui.general.back").getMessage()));
        setOnPage((event) -> showPage());

        // decorate the edges
        ItemStack glass2 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_2.getMaterial(CompatibleMaterial.BLUE_STAINED_GLASS_PANE));
        ItemStack glass3 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_3.getMaterial(CompatibleMaterial.LIGHT_BLUE_STAINED_GLASS_PANE));

        // edges will be type 3
        mirrorFill(0, 2, true, true, glass3);
        mirrorFill(1, 1, true, true, glass3);

        // decorate corners with type 2
        mirrorFill(0, 0, true, true, glass2);
        mirrorFill(1, 0, true, true, glass2);
        mirrorFill(0, 1, true, true, glass2);

        PlayerPunishData playerPunishData = this.plugin.getPunishmentManager().getPlayer(this.toModerate);

        List<PunishmentHolder> punishments = new ArrayList<>();

        if (this.currentActivity == Activity.ACTIVE || this.currentActivity == Activity.BOTH) {
            for (AppliedPunishment punishment : playerPunishData.getActivePunishments()) {
                if (this.punishmentType != PunishmentType.ALL) {
                    if (punishment.getPunishmentType() != this.punishmentType) {
                        continue;
                    }
                }
                punishments.add(new PunishmentHolder(Activity.ACTIVE, punishment));
            }
        }

        if (this.currentActivity == Activity.EXPIRED || this.currentActivity == Activity.BOTH) {
            for (AppliedPunishment punishment : playerPunishData.getExpiredPunishments()) {
                if (this.punishmentType != PunishmentType.ALL) {
                    if (punishment.getPunishmentType() != this.punishmentType) {
                        continue;
                    }
                }
                punishments.add(new PunishmentHolder(Activity.EXPIRED, punishment));
            }
        }

        int numNotes = punishments.size();
        this.pages = (int) Math.floor(numNotes / 28.0);

        punishments = punishments.stream().skip((this.page - 1) * 28).limit(28)
                .collect(Collectors.toList());

        setButton(5, 4, GuiUtils.createButtonItem(CompatibleMaterial.OAK_DOOR,
                        this.plugin.getLocale().getMessage("gui.general.back").getMessage()),
                (event) -> this.guiManager.showGUI(event.player, new PlayerGui(this.plugin, this.toModerate, event.player)));

        setButton(5, 3, GuiUtils.createButtonItem(CompatibleMaterial.APPLE, TextUtils.formatText("&6" + this.currentActivity.getTranslation())),
                (event) -> {
                    this.currentActivity = this.currentActivity.next();
                    this.page = 1;
                    showPage();
                });

        setButton(5, 5, GuiUtils.createButtonItem(CompatibleMaterial.DIAMOND_SWORD, TextUtils.formatText("&6" + this.punishmentType.name())),
                (event) -> {
                    this.punishmentType = this.punishmentType.nextFilter();
                    this.page = 1;
                    showPage();
                });

        int num = 11;
        for (PunishmentHolder punishmentHolder : punishments) {
            if (num == 16 || num == 36) {
                num = num + 2;
            }
            AppliedPunishment appliedPunishment = punishmentHolder.getAppliedPunishment();
            Activity activity = punishmentHolder.getActivity();

            ArrayList<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(this.plugin.getLocale().getMessage("gui.punishments.reason").getMessage());
            lore.add("&7" + appliedPunishment.getReason());
            if (appliedPunishment.getPunishmentType() != PunishmentType.KICK) {
                lore.add("");
                lore.add(this.plugin.getLocale().getMessage("gui.punishments.duration").getMessage());
                lore.add("&7" + (appliedPunishment.getDuration() != -1
                        ? TimeUtils.makeReadable(appliedPunishment.getDuration())
                        : this.plugin.getLocale().getMessage("gui.general.permanent").getMessage()));
                lore.add("");
                lore.add(this.plugin.getLocale().getMessage("gui.punishments.punisher").getMessage());
                lore.add("&7" + (appliedPunishment.getPunisher() == null ? "Console" : Bukkit.getOfflinePlayer(appliedPunishment.getPunisher()).getName()));
                if (activity == Activity.ACTIVE) {
                    lore.add("");
                    if (appliedPunishment.getDuration() != -1) {
                        lore.add(this.plugin.getLocale().getMessage("gui.punishments.remaining").getMessage());
                        lore.add("&7" + TimeUtils.makeReadable(appliedPunishment.getTimeRemaining()));
                        lore.add("");
                    }
                    lore.add(this.plugin.getLocale().getMessage("gui.punishments.click").getMessage());
                }
            }
            lore.add("");
            setButton(num, GuiUtils.createButtonItem(CompatibleMaterial.MAP,
                            TextUtils.formatText("&6&l" + appliedPunishment.getPunishmentType().getTranslation() + " - &7&l" + activity.getTranslation()),
                            TextUtils.formatText(lore)),
                    (event) -> {
                        if (appliedPunishment.getPunishmentType() != PunishmentType.KICK
                                && activity == Activity.ACTIVE) {
                            appliedPunishment.expire();
                            this.plugin.getDataManager().updateAppliedPunishment(appliedPunishment);
                            showPage();
                        }
                    });

            num++;
        }

    }


    private static class PunishmentHolder {
        private final Activity activity;
        private final AppliedPunishment appliedPunishment;

        public PunishmentHolder(Activity activity, AppliedPunishment appliedPunishment) {
            this.activity = activity;
            this.appliedPunishment = appliedPunishment;
        }

        public Activity getActivity() {
            return this.activity;
        }

        public AppliedPunishment getAppliedPunishment() {
            return this.appliedPunishment;
        }
    }

    private enum Activity {
        BOTH, ACTIVE, EXPIRED;

        public Activity next() {
            return values()[(this.ordinal() != values().length - 1 ? this.ordinal() + 1 : 0)];
        }

        public String getTranslation() {
            return UltimateModeration.getPlugin(UltimateModeration.class)
                    .getLocale()
                    .getMessage("gui.punishments.activity." + this.name().toLowerCase())
                    .getMessage();
        }
    }
}
