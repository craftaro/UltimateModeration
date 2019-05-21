package com.songoda.ultimatemoderation.command.commands;

import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.command.AbstractCommand;
import com.songoda.ultimatemoderation.utils.Methods;
import com.songoda.ultimatemoderation.utils.ServerVersion;
import com.songoda.ultimatemoderation.utils.SettingsManager;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.*;

public class CommandVanish extends AbstractCommand {

    private static List<UUID> inVanish = new ArrayList<>();

    public CommandVanish() {
        super(true, false, "Vanish");
    }

    public static void registerVanishedPlayers(Player player) {
        for (UUID uuid : inVanish) {
            Player vanished = Bukkit.getPlayer(uuid);
            if (vanished == null) continue;
            if (player.hasPermission("um.vanish.bypass")) {
                player.showPlayer(vanished);
            } else {
                player.hidePlayer(vanished);
            }
        }
    }

    public static void vanish(Player player) {
        UUID uuid = player.getUniqueId();

        UltimateModeration instance = UltimateModeration.getInstance();

        if (inVanish.contains(uuid)) {
            inVanish.remove(uuid);
            player.setInvulnerable(false);
            player.setCanPickupItems(true);
            player.sendMessage(Methods.formatText(instance.getReferences().getPrefix() + instance.getLocale().getMessage("command.vanish.toggledOff")));
        } else {
            inVanish.add(uuid);
            player.setCanPickupItems(false);
            player.setInvulnerable(true);
            player.sendMessage(Methods.formatText(instance.getReferences().getPrefix() + instance.getLocale().getMessage("command.vanish.toggledOn")));
        }
        if (SettingsManager.Setting.VANISH_EFFECTS.getBoolean()) {
            player.getWorld().playSound(player.getLocation(), Sound.valueOf(SettingsManager.Setting.VANISH_SOUND.getString()), 1L, 1L);

            if (SettingsManager.Setting.VANISH_BATS.getBoolean()) {
                List<Entity> entities = new ArrayList<>();
                for (int i = 0; i < 5; i++) {
                    entities.add(player.getWorld().spawnEntity(player.getLocation().add(0, 1, 0), EntityType.BAT));
                }
                Bukkit.getScheduler().scheduleSyncDelayedTask(instance, () -> {
                    for (Entity entity : entities) {
                        entity.remove();
                    }
                }, 20L * 3L);
            }

            float xx = (float) (0 + (Math.random() * 1));
            float yy = (float) (0 + (Math.random() * 2));
            float zz = (float) (0 + (Math.random() * 1));
            if (instance.isServerVersionAtLeast(ServerVersion.V1_12))
                player.getWorld().spawnParticle(Particle.valueOf(SettingsManager.Setting.VANISH_PARTICLE.getString()), player.getLocation().add(0, 1, 0), 35, xx, yy, zz, 0);
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (inVanish.contains(uuid))
                registerVanishedPlayers(p);
            else
                p.showPlayer(player);
        }
    }

    public static boolean isVanished(Player player) {
        return inVanish.contains(player.getUniqueId());
    }

    @Override
    protected ReturnType runCommand(UltimateModeration instance, CommandSender sender, String... args) {
        Player player = ((Player) sender);
        vanish(player);
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(UltimateModeration instance, CommandSender sender, String... args) {
        if (args.length == 1) {
            List<String> players = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                players.add(player.getName());
            }
            return players;
        }
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "um.vanish";
    }

    @Override
    public String getSyntax() {
        return "/Vanish";
    }

    @Override
    public String getDescription() {
        return "Makes you invisible.";
    }
}
