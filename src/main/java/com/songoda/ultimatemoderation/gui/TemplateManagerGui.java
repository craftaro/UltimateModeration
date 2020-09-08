package com.songoda.ultimatemoderation.gui;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.gui.Gui;
import com.songoda.core.gui.GuiUtils;
import com.songoda.core.utils.TextUtils;
import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.punish.PunishmentType;
import com.songoda.ultimatemoderation.punish.template.Template;
import com.songoda.ultimatemoderation.settings.Settings;
import com.songoda.ultimatemoderation.utils.Methods;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

public class TemplateManagerGui extends Gui {

    private final UltimateModeration plugin;

    private PunishmentType punishmentType = PunishmentType.ALL;
    private final Player player;

    public TemplateManagerGui(UltimateModeration plugin, Player player) {
        super(6);
        setDefaultItem(null);
        this.plugin = plugin;
        this.player = player;

        setTitle(plugin.getLocale().getMessage("gui.templatemanager.title").getMessage());
        showPage();
    }

    protected void showPage() {
        if (inventory != null)
            inventory.clear();
        setActionForRange(0, 53, null);

        int numTemplates = plugin.getTemplateManager().getTemplates().size();
        this.pages = (int) Math.floor(numTemplates / 28.0);

        List<Template> templates = plugin.getTemplateManager().getTemplates().values().stream().skip((page - 1) * 28).limit(28)
                .collect(Collectors.toList());

        setNextPage(0, 5, GuiUtils.createButtonItem(CompatibleMaterial.ARROW, plugin.getLocale().getMessage("gui.general.next").getMessage()));
        setPrevPage(0, 1, GuiUtils.createButtonItem(CompatibleMaterial.ARROW, plugin.getLocale().getMessage("gui.general.back").getMessage()));
        setOnPage((event) -> showPage());

        // decorate the edges
        ItemStack glass2 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_2.getMaterial(CompatibleMaterial.BLUE_STAINED_GLASS_PANE));
        ItemStack glass3 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_3.getMaterial(CompatibleMaterial.LIGHT_BLUE_STAINED_GLASS_PANE));

        // edges will be type 3
        GuiUtils.mirrorFill(this, 0, 2, true, true, glass3);
        GuiUtils.mirrorFill(this, 1, 1, true, true, glass3);

        // decorate corners with type 2
        GuiUtils.mirrorFill(this, 0, 0, true, true, glass2);
        GuiUtils.mirrorFill(this, 1, 0, true, true, glass2);
        GuiUtils.mirrorFill(this, 0, 1, true, true, glass2);

        setButton(5, 3, GuiUtils.createButtonItem(CompatibleMaterial.DIAMOND_SWORD, Methods.formatText("&6" + punishmentType.name())),
                (event) -> {
                    this.punishmentType = punishmentType.nextFilter();
                    this.page = 1;
                    showPage();
                });

        setButton(5, 4, GuiUtils.createButtonItem(CompatibleMaterial.OAK_DOOR,
                plugin.getLocale().getMessage("gui.general.back").getMessage()),
                (event) -> guiManager.showGUI(event.player, new MainGui(plugin, event.player)));

        if (player.hasPermission("um.templates.create"))
            setButton(5, 5, GuiUtils.createButtonItem(CompatibleMaterial.REDSTONE,
                    plugin.getLocale().getMessage("gui.templatemanager.create").getMessage()),
                    (event) -> guiManager.showGUI(event.player, new PunishGui(plugin, null, null, player)));

        if (punishmentType != PunishmentType.ALL)
            templates.removeIf(template -> template.getPunishmentType() != punishmentType);
        int num = 11;
        for (Template template : templates) {
            if (num == 16 || num == 36)
                num = num + 2;
            setButton(num, GuiUtils.createButtonItem(CompatibleMaterial.MAP, TextUtils.formatText("&6&l" + template.getName()),
                    plugin.getLocale().getMessage("gui.templatemanager.leftclick").getMessage(),
                    plugin.getLocale().getMessage("gui.templatemanager.rightclick").getMessage()),
                    (event) -> {
                        if (event.clickType == ClickType.LEFT) {
                            if (player.hasPermission("um.templates.edit"))
                                guiManager.showGUI(player, new PunishGui(plugin, null, template, player));
                        } else if (event.clickType == ClickType.RIGHT) {
                            if (player.hasPermission("um.templates.destroy"))
                                plugin.getTemplateManager().removeTemplate(template);
                            showPage();
                        }
                    });
            num++;
        }
    }

}
