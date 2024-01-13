package com.craftaro.ultimatemoderation;

import com.craftaro.core.SongodaCore;
import com.craftaro.core.SongodaPlugin;
import com.craftaro.core.commands.CommandManager;
import com.craftaro.core.compatibility.ServerVersion;
import com.craftaro.core.configuration.Config;
import com.craftaro.core.gui.GuiManager;
import com.craftaro.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.ultimatemoderation.commands.CommandBan;
import com.craftaro.ultimatemoderation.commands.CommandClearChat;
import com.craftaro.ultimatemoderation.commands.CommandHelp;
import com.craftaro.ultimatemoderation.commands.CommandKick;
import com.craftaro.ultimatemoderation.commands.CommandMute;
import com.craftaro.ultimatemoderation.commands.CommandRandomPlayer;
import com.craftaro.ultimatemoderation.commands.CommandReload;
import com.craftaro.ultimatemoderation.commands.CommandRunTemplate;
import com.craftaro.ultimatemoderation.commands.CommandSettings;
import com.craftaro.ultimatemoderation.commands.CommandSlowMode;
import com.craftaro.ultimatemoderation.commands.CommandStaffChat;
import com.craftaro.ultimatemoderation.commands.CommandTicket;
import com.craftaro.ultimatemoderation.commands.CommandToggleChat;
import com.craftaro.ultimatemoderation.commands.CommandUltimateModeration;
import com.craftaro.ultimatemoderation.commands.CommandUnBan;
import com.craftaro.ultimatemoderation.commands.CommandUnMute;
import com.craftaro.ultimatemoderation.commands.CommandVanish;
import com.craftaro.ultimatemoderation.commands.CommandWarn;
import com.craftaro.ultimatemoderation.database.DataHelper;
import com.craftaro.ultimatemoderation.database.migrations._1_InitialMigration;
import com.craftaro.ultimatemoderation.listeners.BlockListener;
import com.craftaro.ultimatemoderation.listeners.ChatListener;
import com.craftaro.ultimatemoderation.listeners.CommandListener;
import com.craftaro.ultimatemoderation.listeners.DeathListener;
import com.craftaro.ultimatemoderation.listeners.DropListener;
import com.craftaro.ultimatemoderation.listeners.InventoryListener;
import com.craftaro.ultimatemoderation.listeners.LoginListener;
import com.craftaro.ultimatemoderation.listeners.MobTargetLister;
import com.craftaro.ultimatemoderation.listeners.MoveListener;
import com.craftaro.ultimatemoderation.listeners.SkyBlockListener;
import com.craftaro.ultimatemoderation.listeners.SpyingDismountListener;
import com.craftaro.ultimatemoderation.moderate.ModerationManager;
import com.craftaro.ultimatemoderation.punish.AppliedPunishment;
import com.craftaro.ultimatemoderation.punish.PunishmentNote;
import com.craftaro.ultimatemoderation.punish.player.PunishmentManager;
import com.craftaro.ultimatemoderation.punish.template.Template;
import com.craftaro.ultimatemoderation.punish.template.TemplateManager;
import com.craftaro.ultimatemoderation.settings.Settings;
import com.craftaro.ultimatemoderation.staffchat.StaffChatManager;
import com.craftaro.ultimatemoderation.tasks.SlowModeTask;
import com.craftaro.ultimatemoderation.tickets.Ticket;
import com.craftaro.ultimatemoderation.tickets.TicketManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;

public class UltimateModeration extends SongodaPlugin {
    private final GuiManager guiManager = new GuiManager(this);
    private TicketManager ticketManager;
    private TemplateManager templateManager;
    private CommandManager commandManager;
    private PunishmentManager punishmentManager;
    private StaffChatManager staffChatManager;
    private ModerationManager moderationManager;
    private DataHelper dataHelper;

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
        SongodaCore.registerPlugin(this, 29, XMaterial.DIAMOND_CHESTPLATE);

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
            initDatabase(Arrays.asList(new _1_InitialMigration()));
            this.dataHelper = new DataHelper(getDataManager(), this);

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
        getDataManager().getAsyncPool().execute(() -> {
            // Load data from DB
            this.dataHelper.getTemplates((templates) -> {
                for (Template template : templates) {
                    this.templateManager.addTemplate(template);
                }
            });
            this.dataHelper.getAppliedPunishments((appliedPunishments) -> {
                for (AppliedPunishment punishment : appliedPunishments) {
                    this.punishmentManager.getPlayer(punishment.getVictim()).addPunishment(punishment);
                }
            });
            this.dataHelper.getNotes((notes) -> {
                for (PunishmentNote note : notes) {
                    this.punishmentManager.getPlayer(note.getSubject()).addNotes(note);
                }
            });
            this.dataHelper.getTickets((tickets) -> {
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

    public GuiManager getGuiManager() {
        return this.guiManager;
    }

    public ModerationManager getModerationManager() {
        return this.moderationManager;
    }

    public DataHelper getDataHelper() {
        return this.dataHelper;
    }
}
