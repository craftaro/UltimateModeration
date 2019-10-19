package com.songoda.ultimatemoderation.gui;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.compatibility.ServerVersion;
import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.punish.template.Template;
import com.songoda.ultimatemoderation.utils.gui.AbstractGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class GUITemplateSelector extends AbstractGUI {

    private final UltimateModeration plugin;
    private GUIPunish punish;

    public GUITemplateSelector(UltimateModeration plugin, GUIPunish punish, Player player) {
        super(player);
        this.plugin = plugin;
        this.punish = punish;

        init(plugin.getLocale().getMessage("gui.templateselector.title").getMessage(), 54);
    }

    @Override
    protected void constructGUI() {
        createButton(8, CompatibleMaterial.OAK_DOOR.getMaterial(),
                plugin.getLocale().getMessage("gui.general.back").getMessage());

        for (int i = 0; i < 9; i++)
            createButton(9 + i, ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.GRAY_STAINED_GLASS_PANE : new ItemStack(Material.valueOf("STAINED_GLASS_PANE")), "&1");

        ArrayList<Template> templates = new ArrayList<>(plugin.getTemplateManager().getTemplates().values());
        for (int i = 0; i < templates.size(); i++) {
            Template template = templates.get(i);
            createButton(18 + i, Material.MAP, "&6&l" + template.getTemplateName(),
                    plugin.getLocale().getMessage("gui.templateselector.click").getMessage());

            registerClickable(18 + i, ((player1, inventory1, cursor, slot, type) -> {
                punish.setType(template.getPunishmentType());
                punish.setDuration(template.getDuration());
                punish.setReason(template.getReason());
                punish.setTemplate(template);
                punish.init(setTitle, punish.getInventory().getSize());
                punish.runTask();
            }));
        }

    }

    @Override
    protected void registerClickables() {
        registerClickable(8, ((player1, inventory1, cursor, slot, type) -> {
            punish.init(punish.getSetTitle(), punish.getInventory().getSize());
            punish.runTask();
        }));
    }

    @Override
    protected void registerOnCloses() {

    }
}
