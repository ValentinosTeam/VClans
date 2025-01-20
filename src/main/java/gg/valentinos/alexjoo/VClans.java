package gg.valentinos.alexjoo;

import gg.valentinos.alexjoo.Commands.Clan.ClanCommand;
import gg.valentinos.alexjoo.Commands.ConfirmCommand;
import gg.valentinos.alexjoo.Handlers.ClansHandler;
import gg.valentinos.alexjoo.Handlers.ConfirmationHandler;
import gg.valentinos.alexjoo.Handlers.CooldownHandler;
import gg.valentinos.alexjoo.Listeners.PlayerListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class VClans extends JavaPlugin {

    private static VClans instance;
    private ClansHandler clansHandler;
    private CooldownHandler cooldownHandler;
    private ConfirmationHandler confirmationHandler;

    @Override
    public void onEnable() {
        instance = this;

        clansHandler = new ClansHandler();
        cooldownHandler = new CooldownHandler();
        confirmationHandler = new ConfirmationHandler();

        getServer().getPluginManager().registerEvents(new PlayerListener(), this);

        getCommand("clan").setExecutor(new ClanCommand());
        getCommand("clan").setTabCompleter(new ClanCommand());

        getCommand("confirm").setExecutor(new ConfirmCommand());

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
}
