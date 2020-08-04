package com.songoda.ultimatemoderation.gui;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.gui.Gui;
import com.songoda.core.gui.GuiUtils;
import com.songoda.core.input.ChatPrompt;
import com.songoda.core.utils.TextUtils;
import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.punish.PunishmentNote;
import com.songoda.ultimatemoderation.settings.Settings;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class NotesManagerGui extends Gui {

    private final UltimateModeration plugin;

    private final OfflinePlayer toModerate;

    private final boolean create, delete;

    public NotesManagerGui(UltimateModeration plugin, OfflinePlayer toModerate, Player player) {
        this.plugin = plugin;
        setRows(6);
        setDefaultItem(null);
        this.toModerate = toModerate;
        this.create = player.hasPermission("um.notes.create");
        this.delete = player.hasPermission("um.notes.delete");

        setTitle(plugin.getLocale().getMessage("gui.notes.title")
                .processPlaceholder("tonotes", player.getName()).getMessage());

        showPage();
    }

    private void showPage() {
        if (inventory != null)
            inventory.clear();
        setActionForRange(0, 53, null);

        int numNotes = plugin.getPunishmentManager().getPlayer(toModerate).getNotes().size();
        this.pages = (int) Math.max(1, Math.ceil(numNotes / ((double) 28)));

        List<PunishmentNote> notes = plugin.getPunishmentManager().getPlayer(toModerate).getNotes().stream()
                .skip((page - 1) * 28).limit(28).collect(Collectors.toList());

        // enable page events
        setNextPage(0, 1, GuiUtils.createButtonItem(CompatibleMaterial.ARROW, plugin.getLocale().getMessage("gui.general.next").getMessage()));
        setPrevPage(0, 3, GuiUtils.createButtonItem(CompatibleMaterial.ARROW, plugin.getLocale().getMessage("gui.general.back").getMessage()));
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

        setButton(5, 5, GuiUtils.createButtonItem(CompatibleMaterial.OAK_DOOR,
                plugin.getLocale().getMessage("gui.general.back").getMessage()),
                (event) -> guiManager.showGUI(event.player, new PlayerGui(plugin, toModerate, event.player)));

        if (create)
            setButton(5, 3, GuiUtils.createButtonItem(CompatibleMaterial.REDSTONE,
                    plugin.getLocale().getMessage("gui.notes.create").getMessage()),
                    (event) -> {
                        ChatPrompt.showPrompt(plugin, event.player,
                                plugin.getLocale().getMessage("gui.notes.type").getMessage(),
                                (response) -> {
                                    PunishmentNote note = new PunishmentNote(response.getMessage(),
                                            event.player.getUniqueId(), toModerate.getUniqueId(),
                                            System.currentTimeMillis());
                                    plugin.getPunishmentManager().getPlayer(toModerate).addNotes(note);
                                    plugin.getDataManager().createNote(note);

                                    showPage();
                                }).setOnClose(() -> guiManager.showGUI(event.player, new NotesManagerGui(plugin, toModerate, event.player)));
                    });

        int num = 11;
        for (PunishmentNote note : notes) {
            if (num == 16 || num == 36)
                num = num + 2;
            String noteStr = note.getNote();

            ArrayList<String> lore = new ArrayList<>();
            int lastIndex = 0;
            for (int n = 0; n < noteStr.length(); n++) {
                if (n - lastIndex < 20)
                    continue;

                if (noteStr.charAt(n) == ' ') {
                    lore.add("&6" + noteStr.substring(lastIndex, n).trim());
                    lastIndex = n;
                }
            }

            if (lastIndex - noteStr.length() < 20)
                lore.add("&6" + noteStr.substring(lastIndex).trim());

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
            if (delete) lore.add(plugin.getLocale().getMessage("gui.notes.remove").getMessage());

            setButton(num, GuiUtils.createButtonItem(CompatibleMaterial.MAP, TextUtils.formatText(name), TextUtils.formatText(lore)),
                    (event) -> {
                        if (delete) {
                            plugin.getPunishmentManager().getPlayer(toModerate).removeNotes(note);
                            plugin.getDataManager().deleteNote(note);
                            showPage();
                        }
                    });

            num++;
        }

    }
}
