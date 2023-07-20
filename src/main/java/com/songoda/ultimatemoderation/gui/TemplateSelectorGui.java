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
                        this.plugin.getLocale().getMessage("gui.general.back").getMessage()),
                (event) -> {
                    this.guiManager.showGUI(event.player, this.punish);
                    this.punish.runTask();
                });

        ArrayList<Template> templates = new ArrayList<>(this.plugin.getTemplateManager().getTemplates());
        for (int i = 0; i < templates.size(); i++) {
            Template template = templates.get(i);
            setButton(18 + i, GuiUtils.createButtonItem(CompatibleMaterial.MAP, TextUtils.formatText("&6&l" + template.getName()),
                            this.plugin.getLocale().getMessage("gui.templateselector.click").getMessage()),
                    (event) -> {
                        this.punish.setType(template.getPunishmentType());
                        this.punish.setDuration(template.getDuration());
                        this.punish.setReason(template.getReason());
                        this.punish.setTemplate(template);
                        this.punish.runTask();
                        this.punish.paint();
                        this.guiManager.showGUI(event.player, this.punish);
                    });
        }
    }
}
