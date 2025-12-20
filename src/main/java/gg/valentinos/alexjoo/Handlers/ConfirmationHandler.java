package gg.valentinos.alexjoo.Handlers;

import gg.valentinos.alexjoo.Commands.CommandAction;
import gg.valentinos.alexjoo.Data.ConfirmationEntry;
import gg.valentinos.alexjoo.Data.LogType;
import gg.valentinos.alexjoo.VClans;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

import static gg.valentinos.alexjoo.VClans.SendMessage;
import static gg.valentinos.alexjoo.VClans.sendFormattedMessage;

public class ConfirmationHandler {

    HashMap<UUID, ConfirmationEntry> confirmationEntries;

    public ConfirmationHandler() {
        confirmationEntries = new HashMap<>();
    }

    public void addConfirmationEntry(Player player, long duration, CommandAction commandAction) {
        confirmationEntries.put(player.getUniqueId(), new ConfirmationEntry(commandAction, duration));
    }

    public void executeConfirmation(Player player) {
        ConfirmationEntry entry = confirmationEntries.get(player.getUniqueId());
        if (entry == null) {
            sendFormattedMessage(player, VClans.getInstance().getDefaultMessage("nothing-to-confirm"), LogType.INFO);
//            player.sendMessage(VClans.getInstance().getDefaultMessage("nothing-to-confirm"));
            return;
        }
        if (entry.isExpired()) {
            sendFormattedMessage(player, VClans.getInstance().getDefaultMessage("confirmation-expired"), LogType.INFO);
//            player.sendMessage(VClans.getInstance().getDefaultMessage("confirmation-expired"));
            confirmationEntries.remove(player.getUniqueId());
            return;
        }
        entry.execute();
        confirmationEntries.remove(player.getUniqueId());
    }

    public void cancelConfirmation(Player player) {
        ConfirmationEntry entry = confirmationEntries.get(player.getUniqueId());
        if (entry == null) {
            sendFormattedMessage(player, VClans.getInstance().getDefaultMessage("nothing-to-cancel"), LogType.INFO);
//            player.sendMessage(VClans.getInstance().getDefaultMessage("nothing-to-cancel"));
            return;
        }
        if (entry.isExpired()) {
            sendFormattedMessage(player, VClans.getInstance().getDefaultMessage("confirmation-expired"), LogType.INFO);
//            player.sendMessage(VClans.getInstance().getDefaultMessage("confirmation-expired"));
            confirmationEntries.remove(player.getUniqueId());
            return;
        }
        confirmationEntries.remove(player.getUniqueId());
        SendMessage(player, Component.text("Command canceled"), LogType.FINE);
    }
}
