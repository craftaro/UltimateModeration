package com.craftaro.ultimatemoderation.tasks;

import com.craftaro.ultimatemoderation.UltimateModeration;
import com.craftaro.ultimatemoderation.database.DataHelper;
import com.craftaro.ultimatemoderation.punish.Punishment;
import com.craftaro.ultimatemoderation.punish.player.PunishmentManager;
import com.craftaro.ultimatemoderation.settings.Settings;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class DataTask implements Runnable {

    private BukkitTask task;
    private final DataHelper dataHelper;

    public DataTask(DataHelper dataHelper) {
        this.dataHelper = dataHelper;
        this.task = Bukkit.getScheduler().runTaskTimerAsynchronously(UltimateModeration.getPlugin(UltimateModeration.class), this, 0, Settings.DATA_UPDATE_INTERVAL.getInt());
    }

    @Override
    public void run() {
        //Update punisment data
        dataHelper.updateData();
    }

    public void cancel() {
        task.cancel();
    }
}
