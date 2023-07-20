package com.songoda.ultimatemoderation.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.core.compatibility.ServerVersion;
import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.settings.Settings;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CommandVanish extends AbstractCommand {
    private static final List<UUID> inVanish = new ArrayList<>();

    public CommandVanish() {
        super(CommandType.PLAYER_ONLY, "Vanish");
    }

    public static void registerVanishedPlayers(Player player) {
        for (UUID uuid : inVanish) {
            Player vanished = Bukkit.getPlayer(uuid);
            if (vanished == null) {
                continue;
            }
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
            if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_9)) {
                player.setInvulnerable(false);
            }
            player.setCanPickupItems(true);
            instance.getLocale().getMessage("command.vanish.toggledOff").sendPrefixedMessage(player);
        } else {
            inVanish.add(uuid);
            player.setCanPickupItems(false);
            if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_9)) {
                player.setInvulnerable(true);
            }
            instance.getLocale().getMessage("command.vanish.toggledOn").sendPrefixedMessage(player);

        }
        if (Settings.VANISH_EFFECTS.getBoolean()) {
            player.getWorld().playSound(player.getLocation(), Sound.valueOf(Settings.VANISH_SOUND.getString()), 1L, 1L);

            if (Settings.VANISH_BATS.getBoolean()) {
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
            if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_12)) {
                player.getWorld().spawnParticle(Particle.valueOf(Settings.VANISH_PARTICLE.getString()), player.getLocation().add(0, 1, 0), 35, xx, yy, zz, 0);
            }
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (inVanish.contains(uuid)) {
                registerVanishedPlayers(p);
            } else {
                p.showPlayer(player);
            }
        }
        for (Entity e : player.getNearbyEntities(30, 30, 30)) {
            if (e instanceof Monster) {
                ((Monster) e).setTarget(null);
            }
        }
    }

    public static boolean isVanished(Player player) {
        return inVanish.contains(player.getUniqueId());
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        Player player = ((Player) sender);
        vanish(player);
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
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
