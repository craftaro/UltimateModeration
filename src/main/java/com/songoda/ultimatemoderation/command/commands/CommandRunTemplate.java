package com.songoda.ultimatemoderation.command.commands;

import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.command.AbstractCommand;
import com.songoda.ultimatemoderation.punish.Punishment;
import com.songoda.ultimatemoderation.punish.PunishmentType;
import com.songoda.ultimatemoderation.punish.template.Template;
import com.songoda.ultimatemoderation.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CommandRunTemplate extends AbstractCommand {

    public CommandRunTemplate() {
        super(false, true, "RunTemplate");
    }

    @Override
    protected ReturnType runCommand(UltimateModeration instance, CommandSender sender, String... args) {
        if (args.length < 2)
            return ReturnType.SYNTAX_ERROR;

        OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);

        if (player == null) {
            sender.sendMessage(instance.getReferences().getPrefix() + "That player does not exist.");
            return ReturnType.FAILURE;
        }

        StringBuilder templateBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            String line = args[i];
            templateBuilder.append(line).append(" ");
        }
        String templateStr = templateBuilder.toString().trim();

        Template template = instance.getTemplateManager().getTemplate(templateStr);

        if (template == null) {
            sender.sendMessage("That template does not exist...");
            return ReturnType.FAILURE;
        }

        template.execute(sender, player);

        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(UltimateModeration instance, CommandSender sender, String... args) {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "um.template";
    }

    @Override
    public String getSyntax() {
        return "/RunTemplate <player> <template>";
    }

    @Override
    public String getDescription() {
        return "Allows you to use templates on players.";
    }
}
