package com.craftaro.ultimatemoderation.commands;

import com.craftaro.core.commands.AbstractCommand;
import com.craftaro.ultimatemoderation.UltimateModeration;
import com.craftaro.ultimatemoderation.punish.PunishmentType;
import com.craftaro.ultimatemoderation.punish.player.PlayerPunishData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CommandUnMute extends AbstractCommand {
    private final UltimateModeration plugin;

    public CommandUnMute(UltimateModeration plugin) {
        super(CommandType.CONSOLE_OK, "UnMute");
        this.plugin = plugin;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        if (args.length != 1)
            return ReturnType.SYNTAX_ERROR;

        OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);

        if (!this.plugin.getPunishmentManager().getPlayer(player).getActivePunishments()
                .stream().anyMatch(appliedPunishment -> appliedPunishment.getPunishmentType() == PunishmentType.MUTE)) {
            this.plugin.getLocale().newMessage("That player isn't muted.").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }

        PlayerPunishData playerPunishData = this.plugin.getPunishmentManager().getPlayer(player);

        playerPunishData.expirePunishments(PunishmentType.MUTE);

        this.plugin.getLocale().newMessage(this.plugin.getLocale().getMessage("event.unmute.success")
                .processPlaceholder("player", player.getName()).toText()).sendPrefixedMessage(sender);
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
        return "um.mute";
    }

    @Override
    public String getSyntax() {
        return "/UnMute <player>";
    }

    @Override
    public String getDescription() {
        return "Allows you to unmute players.";
    }
}
