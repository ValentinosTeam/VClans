package gg.valentinos.alexjoo.Handlers;

import gg.valentinos.alexjoo.Data.LogType;
import gg.valentinos.alexjoo.VClans;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

import static gg.valentinos.alexjoo.VClans.Log;


public class ClanTierHandler {
    static class ClanTier {
        protected String label;
        protected int price;
        protected int playerLimit;
        protected int chunkLimit;
        protected List<PotionEffect> buffs;
        protected List<PotionEffect> debuffs;

        @Override
        public String toString() {
            return "ClanTier{" +
                    "label='" + label + '\'' +
                    ", price=" + price +
                    ", playerLimit=" + playerLimit +
                    ", chunkLimit=" + chunkLimit +
                    ", buffs=" + formatEffects(buffs) +
                    ", debuffs=" + formatEffects(debuffs) +
                    '}';
        }

        private String formatEffects(List<PotionEffect> effects) {
            if (effects == null) return "null";
            List<String> list = new ArrayList<>();
            for (PotionEffect effect : effects) {
                list.add(effect.getType().toString() + ":" + (effect.getAmplifier() + 1));
            }
            return list.toString();
        }
    }

    private List<ClanTier> tiers;
    private boolean validConfig = false;
    private static final int EFFECT_DURATION = 6 * 20; // 5 seconds = 100 ticks

    public ClanTierHandler() {
        ConfigurationSection config = VClans.getInstance().getConfig().getConfigurationSection("clan-tiers");
        if (config == null) {
            Log("Config for tier system is missing!", LogType.SEVERE);
            return;
        }
        tiers = new ArrayList<>();
        for (String key : config.getKeys(false)) {
            ClanTier clanTier = new ClanTier();

            int tierLevel;
            try {
                tierLevel = Integer.parseInt(key);
            } catch (NumberFormatException e) {
                Log("Tier " + key + " has invalid key. Has to be 0, 1, 2, ... ", LogType.SEVERE);
                return;
            }

            if (tierLevel != tiers.size()) {
                return;
            }

            ConfigurationSection tierSection = config.getConfigurationSection(key);
            if (tierSection == null) return;

            if (!tierSection.isString("label")) {
                Log("Tier " + key + " missing a label!", LogType.SEVERE);
                return;
            }
            clanTier.label = tierSection.getString("label");

            if (!tierSection.isInt("price")) {
                Log("Tier " + key + " missing a price!", LogType.SEVERE);
                return;
            }
            clanTier.price = tierSection.getInt("price");

            if (!tierSection.isInt("player-limit") || !tierSection.isInt("chunk-limit")) {
                Log("Tier " + key + " has invalid player-limit or chunk-limit!", LogType.SEVERE);
                return;
            }
            clanTier.playerLimit = tierSection.getInt("player-limit");
            clanTier.chunkLimit = tierSection.getInt("chunk-limit");

            if (!tierSection.isList("buffs") || !tierSection.isList("debuffs")) {
                Log("Tier " + key + " has invalid buffs or debuffs list!", LogType.SEVERE);
                return;
            }
            clanTier.buffs = new ArrayList<>();
            clanTier.debuffs = new ArrayList<>();

            @SuppressWarnings("unchecked")
            List<List<Object>> rawBuffs = (List<List<Object>>) tierSection.getList("buffs");
            if (rawBuffs == null) {
                Log("Could not load the buffs.", LogType.SEVERE);
                return;
            }
            for (List<Object> buffEntry : rawBuffs) {
                if (buffEntry.size() != 2) {
                    Log("Tier " + key + " has malformed buff entry: " + buffEntry, LogType.SEVERE);
                    return;
                }

                String effectName = buffEntry.get(0).toString();
                int level;
                try {
                    level = Integer.parseInt(buffEntry.get(1).toString());
                } catch (NumberFormatException e) {
                    Log("Tier " + key + " has invalid level for effect " + effectName, LogType.SEVERE);
                    return;
                }

                PotionEffectType type = Registry.POTION_EFFECT_TYPE.get(NamespacedKey.minecraft(effectName.toLowerCase()));
                if (type == null) {
                    Log("Unknown potion effect: " + effectName, LogType.SEVERE);
                    return;
                }

                PotionEffect effect = new PotionEffect(type, 120, level - 1);
                clanTier.buffs.add(effect);
            }

            @SuppressWarnings("unchecked")
            List<List<Object>> rawDebuffs = (List<List<Object>>) tierSection.getList("debuffs");
            if (rawDebuffs == null) {
                Log("Could not load the debuffs.", LogType.SEVERE);
                return;
            }
            for (List<Object> debuffEntry : rawDebuffs) {
                if (debuffEntry.size() != 2) {
                    Log("Tier " + key + " has malformed debuff entry: " + debuffEntry, LogType.SEVERE);
                    return;
                }

                String effectName = debuffEntry.get(0).toString();
                int level;
                try {
                    level = Integer.parseInt(debuffEntry.get(1).toString());
                } catch (NumberFormatException e) {
                    Log("Tier " + key + " has invalid level for effect " + effectName, LogType.SEVERE);
                    return;
                }

                PotionEffectType type = Registry.POTION_EFFECT_TYPE.get(NamespacedKey.minecraft(effectName.toLowerCase()));
                if (type == null) {
                    Log("Unknown potion effect: " + effectName, LogType.SEVERE);
                    return;
                }

                PotionEffect effect = new PotionEffect(type, EFFECT_DURATION, level - 1);
                clanTier.debuffs.add(effect);
            }
            tiers.add(clanTier);
        }

        writeOut();
        validConfig = true;
    }

    private void writeOut() {
        for (int i = 0; i < tiers.size(); i++) {
            Log("Tier " + i + ": " + tiers.get(i), LogType.INFO);
        }
    }

    public int getPlayerLimit(int tier) {
        return tiers.get(tier).playerLimit;
    }
    public int getChunkLimit(int tier) {
        return tiers.get(tier).chunkLimit;
    }
    public int getPrice(int tier) {
        return tiers.get(tier).price;
    }
    public int getHighestTierNumber() {
        return tiers.size() - 1;
    }
    public String getLabel(int tier) {
        return tiers.get(tier).label;
    }
    public List<PotionEffect> getBuffs(int tier) {
        return tiers.get(tier).buffs;
    }
    public List<PotionEffect> getDebuffs(int tier) {
        return tiers.get(tier).debuffs;
    }
    public boolean canAffordUpgrade(Player player, int tier) {
        double price = tiers.get(tier).price;
        return VClans.getInstance().getVaultHandler().getPlayerBalance(player) >= price;
    }
    public boolean isValidConfig() {
        return validConfig;
    }
}
