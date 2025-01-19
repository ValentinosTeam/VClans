package gg.valentinos.alexjoo.Commands;

import gg.valentinos.alexjoo.Handlers.ClansHandler;
import gg.valentinos.alexjoo.Handlers.CooldownHandler;
import gg.valentinos.alexjoo.VClans;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;

public abstract class SubCommand {
    protected final static ClansHandler clansHandler = VClans.getInstance().getClansHandler();
    protected final static CooldownHandler cooldownHandler = VClans.getInstance().getCooldownHandler();
    protected final static FileConfiguration config = VClans.getInstance().getConfig();

    protected String name;
    protected String description;
    protected String usage;
    protected String targetCooldownQuery;
    protected String selfCooldownQuery;
    protected long cooldownDuration;

    protected boolean hasToBePlayer = false;
    protected int minArgs = -1;
    protected int maxArgs = -1;
    protected int requiredArgs = -1;

    protected String configPath;

    public SubCommand(String commandName, String subcommandName) {
        configPath = "commands." + commandName + "." + subcommandName + ".";
        name = subcommandName;
        description = config.getString(configPath + "description");
        usage = config.getString(configPath + "usage");
        cooldownDuration = config.getLong(configPath + "cooldown");
        selfCooldownQuery = commandName + "-" + subcommandName;
        targetCooldownQuery = selfCooldownQuery;
    }

    public String getName(){
        return name;
    }

    public String getDescription(){
        return description;
    }

    public String getUsage(){
        return usage;
    }

    public abstract boolean execute(CommandSender sender, String[] args);

    public abstract List<String> onTabComplete(CommandSender sender, String[] args);

    protected boolean commonChecks(CommandSender sender, String[] args){
        if (!config.getBoolean(configPath + "enabled")) {
            sender.sendMessage(Objects.requireNonNull(config.getString("commands.default.messages.command-disabled")));
            return true;
        }
        if (hasToBePlayer && !(sender instanceof Player)) {
            sender.sendMessage(Objects.requireNonNull(config.getString("commands.default.messages.player-only")));
            return true;
        }
        if (minArgs != -1 && args.length < minArgs) {
            sender.sendMessage(Objects.requireNonNull(config.getString("commands.default.messages.not-enough-arguments")));
            sender.sendMessage("Usage: " + getUsage());
            return true;
        }
        if (maxArgs != -1 && args.length > maxArgs) {
            sender.sendMessage(Objects.requireNonNull(config.getString("commands.default.messages.too-many-arguments")));
            sender.sendMessage("Usage: " + getUsage());
            return true;
        }
        if (requiredArgs != -1 && args.length != requiredArgs) {
            sender.sendMessage(Objects.requireNonNull(config.getString("commands.default.messages.wrong-number-of-arguments")));
            sender.sendMessage("Usage: " + getUsage());
            return true;
        }
        return false;
    }

    protected boolean isOnCooldown(CommandSender sender, String query){
        if (!(sender instanceof Player player))
            return false;
        if (cooldownHandler.isOnCooldown(player.getUniqueId(), query)) {
            String timeLeft = cooldownHandler.getTimeLeft(player.getUniqueId(), query);
            String message = Objects.requireNonNull(config.getString("commands.default.messages.on-cooldown")).replace("{time}", timeLeft);
            player.sendMessage(message);
            return true;
        }
        return false;
    }

    protected void handleCommandResult(CommandSender sender, String errorMessage, String successMessage){
        if (errorMessage == null) {
            if (cooldownDuration != 0) {
                cooldownHandler.createCooldown(((Player) sender).getUniqueId(), targetCooldownQuery, cooldownDuration);
            }
            sender.sendMessage(successMessage);
        } else {
            sender.sendMessage(errorMessage);
        }
    }
    protected void handleCommandResult(CommandSender sender, String errorMessage, CommandAction successAction){
        if (errorMessage == null) {
            if (cooldownDuration != 0) {
                cooldownHandler.createCooldown(((Player) sender).getUniqueId(), targetCooldownQuery, cooldownDuration);
            }
            successAction.execute();
        } else {
            sender.sendMessage(errorMessage);
        }
    }
}
