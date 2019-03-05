package com.songoda.ultimatemoderation.gui;

import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.punish.Punishment;
import com.songoda.ultimatemoderation.punish.PunishmentType;
import com.songoda.ultimatemoderation.punish.template.Template;
import com.songoda.ultimatemoderation.utils.Methods;
import com.songoda.ultimatemoderation.utils.gui.AbstractAnvilGUI;
import com.songoda.ultimatemoderation.utils.gui.AbstractGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;

public class GUIPunish extends AbstractGUI {

    private final UltimateModeration plugin;
    private final OfflinePlayer toModerate;

    private Template template;
    private boolean justSaved = false;

    private PunishmentType type = PunishmentType.BAN;
    private long duration = Long.MAX_VALUE;
    private String reason = null;

    private String templateName = null;

    private int task;

    public GUIPunish(UltimateModeration plugin, OfflinePlayer toModerate, Template template, Player player) {
        super(player);
        this.plugin = plugin;
        this.toModerate = toModerate;
        if (template != null) {
            this.template = template;
            this.type = template.getPunishmentType();
            this.duration = template.getDuration();
            this.reason = template.getReason();
            this.templateName = template.getTemplateName();
        }

        init(toModerate == null ? plugin.getLocale().getMessage("gui.punish.title.template")
                : plugin.getLocale().getMessage("gui.punish.title", toModerate.getName()), 45);
        if (toModerate != null) runTask();
    }

    @Override
    protected void constructGUI() {
        inventory.clear();
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = ((SkullMeta) head.getItemMeta());
        meta.setOwningPlayer(toModerate);
        head.setItemMeta(meta);

        if (toModerate != null)
            createButton(13, head, "&7&l" + toModerate.getName());

        createButton(22, Material.EMERALD_BLOCK, plugin.getLocale().getMessage("gui.punish.submit"));

        createButton(8, Material.OAK_DOOR, plugin.getLocale().getMessage("gui.general.back"));

        createButton(28, Material.ANVIL, plugin.getLocale().getMessage("gui.punish.type.punishment"),
                "&7" + type.getTranslation(),
                "",
                plugin.getLocale().getMessage("gui.punish.type.punishment.click"));

        if (toModerate != null) {
            createButton(30, Material.MAP, plugin.getLocale().getMessage("gui.punish.type.template"),
                    plugin.getLocale().getMessage("gui.punish.type.template.current",
                            (template == null ? plugin.getLocale().getMessage("gui.general.none") : template.getTemplateName())),
                    "",
                    plugin.getLocale().getMessage(plugin.getTemplateManager().getTemplates().size() == 0 ? "gui.punish.type.template.none" : "gui.punish.type.template.click"));
        } else {
            createButton(30, Material.MAP, plugin.getLocale().getMessage("gui.punish.type.name"),
                    plugin.getLocale().getMessage("gui.punish.type.name.current",
                            (templateName == null ? plugin.getLocale().getMessage("gui.general.none") : templateName)),
                    "",
                    plugin.getLocale().getMessage("gui.punish.type.name.current.click"));
        }

        if (type != PunishmentType.KICK) {
            createButton(32, Material.CLOCK, plugin.getLocale().getMessage("gui.punish.type.duration"),
                    plugin.getLocale().getMessage("gui.punish.type.duration.leftclick"),
                    plugin.getLocale().getMessage("gui.punish.type.duration.rightclick"),
                    "",
                    plugin.getLocale().getMessage("gui.punish.type.duration.current"),
                    "&6" + (duration == Long.MAX_VALUE ? plugin.getLocale().getMessage("gui.general.permanent") : Methods.makeReadable(duration)));
        }

        createButton(34, Material.PAPER, plugin.getLocale().getMessage("gui.punish.type.reason"),
                plugin.getLocale().getMessage("gui.punish.type.reason.click"),
                "",
                plugin.getLocale().getMessage("gui.punish.type.reason.current"), "&6" + reason);
    }

    private void notifyTemplate() {
        if (reason == null || duration == 0 || (justSaved && template != null)) {
            inventory.setItem(4, null);
            return;
        }

        Material material = Material.WHITE_WOOL;
        String name = plugin.getLocale().getMessage("gui.punish.template.create");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(plugin.getLocale().getMessage("gui.punish.template.create2"));

        if (!justSaved && template != null) {
            name = plugin.getLocale().getMessage("gui.punish.template.leftclick");
            lore.clear();
            lore.add(plugin.getLocale().getMessage("gui.punish.template.leftclick2", template.getTemplateName()));
            lore.add("");
            lore.add(plugin.getLocale().getMessage("gui.punish.template.rightclick"));
        }

        if (inventory.getItem(4) != null && inventory.getItem(4).getType() == Material.WHITE_WOOL)
            material = Material.YELLOW_WOOL;

        createButton(4, material, name, lore);
    }

