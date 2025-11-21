package gg.valentinos.alexjoo.Handlers;

import gg.valentinos.alexjoo.Data.ClanData.Clan;
import gg.valentinos.alexjoo.Data.ClanData.ClanChunk;
import gg.valentinos.alexjoo.Data.LogType;
import gg.valentinos.alexjoo.GUIs.ChunkRadar;
import gg.valentinos.alexjoo.VClans;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static gg.valentinos.alexjoo.VClans.Log;
import static gg.valentinos.alexjoo.VClans.WORLD_NAME;

public class ChunkHandler {

    private final ClanHandler clanHandler;
    private final HashMap<ChunkPos, ClanChunk> chunks = new HashMap<>();
    private final int enemyProximityRadius;
    private final int regionProximityRadius;
    private final HashMap<Player, ChunkRadar> radars = new HashMap<>();

    // formula evaluation relevant fields for chunk cost
    private String chunkCostFormula;
    private final ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
    private static final Pattern SAFE_MATH_PATTERN = Pattern.compile("^[0-9xX+\\-*/^().\\sMath]*$");
    private static final String DEFAULT_CHUNK_FORMULA = "100*x^2";


    private record ChunkPos(int x, int z) {
    }

    public ChunkHandler() {
        this.clanHandler = VClans.getInstance().getClanHandler();
        for (Clan clan : clanHandler.getClans()) {
            if (clan.getChunks() == null) {
                clan.setChunks(new HashSet<>());
            }
            for (ClanChunk chunk : clan.getChunks()) {
                chunks.put(new ChunkPos(chunk.getX(), chunk.getZ()), chunk);
            }
        }
        this.enemyProximityRadius = VClans.getInstance().getConfig().getInt("settings.enemy-proximity-radius");
        this.regionProximityRadius = VClans.getInstance().getConfig().getInt("settings.region-proximity-radius");

        chunkCostFormula = VClans.getInstance().getConfig().getString("settings.chunk-cost-formula");
        if (chunkCostFormula == null) {
            Log("Formula Not Found - make sure to fill settings.chunk-cost-formula with a correct mathematical formula like. Defaulting to " + DEFAULT_CHUNK_FORMULA, LogType.SEVERE);
            chunkCostFormula = DEFAULT_CHUNK_FORMULA;
        } else if (!isSafeFormula(chunkCostFormula)) {
            Log("Unsafe characters detected in chunk-cost-formula!" + chunkCostFormula + " Defaulting to " + DEFAULT_CHUNK_FORMULA, LogType.SEVERE);
            chunkCostFormula = DEFAULT_CHUNK_FORMULA;
        }
        startEffectInterval();
    }

    public void claimChunk(Chunk chunk, Player player) {
        String clanName = VClans.getInstance().getClanHandler().getClanByMember(player.getUniqueId()).getId();
        Clan clan = clanHandler.getClanById(clanName);
        if (clan == null) return;

        VClans.getInstance().getVaultHandler().withdrawPlayer(player, getNewChunkPrice(clan));

        ClanChunk newChunk = new ClanChunk(chunk.getX(), chunk.getZ(), WORLD_NAME, clanName);
        addChunk(newChunk, clan);
        updateChunkRadar(player, chunk);
        BlueMapHandler blueMapHandler = VClans.getInstance().getBlueMapHandler();
        if (blueMapHandler != null) {
            blueMapHandler.drawClanTerritory(clan);
        }
    }
    public void unclaimChunk(Chunk chunk, Player player) {
        String clanName = VClans.getInstance().getClanHandler().getClanByMember(player.getUniqueId()).getId();
        Clan clan = clanHandler.getClanById(clanName);
        ClanChunk chunkToRemove = chunks.get(new ChunkPos(chunk.getX(), chunk.getZ()));
        removeChunk(chunkToRemove, clan);
        updateChunkRadar(player, chunk);
        BlueMapHandler blueMapHandler = VClans.getInstance().getBlueMapHandler();
        if (blueMapHandler != null) {
            blueMapHandler.drawClanTerritory(clan);
        }
    }
    public void unclaimChunks(String clanName) {
        Clan clan = clanHandler.getClanById(clanName);
        for (ClanChunk chunk : new HashSet<>(clan.getChunks())) {
            removeChunk(chunk, clan);
        }
    }
    public void toggleChunkRadar(Player player) {
        if (radars.containsKey(player)) {
            ChunkRadar radar = radars.get(player);
            if (radar != null) {
                radar.closeRadar();
                radars.remove(player);
            }
        } else {
            if (!(player.getWorld().getName().equals(WORLD_NAME))) return;
            ChunkRadar chunkRadar = new ChunkRadar(player);
            chunkRadar.initializeRadar();
            radars.put(player, chunkRadar);
        }
    }
    public void updateChunkRadar(Player player, Chunk chunk) {
        if (!chunk.getWorld().getName().equals(WORLD_NAME)) return;
        ChunkRadar radar = radars.get(player);
        if (radar != null) {
            if (!(player.getWorld().getName().equals(WORLD_NAME))) {
                radar.closeRadar();
                radars.remove(player);
            } else {
                radar.updateRadar(chunk);
            }
        }
    }

