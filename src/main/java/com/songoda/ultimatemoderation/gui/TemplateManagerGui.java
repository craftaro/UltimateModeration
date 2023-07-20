package com.songoda.ultimatemoderation.gui;

import com.craftaro.core.gui.Gui;
import com.craftaro.core.gui.GuiUtils;
import com.craftaro.core.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.core.utils.TextUtils;
import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.punish.PunishmentType;
import com.songoda.ultimatemoderation.punish.template.Template;
import com.songoda.ultimatemoderation.settings.Settings;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

public class TemplateManagerGui extends Gui {
    private final UltimateModeration plugin;

    private PunishmentType punishmentType = PunishmentType.ALL;
    private final Player player;

    private int page = 1;

    public TemplateManagerGui(UltimateModeration plugin, Player player) {
        super(6);
        setDefaultItem(null);
        this.plugin = plugin;
        this.player = player;

        setTitle(plugin.getLocale().getMessage("gui.templatemanager.title").getMessage());
        toCurrentPage();
    }

    private void toPrevPage() {
        if (this.page <= 1) {
            return;
        }

        --this.page;
        toCurrentPage();
    }

    private void toNextPage() {
        if (findTemplates(this.page + 1, this.punishmentType).size() > 0) {
            ++this.page;
            toCurrentPage();
        }
    }

    private void toCurrentPage() {
        if (this.inventory != null) {
            this.inventory.clear();
        }

        setActionForRange(0, 53, null);

        int numTemplates = this.plugin.getTemplateManager().getTemplates().size();
        this.pages = (int) Math.floor(numTemplates / 28.0);

        // decorate the edges
        ItemStack glass2 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_2.getMaterial(XMaterial.BLUE_STAINED_GLASS_PANE));
        ItemStack glass3 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_3.getMaterial(XMaterial.LIGHT_BLUE_STAINED_GLASS_PANE));

        // edges will be type 3
        mirrorFill(0, 2, true, true, glass3);
        mirrorFill(1, 1, true, true, glass3);

        // decorate corners with type 2
        mirrorFill(0, 0, true, true, glass2);
        mirrorFill(1, 0, true, true, glass2);
        mirrorFill(0, 1, true, true, glass2);

        setButton(5, 3, GuiUtils.createButtonItem(XMaterial.DIAMOND_SWORD, TextUtils.formatText("&6" + this.punishmentType.name())),
                (event) -> {
                    this.punishmentType = this.punishmentType.nextFilter();
                    this.page = 1;
                    toCurrentPage();
                });

        setButton(5, 4, GuiUtils.createButtonItem(XMaterial.OAK_DOOR,
                        this.plugin.getLocale().getMessage("gui.general.back").getMessage()),
                (event) -> this.guiManager.showGUI(event.player, new MainGui(this.plugin, event.player)));

        if (this.player.hasPermission("um.templates.create")) {
            setButton(5, 5, GuiUtils.createButtonItem(XMaterial.REDSTONE,
                            this.plugin.getLocale().getMessage("gui.templatemanager.create").getMessage()),
                    (event) -> this.guiManager.showGUI(event.player, new PunishGui(this.plugin, null, null, this.player)));
        }

        List<Template> templates = findTemplates(this.page, this.punishmentType);

        int num = 11;
        for (Template template : templates) {
            if (num == 16 || num == 36) {
                num = num + 2;
            }

            setButton(num, GuiUtils.createButtonItem(XMaterial.MAP, TextUtils.formatText("&6&l" + template.getName()),
                            this.plugin.getLocale().getMessage("gui.templatemanager.leftclick").getMessage(),
                            this.plugin.getLocale().getMessage("gui.templatemanager.rightclick").getMessage()),
                    (event) -> {
                        if (event.clickType == ClickType.LEFT) {
                            if (this.player.hasPermission("um.templates.edit"))
                                this.guiManager.showGUI(this.player, new PunishGui(this.plugin, null, template, this.player));
                        } else if (event.clickType == ClickType.RIGHT) {
                            if (this.player.hasPermission("um.templates.destroy")) {
                                this.plugin.getTemplateManager().removeTemplate(template);
                                this.plugin.getDataManager().deleteTemplate(template);
                            }

                            toCurrentPage();
                        }
                    });

            ++num;
        }

        setButton(0, 3, GuiUtils.createButtonItem(XMaterial.ARROW, this.plugin.getLocale().getMessage("gui.general.back").getMessage()), (event) -> toPrevPage());
        setButton(0, 5, GuiUtils.createButtonItem(XMaterial.ARROW, this.plugin.getLocale().getMessage("gui.general.next").getMessage()), (event) -> toNextPage());
    }

    private List<Template> findTemplates(int page, PunishmentType punishmentType) {
        return this.plugin.getTemplateManager().getTemplates().stream()
                .filter(template -> punishmentType == PunishmentType.ALL || template.getPunishmentType() == punishmentType)
                .skip((page - 1) * 28L)
                .limit(28)
                .collect(Collectors.toList());
    }
}
