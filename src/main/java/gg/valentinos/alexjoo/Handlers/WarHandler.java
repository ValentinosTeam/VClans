package gg.valentinos.alexjoo.Handlers;

import gg.valentinos.alexjoo.Data.ClanData.Clan;
import gg.valentinos.alexjoo.Data.LogType;
import gg.valentinos.alexjoo.Data.WarData.War;
import gg.valentinos.alexjoo.Data.WarData.Wars;
import gg.valentinos.alexjoo.Utility.JsonUtils;
import gg.valentinos.alexjoo.VClans;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashSet;

import static gg.valentinos.alexjoo.VClans.Log;

public class WarHandler {
    private final long GRACE_PERIOD;
    private final long WAR_DURATION;
    private final long WAR_COOLDOWN;

    private Wars wars;

    public WarHandler() {
        ConfigurationSection config = VClans.getInstance().getConfig().getConfigurationSection("settings.war");
        GRACE_PERIOD = config != null ? config.getLong("grace-period") : 60 * 60 * 24;
        WAR_DURATION = config != null ? config.getLong("war-duration") : 60 * 60 * 4;
        WAR_COOLDOWN = config != null ? config.getLong("war-cooldown") : 60 * 60 * 24;
        loadWars();
    }

    public void declareWar(Clan initiator, Clan target) {
        War war = new War(initiator, target);
        wars.getWars().add(war);
        saveWars();
        Log("Clan " + initiator.getId() + " has declared war on clan " + target.getId(), LogType.INFO);
    }

    public Clan inWar(Clan clan) {
        for (War war : wars.getWars()) {
            if (war.getInitiatorClanId().equals(clan.getId())) {
                VClans.getInstance().getClanHandler().getClanById(war.getTargetClanId());
                return VClans.getInstance().getClanHandler().getClanById(war.getTargetClanId());
            } else if (war.getTargetClanId().equals(clan.getId())) {
                return VClans.getInstance().getClanHandler().getClanById(war.getInitiatorClanId());
            }
        }
        return null;
    }


    // saves and loads the wars
    public void loadWars() {
        wars = new Wars();
        HashSet<War> warHashSet = JsonUtils.deserializeWars("wars.json");
        if (warHashSet == null) {
            Log("Wars file is empty. Creating new wars.json file.", LogType.WARNING);
            saveWars();
            return;
        }
        wars.setWars(warHashSet);
    }
    public void saveWars() {
        JsonUtils.toJsonFile(wars.getWars(), "wars.json");
    }

}
