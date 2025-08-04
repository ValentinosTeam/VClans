package gg.valentinos.alexjoo.Handlers;

import gg.valentinos.alexjoo.Data.ClanData.Clan;
import gg.valentinos.alexjoo.Data.LogType;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.HashMap;

import static gg.valentinos.alexjoo.VClans.Log;
import static org.bukkit.Bukkit.getServer;

public class VaultHandler {
    private Chat chat;
    private Economy econ;
    private Permission perm;

    private HashMap<Player, Clan> playerChatMap;

    private final ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");

    public VaultHandler() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            Log("Vault plugin not found, Lot's of features will be disabled!", LogType.WARNING);
            return;
        }
        if (!setupEconomy()) {
            Log("Vault Economy not found, chunks are going to be free!", LogType.WARNING);
        }
        if (!setupChat()) {
            Log("Vault Chat not found, prefixes are not enabled!", LogType.WARNING);
        }
        if (!setupPermissions()) {
            Log("Vault Permissions not found, prefixes are not enabled!", LogType.WARNING);
        }
        playerChatMap = new HashMap<>();
    }

    public void setPlayerPrefix(Player player, String prefix) {
        if (perm == null) return;
        chat.setPlayerPrefix(null, player, "&r[" + prefix + "&r] ");
    }
    public void removePlayerPrefix(Player player) {
        if (perm == null) return;
        chat.setPlayerPrefix(null, player, "");
    }
    public void setPlayerChat(Player player, Clan clan) {
        playerChatMap.put(player, clan);
    }
    public Clan getClanChat(Player player) {
        return playerChatMap.getOrDefault(player, null);
    }
    public void withdrawPlayer(Player player, double amount) {
        if (econ == null) return;
        econ.withdrawPlayer(player, amount);
    }
    public double getPlayerBalance(Player player) {
        if (econ == null) return 0;
        return econ.getBalance(player);
    }
    public double calculateFormula(String formula, double x) {
        if (econ == null) return 0;
        double result;
        try {
            String expr = formula.replace("x", String.valueOf(x)).replace("^", "**");
            Object engineResult = engine.eval(expr);
            result = Double.parseDouble(engineResult.toString());
        } catch (Exception e) {
            Log("Error evaluating formula: " + formula, LogType.SEVERE);
            return 0;
        }
        if (Double.isNaN(result) || Double.isInfinite(result)) {
            Log("Invalid formula result: " + result + ", y = " + formula + ", x = " + x, LogType.WARNING);
            return 0;
        }
        return result;
    }

    public Chat getChat() {
        return chat;
    }
    public Economy getEconomy() {
        return econ;
    }
    public Permission getPermission() {
        return perm;
    }


    private boolean setupChat() {
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        if (rsp == null) return false;
        chat = rsp.getProvider();
        return true;
    }
    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        econ = rsp.getProvider();
        return true;
    }
    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp == null) return false;
        perm = rsp.getProvider();
        return true;
    }
}
