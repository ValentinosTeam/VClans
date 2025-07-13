package gg.valentinos.alexjoo.Handlers;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import gg.valentinos.alexjoo.Data.LogType;
import gg.valentinos.alexjoo.Data.WorldGuardFlags;
import gg.valentinos.alexjoo.VClans;
import org.bukkit.Bukkit;
import org.bukkit.Color;

import java.util.Map;
import java.util.stream.Collectors;

import static gg.valentinos.alexjoo.VClans.Log;

public class WorldGuardHandler {

    private RegionManager regionManager;
    private int maxHeight;
    private boolean enabled = false;
    private boolean registeredFlags = false;
    private final Color color = Color.fromRGB(250, 25, 25);

    public WorldGuardHandler() {
    }

    public boolean enable() {
        if (!registeredFlags) return false;
        String worldName = VClans.getInstance().getConfig().getString("settings.world-name");
        if (worldName == null) {
            Log("Couldn't find world-name in config.yml!", LogType.SEVERE);
            return false;
        }
        org.bukkit.World bukkitWorld = Bukkit.getWorld(worldName);
        if (bukkitWorld == null) {
            Log("Couldn't find world " + worldName, LogType.SEVERE);
            return false;
        }
        this.maxHeight = bukkitWorld.getMaxHeight();
        try {
            World world = BukkitAdapter.adapt(bukkitWorld);
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            regionManager = container.get(world);
            if (regionManager == null) return false;
            enabled = true;
        } catch (NoClassDefFoundError e) {
            Log("Couldn't find WorldGuard Class");
            return false;
        }
        return true;
    }

    public boolean isEnabled() {
        return enabled;
    }
    public boolean isChunkOverlappingWithRegion(int x, int z) {
        int minX = x << 4;
        int minZ = z << 4;
        int maxX = minX + 15;
        int maxZ = minZ + 15;
        return isAreaOverlappingWithRegion(minX, minZ, maxX, maxZ);
    }
    public boolean isAreaOverlappingWithRegion(int minX, int minZ, int maxX, int maxZ) {
        BlockVector3 min = BlockVector3.at(minX, 0, minZ);
        BlockVector3 max = BlockVector3.at(maxX, maxHeight, maxZ);

        ProtectedRegion tempChunkRegion = new ProtectedCuboidRegion("__temp", min, max);
        ApplicableRegionSet overlapping = regionManager.getApplicableRegions(tempChunkRegion);

        for (ProtectedRegion region : overlapping) {
            StateFlag.State state = region.getFlag(WorldGuardFlags.VCLANS_PROTECTED_REGION);
            if (state == StateFlag.State.ALLOW) {
                return true;
            }
        }
        return false;
    }
    public boolean registerFlag() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            registry.register(WorldGuardFlags.VCLANS_PROTECTED_REGION);
        } catch (FlagConflictException e) {
            Flag<?> existing = registry.get("vclans-protected");
            if (existing instanceof StateFlag) {
                WorldGuardFlags.VCLANS_PROTECTED_REGION = (StateFlag) existing;
            } else {
                Log("vclans-protected flag conflict with incompatible type.", LogType.SEVERE);
                return false;
            }
        }
        registeredFlags = true;
        return true;
    }

    public Color getColor() {
        return color;
    }
    public Map<String, ProtectedRegion> getRegions() {
        return regionManager.getRegions().entrySet().stream()
                .filter(entry -> {
                    StateFlag.State state = entry.getValue().getFlag(WorldGuardFlags.VCLANS_PROTECTED_REGION);
                    return state == StateFlag.State.ALLOW;
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
