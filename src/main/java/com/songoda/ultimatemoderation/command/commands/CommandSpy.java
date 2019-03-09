package com.songoda.ultimatemoderation.command.commands;

import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.command.AbstractCommand;
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

        Player player = oPlayer.getPlayer();

        if (player == null) {
            senderP.sendMessage(instance.getReferences().getPrefix() + "That player does not exist or is not online.");
            return;
        }

        if (player == senderP) {
            senderP.sendMessage(instance.getReferences().getPrefix() + instance.getLocale().getMessage("command.spy.cant"));
            return;
        }
        
        boolean didVanish = false;
        if (!CommandVanish.isVanished(senderP)) {
            didVanish = true;
            CommandVanish.vanish(senderP);
        }
        senderP.teleport(player.getPlayer().getLocation());

        spying.put(senderP.getUniqueId(), new Spy(senderP.getLocation(), didVanish));
        player.getPlayer().addPassenger(senderP);

        senderP.sendMessage(instance.getReferences().getPrefix() + instance.getLocale().getMessage("command.spy.success", player.getName()));
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
            senderP.sendMessage(instance.getReferences().getPrefix() + instance.getLocale().getMessage("command.spy.returned"));
            return ReturnType.SUCCESS;
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
