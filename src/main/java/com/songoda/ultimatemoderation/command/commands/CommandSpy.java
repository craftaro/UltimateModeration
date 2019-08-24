package com.songoda.ultimatemoderation.command.commands;

import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.command.AbstractCommand;
import com.songoda.ultimatemoderation.utils.ServerVersion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class CommandSpy extends AbstractCommand {

    private static Map<UUID, Spy> spying = new HashMap<>();

    public CommandSpy() {
        super(true, true, "Spy");
    }

    public static void spy(OfflinePlayer oPlayer, Player senderP) {
        UltimateModeration instance = UltimateModeration.getInstance();

        if (!UltimateModeration.getInstance().isServerVersionAtLeast(ServerVersion.V1_12)) {
            instance.getLocale().newMessage("This feature is not compatible with this version of spigot.").sendPrefixedMessage(senderP);
            return;
        }

        Player player = oPlayer.getPlayer();

        if (player == null) {
            instance.getLocale().newMessage("That player does not exist or is not online.").sendPrefixedMessage(senderP);
            return;
        }

        if (player == senderP) {
            instance.getLocale().getMessage("command.spy.cant").sendPrefixedMessage(senderP);
            return;
        }

        boolean didVanish = false;
        if (!CommandVanish.isVanished(senderP)) {
            CommandVanish.vanish(senderP);
            senderP.setCanPickupItems(false);
        }
        senderP.teleport(player.getPlayer().getLocation());

        spying.put(senderP.getUniqueId(), new Spy(senderP.getLocation(), didVanish));
        player.getPlayer().addPassenger(senderP);

        instance.getLocale().getMessage("command.spy.success")
                .processPlaceholder("player", player.getName()).sendPrefixedMessage(senderP);
    }

    @Override
    protected ReturnType runCommand(UltimateModeration instance, CommandSender sender, String... args) {
        if (args.length > 1)
            return ReturnType.SYNTAX_ERROR;

        Player senderP = ((Player) sender);

        if (args.length == 0) {
            if (!spying.containsKey(senderP.getUniqueId()))
                return ReturnType.SYNTAX_ERROR;
            senderP.teleport(spying.get(senderP.getUniqueId()).getLastLocation());
            if (spying.get(senderP.getUniqueId()).isVanishApplied() && CommandVanish.isVanished(senderP))
                CommandVanish.vanish(senderP);

            spying.remove(senderP.getUniqueId());
            instance.getLocale().getMessage("command.spy.returned").sendPrefixedMessage(sender);
            return ReturnType.SUCCESS;
        }

        Player player = Bukkit.getPlayer(args[0]);

        if (player == null) {
            instance.getLocale().newMessage("That player does not exist or is not online.").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }

        if (player.hasPermission("um.spy.exempt")) {
            instance.getLocale().newMessage("You cannot spy on that player.").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }

        spy(player, senderP);

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
        return "um.spy";
    }

    @Override
    public String getSyntax() {
        return "/Spy [player]";
    }

    @Override
    public String getDescription() {
        return "Allows you to spy on a player.";
    }

    public static class Spy {
        private Location lastLocation;
        private boolean vanishApplied;

        public Spy(Location lastLocation, boolean vanishApplied) {
            this.lastLocation = lastLocation;
            this.vanishApplied = vanishApplied;
        }

        public Location getLastLocation() {
            return lastLocation;
        }

        public boolean isVanishApplied() {
            return vanishApplied;
        }
    }
}
