package gg.valentinos.alexjoo.Listeners;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import gg.valentinos.alexjoo.VClans;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static gg.valentinos.alexjoo.VClans.Log;

public class PlayerListener implements Listener {

    private final boolean showCurrentClanOnJoin;

    public PlayerListener() {
        showCurrentClanOnJoin = VClans.getInstance().getConfig().getBoolean("show-current-clan-on-join");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (showCurrentClanOnJoin) {
            String clanName = VClans.getInstance().getClanHandler().getClanNameOfMember(player.getUniqueId());
            player.sendMessage("Current clan: " + (clanName == null ? "None" : clanName));
        }
        List<String> invitedClanNames = VClans.getInstance().getClanHandler().getInvitingClanNames(player.getUniqueId());
        StringBuilder sb = new StringBuilder();
        for (String invitedClanName : invitedClanNames) {
            sb.append(invitedClanName).append(", ");
        }
        if (!invitedClanNames.isEmpty())
            player.sendMessage("You have been invited to: \n" + sb);
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(Objects.requireNonNull(Bukkit.getWorld("world"))));

        assert regions != null;
        regions.getApplicableRegions(new BlockVector3(2, 3, 4));
        for (Map.Entry<String, ProtectedRegion> regionEntry : regions.getRegions().entrySet()) {
            ProtectedRegion region = regionEntry.getValue();
            String name = region.getId();
            Log("Region id: " + name + " region info: " + region.toString());
//            region.getMinimumPoint()
        }
    }

}
