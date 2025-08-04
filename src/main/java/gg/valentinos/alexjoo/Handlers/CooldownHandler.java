package gg.valentinos.alexjoo.Handlers;

import gg.valentinos.alexjoo.Data.CooldownData.Cooldown;
import gg.valentinos.alexjoo.Data.CooldownData.PlayerCooldownsMap;
import gg.valentinos.alexjoo.Utility.JsonUtils;

import java.util.UUID;

public class CooldownHandler {
    private PlayerCooldownsMap playerCooldownsMap;

    public CooldownHandler() {
        loadCooldowns();
    }

    public void loadCooldowns() {
        playerCooldownsMap = JsonUtils.deserializeCooldowns("cooldowns.json");
    }

    public void saveCooldowns() {
        playerCooldownsMap.purgeCooldowns(); // Remove expired cooldowns before saving
        JsonUtils.toJsonFile(playerCooldownsMap.getPlayerCooldownsMap(), "cooldowns.json");
    }

    public void createCooldown(UUID player, String query, long duration) {
        if (duration <= 0) return;
        playerCooldownsMap.addCooldown(player, query, duration);
        saveCooldowns();
    }

    public boolean isOnCooldown(UUID player, String query) {
        Cooldown cooldown = playerCooldownsMap.getCooldown(player, query);
        return cooldown != null;
    }

    public String getTimeLeft(UUID player, String query) {
        Cooldown cooldown = playerCooldownsMap.getCooldown(player, query);
        if (cooldown == null) {
            return "00:00";
        }
        return cooldown.getTimeLeft();
    }
}
