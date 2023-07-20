package com.songoda.ultimatemoderation.gui;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.gui.Gui;
import com.songoda.core.gui.GuiUtils;
import com.songoda.core.utils.TextUtils;
import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.moderate.AbstractModeration;
import com.songoda.ultimatemoderation.settings.Settings;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ModerateGui extends Gui {
    public ModerateGui(UltimateModeration plugin, OfflinePlayer toModerate, Player player) {
        super(5);
        setDefaultItem(null);

        setTitle(plugin.getLocale().getMessage("gui.moderate.title")
                .processPlaceholder("toModerate", toModerate.getName()).getMessage());

        // decorate the edges
        ItemStack glass2 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_2.getMaterial(CompatibleMaterial.BLUE_STAINED_GLASS_PANE));
        ItemStack glass3 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_3.getMaterial(CompatibleMaterial.LIGHT_BLUE_STAINED_GLASS_PANE));

        // edges will be type 3
        mirrorFill(0, 2, true, true, glass3);
        mirrorFill(1, 1, true, true, glass3);

        // decorate corners with type 2
        mirrorFill(0, 0, true, true, glass2);
        mirrorFill(1, 0, true, true, glass2);
        mirrorFill(2, 0, false, true, glass2);
        mirrorFill(0, 1, true, true, glass2);

        setButton(8, GuiUtils.createButtonItem(CompatibleMaterial.OAK_DOOR,
                        plugin.getLocale().getMessage("gui.general.back").getMessage()),
                (event) -> this.guiManager.showGUI(event.player, new PlayerGui(plugin, toModerate, event.player)));

        int[] slots = new int[]{11, 13, 15, 29, 31, 33};

        List<AbstractModeration> moderations = new ArrayList<>(plugin.getModerationManager().getModerations().values());

        int i = 0;
        for (AbstractModeration moderation : moderations) {
            if (!moderation.hasPermission(player) || moderation.isExempt(toModerate)) {
                continue;
            }

            int slot = slots[i];
            setButton(slot, GuiUtils.createButtonItem(moderation.getIcon(),
                            TextUtils.formatText("&6&l" + moderation.getProper()),
                            TextUtils.formatText("&7" + moderation.getDescription())),
                    (event) -> moderation.runPreModeration(player, toModerate));
            i++;
        }
    }
}
