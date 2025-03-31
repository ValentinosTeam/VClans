package gg.valentinos.alexjoo.Handlers;

import gg.valentinos.alexjoo.Commands.CommandAction;
import gg.valentinos.alexjoo.Data.ConfirmationEntry;
import gg.valentinos.alexjoo.VClans;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class ConfirmationHandler {

    HashMap<UUID, ConfirmationEntry> confirmationEntries;

    public ConfirmationHandler() {
        confirmationEntries = new HashMap<>();
    }

    public void addConfirmationEntry(Player player, long duration, CommandAction commandAction) {
        VClans.getInstance().getLogger().info("Adding confirmation entry for " + player.getName() + " with duration " + duration);
        confirmationEntries.put(player.getUniqueId(), new ConfirmationEntry(commandAction, duration));
    }

    public void executeConfirmation(Player player) {
        ConfirmationEntry entry = confirmationEntries.get(player.getUniqueId());
        if (entry == null){
            player.sendMessage(VClans.getInstance().getDefaultMessage("nothing-to-confirm"));
            VClans.getInstance().getLogger().info("Player " + player.getName() + " has no confirmation entry.");
            return;
        }
        if (entry.isExpired()){
            player.sendMessage(VClans.getInstance().getDefaultMessage("confirmation-expired"));
            VClans.getInstance().getLogger().info("Player " + player.getName() + " has an expired confirmation entry.");
            confirmationEntries.remove(player.getUniqueId());
            return;
        }
        VClans.getInstance().getLogger().info("Executing confirmation entry for " + player.getName());
        entry.execute();
        confirmationEntries.remove(player.getUniqueId());
    }
}
