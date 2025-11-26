package gg.valentinos.alexjoo.Data.WarData;

import gg.valentinos.alexjoo.Data.ClanData.Clan;
import gg.valentinos.alexjoo.VClans;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static gg.valentinos.alexjoo.VClans.Log;

public class WarProgressBarTask implements Consumer<BukkitTask>, Listener {

    private final War war;
    private final Clan defenderClan;
    private final Clan attackerClan;

    private final int GRACE_PERIOD_DURATION;
    private final int WAR_DURATION;
    private final String GRACE_PERIOD_BOSSBAR_FORMAT;
    private final String WAR_BOSSBAR_FORMAT;

    private int timeLeftInSeconds;
    private BossBar bossBar;

    public WarProgressBarTask(War war, Clan defenderClan, Clan attackerClan) {
        this.war = war;
        this.defenderClan = defenderClan;
        this.attackerClan = attackerClan;

        this.GRACE_PERIOD_DURATION = VClans.getInstance().getWarHandler().GRACE_PERIOD;
        this.WAR_DURATION = VClans.getInstance().getWarHandler().WAR_DURATION;
        this.GRACE_PERIOD_BOSSBAR_FORMAT = VClans.getInstance().getWarHandler().GRACE_PERIOD_BOSSBAR_FORMAT;
        this.WAR_BOSSBAR_FORMAT = VClans.getInstance().getWarHandler().WAR_BOSSBAR_FORMAT;

        this.timeLeftInSeconds = calculateTimeLeftInSeconds();
        Log("Initialized WarProgressBarTask with " + timeLeftInSeconds + " seconds left.");
        updateBossBar();

        Audience audience = Audience.audience(new ArrayList<Player>() {{
            addAll(defenderClan.getOnlinePlayers());
            addAll(attackerClan.getOnlinePlayers());
        }});
        audience.showBossBar(bossBar);

        VClans.getInstance().getServer().getPluginManager().registerEvents(this, VClans.getInstance());
    }

    @Override
    public void accept(BukkitTask bukkitTask) {
        timeLeftInSeconds--;
        Log(timeLeftInSeconds + " seconds left.");
        if (timeLeftInSeconds <= 0) {
            switch (war.getState()) {
                case DECLARED -> {
                    war.startWar();
                    timeLeftInSeconds = WAR_DURATION;
                    bossBar.color(BossBar.Color.RED);
                }
                case IN_PROGRESS -> {
                    war.endWar();
                }
            }
        }
        if (war.getState() == WarState.ENDED) {
            stopTask();
            bukkitTask.cancel();
            return;
        }
        updateBossBar();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (defenderClan.isPlayerMember(player.getUniqueId()) || attackerClan.isPlayerMember(player.getUniqueId())) {
            Audience.audience(player).showBossBar(bossBar);
        }
    }

    private void stopTask() {
        List<Player> players = new ArrayList<>();
        players.addAll(defenderClan.getOnlinePlayers());
        players.addAll(attackerClan.getOnlinePlayers());
        Audience playerAudience = Audience.audience(players);
        playerAudience.hideBossBar(bossBar);
        bossBar = null;
        PlayerJoinEvent.getHandlerList().unregister(this);
    }
    private void updateBossBar() {
//        timeLeftInSeconds = calculateTimeLeftInSeconds();
        Component name = Component.text(formatTime());
        float progress;
        switch (war.getState()) {
            case DECLARED -> progress = (float) timeLeftInSeconds / GRACE_PERIOD_DURATION;
            case IN_PROGRESS -> progress = (float) timeLeftInSeconds / WAR_DURATION;
            default -> progress = 0f;
        }
        if (war.getState() == WarState.ENDED) {
            //TODO: hide bossbar and remove it
            return;
        }
        if (bossBar == null) {
            bossBar = BossBar.bossBar(name, progress, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS);
        } else {
            bossBar.name(name);
            bossBar.progress(progress);
        }
    }
    private int calculateTimeLeftInSeconds() {
        long now = System.currentTimeMillis() / 1000;
        long start = war.getDeclarationTime() / 1000;

        long graceEnd = start + GRACE_PERIOD_DURATION;
        long warEnd = graceEnd + WAR_DURATION;

        long target;

        switch (war.getState()) {
            case DECLARED -> target = graceEnd;
            case IN_PROGRESS -> target = warEnd;
            default -> target = now;
        }

        long timeLeft = target - now;
        Log("Calculated time left: " + timeLeft + " s." + " (now: " + now + ", target: " + target + ")" + " for state: " + war.getState());
        return (int) Math.max(0, timeLeft);
    }
    private String formatTime() {
        int hours = timeLeftInSeconds / 3600;
        int minutes = (timeLeftInSeconds % 3600) / 60;
        int seconds = timeLeftInSeconds % 60;

        String formatString = "";
        switch (war.getState()) {
            case DECLARED -> formatString = GRACE_PERIOD_BOSSBAR_FORMAT;
            case IN_PROGRESS -> formatString = WAR_BOSSBAR_FORMAT;
        }

        return formatString.replace("{h}", String.format("%02d", hours))
                .replace("{m}", String.format("%02d", minutes))
                .replace("{s}", String.format("%02d", seconds));
    }
}
