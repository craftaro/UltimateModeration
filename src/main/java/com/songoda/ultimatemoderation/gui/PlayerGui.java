package com.songoda.ultimatemoderation.gui;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.gui.Gui;
import com.songoda.core.gui.GuiUtils;
import com.songoda.core.utils.ItemUtils;
import com.songoda.core.utils.TextUtils;
import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.settings.Settings;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PlayerGui extends Gui {
    public PlayerGui(UltimateModeration plugin, OfflinePlayer toModerate, Player player) {
        super(6);
        setDefaultItem(null);
        boolean punish = player.hasPermission("um.punish");
        boolean tickets = player.hasPermission("um.tickets");
        boolean punishments = player.hasPermission("um.punishments");
        boolean notes = player.hasPermission("um.notes");
        boolean moderate = player.hasPermission("um.moderation");

        setDefaultItem(null);

        setTitle(plugin.getLocale().getMessage("gui.player.title")
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
        mirrorFill(0, 1, true, true, glass2);

        ItemStack head = ItemUtils.getPlayerSkull(toModerate);

        setItem(13, GuiUtils.createButtonItem(head, TextUtils.formatText("&7&l" + toModerate.getName()),
                TextUtils.formatText(toModerate.isOnline() ? "&a"
                        + plugin.getLocale().getMessage("gui.players.online.online").getMessage()
                        : "&c" + plugin.getLocale().getMessage("gui.players.online.offline").getMessage())));

        setButton(8, GuiUtils.createButtonItem(CompatibleMaterial.OAK_DOOR,
                        plugin.getLocale().getMessage("gui.general.back").getMessage()),
                (event) -> this.guiManager.showGUI(event.player, new MainGui(plugin, event.player)));

        if (punish) {
            setButton(38, GuiUtils.createButtonItem(CompatibleMaterial.ANVIL,
                            plugin.getLocale().getMessage("gui.player.punish").getMessage()),
                    (event) -> plugin.getGuiManager().showGUI(player,
                            new PunishGui(plugin, toModerate, null, event.player)));
        }

        if (tickets) {
            setButton(30, GuiUtils.createButtonItem(CompatibleMaterial.CHEST,
                            plugin.getLocale().getMessage("gui.player.tickets").getMessage()),
                    (event) -> plugin.getGuiManager().showGUI(player,
                            new TicketManagerGui(plugin, toModerate, event.player)));
        }

        if (toModerate.isOnline() && punishments) {
            setButton(32, GuiUtils.createButtonItem(CompatibleMaterial.DIAMOND_SWORD,
                            plugin.getLocale().getMessage("gui.player.punishments").getMessage()),
                    (event) -> plugin.getGuiManager().showGUI(player, new PunishmentsGui(plugin, toModerate)));
        }

        if (notes) {
            setButton(42, GuiUtils.createButtonItem(CompatibleMaterial.MAP,
                            plugin.getLocale().getMessage("gui.player.notes").getMessage()),
                    (event) -> plugin.getGuiManager().showGUI(player,
                            new NotesManagerGui(plugin, toModerate, event.player)));
        }

        if (moderate) {
            setButton(40, GuiUtils.createButtonItem(CompatibleMaterial.DIAMOND_CHESTPLATE,
                            plugin.getLocale().getMessage("gui.player.moderate").getMessage()),
                    (event) -> this.guiManager.showGUI(player, new ModerateGui(plugin, toModerate, event.player)));
        }
    }
}
