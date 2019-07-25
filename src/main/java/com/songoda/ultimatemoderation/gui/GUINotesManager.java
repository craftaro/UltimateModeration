package com.songoda.ultimatemoderation.gui;

import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.punish.PunishmentNote;
import com.songoda.ultimatemoderation.utils.AbstractChatConfirm;
import com.songoda.ultimatemoderation.utils.ServerVersion;
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
import java.util.stream.Collectors;

public class GUINotesManager extends AbstractGUI {

    private final UltimateModeration plugin;

    private final OfflinePlayer toModerate;

    private int page = 0;

    public GUINotesManager(UltimateModeration plugin, OfflinePlayer toModerate, Player player) {
        super(player);
        this.plugin = plugin;
        this.toModerate = toModerate;

        init(plugin.getLocale().getMessage("gui.notes.title")
                .processPlaceholder("tonotes", player.getName()).getMessage(), 54);
    }

    @Override
    protected void constructGUI() {
        inventory.clear();
        resetClickables();
        registerClickables();

        for (int i = 0; i < 9; i++)
            createButton(9 + i, plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.GRAY_STAINED_GLASS_PANE : new ItemStack(Material.valueOf("STAINED_GLASS_PANE")), "&1");

        int numNotes = plugin.getPunishmentManager().getPlayer(toModerate).getNotes().size();
        int maxPage = (int) Math.floor(numNotes / 36.0);

        List<PunishmentNote> notes = plugin.getPunishmentManager().getPlayer(toModerate).getNotes().stream()
                .skip(page * 36).limit(36).collect(Collectors.toList());

        if (page != 0) {
            createButton(1, Material.ARROW, plugin.getLocale().getMessage("gui.general.previous").getMessage());
            registerClickable(1, ((player1, inventory1, cursor, slot, type) -> {
                page --;
                constructGUI();
            }));
        }

        if (page != maxPage) {
            createButton(3, Material.ARROW, plugin.getLocale().getMessage("gui.general.next").getMessage());
            registerClickable(3, ((player1, inventory1, cursor, slot, type) -> {
                page ++;
                constructGUI();
            }));
        }

        createButton(8, plugin.isServerVersionAtLeast(ServerVersion.V1_13)
                ? Material.OAK_DOOR
                : Material.valueOf("WOOD_DOOR"), plugin.getLocale().getMessage("gui.general.back").getMessage());

        createButton(6, Material.REDSTONE, plugin.getLocale().getMessage("gui.notes.create").getMessage());

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

            lore.add(plugin.getLocale().getMessage("gui.notes.createdby")
                    .processPlaceholder("player", Bukkit.getOfflinePlayer(note.getAuthor()).getName())
                    .getMessage());
            lore.add(plugin.getLocale().getMessage("gui.notes.createdon")
                    .processPlaceholder("sent", format.format(new Date(note.getCreationDate())))
                    .getMessage());
            lore.add(plugin.getLocale().getMessage("gui.notes.remove").getMessage());

            createButton(18 + i, Material.MAP, name, lore);

            registerClickable(18 + i, ((player1, inventory1, cursor, slot, type) -> {
                plugin.getPunishmentManager().getPlayer(toModerate).removeNote(note);
                constructGUI();
            }));
        }

    }

    @Override
    protected void registerClickables() {
        registerClickable(8, ((player1, inventory1, cursor, slot, type) ->
                new GUIPlayer(plugin, toModerate, player1)));

        registerClickable(6, ((player1, inventory1, cursor, slot, type) -> {
            plugin.getLocale().getMessage("gui.notes.type").sendMessage(player);
            AbstractChatConfirm abstractChatConfirm = new AbstractChatConfirm(player, event -> {
                plugin.getPunishmentManager().getPlayer(toModerate).addNotes(new PunishmentNote(event.getMessage(),
                        player.getUniqueId(), toModerate.getUniqueId(), System.currentTimeMillis()));
                constructGUI();
            });

            abstractChatConfirm.setOnClose(() ->
                    init(setTitle, inventory.getSize()));
        }));
    }

    @Override
    protected void registerOnCloses() {
    }
}
