package gg.valentinos.alexjoo.Handlers;

import gg.valentinos.alexjoo.Data.PlayerCooldownsMap;
import gg.valentinos.alexjoo.Utility.JsonUtils;
import gg.valentinos.alexjoo.VClans;

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
        JsonUtils.toJsonFile(playerCooldownsMap, "cooldowns.json");
    }
}
