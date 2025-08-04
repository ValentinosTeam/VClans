package gg.valentinos.alexjoo;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import gg.valentinos.alexjoo.Commands.CancelCommand;
import gg.valentinos.alexjoo.Commands.Chunk.ChunkCommand;
import gg.valentinos.alexjoo.Commands.Clan.ClanCommand;
import gg.valentinos.alexjoo.Commands.ConfirmCommand;
import gg.valentinos.alexjoo.Commands.War.WarCommand;
import gg.valentinos.alexjoo.Data.LogType;
import gg.valentinos.alexjoo.Handlers.*;
import gg.valentinos.alexjoo.Listeners.ChatListener;
import gg.valentinos.alexjoo.Listeners.ChunkListener;
import gg.valentinos.alexjoo.Listeners.PlayerListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public final class VClans extends JavaPlugin {

    public static String WORLD_NAME;
    private static VClans instance;
    private ClanHandler clanHandler;
    private WarHandler warHandler;
    private ClanTierHandler clanTierHandler;
    private CooldownHandler cooldownHandler;
    private ConfirmationHandler confirmationHandler;
    private ChunkHandler chunkHandler;
    private BlueMapHandler blueMapHandler = null;
    private WorldGuardHandler worldGuardHandler = null;
    private VaultHandler vaultHandler = null;
    private HashMap<String, String> defaultMessages;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        loadDefaultMessages();

        vaultHandler = new VaultHandler();
        clanTierHandler = new ClanTierHandler();
        clanHandler = new ClanHandler();
        warHandler = new WarHandler();
        cooldownHandler = new CooldownHandler();
        confirmationHandler = new ConfirmationHandler();
        chunkHandler = new ChunkHandler();
        if (!worldGuardHandler.enable()) {
            Log("Something went really wrong when enabling the WorldGuard plugin", LogType.SEVERE);
        }


        if (!setupBlueMap()) {
            Log("BlueMap plugin not found, download bluemap if you want to see the chunks in the browser!", LogType.WARNING);
        }


        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(new ChunkListener(), this);
        getServer().getPluginManager().registerEvents(new ChatListener(vaultHandler.getChat()), this);

        ClanCommand clanCommand = new ClanCommand();
        Objects.requireNonNull(getCommand("clan")).setExecutor(clanCommand);
        Objects.requireNonNull(getCommand("clan")).setTabCompleter(clanCommand);
        ChunkCommand chunkCommand = new ChunkCommand();
        Objects.requireNonNull(getCommand("chunk")).setExecutor(chunkCommand);
        Objects.requireNonNull(getCommand("chunk")).setTabCompleter(chunkCommand);
        WarCommand warCommand = new WarCommand();
        Objects.requireNonNull(getCommand("war")).setExecutor(warCommand);
        Objects.requireNonNull(getCommand("war")).setTabCompleter(warCommand);

        Objects.requireNonNull(getCommand("confirm")).setExecutor(new ConfirmCommand());
        Objects.requireNonNull(getCommand("cancel")).setExecutor(new CancelCommand());

//        getCommand("test").setExecutor(new TestCommand());


        WORLD_NAME = getConfig().getString("settings.world-name");

        getLogger().info("vClans has been enabled!");

    }

    @Override
    public void onLoad() {
        if (!setupWorldGuard()) {
            Log("WorldGuard plugin not found. This plugin has support for it, just so u know :)", LogType.WARNING);
        }
    }

    @Override
    public void onDisable() {
//        if (clanHandler != null)
//            clanHandler.saveClans();
        clanHandler = null;

        clanTierHandler = null;

        if (cooldownHandler != null)
            cooldownHandler.saveCooldowns();
        cooldownHandler = null;

        confirmationHandler = null;
        chunkHandler = null;

//        DynmapCommonAPIListener.unregister(dynmapHandler);
//        dynmapHandler = null;

        blueMapHandler = null;

        defaultMessages = null;

        instance = null;

        getLogger().info("vClans has been disabled.");
    }

    public static VClans getInstance() {
        return instance;
    }

    public static void SendMessage(Player player, Component message, LogType type) {
        player.sendMessage(message);
        String text = PlainTextComponentSerializer.plainText().serialize(message);
        if (type != LogType.NULL) {
            switch (type) {
                case FINE -> getInstance().getLogger().fine("Sending message to " + player.getName() + ": " + text);
                case INFO -> getInstance().getLogger().info("Sending message to " + player.getName() + ": " + text);
                case WARNING ->
                        getInstance().getLogger().warning("Sending message to " + player.getName() + ": " + text);
                case SEVERE -> getInstance().getLogger().severe("Sending message to " + player.getName() + ": " + text);
            }
        }
    }
    public static void Log(String message, LogType type) {
        if (type != LogType.NULL) {
            switch (type) {
                case FINE -> getInstance().getLogger().fine(message);
                case INFO -> getInstance().getLogger().info(message);
                case WARNING -> getInstance().getLogger().warning(message);
                case SEVERE -> getInstance().getLogger().severe(message);
            }
        }
    }
    public static void Log(String message) {
        Log(message, LogType.INFO);
    }
    public static void sendFormattedMessage(CommandSender sender, String message, LogType type, HashMap<String, String> replacements) {
        if (message == null || message.isEmpty()) {
            getInstance().getLogger().severe("Message is null or empty. Not sending message.");
            return;
        }
        for (String key : replacements.keySet()) {
            message = message.replace(key, replacements.get(key));
        }
        Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(message);
        if (sender instanceof Player player) {
            SendMessage(player, component, type);
        } else {
            Log(String.valueOf(component), type);
        }
    }
    public static void sendFormattedMessage(CommandSender sender, String message, LogType type) {
        if (message == null || message.isEmpty()) {
            getInstance().getLogger().severe("Message is null or empty. Not sending message.");
            return;
        }
        Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(message);
        if (sender instanceof Player player) {
            SendMessage(player, component, type);
        } else {
            Log(String.valueOf(component), type);
        }
    }

    public ClanHandler getClanHandler() {
        return clanHandler;
    }
    public ClanTierHandler getClanTierHandler() {
        return clanTierHandler;
    }
    public CooldownHandler getCooldownHandler() {
        return cooldownHandler;
    }
    public ConfirmationHandler getConfirmationHandler() {
        return confirmationHandler;
    }
    public ChunkHandler getChunkHandler() {
        return chunkHandler;
    }
    public BlueMapHandler getBlueMapHandler() {
        return blueMapHandler;
    }
    public WorldGuardHandler getWorldGuardHandler() {
        return worldGuardHandler;
    }
    public VaultHandler getVaultHandler() {
        return vaultHandler;
    }
    public WarHandler getWarHandler() {
        return warHandler;
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
                "confirmation-message",
                "nothing-to-confirm",
                "confirmation-expired"
        );
        for (String key : configKeys) {
            String message = getConfig().getString("commands.default.messages." + key);
            if (message == null) {
                getLogger().warning("Missing message in config.yml for commands.default.messages." + key);
            }
            defaultMessages.put(key, message);
        }
    }
    private boolean setupBlueMap() {
        if (getServer().getPluginManager().getPlugin("BlueMap") == null) {
            return false;
        }
        blueMapHandler = new BlueMapHandler();
        return true;
    }
    private boolean setupWorldGuard() {
        Plugin wgPlugin = Bukkit.getPluginManager().getPlugin("WorldGuard");
        if (!(wgPlugin instanceof WorldGuardPlugin)) return false;

        worldGuardHandler = new WorldGuardHandler();
        return worldGuardHandler.registerFlag();
    }
}
