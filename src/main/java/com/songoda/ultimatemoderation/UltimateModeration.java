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
import com.songoda.ultimatemoderation.commands.CommandBan;
import com.songoda.ultimatemoderation.commands.CommandClearChat;
import com.songoda.ultimatemoderation.commands.CommandHelp;
import com.songoda.ultimatemoderation.commands.CommandKick;
import com.songoda.ultimatemoderation.commands.CommandMute;
import com.songoda.ultimatemoderation.commands.CommandRandomPlayer;
import com.songoda.ultimatemoderation.commands.CommandReload;
import com.songoda.ultimatemoderation.commands.CommandRunTemplate;
import com.songoda.ultimatemoderation.commands.CommandSettings;
import com.songoda.ultimatemoderation.commands.CommandSlowMode;
import com.songoda.ultimatemoderation.commands.CommandStaffChat;
import com.songoda.ultimatemoderation.commands.CommandTicket;
import com.songoda.ultimatemoderation.commands.CommandToggleChat;
import com.songoda.ultimatemoderation.commands.CommandUltimateModeration;
import com.songoda.ultimatemoderation.commands.CommandUnBan;
import com.songoda.ultimatemoderation.commands.CommandUnMute;
import com.songoda.ultimatemoderation.commands.CommandVanish;
import com.songoda.ultimatemoderation.commands.CommandWarn;
import com.songoda.ultimatemoderation.database.DataManager;
import com.songoda.ultimatemoderation.database.migrations._1_InitialMigration;
import com.songoda.ultimatemoderation.listeners.BlockListener;
import com.songoda.ultimatemoderation.listeners.ChatListener;
import com.songoda.ultimatemoderation.listeners.CommandListener;
import com.songoda.ultimatemoderation.listeners.DeathListener;
import com.songoda.ultimatemoderation.listeners.DropListener;
import com.songoda.ultimatemoderation.listeners.InventoryListener;
import com.songoda.ultimatemoderation.listeners.LoginListener;
import com.songoda.ultimatemoderation.listeners.MobTargetLister;
import com.songoda.ultimatemoderation.listeners.MoveListener;
import com.songoda.ultimatemoderation.listeners.SkyBlockListener;
import com.songoda.ultimatemoderation.listeners.SpyingDismountListener;
import com.songoda.ultimatemoderation.moderate.ModerationManager;
import com.songoda.ultimatemoderation.punish.AppliedPunishment;
import com.songoda.ultimatemoderation.punish.PunishmentNote;
import com.songoda.ultimatemoderation.punish.player.PunishmentManager;
import com.songoda.ultimatemoderation.punish.template.Template;
import com.songoda.ultimatemoderation.punish.template.TemplateManager;
import com.songoda.ultimatemoderation.settings.Settings;
import com.songoda.ultimatemoderation.staffchat.StaffChatManager;
import com.songoda.ultimatemoderation.tasks.SlowModeTask;
import com.songoda.ultimatemoderation.tickets.Ticket;
import com.songoda.ultimatemoderation.tickets.TicketManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class UltimateModeration extends SongodaPlugin {
    private final GuiManager guiManager = new GuiManager(this);
    private TicketManager ticketManager;
    private TemplateManager templateManager;
    private CommandManager commandManager;
    private PunishmentManager punishmentManager;
    private StaffChatManager staffChatManager;
    private ModerationManager moderationManager;

    private DatabaseConnector databaseConnector;
    private DataManager dataManager;

    /**
     * @deprecated Use {@link JavaPlugin#getPlugin(Class)} instead.
     */
    @Deprecated
    public static UltimateModeration getInstance() {
        return getPlugin(UltimateModeration.class);
    }

    @Override
    public void onPluginLoad() {
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
                        new CommandSettings(this, this.guiManager),
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
        this.commandManager.addCommand(new CommandTicket(this, this.guiManager));
        this.commandManager.addCommand(new CommandToggleChat(this));
        this.commandManager.addCommand(new CommandUnBan(this));
        this.commandManager.addCommand(new CommandUnMute(this));
        this.commandManager.addCommand(new CommandVanish());
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
                int poolSize = Settings.MYSQL_POOL_SIZE.getInt();

                this.databaseConnector = new MySQLConnector(this, hostname, port, database, username, password, useSSL, poolSize);
                this.getLogger().info("Data handler connected using MySQL.");
            } else {
                this.databaseConnector = new SQLiteConnector(this);
                this.getLogger().info("Data handler connected using SQLite.");
            }

            this.dataManager = new DataManager(this.databaseConnector, this);
            DataMigrationManager dataMigrationManager = new DataMigrationManager(this.databaseConnector, this.dataManager, new _1_InitialMigration(this));
            dataMigrationManager.runMigrations();

        } catch (Exception ex) {
            this.getLogger().severe("Fatal error trying to connect to database. " +
                    "Please make sure all your connection settings are correct and try again. Plugin has been disabled.");
            emergencyStop();
            return;
        }

        // Register Listeners
        this.guiManager.init();
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new CommandListener(this), this);
        pluginManager.registerEvents(new DeathListener(), this);
        pluginManager.registerEvents(new MoveListener(this), this);
        pluginManager.registerEvents(new DropListener(this), this);
        pluginManager.registerEvents(new InventoryListener(this), this);
        pluginManager.registerEvents(new ChatListener(this), this);
        pluginManager.registerEvents(new LoginListener(this), this);
        pluginManager.registerEvents(new MobTargetLister(), this);
        pluginManager.registerEvents(new BlockListener(this), this);
        if (pluginManager.isPluginEnabled("FabledSkyBlock")) {
            pluginManager.registerEvents(new SkyBlockListener(), this);
        }

        if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13)) {
            pluginManager.registerEvents(new SpyingDismountListener(this), this);
        }

        // Start tasks
        SlowModeTask.startTask(this);
    }

    @Override
    public void onDataLoad() {
        getDataManager().runAsync(() -> {
            // Load data from DB
            this.dataManager.getTemplates((templates) -> {
                for (Template template : templates) {
                    this.templateManager.addTemplate(template);
                }
            });
            this.dataManager.getAppliedPunishments((appliedPunishments) -> {
                for (AppliedPunishment punishment : appliedPunishments) {
                    this.punishmentManager.getPlayer(punishment.getVictim()).addPunishment(punishment);
                }
            });
            this.dataManager.getNotes((notes) -> {
                for (PunishmentNote note : notes) {
                    this.punishmentManager.getPlayer(note.getSubject()).addNotes(note);
                }
            });
            this.dataManager.getTickets((tickets) -> {
                for (Ticket ticket : tickets.values()) {
                    this.ticketManager.addTicket(ticket);
                }
            });
        });
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
        return this.commandManager;
    }

    public TemplateManager getTemplateManager() {
        return this.templateManager;
    }

    public PunishmentManager getPunishmentManager() {
        return this.punishmentManager;
    }

    public TicketManager getTicketManager() {
        return this.ticketManager;
    }

    public StaffChatManager getStaffChatManager() {
        return this.staffChatManager;
    }

    public DataManager getDataManager() {
        return this.dataManager;
    }

    public DatabaseConnector getDatabaseConnector() {
        return this.databaseConnector;
    }

    public GuiManager getGuiManager() {
        return this.guiManager;
    }

    public ModerationManager getModerationManager() {
        return this.moderationManager;
    }
}