    public int getClanMaxChunkAmount(Clan clan) {
        return VClans.getInstance().getClanTierHandler().getChunkLimit(clan.getTier());
    }
    public double getNewChunkPrice(@NotNull Clan clan) {
        return VClans.getInstance().getVaultHandler().calculateFormula(chunkCostFormula, clan.getChunks().size());
    }
    public String getChunkInfo(Chunk chunk) {
        ClanChunk clanChunk = getChunk(chunk.getX(), chunk.getZ());
        // TODO: make this configurable.
        if (clanChunk == null) {
            return "Chunk Unclaimed";
        }
        return "Chunk Info:" + "\n" +
                "X: " + clanChunk.getX() + "\n" +
                "Z: " + clanChunk.getZ() + "\n" +
                "World: " + clanChunk.getWorld() + "\n" +
                "Clan Name: " + clanChunk.getClanId() + "\n";
    }
    public String getClanNameByChunk(Chunk chunk) {
        if (!chunk.getWorld().getName().equals(WORLD_NAME)) return null;
        ClanChunk clanChunk = getChunk(chunk.getX(), chunk.getZ());
        if (clanChunk == null) {
            return null;
        }
        return clanChunk.getClanId();
    }
    public String getClanNameByChunk(int x, int z) {
        ClanChunk clanChunk = getChunk(x, z);
        if (clanChunk == null) {
            return null;
        }
        return clanChunk.getClanId();
    }
    public boolean isChunkClaimedByClan(Chunk chunk, String clanName) {
        ClanChunk clanChunk = getChunk(chunk.getX(), chunk.getZ());
        return clanChunk != null && clanChunk.getClanId().equals(clanName);
    }
    public boolean unclaimWillSplit(Chunk chunk, String clanName) {
        if (!chunk.getWorld().getName().equals(WORLD_NAME)) return false;
        Clan clan = clanHandler.getClanById(clanName);
        if (clan == null) {
            Log("Clan not found", LogType.SEVERE);
            return false;
        }
        Set<ChunkPos> chunks = clan.getChunks().stream()
                .map(c -> new ChunkPos(c.getX(), c.getZ()))
                .collect(Collectors.toSet());
        ChunkPos toRemove = new ChunkPos(chunk.getX(), chunk.getZ());
        if (!chunks.contains(toRemove)) return false;

        Set<ChunkPos> remaining = new HashSet<>(chunks);
        remaining.remove(toRemove);

        if (remaining.isEmpty()) return false;

        Set<ChunkPos> visited = new HashSet<>();
        Queue<ChunkPos> queue = new LinkedList<>();
        ChunkPos start = remaining.iterator().next();
        queue.add(start);
        visited.add(start);

        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

        while (!queue.isEmpty()) {
            ChunkPos current = queue.poll();
            for (int[] d : dirs) {
                ChunkPos neighbor = new ChunkPos(current.x() + d[0], current.z() + d[1]);
                if (remaining.contains(neighbor) && visited.add(neighbor)) {
                    queue.add(neighbor);
                }
            }
        }

        return visited.size() != remaining.size();
    }
    public boolean isChunkAdjacentToClan(Chunk chunk, String clanName) {
        if (!chunk.getWorld().getName().equals(WORLD_NAME)) return false;
        for (ClanChunk clanChunk : getAdjacentChunks(chunk.getX(), chunk.getZ(), false)) {
            if (clanChunk != null && clanChunk.getClanId().equals(clanName)) {
                return true;
            }
        }
        return false;
    }
    public boolean isChunkInValidWorld(String name) {
        return name.equals(WORLD_NAME);
    }
    public boolean isChunkCloseToEnemyClan(Chunk chunk, String clanName) {
        if (!chunk.getWorld().getName().equals(WORLD_NAME)) return false;
        for (int dx = -enemyProximityRadius; dx <= enemyProximityRadius; dx++) {
            for (int dz = -enemyProximityRadius; dz <= enemyProximityRadius; dz++) {
                if (dx == 0 && dz == 0) continue;
                ClanChunk clanChunk = getChunk(dx + chunk.getX(), dz + chunk.getZ());
                if (clanChunk != null && !clanChunk.getClanId().equals(clanName)) return true;
            }
        }
        return false;
    }
    public boolean isChunkCloseToRegion(Chunk chunk) {
        if (!chunk.getWorld().getName().equals(WORLD_NAME)) return false;
        int x = chunk.getX();
        int z = chunk.getZ();
        if (VClans.getInstance().getWorldGuardHandler().isEnabled()) {
            int minX = (x - regionProximityRadius) << 4;
            int minZ = (z - regionProximityRadius) << 4;
            int maxX = ((x + regionProximityRadius + 1) << 4) - 1;
            int maxZ = ((z + regionProximityRadius + 1) << 4) - 1;
            return VClans.getInstance().getWorldGuardHandler().isAreaOverlappingWithRegion(minX, minZ, maxX, maxZ);
        }
        // return true cause worldguard isn't enabled if reached this.
        return true;
    }
    public boolean canAffordNewChunk(Player player) {
        Clan clan = clanHandler.getClanByMember(player.getUniqueId());
        if (clan == null) return false;

        return VClans.getInstance().getVaultHandler().getPlayerBalance(player) >= getNewChunkPrice(clan);
    }
    public List<Player> getPlayersInChunk(ClanChunk clanChunk) {
        List<Player> playersInChunk = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld().getName().equals(WORLD_NAME)) {
                Chunk playerChunk = player.getChunk();
                if (playerChunk.getX() == clanChunk.getX() && playerChunk.getZ() == clanChunk.getZ()) {
                    playersInChunk.add(player);
                }
            }
        }
        return playersInChunk;
    }

    public List<ClanChunk> getAdjacentChunks(int x, int z, boolean includeCorners) {
        List<ClanChunk> adjacentChunks = new ArrayList<>();
        int[][] offsets;
        if (includeCorners) {
            offsets = new int[][]{
                    {1, 0},  // right
                    {-1, 0}, // left
                    {0, 1},  // up
                    {0, -1}, // down
                    {1, 1},  // top-right
                    {1, -1}, // bottom-right
                    {-1, 1}, // top-left
                    {-1, -1} // bottom-left
            };
        } else {
            offsets = new int[][]{
                    {1, 0},  // right
                    {-1, 0}, // left
                    {0, 1},  // up
                    {0, -1}  // down
            };
        }
        for (int[] offset : offsets) {
            int newX = x + offset[0];
            int newZ = z + offset[1];
            ClanChunk chunk = getChunk(newX, newZ);
            if (chunk != null) adjacentChunks.add(chunk);
        }
        return adjacentChunks;
    }
    public List<ClanChunk> getAdjacentChunks(ClanChunk clanChunk, boolean includeCorners) {
        return getAdjacentChunks(clanChunk.getX(), clanChunk.getZ(), includeCorners);
    }

    private void addChunk(ClanChunk chunk, Clan clan) {
        chunks.put(new ChunkPos(chunk.getX(), chunk.getZ()), chunk);
        clan.addChunk(chunk);
        clanHandler.saveClans();
    }
    private void removeChunk(ClanChunk chunk, Clan clan) {
        chunks.remove(new ChunkPos(chunk.getX(), chunk.getZ()));
        clan.removeChunk(chunk);
        clanHandler.saveClans();
    }
    private void startEffectInterval() {
        // With BukkitRunnable
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    Clan playerClan = clanHandler.getClanByMember(player.getUniqueId());
                    String clanChunkName = getClanNameByChunk(player.getChunk());
                    Clan chunkClan = clanHandler.getClanByChunkLocation(player.getChunk());
                    List<PotionEffect> effects = new ArrayList<>();
                    if (chunkClan != null) {
                        if (playerClan != null && playerClan.getId().equals(chunkClan.getId())) {
                            // give buff
                            effects = VClans.getInstance().getClanTierHandler().getBuffs(chunkClan.getTier());
                        } else {
                            // give debuff
                            effects = VClans.getInstance().getClanTierHandler().getDebuffs(chunkClan.getTier());
                        }
                    }
                    if (!effects.isEmpty()) {
                        player.addPotionEffects(effects);
                    }
                }
            }
        }.runTaskTimer(VClans.getInstance(), 1, 5 * 20);
    }
    private ClanChunk getChunk(int x, int z) {
        return chunks.get(new ChunkPos(x, z));
    }
    private boolean isSafeFormula(String formula) {
        return SAFE_MATH_PATTERN.matcher(formula).matches();
    }
}
