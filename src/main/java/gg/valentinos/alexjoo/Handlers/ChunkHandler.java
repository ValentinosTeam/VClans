package gg.valentinos.alexjoo.Handlers;

import gg.valentinos.alexjoo.Data.Clan;
import gg.valentinos.alexjoo.Data.ClanChunk;
import gg.valentinos.alexjoo.Data.LogType;
import gg.valentinos.alexjoo.GUIs.ChunkRadar;
import gg.valentinos.alexjoo.VClans;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import static gg.valentinos.alexjoo.VClans.Log;

public class ChunkHandler {

    private final String worldName;
    private final ClanHandler clanHandler;
    private HashMap<ChunkPos, ClanChunk> chunks = new HashMap<>();
    private final int enemyProximityRadius;
    private final int regionProximityRadius;
    private HashMap<Player, ChunkRadar> radars = new HashMap<>();

    // formula evaluation relevant fields for chunk cost
    private String chunkCostFormula;
    private final ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
    private static final Pattern SAFE_MATH_PATTERN = Pattern.compile("^[0-9xX+\\-*/^().\\sMath]*$");
    private static final String DEFAULT_CHUNK_FORMULA = "100*x^2";

    // relevant fields for max chunk amount
    private final int startingChunkAmount;
    private final int chunksPerPlayer;

    private record ChunkPos(int x, int z) {
    }

    public ChunkHandler() {
        this.clanHandler = VClans.getInstance().getClanHandler();
        this.worldName = VClans.getInstance().getConfig().getString("settings.world-name");
        for (Clan clan : clanHandler.getClans()) {
            if (clan.getChunks() == null) {
                clan.setChunks(new HashSet<>());
            }
            for (ClanChunk chunk : clan.getChunks()) {
                chunks.put(new ChunkPos(chunk.getX(), chunk.getZ()), chunk);
            }
        }
        this.startingChunkAmount = VClans.getInstance().getConfig().getInt("settings.starting-chunk-amount");
        this.chunksPerPlayer = VClans.getInstance().getConfig().getInt("settings.chunks-per-player");
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
    }

    public void claimChunk(int x, int z, Player player) {
        String clanName = VClans.getInstance().getClanHandler().getClanByMember(player.getUniqueId()).getName();
        Clan clan = clanHandler.getClanByName(clanName);
        Economy economy = VClans.getInstance().getEconomy();

        if (economy != null) {
            double price = getNewChunkPrice(clan);
            EconomyResponse response = economy.withdrawPlayer(player, price);
        }

        ClanChunk newChunk = new ClanChunk(x, z, worldName, clanName);
        addChunk(newChunk, clan);
        updateChunkRadar(player, x, z);
        BlueMapHandler blueMapHandler = VClans.getInstance().getBlueMapHandler();
        if (blueMapHandler != null) {
            blueMapHandler.drawClanTerritory(clan);
        }
    }
    public void unclaimChunk(int x, int z, Player player) {
        String clanName = VClans.getInstance().getClanHandler().getClanByMember(player.getUniqueId()).getName();
        Clan clan = clanHandler.getClanByName(clanName);
        ClanChunk chunkToRemove = chunks.get(new ChunkPos(x, z));
        removeChunk(chunkToRemove, clan);
        updateChunkRadar(player, x, z);
        BlueMapHandler blueMapHandler = VClans.getInstance().getBlueMapHandler();
        if (blueMapHandler != null) {
            blueMapHandler.drawClanTerritory(clan);
        }
    }
    public void unclaimChunks(String clanName) {
        Clan clan = clanHandler.getClanByName(clanName);
        for (ClanChunk chunk : new HashSet<>(clan.getChunks())) {
            removeChunk(chunk, clan);
        }
    }
    public void toggleChunkRadar(Player player) {
        if (radars.containsKey(player)) {
            Log("Closing radar for player: " + player.getName(), LogType.FINE);
            ChunkRadar radar = radars.get(player);
            if (radar != null) {
                radar.closeRadar();
                radars.remove(player);
            }
        } else {
            Log("Opening radar for player: " + player.getName(), LogType.FINE);
            ChunkRadar chunkRadar = new ChunkRadar(player);
            chunkRadar.initializeRadar();
            radars.put(player, chunkRadar);
        }
    }
    public void updateChunkRadar(Player player, int x, int z) {
        ChunkRadar radar = radars.get(player);
        if (radar != null) {
            radar.updateRadar(x, z);
        }
    }

