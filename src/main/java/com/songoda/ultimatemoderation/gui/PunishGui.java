package com.songoda.ultimatemoderation.gui;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.gui.AnvilGui;
import com.songoda.core.gui.Gui;
import com.songoda.core.gui.GuiUtils;
import com.songoda.core.utils.ItemUtils;
import com.songoda.core.utils.TextUtils;
import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.punish.Punishment;
import com.songoda.ultimatemoderation.punish.PunishmentType;
import com.songoda.ultimatemoderation.punish.template.Template;
import com.songoda.ultimatemoderation.settings.Settings;
import com.songoda.ultimatemoderation.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class PunishGui extends Gui {

    private final UltimateModeration plugin;
    private final Player player;
    private final OfflinePlayer toModerate;

    private Template template;
    private boolean justSaved = false;

    private PunishmentType type = PunishmentType.BAN;
    private long duration = -1;
    private String reason = null;

    private String templateName = null;

    private int task;

    public PunishGui(UltimateModeration plugin, OfflinePlayer toModerate, Template template, Player player) {
        super(5);
        setDefaultItem(null);
        this.player = player;
        this.plugin = plugin;
        this.toModerate = toModerate;
        if (template != null) {
            this.template = template;
            this.type = template.getPunishmentType();
            this.duration = template.getDuration();
            this.reason = template.getReason();
            this.templateName = template.getName();
        }

        setTitle(toModerate == null ? plugin.getLocale().getMessage("gui.punish.title.template").getMessage()
                : plugin.getLocale().getMessage("gui.punish.title")
                .processPlaceholder("toModerate", toModerate.getName()).getMessage());
        if (toModerate != null) runTask();

        setOnClose((event) -> Bukkit.getScheduler().cancelTask(task));
        paint();
    }

    public void paint() {
        if (inventory != null)
            inventory.clear();
        setActionForRange(0, 53, null);

        // decorate the edges
        ItemStack glass2 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_2.getMaterial(CompatibleMaterial.BLUE_STAINED_GLASS_PANE));
        ItemStack glass3 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_3.getMaterial(CompatibleMaterial.LIGHT_BLUE_STAINED_GLASS_PANE));

        // edges will be type 3
        GuiUtils.mirrorFill(this, 0, 2, true, true, glass3);
        GuiUtils.mirrorFill(this, 1, 1, true, true, glass3);

        // decorate corners with type 2
        GuiUtils.mirrorFill(this, 0, 0, true, true, glass2);
        GuiUtils.mirrorFill(this, 1, 0, true, true, glass2);
        GuiUtils.mirrorFill(this, 2, 0, false, true, glass2);
        GuiUtils.mirrorFill(this, 0, 1, true, true, glass2);

        if (toModerate != null)
            setItem(13, GuiUtils.createButtonItem(ItemUtils.getPlayerSkull(toModerate),
                    TextUtils.formatText("&6&l" + toModerate.getName())));

        if (player.hasPermission("um." + type.toString().toLowerCase()))
            setButton(22, GuiUtils.createButtonItem(CompatibleMaterial.EMERALD_BLOCK,
                    plugin.getLocale().getMessage("gui.punish.submit").getMessage()),
                    (event) -> {
                        if (!player.hasPermission("um." + type.toString().toLowerCase())) return;
                        if (duration == -1 && type == PunishmentType.BAN && !player.hasPermission("um.ban.permanent"))
                            return;

                        if (toModerate == null) {
                            if (reason == null || templateName == null) return;

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
                        player.closeInventory();
                    });

        setButton(8, GuiUtils.createButtonItem(CompatibleMaterial.OAK_DOOR,
                plugin.getLocale().getMessage("gui.general.back").getMessage()),
                (event) -> {
                    if (toModerate != null)
                        guiManager.showGUI(player, new PlayerGui(plugin, toModerate, player));
                    else
                        guiManager.showGUI(player, new TemplateManagerGui(plugin, player));
                });

        setButton(28, GuiUtils.createButtonItem(CompatibleMaterial.ANVIL,
                plugin.getLocale().getMessage("gui.punish.type.punishment").getMessage(),
                TextUtils.formatText("&7" + type.getTranslation()),
                "",
                plugin.getLocale().getMessage("gui.punish.type.punishment.click").getMessage()),
                (event) -> {
                    this.type = this.type.next();
                    justSaved = false;
                    paint();
                });

        ItemStack templateItem = toModerate != null ? GuiUtils.createButtonItem(CompatibleMaterial.MAP,
                plugin.getLocale().getMessage("gui.punish.type.template").getMessage(),
                plugin.getLocale().getMessage("gui.punish.type.template.current")
                        .processPlaceholder("template",
                                template == null
                                        ? plugin.getLocale().getMessage("gui.general.none").getMessage()
                                        : template.getName()).getMessage(),
                "",
                plugin.getLocale().getMessage(plugin.getTemplateManager().getTemplates().size() == 0
                        ? "gui.punish.type.template.none"
                        : "gui.punish.type.template.click").getMessage())
                : GuiUtils.createButtonItem(CompatibleMaterial.MAP,
                plugin.getLocale().getMessage("gui.punish.type.name").getMessage(),
                plugin.getLocale().getMessage("gui.punish.type.name.current")
                        .processPlaceholder("name",
                                templateName == null
                                        ? plugin.getLocale().getMessage("gui.punish.type.name.current").getMessage()
                                        : templateName).getMessage(),
                "",
                plugin.getLocale().getMessage("gui.punish.type.name.current.click").getMessage());

        setButton(30, templateItem, (event) -> {
            if (toModerate == null) {
                nameTemplate();
                return;
            }
            if (plugin.getTemplateManager().getTemplates().size() == 0) return;

            if (player.hasPermission("um.templates.use"))
                guiManager.showGUI(player, new TemplateSelectorGui(plugin, this, player));
        });

        if (type != PunishmentType.KICK) {
            setButton(32, GuiUtils.createButtonItem(CompatibleMaterial.CLOCK,
                    plugin.getLocale().getMessage("gui.punish.type.duration").getMessage(),
                    plugin.getLocale().getMessage("gui.punish.type.duration.leftclick").getMessage(),
                    plugin.getLocale().getMessage("gui.punish.type.duration.rightclick").getMessage(),
                    "",
                    plugin.getLocale().getMessage("gui.punish.type.duration.current").getMessage(),
                    TextUtils.formatText("&6" + (duration == -1 ? plugin.getLocale().getMessage("gui.general.permanent").getMessage()
                            : Methods.makeReadable(duration)))),
                    (event) -> {
                        if (this.type == PunishmentType.KICK) return;
                        if (event.clickType == ClickType.LEFT) {
                            AnvilGui gui = new AnvilGui(player, this);
                            gui.setAction(evt -> {
                                this.duration = Methods.parseTime(gui.getInputText());
                                justSaved = false;
                                guiManager.showGUI(player, this);
                                paint();
                            });

                            ItemStack item = new ItemStack(Material.PAPER);
                            ItemMeta meta = item.getItemMeta();

                            meta.setDisplayName(duration == -1 || duration == 0 ? "1d 1h 1m" : Methods.makeReadable(duration));
                            item.setItemMeta(meta);

                            gui.setInput(item);
                            guiManager.showGUI(player, gui);
                        } else {
                            duration = -1;
                            paint();
                        }
                    });
        }

        setButton(34, GuiUtils.createButtonItem(CompatibleMaterial.PAPER,
                plugin.getLocale().getMessage("gui.punish.type.reason").getMessage(),
                plugin.getLocale().getMessage("gui.punish.type.reason.click").getMessage(),
                "",
                plugin.getLocale().getMessage("gui.punish.type.reason.current").getMessage(),
                TextUtils.formatText("&6" + reason)), (event) -> {

            AnvilGui gui = new AnvilGui(player, this);
            gui.setAction(evnt -> {
                this.reason = gui.getInputText();
                justSaved = false;
                guiManager.showGUI(player, this);
                paint();
            });

            ItemStack item = GuiUtils.createButtonItem(CompatibleMaterial.PAPER,
                    reason == null ? plugin.getLocale().getMessage("gui.general.reason").getMessage() : reason);

            gui.setInput(item);
            guiManager.showGUI(player, gui);
        });
    }

    private void notifyTemplate() {
        if (reason == null || (justSaved && template != null)) {
            inventory.setItem(4, null);
            return;
        }

        CompatibleMaterial material = CompatibleMaterial.WHITE_WOOL;
        String name = plugin.getLocale().getMessage("gui.punish.template.create").getMessage();
        ArrayList<String> lore = new ArrayList<>();
        lore.add(plugin.getLocale().getMessage("gui.punish.template.create2").getMessage());

        if (!justSaved && template != null) {
            name = plugin.getLocale().getMessage("gui.punish.template.leftclick").getMessage();
            lore.clear();
            lore.add(plugin.getLocale().getMessage("gui.punish.template.leftclick2")
                    .processPlaceholder("template", template.getName()).getMessage());
            lore.add("");
            lore.add(plugin.getLocale().getMessage("gui.punish.template.rightclick").getMessage());
        }

        if (getItem(4) != null && CompatibleMaterial.getMaterial(getItem(4)) == CompatibleMaterial.WHITE_WOOL)
            material = CompatibleMaterial.YELLOW_WOOL;

        setButton(4, GuiUtils.createButtonItem(material, name, lore), (event) -> {

            if (reason == null || duration == 0) return;

            if (template != null && event.clickType == ClickType.LEFT) {
                updateTemplate();
                return;
            }
            nameTemplate();
        });
    }

    public void runTask() {
        task = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::notifyTemplate, 10L, 10L);
    }

    private void nameTemplate() {
        AnvilGui gui = new AnvilGui(player, this);
        gui.setAction(event -> {
            this.templateName = gui.getInputText();

            if (reason != null && templateName != null) {
                if (template == null)
                    finishTemplate();
                else
                    updateTemplate();
            }

            justSaved = true;
            guiManager.showGUI(player, this);
            paint();
        });

        ItemStack item = GuiUtils.createButtonItem(CompatibleMaterial.PAPER,
                template == null ? plugin.getLocale().getMessage("gui.general.templatename").getMessage() : template.getName());

        gui.setInput(item);
        guiManager.showGUI(player, gui);
    }

    private void updateTemplate() {
        Template template = new Template(this.type, this.duration, this.reason, this.template.getCreator(), this.templateName);
        plugin.getTemplateManager().removeTemplate(this.template);
        plugin.getTemplateManager().addTemplate(template);
        plugin.getDataManager().deleteTemplate(this.template);
        plugin.getDataManager().createTemplate(template);
        justSaved = true;
        if (toModerate == null)
            guiManager.showGUI(player, new TemplateManagerGui(plugin, player));
    }

    private void finishTemplate() {
        Template template = new Template(this.type, this.duration, this.reason, player, templateName);
        plugin.getTemplateManager().addTemplate(template);
        plugin.getDataManager().createTemplate(template);
        this.template = template;
        if (toModerate == null)
            guiManager.showGUI(player, new TemplateManagerGui(plugin, player));
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