    public void runTask() {
        task = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::notifyTemplate, 10L, 10L);
    }

    @Override
    protected void registerClickables() {
        registerClickable(8, ((player1, inventory1, cursor, slot, type) -> {
            if (toModerate != null)
                new GUIPlayer(plugin, toModerate, player);
            else
                new GUITemplateManager(plugin, player);
        }));

        registerClickable(28, ((player1, inventory1, cursor, slot, type) -> {
            this.type = this.type.next();
            justSaved = false;
            constructGUI();
        }));

        registerClickable(4, ((player1, inventory1, cursor, slot, type) -> {
            if (reason == null || duration == 0) return;

            if (template != null && type == ClickType.LEFT) {
                updateTemplate();
                return;
            }
            nameTemplate();
        }));

        registerClickable(30, ((player1, inventory1, cursor, slot, type) -> {
            if (toModerate == null) {
                nameTemplate();
                return;
            }
            if (plugin.getTemplateManager().getTemplates().size() == 0) return;

            new GUITemplateSelector(plugin, this, player);
        }));
        registerClickable(32, ((player1, inventory1, cursor, slot, type) -> {
            if (this.type == PunishmentType.KICK) return;
            if (type == ClickType.LEFT) {
                AbstractAnvilGUI gui = new AbstractAnvilGUI(player, event -> {
                    this.duration = Methods.parseTime(event.getName());
                    justSaved = false;
                });

                gui.setOnClose((player2, inventory3) -> init(inventory.getTitle(), inventory.getSize()));

                ItemStack item = new ItemStack(Material.PAPER);
                ItemMeta meta = item.getItemMeta();

                meta.setDisplayName(duration == Long.MAX_VALUE || duration == 0 ? "1d 1h 1m" : Methods.makeReadable(duration));
                item.setItemMeta(meta);

                gui.setSlot(AbstractAnvilGUI.AnvilSlot.INPUT_LEFT, item);
                gui.open();
            } else {
                duration = Long.MAX_VALUE;
                constructGUI();
            }
        }));


        registerClickable(34, ((player1, inventory1, cursor, slot, type) -> {
            AbstractAnvilGUI gui = new AbstractAnvilGUI(player, event -> {
                this.reason = event.getName();
                justSaved = false;
            });

            gui.setOnClose((player2, inventory3) -> init(inventory.getTitle(), inventory.getSize()));

            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(reason == null ? plugin.getLocale().getMessage("gui.general.reason") : reason);
            item.setItemMeta(meta);

            gui.setSlot(AbstractAnvilGUI.AnvilSlot.INPUT_LEFT, item);
            gui.open();
        }));

        registerClickable(22, ((player1, inventory1, cursor, slot, type1) -> {
            if (toModerate == null) {
                if (reason == null || duration == 0 || templateName == null) return;

                if (template == null)
                    finishTemplate();
                else
                    updateTemplate();
                return;
            }

            switch (type) {
                case BAN:
                case MUTE:
                case WARNING:
                    new Punishment(type, duration, reason).execute(player, toModerate);
                    break;
                case KICK:
                    new Punishment(type, reason).execute(player, toModerate);
                    break;
            }
            new GUIPlayer(plugin, toModerate, player);
        }));
    }

    @Override
    protected void registerOnCloses() {
        registerOnClose(((player1, inventory1) -> Bukkit.getScheduler().cancelTask(task)));
    }

    private void nameTemplate() {
        AbstractAnvilGUI gui = new AbstractAnvilGUI(player, event -> {
            this.templateName = event.getName();

            if (toModerate != null)
                finishTemplate();

            justSaved = true;
        });

        gui.setOnClose((player2, inventory3) ->
                init(inventory.getTitle(), inventory.getSize()));

        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(plugin.getLocale().getMessage("gui.general.templatename"));
        item.setItemMeta(meta);

        gui.setSlot(AbstractAnvilGUI.AnvilSlot.INPUT_LEFT, item);
        gui.open();
    }

    private void updateTemplate() {
        Template template = new Template(this.type, this.duration, this.reason, this.template.getCreator(), this.templateName);
        plugin.getTemplateManager().updateTemplate(this.template.getUUID(), template);
        justSaved = true;
        if (toModerate == null)
            new GUITemplateManager(plugin, player);
    }

    private void finishTemplate() {
        Template template = new Template(this.type, this.duration, this.reason, player, templateName);
        plugin.getTemplateManager().addTemplate(template);
        this.template = template;
        if (toModerate == null)
            new GUITemplateManager(plugin, player);
    }

    public void setTemplate(Template template) {
        this.justSaved = true;
        this.template = template;
    }

    public void setType(PunishmentType type) {
        this.type = type;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
