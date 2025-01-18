package gg.valentinos.alexjoo.Handlers;

import gg.valentinos.alexjoo.Data.Cooldown;
import gg.valentinos.alexjoo.Data.PlayerCooldownsMap;
import gg.valentinos.alexjoo.Utility.JsonUtils;
import gg.valentinos.alexjoo.VClans;

import java.util.UUID;

public class CooldownHandler {
    private PlayerCooldownsMap playerCooldownsMap;

    public CooldownHandler() {
        loadCooldowns();
    }

    public void loadCooldowns() {
        playerCooldownsMap = JsonUtils.deserializeCooldowns("cooldowns.json");
        ListCooldowns();
        if (playerCooldownsMap == null) {
            VClans.getInstance().getLogger().warning("Cooldowns file is empty. Creating new cooldowns.json file.");
            playerCooldownsMap = new PlayerCooldownsMap();
            saveCooldowns();
        }
    }

    public void saveCooldowns() {
        ListCooldowns();
        JsonUtils.toJsonFile(playerCooldownsMap.getPlayerCooldownsMap(), "cooldowns.json");
    }

    public void createCooldown(UUID player, String query, long duration) {
        playerCooldownsMap.addCooldown(player, query, duration);
        saveCooldowns();
    }

    public boolean isOnCooldown(UUID player, String query) {
        Cooldown cooldown = playerCooldownsMap.getCooldown(player, query);
        saveCooldowns();
        return cooldown != null;
    }

    public String getTimeLeft(UUID player, String query) {
        Cooldown cooldown = playerCooldownsMap.getCooldown(player, query);
        if (cooldown == null) {
            return "00:00";
        }
        saveCooldowns();
        return cooldown.getTimeLeft();
    }

    private void ListCooldowns(){
        VClans.getInstance().getLogger().info("Cooldowns: ");
        for (UUID player : playerCooldownsMap.getPlayerCooldownsMap().keySet()) {
            for (Cooldown cooldown : playerCooldownsMap.getPlayerCooldownsMap().get(player)) {
                VClans.getInstance().getLogger().info(player + " is on cooldown for " + cooldown.getQuery() + " for " + cooldown.getTimeLeft());
            }
        }
    }
}
