package gg.valentinos.alexjoo;

import gg.valentinos.alexjoo.Commands.Clan.ClanCommand;
import gg.valentinos.alexjoo.Handlers.ClansHandler;
import gg.valentinos.alexjoo.Listeners.PlayerListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class VClans extends JavaPlugin {

    private static VClans instance;
    private ClansHandler clansHandler;

    @Override
    public void onEnable() {
        instance = this;

        clansHandler = new ClansHandler();

        getServer().getPluginManager().registerEvents(new PlayerListener(), this);

        getCommand("clan").setExecutor(new ClanCommand());
        getCommand("clan").setTabCompleter(new ClanCommand());

        getLogger().info("vClans has been enabled!");
    }

    @Override
    public void onDisable() {
        if (clansHandler != null)
            clansHandler.saveClans();
        clansHandler = null;

        instance = null;

        getLogger().info("vClans has been disabled.");
    }

    public static VClans getInstance() {
        return instance;
    }

    public ClansHandler getClansHandler() {
        return clansHandler;
    }
}
