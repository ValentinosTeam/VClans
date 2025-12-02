package gg.valentinos.alexjoo.GUIs;

import gg.valentinos.alexjoo.Data.ClanData.Clan;
import gg.valentinos.alexjoo.Data.ClanData.ClanChunk;
import gg.valentinos.alexjoo.Data.WarData.War;
import gg.valentinos.alexjoo.Handlers.ChunkHandler;
import gg.valentinos.alexjoo.Handlers.ClanHandler;
import gg.valentinos.alexjoo.Handlers.WarHandler;
import gg.valentinos.alexjoo.Handlers.WorldGuardHandler;
import gg.valentinos.alexjoo.VClans;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

public class ChunkRadar {
    private final ChunkHandler chunkHandler;
    private final ClanHandler clanHandler;
    private final WorldGuardHandler worldGuardHandler;
    private final WarHandler warHandler;
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
        this.warHandler = VClans.getInstance().getWarHandler();
        this.player = player;
        this.manager = Bukkit.getScoreboardManager();
    }

    public void initializeRadar() {
        if (manager == null) return;

        this.scoreboard = manager.getNewScoreboard();

        objective = scoreboard.registerNewObjective("Chunk Radar", Criteria.DUMMY, Component.text(" North").decorate(TextDecoration.BOLD).color(TextColor.color(255, 85, 85)));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        updateRadar(player.getChunk());
    }
    public void updateRadar(Chunk chunk) {
        int posX = chunk.getX();
        int posZ = chunk.getZ();
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
                String clanName = chunkHandler.getClanIdByChunk(x, z);
                if (clanName != null) {
                    Clan clan = clanHandler.getClanById(clanName);
                    if (warHandler.isInWar(clan)) {
                        War war = warHandler.getWar(clan);
                        ClanChunk clanChunk = clan.getChunkByLocation(x, z);
                        if (clanChunk != null && clanChunk.getIsLost()) {
                            Clan otherClan;
                            String otherClanId = warHandler.getWar(clan).getInitiatorClanId();
                            if (otherClanId.equals(clan.getId())) otherClan = clanHandler.getClanById(war.getTargetClanId());
                            else otherClan = clanHandler.getClanById(war.getInitiatorClanId());
                            color = TextColor.color(otherClan.getColor().get(0), otherClan.getColor().get(1), otherClan.getColor().get(2));
                        } else {
                            color = TextColor.color(clan.getColor().get(0), clan.getColor().get(1), clan.getColor().get(2));
                        }
                    } else {
                        color = TextColor.color(clan.getColor().get(0), clan.getColor().get(1), clan.getColor().get(2));
                    }
                }
                if (worldGuardHandler != null && worldGuardHandler.isChunkOverlappingWithRegion(x, z)) {
                    color = TextColor.color(worldGuardHandler.getColor().getRed(), worldGuardHandler.getColor().getGreen(), worldGuardHandler.getColor().getBlue());
                }

                if (x == posX && z == posZ) {
                    if (color == null) {
                        row = row.append(Component.text(emptyCenterSymbol).color(NO_CLAN_CHUNK_COLOR));
                    } else {
                        row = row.append(Component.text(occupiedCenterSymbol).color(color));
                    }
                } else {
                    if (color == null) {
                        row = row.append(Component.text(emptyRadarSymbol).color(NO_CLAN_CHUNK_COLOR));
                    } else {
                        row = row.append(Component.text(occupiedRadarSymbol).color(color));
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
    public void updateRadar() {
        Chunk chunk = player.getChunk();
        updateRadar(chunk);
    }

    public void closeRadar() {
        player.setScoreboard(manager.getNewScoreboard());
    }
}
