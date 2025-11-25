package gg.valentinos.alexjoo.Commands.War;

import gg.valentinos.alexjoo.Commands.CommandAction;
import gg.valentinos.alexjoo.Commands.SubCommand;
import gg.valentinos.alexjoo.Data.ClanData.Clan;
import gg.valentinos.alexjoo.Data.LogType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;

public class WarPeaceSubcommand extends SubCommand {

    public WarPeaceSubcommand() {
        super("war", "peace", List.of("success", "not-in-war"));
        hasToBePlayer = true;
        requiredArgs = 3;
    }

    @Override
    public CommandAction getAction(CommandSender sender, String[] args) {
        //TODO:
        // get player offering peace
        // get war and clans involved
        // if not arg or arg == 0
        //      offer peace to the other clan for free
        // if arg int and != 0
        //      if positive - create peace treaty that other clan has to pay for
        //      else - create peace treaty that player clan has to pay for
        // if arg string
        //      if arg is accept - end war
        //      if arg is deny - delete peace treaty

        return null;
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return List.of("peace");
        }
        //TODO:
        // show accept or deny if a peace treaty is made by the other clan
        // show an example amount of 1000 if creating a new one
        return List.of();
    }
    @Override
    protected boolean hasSpecificErrors(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Clan clan = clanHandler.getClanByMember(player.getUniqueId());

        if (clan == null) {
            sendFormattedPredefinedMessage(sender, "not-in-clan", LogType.WARNING);
            return true;
        }
        HashMap<String, Boolean> permissions = clan.getRank(player.getUniqueId()).getPermissions();
        if (!permissions.get("canOfferPeace")) {
            sendFormattedPredefinedMessage(sender, "no-permission", LogType.WARNING);
            return true;
        }
        if (warHandler.getWarEnemyClan(clan) == null) {
            sendFormattedPredefinedMessage(sender, "not-in-war", LogType.WARNING);
            return true;
        }
        //TODO: check for
        // if peace accept/deny, check if peace offering exists first, if not cant accept/deny
        // if war state has ended, if it did, there is no war to offer peace
        // if peace price offered is non-zero, check if vault is installed on the server, if not, then can't use vault for money management
        // if player offering peace and is giving money, check if they have the amount promised
        return false;
    }
    @Override
    public boolean suggestCommand(CommandSender sender) {
        return false;
    }
    @Override
    protected void loadReplacementValues(CommandSender sender, String[] args) {

    }
}
