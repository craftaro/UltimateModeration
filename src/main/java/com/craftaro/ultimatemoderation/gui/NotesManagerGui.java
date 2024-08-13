package com.craftaro.ultimatemoderation.gui;

import com.craftaro.core.gui.Gui;
import com.craftaro.core.gui.GuiUtils;
import com.craftaro.core.input.ChatPrompt;
import com.craftaro.core.utils.TextUtils;
import com.craftaro.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.ultimatemoderation.UltimateModeration;
import com.craftaro.ultimatemoderation.punish.PunishmentNote;
import com.craftaro.ultimatemoderation.settings.Settings;
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
        if (this.inventory != null) {
            this.inventory.clear();
        }
        setActionForRange(0, 53, null);

        int numNotes = this.plugin.getPunishmentManager().getPlayer(this.toModerate).getNotes().size();
        this.pages = (int) Math.max(1, Math.ceil(numNotes / ((double) 28)));

        List<PunishmentNote> notes = this.plugin.getPunishmentManager().getPlayer(this.toModerate).getNotes().stream()
                .skip((this.page - 1) * 28).limit(28).collect(Collectors.toList());

        // enable page events
        setNextPage(0, 1, GuiUtils.createButtonItem(XMaterial.ARROW, this.plugin.getLocale().getMessage("gui.general.next").getMessage()));
        setPrevPage(0, 3, GuiUtils.createButtonItem(XMaterial.ARROW, this.plugin.getLocale().getMessage("gui.general.back").getMessage()));
        setOnPage((event) -> showPage());

        // decorate the edges
        ItemStack glass2 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_2.getMaterial(XMaterial.BLUE_STAINED_GLASS_PANE));
        ItemStack glass3 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_3.getMaterial(XMaterial.LIGHT_BLUE_STAINED_GLASS_PANE));

        // edges will be type 3
        mirrorFill(0, 2, true, true, glass3);
        mirrorFill(1, 1, true, true, glass3);

        // decorate corners with type 2
        mirrorFill(0, 0, true, true, glass2);
        mirrorFill(1, 0, true, true, glass2);
        mirrorFill(0, 1, true, true, glass2);

        setButton(5, 5, GuiUtils.createButtonItem(XMaterial.OAK_DOOR,
                        this.plugin.getLocale().getMessage("gui.general.back").getMessage()),
                (event) -> this.guiManager.showGUI(event.player, new PlayerGui(this.plugin, this.toModerate, event.player)));

        if (this.create) {
            setButton(5, 3, GuiUtils.createButtonItem(XMaterial.REDSTONE,
                            this.plugin.getLocale().getMessage("gui.notes.create").getMessage()),
                    (event) -> {
                        ChatPrompt.showPrompt(this.plugin, event.player,
                                this.plugin.getLocale().getMessage("gui.notes.type").toText(),
                                (response) -> {
                                    PunishmentNote note = new PunishmentNote(response.getMessage(),
                                            event.player.getUniqueId(), this.toModerate.getUniqueId(),
                                            System.currentTimeMillis());
                                    this.plugin.getPunishmentManager().getPlayer(this.toModerate).addNotes(note);
                                    this.plugin.getDataHelper().createNote(note);

                                    showPage();
                                }).setOnClose(() -> this.guiManager.showGUI(event.player, new NotesManagerGui(this.plugin, this.toModerate, event.player)));
                    });
        }

        int num = 11;
        for (PunishmentNote note : notes) {
            if (num == 16 || num == 36) {
                num = num + 2;
            }
            String noteStr = note.getNote();

            ArrayList<String> lore = new ArrayList<>();
            int lastIndex = 0;
            for (int n = 0; n < noteStr.length(); n++) {
                if (n - lastIndex < 20) {
                    continue;
                }

                if (noteStr.charAt(n) == ' ') {
                    lore.add("&6" + noteStr.substring(lastIndex, n).trim());
                    lastIndex = n;
                }
            }

            if (lastIndex - noteStr.length() < 20) {
                lore.add("&6" + noteStr.substring(lastIndex).trim());
            }

            String name = lore.get(0);
            lore.remove(0);

            lore.add("");

            SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy");

            lore.add(this.plugin.getLocale().getMessage("gui.notes.createdby")
                    .processPlaceholder("player", Bukkit.getOfflinePlayer(note.getAuthor()).getName())
                    .toText());
            lore.add(this.plugin.getLocale().getMessage("gui.notes.createdon")
                    .processPlaceholder("sent", format.format(new Date(note.getCreationDate())))
                    .toText());
            if (this.delete) {
                lore.add(this.plugin.getLocale().getMessage("gui.notes.remove").toText());
            }

            setButton(num, GuiUtils.createButtonItem(XMaterial.MAP, TextUtils.formatText(name), TextUtils.formatText(lore)),
                    (event) -> {
                        if (this.delete) {
                            this.plugin.getPunishmentManager().getPlayer(this.toModerate).removeNotes(note);
                            this.plugin.getDataHelper().deleteNote(note);
                            showPage();
                        }
                    });

            num++;
        }
    }
}
