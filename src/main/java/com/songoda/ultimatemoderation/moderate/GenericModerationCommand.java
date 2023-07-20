package com.songoda.ultimatemoderation.moderate;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.ultimatemoderation.UltimateModeration;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class GenericModerationCommand extends AbstractCommand {
    private final UltimateModeration plugin;
    private final AbstractModeration moderation;

    public GenericModerationCommand(UltimateModeration plugin, AbstractModeration moderation) {
        super(moderation.isAllowConsole() ? CommandType.CONSOLE_OK : CommandType.PLAYER_ONLY, moderation.getProper());
        this.plugin = plugin;
        this.moderation = moderation;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        if (args.length != 1) {
            return ReturnType.SYNTAX_ERROR;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);

        if (!player.hasPlayedBefore()) {
            this.plugin.getLocale().newMessage("&cThis player has never played this server before...");
            return ReturnType.FAILURE;
        }

        this.moderation.runPreModeration(sender, player);

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
        return this.moderation.getPermission();
    }

    @Override
    public String getSyntax() {
        return "/" + this.moderation.getProper() + " <player>";
    }

    @Override
    public String getDescription() {
        return this.moderation.getDescription();
    }
}
