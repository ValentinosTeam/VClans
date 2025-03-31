package gg.valentinos.alexjoo.GUIs;

import gg.valentinos.alexjoo.VClans;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public abstract class AbstractGui implements Listener {
    protected String title;
    protected Inventory inventory;

    public AbstractGui(String title, int rows) {
        this.title = title;
        this.inventory = Bukkit.createInventory(null, getInventorySize(rows), Component.text(title));
        Bukkit.getPluginManager().registerEvents(this, VClans.getInstance());
        initializeItems();
    }

    public void openInventory(Player player) {
        player.openInventory(inventory);
    }

    protected void setItem(int row, int column, ItemStack item) {
        if (row < 0 || row >= inventory.getSize() / 9 || column < 0 || column >= 9) {
            throw new IllegalArgumentException("Row or column out of bounds");
        }
        inventory.setItem(row * 9 + column, item);
    }
    protected abstract void initializeItems();

    private int getInventorySize(int rows) {
        if (rows < 1 || rows > 6) {
            throw new IllegalArgumentException("Rows must be between 1 and 6");
        }
        return rows * 9;
    }
}
