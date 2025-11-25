package gg.valentinos.alexjoo.Data.WarData;

import gg.valentinos.alexjoo.Data.ClanData.Clan;
import org.bukkit.scheduler.BukkitTask;

import java.util.function.Consumer;

public class WarProgressBarTask implements Consumer<BukkitTask> {

    private final War war;
    private final Clan defenderClan;
    private final Clan attackerClan;

    public WarProgressBarTask(War war, Clan defenderClan, Clan attackerClan) {
        this.war = war;
        this.defenderClan = defenderClan;
        this.attackerClan = attackerClan;
    }

    @Override
    public void accept(BukkitTask bukkitTask) {
        //TODO: Create a bossbar showing the progress of the war
        // check if war has ended, if it did: hide the bossbar and stop this task
        // first case: Waiting for grace period to end
        // second case: Waiting for war duration to end
        // in both cases, this task ran every second will count down 1 second from the timer
        // update the bossbar progress and apply that float to it
        // update the bossbar name to be the configurable string that takes parameters of hour minute and second, for example...
        //      grace-period-bossbar: "{h}h {m}m {s}s left until the war starts" to produce "2h 29m 5s left until the war starts" or...
        //      war-period-bossbar: "WAR ENDs IN {h}:{m}:{s}" to produce "WAR ENDS IN 1:25:1"
    }
}
