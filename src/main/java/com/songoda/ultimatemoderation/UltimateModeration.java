package com.songoda.ultimatemoderation;

import com.songoda.ultimatemoderation.command.CommandManager;
import com.songoda.ultimatemoderation.listeners.*;
import com.songoda.ultimatemoderation.punish.AppliedPunishment;
import com.songoda.ultimatemoderation.punish.PunishmentNote;
import com.songoda.ultimatemoderation.punish.PunishmentType;
import com.songoda.ultimatemoderation.punish.player.PlayerPunishData;
import com.songoda.ultimatemoderation.punish.player.PunishmentManager;
import com.songoda.ultimatemoderation.punish.template.Template;
import com.songoda.ultimatemoderation.punish.template.TemplateManager;
import com.songoda.ultimatemoderation.staffchat.StaffChatManager;
import com.songoda.ultimatemoderation.storage.Storage;
import com.songoda.ultimatemoderation.storage.StorageRow;
import com.songoda.ultimatemoderation.storage.types.StorageMysql;
import com.songoda.ultimatemoderation.storage.types.StorageYaml;
import com.songoda.ultimatemoderation.tasks.SlowModeTask;
import com.songoda.ultimatemoderation.tickets.Ticket;
import com.songoda.ultimatemoderation.tickets.TicketManager;
import com.songoda.ultimatemoderation.tickets.TicketResponse;
import com.songoda.ultimatemoderation.tickets.TicketStatus;
import com.songoda.ultimatemoderation.utils.Methods;
import com.songoda.ultimatemoderation.utils.Metrics;
import com.songoda.ultimatemoderation.utils.ServerVersion;
import com.songoda.ultimatemoderation.utils.SettingsManager;
import com.songoda.ultimatemoderation.utils.gui.AbstractGUI;
import com.songoda.ultimatemoderation.utils.updateModules.LocaleModule;
import com.songoda.update.Plugin;
import com.songoda.update.SongodaUpdate;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class UltimateModeration extends JavaPlugin {
    private static CommandSender console = Bukkit.getConsoleSender();
    private static UltimateModeration INSTANCE;
    private References references;

    private ServerVersion serverVersion = ServerVersion.fromPackageName(Bukkit.getServer().getClass().getPackage().getName());

    private TicketManager ticketManager;
    private TemplateManager templateManager;
    private SettingsManager settingsManager;
    private CommandManager commandManager;
    private PunishmentManager punishmentManager;
    private StaffChatManager staffChatManager;

    private Locale locale;
    private Storage storage;

    public static UltimateModeration getInstance() {
        return INSTANCE;
    }

    @Override
    public void onEnable() {
        INSTANCE = this;

        console.sendMessage(Methods.formatText("&a============================="));
        console.sendMessage(Methods.formatText("&7UltimateModeration " + this.getDescription().getVersion() + " by &5Songoda <3!"));
        console.sendMessage(Methods.formatText("&7Action: &aEnabling&7..."));

        this.settingsManager = new SettingsManager(this);
        this.setupConfig();

        // Setup language
        String langMode = SettingsManager.Setting.LANGUGE_MODE.getString();
        Locale.init(this);
        Locale.saveDefaultLocale("en_US");
        this.locale = Locale.getLocale(getConfig().getString("System.Language Mode", langMode));

        //Running Songoda Updater
        Plugin plugin = new Plugin(this, 29);
        plugin.addModule(new LocaleModule());
        SongodaUpdate.load(plugin);

        this.references = new References();

        // Setup Managers
        this.ticketManager = new TicketManager();
        this.templateManager = new TemplateManager();
        this.commandManager = new CommandManager(this);
        this.punishmentManager = new PunishmentManager();
        this.staffChatManager = new StaffChatManager();

        // Load data
        this.checkStorage();
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, this::loadFromFile, 1L);

        // Register Listeners
        AbstractGUI.initializeListeners(this);
        Bukkit.getPluginManager().registerEvents(new CommandListener(this), this);
        Bukkit.getPluginManager().registerEvents(new DeathListener(this), this);
        Bukkit.getPluginManager().registerEvents(new MoveListener(this), this);
        Bukkit.getPluginManager().registerEvents(new DropListener(this), this);
        Bukkit.getPluginManager().registerEvents(new InventoryListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ChatListener(this), this);
        Bukkit.getPluginManager().registerEvents(new LoginListener(this), this);

        // Start tasks
        SlowModeTask.startTask(this);

        // Starting Metrics
        new Metrics(this);

        int timeout = SettingsManager.Setting.AUTOSAVE.getInt() * 60 * 20;
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> storage.doSave(), timeout, timeout);
        console.sendMessage(Methods.formatText("&a============================="));
    }

    @Override
    public void onDisable() {
        storage.doSave();
        this.storage.closeConnection();
        console.sendMessage(Methods.formatText("&a============================="));
        console.sendMessage(Methods.formatText("&7UltimateModeration " + this.getDescription().getVersion() + " by &5Songoda <3!"));
        console.sendMessage(Methods.formatText("&7Action: &cDisabling&7..."));
        console.sendMessage(Methods.formatText("&a============================="));
    }

    private void checkStorage() {
        if (getConfig().getBoolean("Database.Activate Mysql Support")) {
            this.storage = new StorageMysql(this);
        } else {
            this.storage = new StorageYaml(this);
        }
    }

    private void loadFromFile() {
        if (storage.containsGroup("templates")) {
            for (StorageRow row : storage.getRowsByGroup("templates")) {
                Template template = new Template(PunishmentType.valueOf(row.get("type").asString()),
                        row.get("duration").asLong(),
                        row.get("reason").asString(),
                        UUID.fromString(row.get("creator").asString()),
                        row.get("name").asString(),
                        UUID.fromString(row.get("uuid").asString()));
                templateManager.addTemplate(template);
            }
        }
        if (storage.containsGroup("punishments")) {
            for (StorageRow row : storage.getRowsByGroup("punishments")) {
                UUID playerUUID = UUID.fromString(row.get("victim").asString());
                AppliedPunishment appliedPunishment = new AppliedPunishment(PunishmentType.valueOf(row.get("type").asString()),
                        row.get("duration").asLong(),
                        row.get("reason").asString(),
                        UUID.fromString(row.get("victim").asString()),
                        UUID.fromString(row.get("punisher").asString()),
                        row.get("expiration").asLong(),
                        playerUUID);
                PlayerPunishData playerPunishData = getPunishmentManager().getPlayer(playerUUID);
                playerPunishData.addPunishment(appliedPunishment);
                playerPunishData.audit();
            }
        }

        if (storage.containsGroup("notes")) {
            for (StorageRow row : storage.getRowsByGroup("notes")) {
                UUID playerUUID = UUID.fromString(row.get("subject").asString());
                PunishmentNote note = new PunishmentNote(UUID.fromString(row.get("uuid").asString()),
                        row.get("note").asString(),
                        UUID.fromString(row.get("author").asString()),
                        UUID.fromString(row.get("subject").asString()),
                        row.get("creation").asLong());

                PlayerPunishData playerPunishData = getPunishmentManager().getPlayer(playerUUID);
                playerPunishData.addNotes(note);
            }
        }

        if (storage.containsGroup("tickets")) {
            for (StorageRow row : storage.getRowsByGroup("tickets")) {

                int id = row.get("id").asInt();
                Ticket ticket = new Ticket(
                        UUID.fromString(row.get("player").asString()),
                        row.get("subject").asString(),
                        row.get("type").asString());
                ticket.setTicketId(id);
                ticket.setLocation(Methods.unserializeLocation(row.get("location").asString()));
                ticket.setStatus(TicketStatus.valueOf(row.get("status").asString()));
                ticketManager.addTicket(ticket, id);
            }
        }

        if (storage.containsGroup("ticketresponses")) {
            for (StorageRow row : storage.getRowsByGroup("ticketresponses")) {
                int id = row.get("ticketid").asInt();
                TicketResponse ticketResponse = new TicketResponse(
                        UUID.fromString(row.get("author").asString()),
                                row.get("message").asString(),
                                row.get("posted").asLong());
                ticketResponse.setTicketId(id);
                ticketManager.getTicket(id).addResponse(ticketResponse);

            }
        }
        storage.doSave();
    }

    public ServerVersion getServerVersion() {
        return serverVersion;
    }

    public boolean isServerVersion(ServerVersion version) {
        return serverVersion == version;
    }
    public boolean isServerVersion(ServerVersion... versions) {
        return ArrayUtils.contains(versions, serverVersion);
    }

    public boolean isServerVersionAtLeast(ServerVersion version) {
        return serverVersion.ordinal() >= version.ordinal();
    }

    private void setupConfig() {
        settingsManager.updateSettings();
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();
    }

    public void reload() {
        locale.reloadMessages();
        references = new References();
        this.setupConfig();
        saveConfig();
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public SettingsManager getSettingsManager() {
        return settingsManager;
    }

    public Locale getLocale() {
        return locale;
    }

    public References getReferences() {
        return references;
    }

    public TemplateManager getTemplateManager() {
        return templateManager;
    }

    public PunishmentManager getPunishmentManager() {
        return punishmentManager;
    }

    public TicketManager getTicketManager() {
        return ticketManager;
    }

    public StaffChatManager getStaffChatManager() {
        return staffChatManager;
    }
}
