package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.CommandAction;
import gg.valentinos.alexjoo.Commands.SubCommand;
import gg.valentinos.alexjoo.Data.ClanData.Clan;
import gg.valentinos.alexjoo.Data.LogType;
import gg.valentinos.alexjoo.Utility.Decorator;
import gg.valentinos.alexjoo.VClans;
import net.kyori.adventure.key.Key;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class ClanChatSubcommand extends SubCommand {
    public ClanChatSubcommand() {
        super("clan", "chat", List.of("success", "no-vault"));
        hasToBePlayer = true;
        requiredArgs = 1;
        successVolume = 0;
    }

    @Override
    public CommandAction getAction(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Clan clan = clanHandler.getClanByMember(player.getUniqueId());

        return () -> {
            sendFormattedPredefinedMessage(sender, "success", LogType.FINE);
            if (VClans.getInstance().getVaultHandler().getClanChat(player) == null) {
                VClans.getInstance().getVaultHandler().setPlayerChat(player, clan);
                Decorator.PlaySound(player, Key.key("minecraft:block.ender_chest.open"), 0.1f);
            } else {
                VClans.getInstance().getVaultHandler().setPlayerChat(player, null);
                Decorator.PlaySound(player, Key.key("minecraft:block.ender_chest.close"), 0.1f);
            }
        };
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return List.of("chat");
        } else {
            return List.of();
        }
    }
    @Override
    protected boolean hasSpecificErrors(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();
        Clan clan = clanHandler.getClanByMember(playerUUID);
        if (clan == null) {
            sendFormattedPredefinedMessage(sender, "not-in-clan", LogType.INFO);
            return true;
        }
        if (VClans.getInstance().getVaultHandler().getPermission() == null) {
            sendFormattedPredefinedMessage(sender, "no-vault");
            return true;
        }
        return false;
    }
    @Override
    public boolean suggestCommand(CommandSender sender) {
        if (!(sender instanceof Player player)) return false;
        Clan clan = VClans.getInstance().getClanHandler().getClanByMember(player.getUniqueId());
        return VClans.getInstance().getVaultHandler().getChat() != null && clan != null;
    }
    @Override
    protected void loadReplacementValues(CommandSender sender, String[] args) {
        // clan chat default cause by default the player will be in GLOBAL mode, and since this runs before success message displays
        String chat = "faction";
        if (sender instanceof Player player) {
            Clan clan = VClans.getInstance().getVaultHandler().getClanChat(player);
            if (clan != null) {
                chat = "global";
            }
        }
        replacements.put("{chat}", chat);

    }
}
