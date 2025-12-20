package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.SubCommand;
import gg.valentinos.alexjoo.Data.ClanData.Clan;
import gg.valentinos.alexjoo.Data.ClanData.ClanChunk;
import gg.valentinos.alexjoo.Data.LogType;
import gg.valentinos.alexjoo.Handlers.ClanTierHandler;
import gg.valentinos.alexjoo.VClans;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ClanCommand implements CommandExecutor, TabCompleter {
    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public ClanCommand() {
        ClanHelpSubcommand clanHelpSubcommand = new ClanHelpSubcommand();
        registerSubCommand(clanHelpSubcommand);
        registerSubCommand(new ClanCreateSubcommand());
        registerSubCommand(new ClanDisbandSubcommand());
        registerSubCommand(new ClanListSubcommand());
        registerSubCommand(new ClanInviteSubcommand());
        registerSubCommand(new ClanJoinSubcommand());
        registerSubCommand(new ClanLeaveSubcommand());
        registerSubCommand(new ClanKickSubcommand());
        registerSubCommand(new ClanRankSubcommand());
        registerSubCommand(new ClanColorSubcommand());
        registerSubCommand(new ClanUpgradeSubcommand());
        registerSubCommand(new ClanChatSubcommand());
        registerSubCommand(new ClanPrefixSubcommand());
        registerSubCommand(new ClanRenameSubcommand());
        registerSubCommand(new ClanDowngradeSubcommand());
        registerSubCommand(new ClanGuideSubcommand());

        clanHelpSubcommand.setSubCommands(subCommands);
    }

    private void registerSubCommand(SubCommand subCommand) {
        subCommands.put(subCommand.getName().toLowerCase(), subCommand);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player player) {
                Clan clan = VClans.getInstance().getClanHandler().getClanByMember(player.getUniqueId());
                if (clan != null) {
                    StringBuilder builder = new StringBuilder();
                    ClanTierHandler clanTierHandler = VClans.getInstance().getClanTierHandler();
                    builder.append("&7Your current clan&r: [").append(clan.getPrefix()).append("&r] ").append(clan.getName()).append("&r, level ").append(clanTierHandler.getLabel(clan.getTier())).append("\n");
                    builder.append("&7Players&r: ").append(clan.getMembers().size()).append("/").append(clanTierHandler.getPlayerLimit(clan.getTier())).append(" (").append(clan.getOnlinePlayers().size()).append(" online)\n");
                    builder.append("&7Chunks&r: ").append(clan.getChunks().size()).append("/").append(clanTierHandler.getChunkLimit(clan.getTier()));
                    if (!clan.getChunks().isEmpty()) {
                        ClanChunk chunk = clan.getChunks().iterator().next();
                        String location = " Location: (" + chunk.getX() + ", " + chunk.getZ() + ")";
                        builder.append(location);
                    }
                    builder.append("\n&7Buffs&r: \n");
                    for (PotionEffect effect : clanTierHandler.getBuffs(clan.getTier())) {
                        builder.append(" &7- &a").append(effect.getType().getKey().asString().replace("minecraft:", "")).append(" &r[").append(effect.getAmplifier() + 1).append("]\n");
                    }
                    builder.append("&7Debuffs&r: \n");
                    for (PotionEffect effect : clanTierHandler.getDebuffs(clan.getTier())) {
                        builder.append(" &7- &c").append(effect.getType().getKey().asString().replace("minecraft:", "")).append(" &r[").append(effect.getAmplifier() + 1).append("]\n");
                    }
                    if (clan.getTier() < clanTierHandler.getHighestTierNumber()) {
                        builder.append("&7Next tier upgrade ").append(clanTierHandler.getLabel(clan.getTier() + 1)).append(" costs&r: $").append(clanTierHandler.getPrice(clan.getTier() + 1));
                    } else if (clan.getTier() == clanTierHandler.getHighestTierNumber()) {
                        builder.append("&7Max tier reached&r: ").append(clanTierHandler.getLabel(clan.getTier()));
                    }
                    VClans.sendFormattedMessage(sender, builder.toString(), LogType.FINE);
                }
            }
            VClans.sendFormattedMessage(sender, "Use '/clan help' for help.", LogType.FINE);
            return true;
        }

        SubCommand subCommand = subCommands.get(args[0].toLowerCase());
        if (subCommand == null) {
            VClans.sendFormattedMessage(sender, "Unknown subcommand. Use '/clan help' for help.", LogType.FINE);
            return true;
        }

        subCommand.execute(sender, args);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            for (Map.Entry<String, SubCommand> entry : subCommands.entrySet()) {
                String commandName = entry.getKey();
                SubCommand subCommand = entry.getValue();
                if (subCommand.suggestCommand(sender)) {
                    suggestions.add(commandName);
                }
            }
            return suggestions;
        } else if (args.length > 1) {
            SubCommand subCommand = subCommands.get(args[0].toLowerCase());
            if (subCommand != null) {
                return subCommand.onTabComplete(sender, args);
            }
        }

        return Collections.emptyList();
    }
}
