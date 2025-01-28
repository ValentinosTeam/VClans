package gg.valentinos.alexjoo.Commands;

import gg.valentinos.alexjoo.Data.LogType;
import gg.valentinos.alexjoo.Handlers.ClansHandler;
import gg.valentinos.alexjoo.Handlers.ConfirmationHandler;
import gg.valentinos.alexjoo.Handlers.CooldownHandler;
import gg.valentinos.alexjoo.VClans;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

public abstract class SubCommand {
    protected final static ClansHandler clansHandler = VClans.getInstance().getClansHandler();
    protected final static CooldownHandler cooldownHandler = VClans.getInstance().getCooldownHandler();
    protected final static ConfirmationHandler confirmationHandler = VClans.getInstance().getConfirmationHandler();
    protected final static FileConfiguration config = VClans.getInstance().getConfig();
    protected final static Logger logger = VClans.getInstance().getLogger();

    protected String name;
    protected String description;
    protected String usage;
    protected String targetCooldownQuery;
    protected String selfCooldownQuery;
    protected long cooldownDuration;
    protected long confirmationDuration;
    protected boolean enabled = false;
    protected HashMap<String, String> messages;
    protected HashMap<String, String> replacements;

    protected boolean hasToBePlayer = false;
    protected int minArgs = -1;
    protected int maxArgs = -1;
    protected int requiredArgs = -1;

    protected String configPath;

    public SubCommand(String commandName, String subcommandName, List<String> configKeys){
        logger.info("Loading command " + commandName + " " + subcommandName);
        loadConfigs(commandName, subcommandName);
        loadMessages(configKeys);
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

    public final void execute(CommandSender sender, String[] args){
        loadReplacementValues(sender, args);
        if (hasCommonIssues(sender, args)) return;
        if (hasSpecificErrors(sender, args)) return;
        if (isOnCooldown(sender, selfCooldownQuery)) return;

        CommandAction action = getAction(sender, args);
        executeWithConfirmation(sender, action);
    }

    public abstract CommandAction getAction(CommandSender sender, String[] args);

    public abstract List<String> onTabComplete(CommandSender sender, String[] args);

    protected abstract boolean hasSpecificErrors(CommandSender sender, String[] args);

    protected abstract void loadReplacementValues(CommandSender sender, String[] args);

    protected void sendFormattedMessage(CommandSender sender, String message, LogType type){
        if (message == null || message.isEmpty()) {
            logger.severe("Message is null or empty. Not sending message.");
            return;
        }
        for (String key : replacements.keySet()) {
            message = message.replace(key, replacements.get(key));
        }
        logger.info("Sending message: " + message);
        logger.info("replacements: " + replacements);
        sender.sendMessage(message);
        if (type != LogType.NULL){
            switch (type){
                case FINE -> logger.fine(message);
                case INFO -> logger.info(message);
                case WARNING -> logger.warning(message);
                case SEVERE -> logger.severe(message);
            }
        }
    }

    protected void loadMessages(List<String> configKeys){
        messages = new HashMap<>();
        replacements = new HashMap<>();
        for (String key : configKeys) {
            String message = config.getString(configPath + "messages." + key);
            if (message == null){
                logger.warning("Missing message in config.yml for " + configPath + "messages." + key);
            }
            messages.put(key, message);
        }
    }

    private void loadConfigs(String commandName, String subcommandName){
        configPath = "commands." + commandName + "." + subcommandName + ".";
        name = subcommandName;
        if ((description = config.getString(configPath + "description")) == null){
            logger.severe("No description provided for command " + commandName + " " + subcommandName + ". Disabling command.");
            return;
        }
        if ((usage = config.getString(configPath + "usage")) == null){
            logger.severe("No usage provided for command " + commandName + " " + subcommandName + ". Disabling command.");
            return;
        }
        if (!(enabled = config.getBoolean(configPath + "enabled"))){
            logger.severe("Command not enabled " + commandName + " " + subcommandName + ". Disabling command.");
            return;
        }
        cooldownDuration = config.getLong(configPath + "cooldown");
        confirmationDuration = config.getLong(configPath + "confirmation");
        selfCooldownQuery = commandName + "-" + subcommandName;
        targetCooldownQuery = config.getString(configPath + "target-cooldown");
        if(targetCooldownQuery == null)
            targetCooldownQuery = selfCooldownQuery;
    }

    private boolean hasCommonIssues(CommandSender sender, String[] args){
        VClans instance = VClans.getInstance();
        if (!enabled) {
            sender.sendMessage(instance.getDefaultMessage("command-disabled"));
            return true;
        }
        if (hasToBePlayer && !(sender instanceof Player)) {
            sender.sendMessage(instance.getDefaultMessage("player-only"));
            return true;
        }
        if (minArgs != -1 && args.length < minArgs) {
            sender.sendMessage(instance.getDefaultMessage("not-enough-arguments"));
            sender.sendMessage("Usage: " + getUsage());
            return true;
        }
        if (maxArgs != -1 && args.length > maxArgs) {
            sender.sendMessage(instance.getDefaultMessage("too-many-arguments"));
            sender.sendMessage("Usage: " + getUsage());
            return true;
        }
        if (requiredArgs != -1 && args.length != requiredArgs) {
            sender.sendMessage(instance.getDefaultMessage("wrong-number-of-arguments"));
            sender.sendMessage("Usage: " + getUsage());
            return true;
        }
        return false;
    }

    private boolean isOnCooldown(CommandSender sender, String query){
        if (!(sender instanceof Player player))
            return false;
        if (cooldownHandler.isOnCooldown(player.getUniqueId(), query)) {
            String timeLeft = cooldownHandler.getTimeLeft(player.getUniqueId(), query);
            String message = VClans.getInstance().getDefaultMessage("on-cooldown").replace("{time}", timeLeft);
            player.sendMessage(message);
            return true;
        }
        return false;
    }

    private void executeWithConfirmation(CommandSender sender, CommandAction action){
        if (sender instanceof Player player && confirmationDuration > 0)
            confirmationHandler.addConfirmationEntry(player, confirmationDuration, action);
        else{
            action.execute();
        }
    }
}
