package com.songoda.ultimatemoderation.gui;

import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.punish.PunishmentType;
import com.songoda.ultimatemoderation.punish.template.Template;
import com.songoda.ultimatemoderation.utils.Methods;
import com.songoda.ultimatemoderation.utils.gui.AbstractGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;

public class GUITemplateManager extends AbstractGUI {

    private final UltimateModeration plugin;

    private PunishmentType punishmentType = PunishmentType.ALL;

    public GUITemplateManager(UltimateModeration plugin, Player player) {
        super(player);
        this.plugin = plugin;

        init(plugin.getLocale().getMessage("gui.templatemanager.title"), 54);
    }

    @Override
    protected void constructGUI() {
        inventory.clear();

        createButton(1, Material.ARROW, plugin.getLocale().getMessage("gui.general.previous"));

        createButton(3 ,Material.DIAMOND_SWORD, Methods.formatText("&6" + punishmentType.name()));

        createButton(5, Material.ARROW, plugin.getLocale().getMessage("gui.general.next"));

        createButton(8, Material.OAK_DOOR, plugin.getLocale().getMessage("gui.general.back"));

        createButton(7, Material.REDSTONE, plugin.getLocale().getMessage("gui.templatemanager.create"));

        for (int i = 0; i < 9; i++)
            createButton(9 + i, Material.GRAY_STAINED_GLASS_PANE, "&1");

        ArrayList<Template> templates = new ArrayList<>(plugin.getTemplateManager().getTemplates().values());
        if (punishmentType != PunishmentType.ALL)
            templates.removeIf(template -> template.getPunishmentType() != punishmentType);
        for (int i = 0; i < templates.size(); i++) {
            Template template = templates.get(i);
            createButton(18 + i, Material.MAP, "&6&l" + template.getTemplateName(),
                    plugin.getLocale().getMessage("gui.templatemanager.leftclick"),
                    plugin.getLocale().getMessage("gui.templatemanager.rightclick"));

            registerClickable(18 + i, ((player1, inventory1, cursor, slot, type) -> {
                if (type == ClickType.LEFT) {
                    new GUIPunish(plugin, null, template, player);
                } else if (type == ClickType.RIGHT) {
                    plugin.getTemplateManager().removeTemplate(template.getUUID());
                    constructGUI();
                }
            }));
        }
    }

    @Override
    protected void registerClickables() {
        registerClickable(8, ((player1, inventory1, cursor, slot, type) ->
                new GUIPlayers(plugin, player)));

        registerClickable(7, ((player1, inventory1, cursor, slot, type) ->
                new GUIPunish(plugin, null, null, player)));

        registerClickable(3, ((player1, inventory1, cursor, slot, type) -> {
            this.punishmentType = punishmentType.nextFilter();
            constructGUI();
        }));
    }

    @Override
    protected void registerOnCloses() {
    }
}
