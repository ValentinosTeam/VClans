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
        playerCooldownsMap = JsonUtils.fromJsonFile("cooldowns.json", PlayerCooldownsMap.class);
        if (playerCooldownsMap == null) {
            VClans.getInstance().getLogger().warning("Clans file is empty. Creating new clans.json file.");
            playerCooldownsMap = new PlayerCooldownsMap();
            saveCooldowns();
        }
    }

    public void saveCooldowns() {
        JsonUtils.toJsonFile(playerCooldownsMap.getCooldowns(), "cooldowns.json");
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
}
