package com.songoda.ultimatemoderation.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.gui.TicketManagerGui;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandTicket extends AbstractCommand {

    private final UltimateModeration plugin;

    public CommandTicket(UltimateModeration plugin) {
        super(CommandType.PLAYER_ONLY, "Ticket");
        this.plugin = plugin;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        Player senderP = ((Player) sender);

        new TicketManagerGui(plugin, senderP, senderP);
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "um.ticket";
    }

    @Override
    public String getSyntax() {
        return "/ticket";
    }

    @Override
    public String getDescription() {
        return "Opens the ticket interface.";
    }

}
