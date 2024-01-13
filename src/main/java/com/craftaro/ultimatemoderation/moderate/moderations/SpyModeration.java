package com.craftaro.ultimatemoderation.moderate.moderations;

import com.craftaro.core.compatibility.ServerVersion;
import com.craftaro.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.ultimatemoderation.UltimateModeration;
import com.craftaro.ultimatemoderation.commands.CommandVanish;
import com.craftaro.ultimatemoderation.listeners.SpyingDismountListener;
import com.craftaro.ultimatemoderation.moderate.AbstractModeration;
import com.craftaro.ultimatemoderation.moderate.ModerationType;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpyModeration extends AbstractModeration {
    private static final Map<UUID, Spy> spying = new HashMap<>();

    public SpyModeration(UltimateModeration plugin) {
        super(plugin, true, false);
        registerCommand(plugin);
    }

    @Override
    public ModerationType getType() {
        return ModerationType.SPY;
    }

    @Override
    public XMaterial getIcon() {
        return XMaterial.ENDER_EYE;
    }

    @Override
    public String getProper() {
        return "Spy";
    }

    @Override
    public String getDescription() {
        return "Allows you to spy on a player.";
    }

    @Override
    protected boolean runModeration(CommandSender runner, OfflinePlayer toModerate) {
        Player toModeratePlayer = (Player) toModerate;
        Player runnerPlayer = (Player) runner;

        if (spying.containsKey(runnerPlayer.getUniqueId())) {
            Spy spyingEntry = spying.remove(runnerPlayer.getUniqueId());
            runnerPlayer.teleport(spyingEntry.getLastLocation());
            if (spyingEntry.isVanishApplied() && CommandVanish.isVanished(runnerPlayer)) {
                CommandVanish.vanish(runnerPlayer);
            }

            this.plugin.getLocale().getMessage("command.spy.returned").sendPrefixedMessage(runner);
            return true;
        }

        spy(toModeratePlayer, runnerPlayer);
        return false;
    }

    public static boolean isSpying(OfflinePlayer player) {
        return spying.containsKey(player.getUniqueId());
    }

    public static void spy(OfflinePlayer oPlayer, Player senderP) {
        UltimateModeration instance = UltimateModeration.getInstance();

        if (spying.containsKey(senderP) && oPlayer == null) {
            Spy spyingEntry = spying.remove(senderP.getUniqueId());
            senderP.teleport(spyingEntry.getLastLocation());
            if (spyingEntry.isVanishApplied() && CommandVanish.isVanished(senderP)) {
                CommandVanish.vanish(senderP);
            }
            senderP.setGameMode(SpyingDismountListener.getGamemodes().get(senderP.getUniqueId()));

            UltimateModeration.getInstance().getLocale().getMessage("command.spy.returned").sendPrefixedMessage(senderP);
        }

        if (!ServerVersion.isServerVersionAtLeast(ServerVersion.V1_12)) {
            instance.getLocale().newMessage("This feature is not compatible with this version of spigot.").sendPrefixedMessage(senderP);
            return;
        }

        if (oPlayer == null || !oPlayer.isOnline()) {
            instance.getLocale().newMessage("That player does not exist or is not online.").sendPrefixedMessage(senderP);
            return;
        }
        Player player = oPlayer.getPlayer();

        if (player == senderP) {
            instance.getLocale().getMessage("command.spy.cant").sendPrefixedMessage(senderP);
            return;
        }

        boolean didVanish = false;
        if (!CommandVanish.isVanished(senderP)) {
            CommandVanish.vanish(senderP);
            senderP.setCanPickupItems(false);
            didVanish = true;
        }
        spying.put(senderP.getUniqueId(), new Spy(senderP.getLocation(), didVanish));

        senderP.teleport(player.getPlayer().getLocation());

        player.getPlayer().addPassenger(senderP);

        instance.getLocale().getMessage("command.spy.success")
                .processPlaceholder("player", player.getName()).sendPrefixedMessage(senderP);
    }

    public static class Spy {
        private final Location lastLocation;
        private final boolean vanishApplied;

        public Spy(Location lastLocation, boolean vanishApplied) {
            this.lastLocation = lastLocation;
            this.vanishApplied = vanishApplied;
        }

        public Location getLastLocation() {
            return this.lastLocation;
        }

        public boolean isVanishApplied() {
            return this.vanishApplied;
        }
    }
}
