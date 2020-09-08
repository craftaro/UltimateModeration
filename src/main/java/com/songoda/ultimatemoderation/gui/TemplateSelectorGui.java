package com.songoda.ultimatemoderation.gui;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.gui.Gui;
import com.songoda.core.gui.GuiUtils;
import com.songoda.core.utils.TextUtils;
import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.punish.template.Template;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class TemplateSelectorGui extends Gui {

    private final UltimateModeration plugin;
    private final PunishGui punish;

    public TemplateSelectorGui(UltimateModeration plugin, PunishGui punish, Player player) {
        super(6);
        setDefaultItem(null);
        this.plugin = plugin;
        this.punish = punish;

        setTitle(plugin.getLocale().getMessage("gui.templateselector.title").getMessage());
        paint();
    }

    private void paint() {
        setButton(8, GuiUtils.createButtonItem(CompatibleMaterial.OAK_DOOR,
                plugin.getLocale().getMessage("gui.general.back").getMessage()),
                (event) -> {
                    guiManager.showGUI(event.player, punish);
                    punish.runTask();
                });

        ArrayList<Template> templates = new ArrayList<>(plugin.getTemplateManager().getTemplates().values());
        for (int i = 0; i < templates.size(); i++) {
            Template template = templates.get(i);
            setButton(18 + i, GuiUtils.createButtonItem(CompatibleMaterial.MAP, TextUtils.formatText("&6&l" + template.getName()),
                    plugin.getLocale().getMessage("gui.templateselector.click").getMessage()),
                    (event) -> {
                        punish.setType(template.getPunishmentType());
                        punish.setDuration(template.getDuration());
                        punish.setReason(template.getReason());
                        punish.setTemplate(template);
                        punish.runTask();
                        punish.paint();
                        guiManager.showGUI(event.player, punish);
                    });
        }

    }
}
