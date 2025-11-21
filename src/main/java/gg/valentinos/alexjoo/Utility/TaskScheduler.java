package gg.valentinos.alexjoo.Utility;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.util.function.Consumer;

import static gg.valentinos.alexjoo.VClans.Log;

public class TaskScheduler {

    private final BukkitScheduler scheduler;
    private final Plugin plugin;

    public TaskScheduler(Plugin plugin) {
        this.plugin = plugin;
        this.scheduler = plugin.getServer().getScheduler();
    }

    public void runTaskLater(Runnable task, long delayTicks) {
        Log("Running delayed task in " + delayTicks + " ticks");
        scheduler.scheduleSyncDelayedTask(plugin, task, delayTicks);
    }

    public void runTaskTimer(Consumer<BukkitTask> task, long delayTicks, long intervalTicks) {
        Log("Running repeated task");
        scheduler.runTaskTimer(plugin, task, delayTicks, intervalTicks);
    }

}
