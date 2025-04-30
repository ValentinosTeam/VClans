package gg.valentinos.alexjoo.GUIs;

import gg.valentinos.alexjoo.Handlers.ChunkHandler;
import gg.valentinos.alexjoo.Handlers.ClanHandler;
import gg.valentinos.alexjoo.VClans;
import org.bukkit.entity.Player;

public class ChunkRadar {
    private final ChunkHandler chunkHandler;
    private final ClanHandler clanHandler;
    private final Player player;

    public ChunkRadar(Player player){
        this.chunkHandler = VClans.getInstance().getChunkHandler();
        this.clanHandler = VClans.getInstance().getClanHandler();
        this.player = player;
    }
}
