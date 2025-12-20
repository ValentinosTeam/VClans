package gg.valentinos.alexjoo.Listeners;

import gg.valentinos.alexjoo.Data.ClanData.Clan;
import gg.valentinos.alexjoo.Data.LogType;
import gg.valentinos.alexjoo.Utility.Decorator;
import gg.valentinos.alexjoo.VClans;
import net.kyori.adventure.inventory.Book;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.List;

import static gg.valentinos.alexjoo.VClans.sendFormattedMessage;

public class PlayerListener implements Listener {

    private final boolean showCurrentClanOnJoin;

    public PlayerListener() {
        showCurrentClanOnJoin = VClans.getInstance().getConfig().getBoolean("show-current-clan-on-join");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!player.hasPlayedBefore()) {
            giveGuideBook(player);
        }

        if (showCurrentClanOnJoin) {
            Clan clan = VClans.getInstance().getClanHandler().getClanByMember(player.getUniqueId());
            player.performCommand("/clan");

            sendFormattedMessage(player, "Current clan: " + (clan == null ? "None" : clan.getName()), LogType.INFO);
        }

        Clan clan = VClans.getInstance().getClanHandler().getClanByMember(player.getUniqueId());
        if (clan != null) {
            VClans.getInstance().getVaultHandler().setPlayerPrefix(player, clan.getPrefix());
        } else {
            VClans.getInstance().getVaultHandler().removePlayerPrefix(player);
        }

        List<String> invitedClanNames = VClans.getInstance().getClanHandler().getInvitingClanNames(player.getUniqueId());
        StringBuilder sb = new StringBuilder();
        for (String invitedClanName : invitedClanNames) {
            sb.append(invitedClanName).append(", ");
        }
        if (!invitedClanNames.isEmpty())
            sendFormattedMessage(player, "You have been invited to: \n" + sb, LogType.INFO);
//            player.sendMessage("You have been invited to: \n" + sb);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        if (!(damager instanceof Firework firework)) return;
        if (firework.hasMetadata(Decorator.HARMLESS_METADATA_KEY)) {
            event.setCancelled(true);
        }
    }

    private void giveGuideBook(Player player) {
        ItemStack item = ItemStack.of(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) item.getItemMeta();
        Book book = VClans.getInstance().getGuideBookHandler().getBook("navigation");

        meta.title(book.title());
        meta.author(book.author());
        meta.pages(book.pages());

        item.setItemMeta(meta);

        player.getInventory().addItem(item);
    }

//    @EventHandler
//    public void onPlayerJump(PlayerJumpEvent event) {
//        Decorator.SummonFirework(event.getPlayer(), Color.GREEN, 1, true, true, FireworkEffect.Type.CREEPER);
//        Decorator.SummonLightning(event.getPlayer());
//        Decorator.Broadcast(event.getPlayer(), Component.text("War Ended").color(TextColor.color(255, 10, 10)), Component.text("thats it you won").color(TextColor.color(10, 255, 10)), 5);
//        Decorator.PlaySound(event.getPlayer(), Key.key("minecraft:entity.experience_orb.pickup"), 0.2f);
//    }

}
