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
        player.sendMessage(VClans.getInstance().getDefaultMessage("confirmation").replace("{time}", String.valueOf(duration)));
        confirmationEntries.put(player.getUniqueId(), new ConfirmationEntry(commandAction, duration));
    }

    public void executeConfirmation(Player player) {
        ConfirmationEntry entry = confirmationEntries.get(player.getUniqueId());
        if (entry == null){
            player.sendMessage(VClans.getInstance().getDefaultMessage("nothing-to-confirm"));
            return;
        }
        entry.execute();
        confirmationEntries.remove(player.getUniqueId());
    }
}
