package gg.valentinos.alexjoo.Data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class PlayerCooldownsMap {
    private HashMap<UUID, HashSet<Cooldown>> playerCooldownsMap;


    public PlayerCooldownsMap() {
        playerCooldownsMap = new HashMap<>();
    }

    public HashMap<UUID, HashSet<Cooldown>> getCooldowns() {
        return playerCooldownsMap;
    }

    public void setCooldowns(HashMap<UUID, HashSet<Cooldown>> playerCooldownsMap) {
        this.playerCooldownsMap = playerCooldownsMap;
    }

    Cooldown getAndRemoveExpiredCooldown(UUID player, String query) {
        HashSet<Cooldown> cooldowns = playerCooldownsMap.get(player);
        if (cooldowns == null) {
            return null;
        }
        Cooldown cooldown = cooldowns.stream().filter(c -> c.getName().equals(query)).findFirst().orElse(null);
        if (cooldown == null) {
            return null;
        }
        if (cooldown.isExpired()) {
            cooldowns.remove(cooldown);
            return cooldown;
        }
        return null;
    }

    boolean isOnCooldown(UUID player, String query) {
        Cooldown cooldown = getAndRemoveExpiredCooldown(player, query);
        return cooldown != null;
    }

    HashSet<Cooldown> getCooldowns(UUID player) {
        return playerCooldownsMap.get(player);
    }

    Cooldown getCooldown(UUID player, String query) {
        HashSet<Cooldown> cooldowns = getCooldowns(player);
        if (cooldowns == null) {
            return null;
        }
        return cooldowns.stream().filter(c -> c.getName().equals(query)).findFirst().orElse(null);
    }
}
