package gg.valentinos.alexjoo.GUIs;

import gg.valentinos.alexjoo.Data.Clan;
import gg.valentinos.alexjoo.Handlers.ChunkHandler;
import gg.valentinos.alexjoo.Handlers.ClanHandler;
import gg.valentinos.alexjoo.Handlers.WorldGuardHandler;
import gg.valentinos.alexjoo.VClans;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

public class ChunkRadar {
    private final ChunkHandler chunkHandler;
    private final ClanHandler clanHandler;
    private final WorldGuardHandler worldGuardHandler;
    private final Player player;
    private final ScoreboardManager manager;
    private Scoreboard scoreboard;
    private Objective objective;
    private static final TextColor NO_CLAN_CHUNK_COLOR = TextColor.color(200, 200, 200);
    final static String occupiedRadarSymbol = "■ ";
    final static String emptyRadarSymbol = "□ ";
    final static String occupiedCenterSymbol = "x ";
    final static String emptyCenterSymbol = "o ";


    public ChunkRadar(Player player) {
        this.chunkHandler = VClans.getInstance().getChunkHandler();
        this.clanHandler = VClans.getInstance().getClanHandler();
        this.worldGuardHandler = VClans.getInstance().getWorldGuardHandler();
        this.player = player;
        this.manager = Bukkit.getScoreboardManager();
    }

    public void initializeRadar() {
        if (manager == null) return;

        this.scoreboard = manager.getNewScoreboard();

        objective = scoreboard.registerNewObjective("Chunk Radar", Criteria.DUMMY, Component.text("Chunk Radar").decorate(TextDecoration.UNDERLINED).color(TextColor.color(255, 85, 85)));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        updateRadar(player.getChunk().getX(), player.getChunk().getZ());
    }
    public void updateRadar(int posX, int posZ) {
        final int radarSize = 5; // HAS TO BE ODD!!!
        final int mid = (radarSize - 1) / 2;
        for (int i = 0; i < radarSize; i++) {
            Component row = Component.text(" ".repeat(i));
            int dz = mid - i;
            for (int j = 0; j < radarSize; j++) {
                int dx = j - mid;
                int x = posX + dx;
                int z = posZ + dz;
                TextColor color = null;
                String clanName = chunkHandler.getClanNameByChunk(x, z);
                if (clanName != null) {
                    Clan clan = clanHandler.getClanByName(clanName);
                    color = TextColor.color(clan.getColor().get(0), clan.getColor().get(1), clan.getColor().get(2));
                }
                if (worldGuardHandler.isChunkOverlappingWithRegion(x, z)) {
                    color = TextColor.color(worldGuardHandler.getColor().getRed(), worldGuardHandler.getColor().getGreen(), worldGuardHandler.getColor().getBlue());
                }

                if (x == posX && z == posZ) {
                    if (color == null) {
                        row = row.append(Component.text(emptyCenterSymbol).color(NO_CLAN_CHUNK_COLOR)); // gray
                    } else {
                        row = row.append(Component.text(occupiedCenterSymbol).color(color)); // dark green
                    }
                } else {
                    if (color == null) {
                        row = row.append(Component.text(emptyRadarSymbol).color(NO_CLAN_CHUNK_COLOR)); // white
                    } else {
                        row = row.append(Component.text(occupiedRadarSymbol).color(color)); // dark green
                    }
                }
            }
            Team team = scoreboard.getTeam(String.valueOf(i));
            if (team == null) {
                team = scoreboard.registerNewTeam(String.valueOf(i));
            }
            team.suffix(row);
            team.addEntry(" ".repeat(radarSize - i));
            objective.getScore(" ".repeat(radarSize - i)).setScore(i);
        }
        player.setScoreboard(scoreboard);
    }
    public void closeRadar() {
        player.setScoreboard(manager.getNewScoreboard());
    }
}
