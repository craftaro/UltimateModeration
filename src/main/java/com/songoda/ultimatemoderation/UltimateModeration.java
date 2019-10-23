package com.songoda.ultimatemoderation;

import com.songoda.core.SongodaCore;
import com.songoda.core.SongodaPlugin;
import com.songoda.core.commands.CommandManager;
import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.compatibility.ServerVersion;
import com.songoda.core.configuration.Config;
import com.songoda.core.gui.GuiManager;
import com.songoda.ultimatemoderation.commands.*;
import com.songoda.ultimatemoderation.listeners.*;
import com.songoda.ultimatemoderation.punish.AppliedPunishment;
import com.songoda.ultimatemoderation.punish.PunishmentNote;
import com.songoda.ultimatemoderation.punish.PunishmentType;
import com.songoda.ultimatemoderation.punish.player.PlayerPunishData;
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
import com.songoda.ultimatemoderation.utils.gui.AbstractGUI;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.UUID;

public class UltimateModeration extends SongodaPlugin {
    private static UltimateModeration INSTANCE;

    private final GuiManager guiManager = new GuiManager(this);
    private TicketManager ticketManager;
    private TemplateManager templateManager;
    private CommandManager commandManager;
    private PunishmentManager punishmentManager;
    private StaffChatManager staffChatManager;

    private Storage storage;

    public static UltimateModeration getInstance() {
        return INSTANCE;
    }

    @Override
    public void onPluginLoad() {
        INSTANCE = this;
    }

    @Override
    public void onPluginDisable() {
        storage.doSave();
        this.storage.closeConnection();
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
        this.commandManager.addCommand(new CommandCommandSpy(this));
        this.commandManager.addCommand(new CommandFreeze(this));
        this.commandManager.addCommand(new CommandInvSee(this));
        this.commandManager.addCommand(new CommandKick(this));
        this.commandManager.addCommand(new CommandMute(this));
        this.commandManager.addCommand(new CommandRandomPlayer(this));
        this.commandManager.addCommand(new CommandRevive(this));
        this.commandManager.addCommand(new CommandRunTemplate(this));
        this.commandManager.addCommand(new CommandSlowMode(this));
        this.commandManager.addCommand(new CommandSpy(this));
        this.commandManager.addCommand(new CommandStaffChat(this));
        this.commandManager.addCommand(new CommandTicket(this));
        this.commandManager.addCommand(new CommandToggleChat(this));
        this.commandManager.addCommand(new CommandUnBan(this));
        this.commandManager.addCommand(new CommandUnMute(this));
        this.commandManager.addCommand(new CommandVanish(this));
        this.commandManager.addCommand(new CommandViewEnderChest(this));
        this.commandManager.addCommand(new CommandWarn(this));

        // Setup Managers
        this.ticketManager = new TicketManager();
        this.templateManager = new TemplateManager();
        this.punishmentManager = new PunishmentManager();
        this.staffChatManager = new StaffChatManager();

        // Load data
        this.checkStorage();
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, this::loadFromFile, 1L);

        // Register Listeners
        guiManager.init();
        AbstractGUI.initializeListeners(this);
        Bukkit.getPluginManager().registerEvents(new CommandListener(this), this);
        Bukkit.getPluginManager().registerEvents(new DeathListener(this), this);
        Bukkit.getPluginManager().registerEvents(new MoveListener(this), this);
        Bukkit.getPluginManager().registerEvents(new DropListener(this), this);
        Bukkit.getPluginManager().registerEvents(new InventoryListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ChatListener(this), this);
        Bukkit.getPluginManager().registerEvents(new LoginListener(this), this);
        Bukkit.getPluginManager().registerEvents(new MobTargetLister(), this);
        Bukkit.getPluginManager().registerEvents(new BlockListener(this), this);

        if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13))
            Bukkit.getPluginManager().registerEvents(new SpyingDismountListener(), this);

        // Start tasks
        SlowModeTask.startTask(this);

        int timeout = Settings.AUTOSAVE.getInt() * 60 * 20;
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> storage.doSave(), timeout, timeout);
    }

    private void checkStorage() {
        this.storage = new StorageYaml(this);
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
}
