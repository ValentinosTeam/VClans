package gg.valentinos.alexjoo.Commands.Chunk;

import gg.valentinos.alexjoo.Commands.CommandAction;
import gg.valentinos.alexjoo.Commands.SubCommand;
import gg.valentinos.alexjoo.Handlers.ChunkHandler;
import gg.valentinos.alexjoo.VClans;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ChunkRadarSubcommand extends SubCommand {
    private final ChunkHandler chunkHandler;

    public ChunkRadarSubcommand() {
        super("chunk", "radar", List.of());
        hasToBePlayer = true;
        requiredArgs = 1;
        this.chunkHandler = VClans.getInstance().getChunkHandler();
    }

    @Override
    public CommandAction getAction(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        return () -> chunkHandler.toggleChunkRadar(player);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return List.of("radar");
        }
        else{
            return List.of();
        }
    }

    @Override
    protected boolean hasSpecificErrors(CommandSender sender, String[] args) {
        return false;
    }

    @Override
    public boolean suggestCommand(CommandSender sender) {
        return true;
    }

    @Override
    protected void loadReplacementValues(CommandSender sender, String[] args) {

    }
}
