package com.songoda.ultimatemoderation.gui;

import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.punish.Punishment;
import com.songoda.ultimatemoderation.punish.PunishmentType;
import com.songoda.ultimatemoderation.punish.template.Template;
import com.songoda.ultimatemoderation.utils.Methods;
import com.songoda.ultimatemoderation.utils.ServerVersion;
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
    private long duration = -1;
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

        init(toModerate == null ? plugin.getLocale().getMessage("gui.punish.title.template").getMessage()
                : plugin.getLocale().getMessage("gui.punish.title")
                .processPlaceholder("toModerate", toModerate.getName()).getMessage(), 45);
        if (toModerate != null) runTask();
    }

    @Override
    protected void constructGUI() {
        inventory.clear();

        if (toModerate != null) {
            ItemStack head = new ItemStack(plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.PLAYER_HEAD : Material.valueOf("SKULL_ITEM"), 1, (byte) 3);
            SkullMeta meta = ((SkullMeta) head.getItemMeta());
            if (plugin.isServerVersionAtLeast(ServerVersion.V1_13))
                meta.setOwningPlayer(toModerate);
            else
                meta.setOwner(toModerate.getName());
            head.setItemMeta(meta);

            createButton(13, head, "&7&l" + toModerate.getName());
        }

        if (player.hasPermission("um." + type.toString().toLowerCase()))
            createButton(22, Material.EMERALD_BLOCK, plugin.getLocale().getMessage("gui.punish.submit").getMessage());

        createButton(8, plugin.isServerVersionAtLeast(ServerVersion.V1_13)
                ? Material.OAK_DOOR
                : Material.valueOf("WOOD_DOOR"), plugin.getLocale().getMessage("gui.general.back").getMessage());

        createButton(28, Material.ANVIL,
                plugin.getLocale().getMessage("gui.punish.type.punishment").getMessage(),
                "&7" + type.getTranslation(),
                "",
                plugin.getLocale().getMessage("gui.punish.type.punishment.click").getMessage());

        if (toModerate != null) {
            createButton(30, Material.MAP,
                    plugin.getLocale().getMessage("gui.punish.type.template").getMessage(),
                    plugin.getLocale().getMessage("gui.punish.type.template.current")
                            .processPlaceholder("template",
                                    template == null
                                            ? plugin.getLocale().getMessage("gui.general.none")
                                            : template.getTemplateName()).getMessage(),
                    "",
                    plugin.getLocale().getMessage(plugin.getTemplateManager().getTemplates().size() == 0
                            ? "gui.punish.type.template.none"
                            : "gui.punish.type.template.click").getMessage());
        } else {
            createButton(30, Material.MAP,
                    plugin.getLocale().getMessage("gui.punish.type.name").getMessage(),
                    plugin.getLocale().getMessage("gui.punish.type.name.current")
                            .processPlaceholder("template",
                                    templateName == null
                                            ? plugin.getLocale().getMessage("gui.punish.type.name.current").getMessage()
                                            : templateName).getMessage(),
                    "",
                    plugin.getLocale().getMessage("gui.punish.type.name.current.click").getMessage());
        }

        if (type != PunishmentType.KICK) {
            createButton(32, plugin.isServerVersionAtLeast(ServerVersion.V1_13)
                    ? Material.CLOCK
                    : Material.valueOf("WATCH"),
                    plugin.getLocale().getMessage("gui.punish.type.duration").getMessage(),
                    plugin.getLocale().getMessage("gui.punish.type.duration.leftclick").getMessage(),
                    plugin.getLocale().getMessage("gui.punish.type.duration.rightclick").getMessage(),
                    "",
                    plugin.getLocale().getMessage("gui.punish.type.duration.current").getMessage(),
                    "&6" + (duration == -1 ? plugin.getLocale().getMessage("gui.general.permanent").getMessage()
                            : Methods.makeReadable(duration)));
        }

        createButton(34, Material.PAPER,
                plugin.getLocale().getMessage("gui.punish.type.reason").getMessage(),
                plugin.getLocale().getMessage("gui.punish.type.reason.click").getMessage(),
                "",
                plugin.getLocale().getMessage("gui.punish.type.reason.current").getMessage(),
                "&6" + reason);
    }

    private void notifyTemplate() {
        if (reason == null || duration == 0 || (justSaved && template != null)) {
            inventory.setItem(4, null);
            return;
        }

        Material material = plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.WHITE_WOOL : Material.valueOf("WOOL");
        String name = plugin.getLocale().getMessage("gui.punish.template.create").getMessage();
        ArrayList<String> lore = new ArrayList<>();
        lore.add(plugin.getLocale().getMessage("gui.punish.template.create2").getMessage());

        if (!justSaved && template != null) {
            name = plugin.getLocale().getMessage("gui.punish.template.leftclick").getMessage();
            lore.clear();
            lore.add(plugin.getLocale().getMessage("gui.punish.template.leftclick2")
                    .processPlaceholder("template", template.getTemplateName()).getMessage());
            lore.add("");
            lore.add(plugin.getLocale().getMessage("gui.punish.template.rightclick").getMessage());
        }

        if (plugin.isServerVersionAtLeast(ServerVersion.V1_13) && inventory.getItem(4) != null && inventory.getItem(4).getType() == Material.WHITE_WOOL)
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

            if (player.hasPermission("um.templates.use")) new GUITemplateSelector(plugin, this, player);
        }));

        registerClickable(32, ((player1, inventory1, cursor, slot, type) -> {
            if (this.type == PunishmentType.KICK) return;
            if (type == ClickType.LEFT) {
                AbstractAnvilGUI gui = new AbstractAnvilGUI(player, event -> {
                    this.duration = Methods.parseTime(event.getName());
                    justSaved = false;
                });

                gui.setOnClose((player2, inventory3) -> init(setTitle, inventory.getSize()));

                ItemStack item = new ItemStack(Material.PAPER);
                ItemMeta meta = item.getItemMeta();

                meta.setDisplayName(duration == -1 || duration == 0 ? "1d 1h 1m" : Methods.makeReadable(duration));
                item.setItemMeta(meta);

                gui.setSlot(AbstractAnvilGUI.AnvilSlot.INPUT_LEFT, item);
                gui.open();
            } else {
                duration = -1;
                constructGUI();
            }
        }));


        registerClickable(34, ((player1, inventory1, cursor, slot, type) -> {
            AbstractAnvilGUI gui = new AbstractAnvilGUI(player, event -> {
                this.reason = event.getName();
                justSaved = false;
            });

            gui.setOnClose((player2, inventory3) -> init(setTitle, inventory.getSize()));

            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(reason == null ? plugin.getLocale().getMessage("gui.general.reason").getMessage() : reason);
            item.setItemMeta(meta);

            gui.setSlot(AbstractAnvilGUI.AnvilSlot.INPUT_LEFT, item);
            gui.open();
        }));

        registerClickable(22, ((player1, inventory1, cursor, slot, type1) -> {
            if (!player.hasPermission("um." + type.toString().toLowerCase())) return;
            if (duration == -1 && type == PunishmentType.BAN && !player.hasPermission("um.ban.permanent")) return;

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
                init(setTitle, inventory.getSize()));

        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(plugin.getLocale().getMessage("gui.general.templatename").getMessage());
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