    public int getClanMaxChunkAmount(Clan clan) {
        int amount = startingChunkAmount;
        amount += clan.getMembers().size() * chunksPerPlayer;
        return amount;
    }
    public double getNewChunkPrice(@NotNull Clan clan) {
        Economy economy = VClans.getInstance().getEconomy();
        if (economy == null) return 0;

        int chunkAmount = clan.getChunks().size();
        double price;
        try {
            Log("chunk-cost-formula is: " + chunkCostFormula);
            String expr = chunkCostFormula.replace("x", String.valueOf(chunkAmount)).replace("^", "**");
            Object result = engine.eval(expr);
            price = Double.parseDouble(result.toString());
        } catch (Exception e) {
            Log("Error evaluating chunk-cost-formula: " + chunkCostFormula, LogType.SEVERE);
            throw new RuntimeException(e);
        }
        if (Double.isNaN(price) || Double.isInfinite(price) || price < 0) {
            Log("Invalid chunk cost formula result: " + price + " for x=" + chunkAmount, LogType.WARNING);
            return 0;
        }
        return price;
    }
    public String getChunkInfo(int x, int z) {
        ClanChunk chunk = getChunk(x, z);
        // TODO: make this configurable.
        if (chunk == null) {
            return "Chunk Unclaimed";
        }
        return "Chunk Info:" + "\n" +
                "X: " + chunk.getX() + "\n" +
                "Z: " + chunk.getZ() + "\n" +
                "World: " + chunk.getWorld() + "\n" +
                "Clan Name: " + chunk.getClanName() + "\n";
    }
    public String getWorldName() {
        return worldName;
    }
    public String getClanNameByChunk(int x, int z) {
        ClanChunk chunk = getChunk(x, z);
        if (chunk == null) {
            return null;
        }
        return chunk.getClanName();
    }
    public boolean isChunkClaimedByClan(int x, int z, String clanName) {
        ClanChunk chunk = getChunk(x, z);
        return chunk != null && chunk.getClanName().equals(clanName);
    }
    public boolean unclaimWillSplit(int x, int z, String clanName) {
        Clan clan = clanHandler.getClanByName(clanName);
        if (clan == null) {
            Log("Clan not found", LogType.SEVERE);
            return false;
        }
        if (chunks.size() == 2) return false;
        for (ClanChunk chunk : getAdjacentChunks(x, z, false)) {
            if (getAdjacentChunks(chunk.getX(), chunk.getZ(), false).size() <= 1) {
                return true;
            }
        }
        return false;
    }
    public boolean isChunkAdjacentToClan(int x, int z, String clanName) {
        for (ClanChunk chunk : getAdjacentChunks(x, z, false)) {
            if (chunk != null && chunk.getClanName().equals(clanName)) {
                return true;
            }
        }
        return false;
    }
    public boolean isChunkInValidWorld(String name) {
        Log("config world: " + this.worldName + ". given world: " + name + ". Equal? " + name.equals(this.worldName), LogType.INFO);
        return name.equals(this.worldName);
    }
    public boolean isChunkCloseToEnemyClan(int x, int z, String clanName) {
        for (int dx = -enemyProximityRadius; dx <= enemyProximityRadius; dx++) {
            for (int dz = -enemyProximityRadius; dz <= enemyProximityRadius; dz++) {
                if (dx == 0 && dz == 0) continue;
                ClanChunk chunk = getChunk(dx + x, dz + z);
                if (chunk != null && !chunk.getClanName().equals(clanName)) return true;
            }
        }
        return false;
    }
    public boolean isChunkCloseToRegion(int x, int z) {
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
        Economy economy = VClans.getInstance().getEconomy();
        if (economy == null) return true;
        Clan clan = clanHandler.getClanByMember(player.getUniqueId());
        if (clan == null) return false;

        double price = getNewChunkPrice(clan);

        return economy.getBalance(player) >= price;
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
    private ClanChunk getChunk(int x, int z) {
        return chunks.get(new ChunkPos(x, z));
    }
    private boolean isSafeFormula(String formula) {
        return SAFE_MATH_PATTERN.matcher(formula).matches();
    }
}
