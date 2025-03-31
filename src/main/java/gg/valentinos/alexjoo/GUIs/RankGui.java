package gg.valentinos.alexjoo.GUIs;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class RankGui extends AbstractGui {

    public RankGui() {
        super("Rank Manager", 3);
    }

    @Override
    protected void initializeItems() {
        setItem(0, 0, new ItemStack(Material.DIAMOND_HOE));
        setItem(1, 5, new ItemStack(Material.IRON_HOE));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getView().title().equals(Component.text(title))) {
            e.setCancelled(true);
            // Handle item clicks here
            // Example: e.getCurrentItem().getType() to get the clicked item
            Player player = (Player) e.getWhoClicked();
            player.sendMessage("Player " + player.getName() + " clicked on " + Objects.requireNonNull(e.getCurrentItem()).getType());
            ItemStack item = e.getCurrentItem();
            if (item != null) {
                if (item.getType() == Material.DIAMOND_HOE) {
                    player.sendMessage("You clicked on the Diamond Hoe!");
                } else if (e.getSlot() == 9+5) {
                    player.sendMessage("You clicked on the Iron Hoe!");
                }
            }
        }
    }
}
