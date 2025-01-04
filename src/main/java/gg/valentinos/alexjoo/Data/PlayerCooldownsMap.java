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

    public void addCooldown(UUID player, String query, long duration) {
        Cooldown cooldown = getCooldown(player, query);
        if (cooldown != null) {
            return;
        }
        playerCooldownsMap.computeIfAbsent(player, k -> new HashSet<Cooldown>());
        HashSet<Cooldown> cooldowns = playerCooldownsMap.get(player);
        Cooldown new_cooldown = new Cooldown(query, duration);
        cooldowns.add(new_cooldown);
    }

    public Cooldown getCooldown(UUID player, String query) {
        // Will return cooldown if it exists AND if it's in effect. Automatically removes expired cooldowns
        HashSet<Cooldown> cooldowns = playerCooldownsMap.get(player);
        if (cooldowns == null) {
            return null;
        }
        Cooldown cooldown = cooldowns.stream().filter(c -> c.getQuery().equals(query)).findFirst().orElse(null);
        if (cooldown != null && cooldown.isExpired()) {
            cooldowns.remove(cooldown);
        }
        return cooldown;
    }

}
