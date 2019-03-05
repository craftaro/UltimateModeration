package com.songoda.ultimatemoderation.gui;

import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.punish.PunishmentNote;
import com.songoda.ultimatemoderation.utils.AbstractChatConfirm;
import com.songoda.ultimatemoderation.utils.gui.AbstractAnvilGUI;
import com.songoda.ultimatemoderation.utils.gui.AbstractGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GUINotesManager extends AbstractGUI {

    private final UltimateModeration plugin;

    private final OfflinePlayer toModerate;

    public GUINotesManager(UltimateModeration plugin, OfflinePlayer toModerate, Player player) {
        super(player);
        this.plugin = plugin;
        this.toModerate = toModerate;

        init(plugin.getLocale().getMessage("gui.notes.title", player.getName()), 54);
    }

    @Override
    protected void constructGUI() {
        inventory.clear();

        createButton(1, Material.ARROW, plugin.getLocale().getMessage("gui.general.previous"));

        createButton(3, Material.ARROW, plugin.getLocale().getMessage("gui.general.next"));

        createButton(8, Material.OAK_DOOR, plugin.getLocale().getMessage("gui.general.back"));

        createButton(6, Material.REDSTONE, plugin.getLocale().getMessage("gui.moderate.create"));

        for (int i = 0; i < 9; i++)
            createButton(9 + i, Material.GRAY_STAINED_GLASS_PANE, "&1");

        List<PunishmentNote> notes = plugin.getPunishmentManager().getPlayer(toModerate).getNotes();

        for (int i = 0; i < notes.size(); i++) {
            PunishmentNote note = notes.get(i);

            String noteStr = note.getNote();

            ArrayList<String> lore = new ArrayList<>();
            int lastIndex = 0;
            for (int n = 0; n < noteStr.length(); n++) {
                if (n - lastIndex < 20)
                    continue;

                if (noteStr.charAt(n) == ' ') {
                    lore.add("&6" +noteStr.substring(lastIndex, n).trim());
                    lastIndex = n;
                }
            }

            if (lastIndex - noteStr.length() < 20)
                lore.add("&6" + noteStr.substring(lastIndex, noteStr.length()).trim());

            String name = lore.get(0);
            lore.remove(0);

            lore.add("");

            SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy");

            lore.add(plugin.getLocale().getMessage("gui.moderate.createdby", Bukkit.getOfflinePlayer(note.getAuthor()).getName()));
            lore.add(plugin.getLocale().getMessage("gui.moderate.createdon", format.format(new Date(note.getCreationDate()))));
            lore.add(plugin.getLocale().getMessage("gui.moderate.remove"));

            createButton(18 + i, Material.MAP, name, lore);

            registerClickable(18 + i, ((player1, inventory1, cursor, slot, type) -> {
                plugin.getPunishmentManager().getPlayer(toModerate).removeNote(note);
                constructGUI();
                resetClickables();
                registerClickables();
            }));
        }

    }

    @Override
    protected void registerClickables() {
        registerClickable(8, ((player1, inventory1, cursor, slot, type) ->
                new GUIPlayers(plugin, player)));

        registerClickable(6, ((player1, inventory1, cursor, slot, type) -> {
            player.sendMessage(plugin.getLocale().getMessage("gui.moderate.type"));
            new AbstractChatConfirm(player, event -> {
                plugin.getPunishmentManager().getPlayer(toModerate).addNotes(new PunishmentNote(event.getMessage(),
                        player.getUniqueId(), toModerate.getUniqueId(), System.currentTimeMillis()));
                constructGUI();
            });
        }));
    }

    @Override
    protected void registerOnCloses() {
    }
}
