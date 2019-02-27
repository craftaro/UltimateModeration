package com.songoda.ultimatemoderation.command;

import com.songoda.ultimatemoderation.UltimateModeration;
import com.songoda.ultimatemoderation.command.commands.*;
import com.songoda.ultimatemoderation.utils.Methods;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandManager implements CommandExecutor {

    private UltimateModeration instance;
    private TabManager tabManager;

    private List<AbstractCommand> commands = new ArrayList<>();

    public CommandManager(UltimateModeration instance) {
        this.instance = instance;
        this.tabManager = new TabManager(this);

        instance.getCommand("UltimateModeration").setExecutor(this);
        instance.getCommand("ClearChat").setExecutor(this);
        instance.getCommand("ToggleChat").setExecutor(this);
        instance.getCommand("RandomPlayer").setExecutor(this);
        instance.getCommand("Vanish").setExecutor(this);

        AbstractCommand commandUltimateModeration = addCommand(new CommandUltimateModeration());
        addCommand(new CommandClearChat());
        addCommand(new CommandToggleChat());
        addCommand(new CommandRandomPlayer());
        addCommand(new CommandVanish());

        addCommand(new CommandSettings(commandUltimateModeration));
        addCommand(new CommandReload(commandUltimateModeration));

        for (AbstractCommand abstractCommand : commands) {
            if (abstractCommand.getParent() != null) continue;
            instance.getCommand(abstractCommand.getCommand()).setTabCompleter(tabManager);
        }
    }

    private AbstractCommand addCommand(AbstractCommand abstractCommand) {
        commands.add(abstractCommand);
        return abstractCommand;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        for (AbstractCommand abstractCommand : commands) {
            if (abstractCommand.getCommand() == null)
                continue;
            if (!abstractCommand.getCommand().equalsIgnoreCase(command.getName())) return false;
            
            if (strings.length == 0) {
                processRequirements(abstractCommand, commandSender, strings);
                return true;
            }

            String cmd = strings[0];
            String cmd2 = strings.length >= 2 ? String.join(" ", strings[0], strings[1]) : null;
            for (String sub : abstractCommand.getSubCommand()) {
                if (cmd.equalsIgnoreCase(sub) || (cmd2 != null && cmd2.equalsIgnoreCase(sub))) {
                    processRequirements(abstractCommand, commandSender, strings);
                    return true;
                }
            }
        }
        commandSender.sendMessage(instance.getReferences().getPrefix() + Methods.formatText("&7The command you entered does not exist or is spelt incorrectly."));
        return true;
    }

    private void processRequirements(AbstractCommand command, CommandSender sender, String[] strings) {
        if (!(sender instanceof Player) && command.isNoConsole()) {
            sender.sendMessage("You must be a player to use this command.");
            return;
        }
        if (command.getPermissionNode() == null || sender.hasPermission(command.getPermissionNode())) {
            AbstractCommand.ReturnType returnType = command.runCommand(instance, sender, strings);
            if (returnType == AbstractCommand.ReturnType.SYNTAX_ERROR) {
                sender.sendMessage(instance.getReferences().getPrefix() + Methods.formatText("&cInvalid Syntax!"));
                sender.sendMessage(instance.getReferences().getPrefix() + Methods.formatText("&7The valid syntax is: &6" + command.getSyntax() + "&7."));
            }
            return;
        }
        sender.sendMessage(instance.getReferences().getPrefix() + instance.getLocale().getMessage("event.general.nopermission"));
    }

    public List<AbstractCommand> getCommands() {
        return Collections.unmodifiableList(commands);
    }
}
