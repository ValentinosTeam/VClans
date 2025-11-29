package gg.valentinos.alexjoo.Handlers;

import gg.valentinos.alexjoo.Data.ClanData.Clan;
import gg.valentinos.alexjoo.Data.LogType;
import gg.valentinos.alexjoo.Data.WarData.*;
import gg.valentinos.alexjoo.Utility.JsonUtils;
import gg.valentinos.alexjoo.Utility.TaskScheduler;
import gg.valentinos.alexjoo.VClans;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;

import static gg.valentinos.alexjoo.VClans.Log;

public class WarHandler {
    public final int GRACE_PERIOD;
    public final int WAR_DURATION;
    public final int WAR_COOLDOWN;
    public final int GRACE_SKIP_TILL;
    public final int CHUNK_HEALTH_POINTS;
    public final int CHUNK_OCCUPATION_DAMAGE;
    public final int MIN_CLAN_TIER;
    public final String GRACE_PERIOD_BOSSBAR_FORMAT;
    public final String WAR_BOSSBAR_FORMAT;

    private final TaskScheduler scheduler;

    private Wars wars;
    private HashMap<War, WarProgressBarTask> warMap;

    public WarHandler() {
        ConfigurationSection config = VClans.getInstance().getConfig().getConfigurationSection("settings.war");
        GRACE_PERIOD = config != null ? config.getInt("grace-period") : 86400;
        WAR_DURATION = config != null ? config.getInt("war-duration") : 25200;
        WAR_COOLDOWN = config != null ? config.getInt("war-cooldown") : 86400;
        GRACE_SKIP_TILL = config != null ? config.getInt("grace-skip-up-to") : 1800;
        CHUNK_HEALTH_POINTS = config != null ? config.getInt("chunk-health-points") : 1000;
        CHUNK_OCCUPATION_DAMAGE = config != null ? config.getInt("chunk-occupation-damage") : 1;
        MIN_CLAN_TIER = config != null ? config.getInt("min_clan_tier") : 1;
        GRACE_PERIOD_BOSSBAR_FORMAT = config != null ? config.getString("grace-period-bossbar-name") : "Grace Period ends in: {h}h {m}m {s}s.";
        WAR_BOSSBAR_FORMAT = config != null ? config.getString("war-bossbar-name") : "{h}:{m}:{s} before war ends!";
        scheduler = VClans.getInstance().getTaskScheduler();
        warMap = new HashMap<>();
    }

    public void declareWar(Clan initiator, Clan target) {
        War war = new War(initiator, target);
        wars.getWars().add(war);
        war.declareWar();
        WarProgressBarTask warProgressBarTask = new WarProgressBarTask(war, target, initiator);
        scheduler.runTaskTimer(warProgressBarTask, 1, 20);
        warMap.put(war, warProgressBarTask);
        saveWars();
    }
    public void createPeaceTreaty(Player creator, War war, int amount) {
        Clan initiatorClan = VClans.getInstance().getClanHandler().getClanById(war.getInitiatorClanId());
        Clan targetClan = VClans.getInstance().getClanHandler().getClanById(war.getTargetClanId());
        if (initiatorClan == null || targetClan == null) {
            return;
        }
        PeaceTreaty peaceTreaty;
        if (initiatorClan.isPlayerMember(creator.getUniqueId())) {
            peaceTreaty = new PeaceTreaty(creator, targetClan, amount);
        } else if (targetClan.isPlayerMember(creator.getUniqueId())) {
            peaceTreaty = new PeaceTreaty(creator, initiatorClan, amount);
        } else return;
        war.setPeaceTreaty(peaceTreaty);
    }
    public void acceptPeaceTreaty(Player acceptor, War war) {
        PeaceTreaty peaceTreaty = war.getPeaceTreaty();
        if (peaceTreaty == null) return;
        peaceTreaty.payOutTreaty(acceptor);
        war.endWar();
    }
    public void declinePeaceTreaty(War war) {
        war.setPeaceTreaty(null);
    }
    public void skipForwardGrace(War war) {
        WarProgressBarTask warProgressBarTask = warMap.get(war);
        if (warProgressBarTask == null) return;
        warProgressBarTask.skipForwardGrace();
    }

    public Clan getWarEnemyClan(Clan clan) {
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
    public int getTimeLeftInSeconds(War war) {
        WarProgressBarTask warProgressBarTask = warMap.get(war);
        if (warProgressBarTask == null) return 0;
        else return warProgressBarTask.getTimeLeftInSeconds();
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
    public boolean isInActiveWarWith(Player player, Clan clan) {
        War war = getWarBetween(player, clan);
        return (war != null && war.getState() == WarState.IN_PROGRESS);
    }
    public boolean isInWar(Player player) {
        Clan playerClan = VClans.getInstance().getClanHandler().getClanByMember(player.getUniqueId());
        return isInWar(playerClan);
    }
    public boolean isInWar(Clan clan) {
        return getWar(clan) != null;
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
//        wars.setWars(warHashSet);
        // Start timers for each war
        //TODO: handle war loading states
        for (War war : warHashSet) {
            if (war.getState() != WarState.ENDED) {

                Clan initiatorClan = VClans.getInstance().getClanHandler().getClanById(war.getInitiatorClanId());
                Clan targetClan = VClans.getInstance().getClanHandler().getClanById(war.getTargetClanId());
                if (initiatorClan == null || targetClan == null) {
                    Log("One of the clans in war " + war.getInitiatorClanId() + " vs " + war.getTargetClanId() + " does not exist. Ending war.", LogType.WARNING);
                    war.setState(WarState.ENDED);
                } else {
                    wars.addWar(war);
                    war.initHandlers();
                    scheduler.runTaskTimer(new WarProgressBarTask(war, targetClan, initiatorClan), 1, 20);
                }
            }
        }
    }
    public void saveWars() {
        JsonUtils.toJsonFile(wars.getWars(), "wars.json");
    }

}
