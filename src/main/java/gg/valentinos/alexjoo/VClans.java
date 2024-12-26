package gg.valentinos.alexjoo;

import gg.valentinos.alexjoo.Handlers.ClansHandler;
import org.bukkit.plugin.java.JavaPlugin;

public final class VClans extends JavaPlugin {

    private static VClans instance;
    private ClansHandler clansHandler;

    @Override
    public void onEnable() {
        instance = this;
        clansHandler = new ClansHandler();

//        clansHandler.loadClans();
        clansHandler.saveClans();
        getLogger().info("vClans has been enabled!");

    }

    @Override
    public void onDisable() {
        getLogger().info("vClans has been disabled.");
        instance = null;
    }

    public static VClans getInstance() {
        return instance;
    }
}
