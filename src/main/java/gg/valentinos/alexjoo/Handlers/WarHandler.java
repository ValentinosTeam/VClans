package gg.valentinos.alexjoo.Handlers;

import gg.valentinos.alexjoo.Data.ClanData.Clan;
import gg.valentinos.alexjoo.Data.LogType;
import gg.valentinos.alexjoo.Data.WarData.War;
import gg.valentinos.alexjoo.Data.WarData.WarState;
import gg.valentinos.alexjoo.Data.WarData.Wars;
import gg.valentinos.alexjoo.Utility.JsonUtils;
import gg.valentinos.alexjoo.Utility.TaskScheduler;
import gg.valentinos.alexjoo.VClans;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashSet;

import static gg.valentinos.alexjoo.VClans.Log;

public class WarHandler {
    public final long GRACE_PERIOD;
    public final long WAR_DURATION;
    public final long WAR_COOLDOWN;
    public final int MAX_DEFENCE_HP = 100;

    private final TaskScheduler scheduler;

    private Wars wars;

    public WarHandler() {
        ConfigurationSection config = VClans.getInstance().getConfig().getConfigurationSection("settings.war");
        GRACE_PERIOD = config != null ? config.getLong("grace-period") : 60 * 60 * 24 * 20;
        WAR_DURATION = config != null ? config.getLong("war-duration") : 60 * 60 * 4 * 20;
        WAR_COOLDOWN = config != null ? config.getLong("war-cooldown") : 60 * 60 * 24 * 20;
        Log("Grace " + GRACE_PERIOD);
        Log("Duration " + WAR_DURATION);
        Log("Cooldown " + WAR_COOLDOWN);
        scheduler = VClans.getInstance().getTaskScheduler();
        loadWars();

    }

    public void declareWar(Clan initiator, Clan target) {
        War war = new War(initiator, target);
        wars.getWars().add(war);
//        saveWars();
        Log("Clan " + initiator.getId() + " has declared war on clan " + target.getId(), LogType.INFO);
        changeWarState(war, WarState.DECLARED, 0);
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

    public int getWarCooldown(Clan clan) {
        long currentTime = System.currentTimeMillis();
        long lastWarTime = clan.getLastWarTime();
        long timeSinceLastWar = (currentTime - lastWarTime) / 1000;
        if (timeSinceLastWar >= WAR_COOLDOWN) {
            return 0;
        } else {
            return (int) (WAR_COOLDOWN - timeSinceLastWar);
        }
    }
    public War getWar(Clan clan) {
        if (clan == null) return null;
        String clanId = clan.getId();
        for (War war : wars.getWars()) {
            if (war.getTargetClanId().equals(clanId) || war.getInitiatorClanId().equals(clanId)) return war;
        }
        return null;
    }
    public War getWarBetween(Player player, Clan clan) {
        Clan playerClan = VClans.getInstance().getClanHandler().getClanByMember(player.getUniqueId());
        if (playerClan == null) return null;
        if (clan == null) return null;
        if (playerClan.equals(clan)) return null;
        String playerClanId = playerClan.getId();
        String clanId = clan.getId();
        for (War war : wars.getWars()) {
            if ((war.getTargetClanId().equals(playerClanId) || war.getInitiatorClanId().equals(playerClanId)) &&
                    (war.getTargetClanId().equals(clanId) || war.getInitiatorClanId().equals(clanId))) {
                return war;
            }
        }
        return null;
    }
    public boolean isInWarWith(Player player, Clan clan) {
        return getWarBetween(player, clan) != null;
    }
    public boolean isInWar(Player player) {
        Clan playerClan = VClans.getInstance().getClanHandler().getClanByMember(player.getUniqueId());
        return isInWar(playerClan);
    }
    public boolean isInWar(Clan clan) {
        return getWar(clan) != null;
    }

    private void changeWarState(War war, WarState newState, long delayTicks) {
        scheduler.runTaskLater(() -> {
            war.changeState(newState);
            Log("War between " + war.getInitiatorClanId() + " and " + war.getTargetClanId() + " changed to " + newState, LogType.INFO);
        }, delayTicks);
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
        // Start timers for each war
        long currentTime = System.currentTimeMillis() / 1000;
        for (War war : wars.getWars()) {
            long declarationTime = war.getDeclarationTime() / 1000;
            long timeSinceDeclaration = currentTime - declarationTime;

            if (timeSinceDeclaration < GRACE_PERIOD) {
                war.setState(WarState.DECLARED);
                long delay = (GRACE_PERIOD - timeSinceDeclaration) * 20;
                changeWarState(war, WarState.IN_PROGRESS, delay);
            } else if (timeSinceDeclaration < GRACE_PERIOD + WAR_DURATION) {
                war.setState(WarState.IN_PROGRESS);
                long delay = (GRACE_PERIOD + WAR_DURATION - timeSinceDeclaration) * 20;
                changeWarState(war, WarState.ENDED, delay);
            }
        }
    }
    public void saveWars() {
        JsonUtils.toJsonFile(wars.getWars(), "wars.json");
    }

}
