package com.songoda.ultimatemoderation.storage;

import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.punish.AppliedPunishment;
import com.songoda.ultimatemoderation.punish.PunishmentNote;
import com.songoda.ultimatemoderation.punish.player.PlayerPunishData;
import com.songoda.ultimatemoderation.punish.template.Template;
import com.songoda.ultimatemoderation.utils.ConfigWrapper;

import java.util.List;

public abstract class Storage {

    protected final UltimateModeration instance;
    protected final ConfigWrapper dataFile;

    public Storage(UltimateModeration instance) {
        this.instance = instance;
        this.dataFile = new ConfigWrapper(instance, "", "data.yml");
        this.dataFile.createNewFile(null, "UltimateModeration Data File");
        this.dataFile.getConfig().options().copyDefaults(true);
        this.dataFile.saveConfig();
    }

    public abstract boolean containsGroup(String group);

    public abstract List<StorageRow> getRowsByGroup(String group);

    public abstract void prepareSaveItem(String group, StorageItem... items);

    public void updateData(UltimateModeration instance) {
        // Save game data
        for (Template template : instance.getTemplateManager().getTemplates().values()) {
            prepareSaveItem("templates", new StorageItem("uuid", template.getUUID().toString()),
                    new StorageItem("type", template.getPunishmentType().name()),
                    new StorageItem("duration", template.getDuration()),
                    new StorageItem("reason", template.getReason()),
                    new StorageItem("name", template.getTemplateName()),
                    new StorageItem("creator", template.getCreator().toString()));
        }

        for (PlayerPunishData playerPunishData : instance.getPunishmentManager().getPunishments().values()) {
            List<AppliedPunishment> appliedPunishments = playerPunishData.getActivePunishments();
            appliedPunishments.addAll(playerPunishData.getExpiredPunishments());
            for (AppliedPunishment appliedPunishment : appliedPunishments) {
                prepareSaveItem("punishments", new StorageItem("uuid", appliedPunishment.getUUID().toString()),
                        new StorageItem("type", appliedPunishment.getPunishmentType().name()),
                        new StorageItem("duration", appliedPunishment.getDuration()),
                        new StorageItem("reason", appliedPunishment.getReason()),
                        new StorageItem("victim", appliedPunishment.getVictim().toString()),
                        new StorageItem("punisher", appliedPunishment.getPunisher().toString()),
                        new StorageItem("expiration", appliedPunishment.getExpiration()));
            }

            List<PunishmentNote> notes = playerPunishData.getNotes();
            for (PunishmentNote note : notes) {
                prepareSaveItem("notes", new StorageItem("uuid", note.getUUID().toString()),
                        new StorageItem("note", note.getNote()),
                        new StorageItem("author", note.getAuthor().toString()),
                        new StorageItem("subject", note.getSubject().toString()),
                        new StorageItem("creation", note.getCreationDate()));
            }
        }
    }

    public abstract void doSave();

    public abstract void save();

    public abstract void makeBackup();

    public abstract void closeConnection();

}
