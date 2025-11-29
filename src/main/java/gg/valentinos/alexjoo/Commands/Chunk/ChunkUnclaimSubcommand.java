package gg.valentinos.alexjoo.Commands.Chunk;

import gg.valentinos.alexjoo.Commands.CommandAction;
import gg.valentinos.alexjoo.Commands.SubCommand;
import gg.valentinos.alexjoo.Data.ClanData.Clan;
import gg.valentinos.alexjoo.Data.ClanData.ClanRankPermission;
import gg.valentinos.alexjoo.Data.LogType;
import gg.valentinos.alexjoo.Handlers.ChunkHandler;
import gg.valentinos.alexjoo.VClans;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ChunkUnclaimSubcommand extends SubCommand {
    private final ChunkHandler chunkHandler;

    public ChunkUnclaimSubcommand() {
        super("chunk", "unclaim", List.of("success", "not-claimed", "no-permission", "territory-split"));
        hasToBePlayer = true;
        requiredArgs = 1;
        this.chunkHandler = VClans.getInstance().getChunkHandler();
    }

    @Override
    public CommandAction getAction(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        return () -> {
            sendFormattedPredefinedMessage(sender, "success", LogType.FINE);
            chunkHandler.unclaimChunk(player.getChunk(), player);
        };
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return List.of("unclaim");
        } else {
            return List.of();
        }
    }

    @Override
    protected boolean hasSpecificErrors(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Clan clan = clanHandler.getClanByMember(player.getUniqueId());
        if (clan == null) {
            sendFormattedPredefinedMessage(sender, "not-in-clan", LogType.WARNING);
            return true;
        }
        String clanName = clan.getId();
        if (!clanHandler.hasPermission(player, ClanRankPermission.CAN_UNCLAIM_CHUNKS)) {
            sendFormattedPredefinedMessage(sender, "no-permission", LogType.WARNING);
            return true;
        }
        if (!chunkHandler.isChunkClaimedByClan(player.getChunk(), clanName)) {
            sendFormattedPredefinedMessage(sender, "not-claimed", LogType.WARNING);
            return true;
        }
        if (chunkHandler.unclaimWillSplit(player.getChunk(), clanName)) {
            sendFormattedPredefinedMessage(sender, "territory-split", LogType.WARNING);
            return true;
        }
        if (warHandler.isInWar(clan)) {
            sendFormattedPredefinedMessage(sender, "is-in-war", LogType.WARNING);
            return true;
        }
        return false;
    }

    @Override
    public boolean suggestCommand(CommandSender sender) {
        if (sender instanceof Player player) {
            return clanHandler.hasPermission(player, ClanRankPermission.CAN_UNCLAIM_CHUNKS);
        }
        return false;
    }

    @Override
    protected void loadReplacementValues(CommandSender sender, String[] args) {
        String clanName = "ERROR";

        if (sender instanceof Player player) {
            int x = player.getChunk().getX();
            int z = player.getChunk().getZ();
            Clan clan = clanHandler.getClanByChunkLocation(x, z);
            if (clan != null) {
                clanName = clan.getName();
            }
        }

        replacements.put("{clan-name}", clanName);

    }
}
