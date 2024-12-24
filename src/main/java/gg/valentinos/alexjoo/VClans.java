package gg.valentinos.alexjoo;

import org.bukkit.plugin.java.JavaPlugin;

public final class VClans extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("vClans has been enabled!");

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);

    }

    @Override
    public void onDisable() {
        getLogger().info("vClans has been disabled.");
    }

}
