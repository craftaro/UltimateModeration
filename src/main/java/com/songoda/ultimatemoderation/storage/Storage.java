package com.songoda.ultimatemoderation.storage;

import com.songoda.core.configuration.Config;
import com.songoda.ultimatemoderation.UltimateModeration;

import java.util.List;

public abstract class Storage {

    protected final UltimateModeration plugin;
    protected final Config dataFile;

    public Storage(UltimateModeration plugin) {
        this.plugin = plugin;
        this.dataFile = new Config(plugin, "data.yml");
        this.dataFile.load();
    }

    public abstract boolean containsGroup(String group);

    public abstract List<StorageRow> getRowsByGroup(String group);

    public abstract void prepareSaveItem(String group, StorageItem... items);

    public void updateData(UltimateModeration instance) {
    }

    public abstract void doSave();

    public abstract void save();

    public abstract void makeBackup();

    public abstract void closeConnection();

}
