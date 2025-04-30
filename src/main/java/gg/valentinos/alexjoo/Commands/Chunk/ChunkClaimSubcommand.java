package gg.valentinos.alexjoo.Commands.Chunk;

import gg.valentinos.alexjoo.Commands.CommandAction;
import gg.valentinos.alexjoo.Commands.SubCommand;
import gg.valentinos.alexjoo.Data.Clan;
import gg.valentinos.alexjoo.Data.LogType;
import gg.valentinos.alexjoo.Handlers.ChunkHandler;
import gg.valentinos.alexjoo.VClans;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;

public class ChunkClaimSubcommand extends SubCommand {
    private final ChunkHandler chunkHandler;

    public ChunkClaimSubcommand() {
        super("chunk", "claim", List.of("success", "already-claimed", "no-permission", "max-chunks", "not-adjacent", "too-close"));
        hasToBePlayer = true;
        requiredArgs = 1;
        this.chunkHandler = VClans.getInstance().getChunkHandler();
    }

    @Override
    public CommandAction getAction(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        int x = player.getChunk().getX();
        int z = player.getChunk().getZ();
        Clan clan = clanHandler.getClanByMember(player.getUniqueId());
        String clanName = clan.getName();

        return () -> {
            sendFormattedPredefinedMessage(sender, "success", LogType.FINE);
            chunkHandler.claimChunk(x, z, clanName);
        };
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return List.of("claim");
        }
        else{
            return List.of();
        }
    }

    @Override
    protected boolean hasSpecificErrors(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        int x = player.getChunk().getX();
        int z = player.getChunk().getZ();
        Clan clan = clanHandler.getClanByMember(player.getUniqueId());
        if (clan == null) {
            sendFormattedPredefinedMessage(sender, "not-in-clan", LogType.WARNING);
            return true;
        }
        String clanName = clan.getName();
        HashMap<String, Boolean> permissions = clan.getRank(player.getUniqueId()).getPermissions();
        if (!permissions.get("canClaimChunks")) {
            sendFormattedPredefinedMessage(sender, "no-permission", LogType.WARNING);
            return true;
        }
        Clan chunkClan = clanHandler.getClanByChunkLocation(x, z);
        if (chunkClan != null) {
            sendFormattedPredefinedMessage(sender, "already-claimed", LogType.WARNING);
            return true;
        }
        if (chunkHandler.isChunkAdjacentToEnemyClan(x, z, clanName)) {
            sendFormattedPredefinedMessage(sender, "too-close", LogType.WARNING);
            return true;
        }
        if (!clan.getChunks().isEmpty() && !chunkHandler.isChunkAdjacentToClan(x, z, clanName)) {
            sendFormattedPredefinedMessage(sender, "not-adjacent", LogType.WARNING);
            return true;
        }
        if (clan.getChunks().size() + 1 > chunkHandler.getMaxChunkAmount()) {
            sendFormattedPredefinedMessage(sender, "max-chunks", LogType.WARNING);
            return true;
        }
        return false;
    }

    @Override
    public boolean suggestCommand(CommandSender sender) {
        if (sender instanceof Player player) {
            Clan clan = clanHandler.getClanByMember(player.getUniqueId());
            return clan != null && clan.getRank(player.getUniqueId()).getPermissions().get("canClaimChunks");
        }
        return false;
    }

    @Override
    protected void loadReplacementValues(CommandSender sender, String[] args) {
        String clanName = "None";
        String maxAmount = String.valueOf(chunkHandler.getMaxChunkAmount());

        if (sender instanceof Player player) {
            int x = player.getChunk().getX();
            int z = player.getChunk().getZ();
            Clan clan = clanHandler.getClanByChunkLocation(x, z);
            if (clan != null) {
                clanName = clan.getName();
            }
        }

        replacements.put("{max-chunks}", maxAmount);
        replacements.put("{clan-name}", clanName);
    }
}
