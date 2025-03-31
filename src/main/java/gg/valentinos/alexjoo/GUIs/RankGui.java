package gg.valentinos.alexjoo.GUIs;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class RankGui extends AbstractGui {

    public RankGui() {
        super("Rank Manager", 3);
    }

    @Override
    protected void initializeItems() {
        initializeRankManager();
    }

    private void initializeRankManager(){
        // top menu bar
        setItem(0, 0, createItemStack(Material.CRAFTING_TABLE, "Create Rank", "Click to create a new rank"));
        setItem(0,1, createItemStack(Material.TNT, "Delete Rank", "Click this and select a rank to delete it."));
        setItem(0, 2, createItemStack(Material.WRITABLE_BOOK, "Edit Rank", "Click this and select a rank to edit it."));
        ItemStack playerHead = createItemStack(Material.PLAYER_HEAD, "Your Rank", "Your rank is: ERROR", "Click this to send information about your rank in the chat.");
        SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
        if (skullMeta != null) {
            skullMeta.setOwningPlayer(player);
            playerHead.setItemMeta(skullMeta);
        }
        setItem(0, 7, playerHead);
        setItem(0,8, createItemStack(Material.BARRIER, "Close", "Click this to close the GUI."));
        // rank list
        // ...
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getView().title().equals(Component.text(title)) && e.getWhoClicked() == player) {
            e.setCancelled(true);
            Player player = (Player) e.getWhoClicked();
            ItemStack item = e.getCurrentItem();
            if (item != null) {
                player.sendMessage("Player " + player.getName() + " clicked on " + item.getType());
            }
        }
    }
}
