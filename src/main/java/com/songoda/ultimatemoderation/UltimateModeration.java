package com.songoda.ultimatemoderation;

import com.songoda.core.SongodaCore;
import com.songoda.core.SongodaPlugin;
import com.songoda.core.commands.CommandManager;
import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.compatibility.ServerVersion;
import com.songoda.core.configuration.Config;
import com.songoda.core.database.DataMigrationManager;
import com.songoda.core.database.DatabaseConnector;
import com.songoda.core.database.MySQLConnector;
import com.songoda.core.database.SQLiteConnector;
import com.songoda.core.gui.GuiManager;
import com.songoda.ultimatemoderation.commands.*;
import com.songoda.ultimatemoderation.database.DataManager;
import com.songoda.ultimatemoderation.database.migrations._1_InitialMigration;
import com.songoda.ultimatemoderation.listeners.*;
import com.songoda.ultimatemoderation.moderate.ModerationManager;
import com.songoda.ultimatemoderation.punish.AppliedPunishment;
import com.songoda.ultimatemoderation.punish.PunishmentNote;
import com.songoda.ultimatemoderation.punish.PunishmentType;
import com.songoda.ultimatemoderation.punish.player.PunishmentManager;
import com.songoda.ultimatemoderation.punish.template.Template;
import com.songoda.ultimatemoderation.punish.template.TemplateManager;
import com.songoda.ultimatemoderation.settings.Settings;
import com.songoda.ultimatemoderation.staffchat.StaffChatManager;
import com.songoda.ultimatemoderation.storage.Storage;
import com.songoda.ultimatemoderation.storage.StorageRow;
import com.songoda.ultimatemoderation.storage.types.StorageYaml;
import com.songoda.ultimatemoderation.tasks.SlowModeTask;
import com.songoda.ultimatemoderation.tickets.Ticket;
import com.songoda.ultimatemoderation.tickets.TicketManager;
import com.songoda.ultimatemoderation.tickets.TicketResponse;
import com.songoda.ultimatemoderation.tickets.TicketStatus;
import com.songoda.ultimatemoderation.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class UltimateModeration extends SongodaPlugin {
    private static UltimateModeration INSTANCE;

    private final GuiManager guiManager = new GuiManager(this);
    private TicketManager ticketManager;
    private TemplateManager templateManager;
    private CommandManager commandManager;
    private PunishmentManager punishmentManager;
    private StaffChatManager staffChatManager;
    private ModerationManager moderationManager;

    private DatabaseConnector databaseConnector;
    private DataMigrationManager dataMigrationManager;
    private DataManager dataManager;

    public static UltimateModeration getInstance() {
        return INSTANCE;
    }

    @Override
    public void onPluginLoad() {
        INSTANCE = this;
    }

    @Override
    public void onPluginDisable() {
    }

    @Override
    public void onPluginEnable() {
        // Run Songoda Updater
        SongodaCore.registerPlugin(this, 29, CompatibleMaterial.DIAMOND_CHESTPLATE);

        // Setup Config
        Settings.setupConfig();
        this.setLocale(Settings.LANGUGE_MODE.getString(), false);

        // Register commands
        this.commandManager = new CommandManager(this);
        this.commandManager.addCommand(new CommandUltimateModeration(this))
                .addSubCommands(
                        new CommandReload(this),
                        new CommandSettings(this, guiManager),
                        new CommandHelp(this)
                );
        this.commandManager.addCommand(new CommandBan(this));
        this.commandManager.addCommand(new CommandClearChat(this));
        this.commandManager.addCommand(new CommandKick(this));
        this.commandManager.addCommand(new CommandMute(this));
        this.commandManager.addCommand(new CommandRandomPlayer(this));
        this.commandManager.addCommand(new CommandRunTemplate(this));
        this.commandManager.addCommand(new CommandSlowMode(this));
        this.commandManager.addCommand(new CommandStaffChat(this));
        this.commandManager.addCommand(new CommandTicket(this));
        this.commandManager.addCommand(new CommandToggleChat(this));
        this.commandManager.addCommand(new CommandUnBan(this));
        this.commandManager.addCommand(new CommandUnMute(this));
        this.commandManager.addCommand(new CommandVanish(this));
        this.commandManager.addCommand(new CommandWarn(this));

        // Setup Managers
        this.ticketManager = new TicketManager();
        this.templateManager = new TemplateManager();
        this.punishmentManager = new PunishmentManager();
        this.staffChatManager = new StaffChatManager();
        this.moderationManager = new ModerationManager(this);


        try {
            if (Settings.MYSQL_ENABLED.getBoolean()) {
                String hostname = Settings.MYSQL_HOSTNAME.getString();
                int port = Settings.MYSQL_PORT.getInt();
                String database = Settings.MYSQL_DATABASE.getString();
                String username = Settings.MYSQL_USERNAME.getString();
                String password = Settings.MYSQL_PASSWORD.getString();
                boolean useSSL = Settings.MYSQL_USE_SSL.getBoolean();

                this.databaseConnector = new MySQLConnector(this, hostname, port, database, username, password, useSSL);
                this.getLogger().info("Data handler connected using MySQL.");
            } else {
                this.databaseConnector = new SQLiteConnector(this);
                this.getLogger().info("Data handler connected using SQLite.");
            }

            this.dataManager = new DataManager(this.databaseConnector, this);
            this.dataMigrationManager = new DataMigrationManager(this.databaseConnector, this.dataManager,
                    new _1_InitialMigration());
            this.dataMigrationManager.runMigrations();

        } catch (Exception ex) {
            this.getLogger().severe("Fatal error trying to connect to database. " +
                    "Please make sure all your connection settings are correct and try again. Plugin has been disabled.");
            emergencyStop();
            return;
        }

        Bukkit.getScheduler().runTaskLaterAsynchronously(this, () -> {
            // Legacy Data
            File folder = getDataFolder();
            File dataFile = new File(folder, "data.yml");

            boolean converted = false;
            if (dataFile.exists()) {
                converted = true;
                Storage storage = new StorageYaml(this);
                console.sendMessage("[" + getDescription().getName() + "] " + ChatColor.RED + "Conversion process starting DO NOT turn off your server... " +
                        "UltimateModeration hasn't fully loaded yet so its best users don't interact with the plugin until conversion completes.");
                if (storage.containsGroup("templates")) {
                    for (StorageRow row : storage.getRowsByGroup("templates")) {
                        Template template = new Template(PunishmentType.valueOf(row.get("type").asString()),
                                row.get("duration").asLong(),
                                row.get("reason").asString(),
                                UUID.fromString(row.get("creator").asString()),
                                row.get("name").asString());
                        dataManager.createTemplate(template);
                    }
                }

                if (storage.containsGroup("punishments")) {
                    for (StorageRow row : storage.getRowsByGroup("punishments")) {
                        AppliedPunishment appliedPunishment = new AppliedPunishment(PunishmentType.valueOf(row.get("type").asString()),
                                row.get("duration").asLong(),
                                row.get("reason").asString(),
                                UUID.fromString(row.get("victim").asString()),
                                row.get("punisher").asObject() == null ? null : UUID.fromString(row.get("punisher").asString()),
                                row.get("expiration").asLong());
                        dataManager.createAppliedPunishment(appliedPunishment);
                    }
                }

                if (storage.containsGroup("notes")) {
                    for (StorageRow row : storage.getRowsByGroup("notes")) {
                        PunishmentNote note = new PunishmentNote(row.get("note").asString(),
                                UUID.fromString(row.get("author").asString()),
                                UUID.fromString(row.get("subject").asString()),
                                row.get("creation").asLong());
                        dataManager.createNote(note);
                    }
                }

                Map<Integer, Ticket> tickets = new HashMap<>();
                if (storage.containsGroup("tickets")) {
                    for (StorageRow row : storage.getRowsByGroup("tickets")) {

                        int id = Integer.parseInt(row.get("id").asString());
                        Ticket ticket = new Ticket(
                                UUID.fromString(row.get("player").asString()),
                                row.get("subject").asString(),
                                row.get("type").asString());
                        ticket.setId(id);
                        ticket.setLocation(Methods.unserializeLocation(row.get("location").asString()));
                        ticket.setStatus(TicketStatus.valueOf(row.get("status").asString()));
                        tickets.put(id, ticket);
                    }
                }

                if (storage.containsGroup("ticketresponses")) {
                    for (StorageRow row : storage.getRowsByGroup("ticketresponses")) {
                        int id = row.get("ticketid").asInt();
                        TicketResponse ticketResponse = new TicketResponse(
                                UUID.fromString(row.get("author").asString()),
                                row.get("message").asString(),
                                Long.parseLong(row.get("posted").asString()));

                        tickets.get(id).addResponse(ticketResponse);
                        ticketResponse.setTicketId(id);
                    }
                }
                for (Ticket ticket : tickets.values())
                    dataManager.createTicket(ticket);
            }
            dataFile.delete();

            final boolean convrted = converted;
            getDataManager().queueAsync(() -> {
                if (convrted)
                    console.sendMessage("[" + getDescription().getName() + "] " + ChatColor.GREEN + "Conversion complete :)");
                // Load data from DB
                this.dataManager.getTemplates((templates) -> {
                    for (Template template : templates) {
                        this.templateManager.addTemplate(template);
                    }
                });
                this.dataManager.getAppliedPunishments((appliedPunishments) -> {
                    for (AppliedPunishment punishment : appliedPunishments)
                        this.punishmentManager.getPlayer(punishment.getVictim()).addPunishment(punishment);
                });
                this.dataManager.getNotes((notes) -> {
                    for (PunishmentNote note : notes)
                        this.punishmentManager.getPlayer(note.getSubject()).addNotes(note);
                });
                this.dataManager.getTickets((tickets) -> {
                    for (Ticket ticket : tickets.values())
                        this.ticketManager.addTicket(ticket);
                });
            }, "create");
        }, 20);

        // Register Listeners
        guiManager.init();
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new CommandListener(this), this);
        pluginManager.registerEvents(new DeathListener(this), this);
        pluginManager.registerEvents(new MoveListener(this), this);
        pluginManager.registerEvents(new DropListener(this), this);
        pluginManager.registerEvents(new InventoryListener(this), this);
        pluginManager.registerEvents(new ChatListener(this), this);
        pluginManager.registerEvents(new LoginListener(this), this);
        pluginManager.registerEvents(new MobTargetLister(), this);
        pluginManager.registerEvents(new BlockListener(this), this);
        if (pluginManager.isPluginEnabled("FabledSkyBlock"))
            pluginManager.registerEvents(new SkyBlockListener(this), this);

        if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13))
            pluginManager.registerEvents(new SpyingDismountListener(), this);

        // Start tasks
        SlowModeTask.startTask(this);
    }

    @Override
    public void onConfigReload() {
        this.setLocale(getConfig().getString("System.Language Mode"), true);
        this.locale.reloadMessages();
    }

    @Override
    public List<Config> getExtraConfig() {
        return null;
    }

    public CommandManager getCommandManager() {
        return commandManager;
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

    public DataManager getDataManager() {
        return dataManager;
    }

    public DatabaseConnector getDatabaseConnector() {
        return databaseConnector;
    }

    public GuiManager getGuiManager() {
        return guiManager;
    }

    public ModerationManager getModerationManager() {
        return moderationManager;
    }
}
