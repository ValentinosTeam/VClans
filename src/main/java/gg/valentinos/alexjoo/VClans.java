package gg.valentinos.alexjoo;

import gg.valentinos.alexjoo.Commands.Clan.ClanCommand;
import gg.valentinos.alexjoo.Commands.ConfirmCommand;
import gg.valentinos.alexjoo.Handlers.ClansHandler;
import gg.valentinos.alexjoo.Handlers.ConfirmationHandler;
import gg.valentinos.alexjoo.Handlers.CooldownHandler;
import gg.valentinos.alexjoo.Listeners.PlayerListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public final class VClans extends JavaPlugin {

    private static VClans instance;
    private ClansHandler clansHandler;
    private CooldownHandler cooldownHandler;
    private ConfirmationHandler confirmationHandler;
    private HashMap<String, String> defaultMessages;

    @Override
    public void onEnable() {
        instance = this;

        loadDefaultMessages();

        clansHandler = new ClansHandler();
        cooldownHandler = new CooldownHandler();
        confirmationHandler = new ConfirmationHandler();

        getServer().getPluginManager().registerEvents(new PlayerListener(), this);

        ClanCommand clanCommand = new ClanCommand();
        Objects.requireNonNull(getCommand("clan")).setExecutor(clanCommand);
        Objects.requireNonNull(getCommand("clan")).setTabCompleter(clanCommand);

        Objects.requireNonNull(getCommand("confirm")).setExecutor(new ConfirmCommand());

        saveDefaultConfig();

        getLogger().info("vClans has been enabled!");
    }

    @Override
    public void onDisable() {
        if (clansHandler != null)
            clansHandler.saveClans();
        clansHandler = null;

        if (cooldownHandler != null)
            cooldownHandler.saveCooldowns();
        cooldownHandler = null;

        confirmationHandler = null;
        defaultMessages = null;

        instance = null;

        getLogger().info("vClans has been disabled.");
    }

    public static VClans getInstance() {
        return instance;
    }

    public ClansHandler getClansHandler() {
        return clansHandler;
    }

    public CooldownHandler getCooldownHandler() {
        return cooldownHandler;
    }

    public ConfirmationHandler getConfirmationHandler() {
        return confirmationHandler;
    }

    public String getDefaultMessage(String key) {
        if (!defaultMessages.containsKey(key)) {
            getLogger().severe("Attempted to get a default message that does not exist: " + key);
            return "ERROR";
        }
        if (defaultMessages.get(key) == null) {
            getLogger().severe("Attempted to get a default message that is null: " + key);
            return "NULL";
        }
        return defaultMessages.get(key);
    }

    private void loadDefaultMessages() {
        defaultMessages = new HashMap<>();
        List<String> configKeys = List.of(
                "no-permission",
                "player-only",
                "not-enough-arguments",
                "too-many-arguments",
                "wrong-number-of-arguments",
                "on-cooldown",
                "command-disabled",
                "not-in-clan",
                "not-owner",
                "never-joined",
                "confirmation",
                "nothing-to-confirm",
                "confirmation-expired"
        );
        for (String key : configKeys) {
            String message = getConfig().getString("commands.default.messages." + key);
            if (message == null){
                getLogger().warning("Missing message in config.yml for commands.default.messages." + key);
            }
            defaultMessages.put(key, message);
        }
    }
}
