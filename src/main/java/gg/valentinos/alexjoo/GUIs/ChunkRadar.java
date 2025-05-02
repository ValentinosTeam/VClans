package gg.valentinos.alexjoo.GUIs;

import gg.valentinos.alexjoo.Handlers.ChunkHandler;
import gg.valentinos.alexjoo.Handlers.ClanHandler;
import gg.valentinos.alexjoo.VClans;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import static gg.valentinos.alexjoo.VClans.Log;

public class ChunkRadar {
    private final ChunkHandler chunkHandler;
    private final ClanHandler clanHandler;
    private final Player player;
    private final ScoreboardManager manager;
    private Scoreboard scoreboard;
    private Objective objective;


    public ChunkRadar(Player player){
        this.chunkHandler = VClans.getInstance().getChunkHandler();
        this.clanHandler = VClans.getInstance().getClanHandler();
        this.player = player;
        this.manager = Bukkit.getScoreboardManager();
    }

    public void initializeRadar(){
        if (manager == null)
            return;
        this.scoreboard = manager.getNewScoreboard();

        Log("Initializing radar for player: " + player.getName());

        objective = scoreboard.registerNewObjective("Chunk Radar", Criteria.DUMMY, Component.text("Chunk Radar").decorate(TextDecoration.UNDERLINED).color(TextColor.color(255,85,85)));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        updateRadar(player.getChunk().getX(), player.getChunk().getZ());
    }
    public void updateRadar(int posX, int posZ){
        final String radarSymbol = "■ ";
        final int radarSize = 5; // HAS TO BE ODD!!!
        final int mid = (radarSize-1)/2;
        Log("Updating radar for player: " + player.getName());
        for (int i = 0; i < radarSize; i++){
            Component row = Component.text(" ".repeat(i));
            int dz = mid - i;
            for (int j = 0; j < radarSize; j++){
                int dx = j - mid;
                int x = posX + dx;
                int z = posZ + dz;
                String clanName = chunkHandler.getClanNameByChunk(x, z);
                String playerClanName = clanHandler.getClanNameOfMember(player.getUniqueId());
                if (x == posX && z == posZ){
                    if (clanName == null){
                        row = row.append(Component.text(radarSymbol).color(TextColor.color(155, 155, 155))); // gray
                    }
                    else if (clanName.equals(playerClanName)){
                        row = row.append(Component.text(radarSymbol).color(TextColor.color(0, 155, 0))); // dark green
                    }
                    else{
                        row = row.append(Component.text(radarSymbol).color(TextColor.color(155, 0, 0))); // dark red
                    }
                } else {
                    if (clanName == null){
                        row = row.append(Component.text(radarSymbol).color(TextColor.color(255, 255, 255))); // white
                    }
                    else if (clanName.equals(playerClanName)){
                        row = row.append(Component.text(radarSymbol).color(TextColor.color(0, 255, 0))); // green
                    }
                    else{
                        row = row.append(Component.text(radarSymbol).color(TextColor.color(255, 0, 0))); // red
                    }
                }
            }
            Team team = scoreboard.getTeam(String.valueOf(i));
            if (team == null){
                team = scoreboard.registerNewTeam(String.valueOf(i));
            }
            team.suffix(row);
            team.addEntry(" ".repeat(radarSize-i));
            objective.getScore(" ".repeat(radarSize-i)).setScore(i);
        }
        player.setScoreboard(scoreboard);
    }
    public void closeRadar(){
        player.setScoreboard(manager.getNewScoreboard());
    }
}
