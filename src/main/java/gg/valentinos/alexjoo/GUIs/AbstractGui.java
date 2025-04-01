package gg.valentinos.alexjoo.GUIs;

import gg.valentinos.alexjoo.VClans;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;

public abstract class AbstractGui implements Listener {
    protected String title;
    protected Inventory inventory;
    protected Player player;

    public AbstractGui(String title, int rows) {
        this.title = title;
        this.inventory = Bukkit.createInventory(null, getInventorySize(rows), Component.text(title));
        Bukkit.getPluginManager().registerEvents(this, VClans.getInstance());
    }

    public void openInventory(Player player) {
        this.player = player;
        initializeItems();
        player.openInventory(inventory);
    }
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(inventory)) {
            HandlerList.unregisterAll(this);
        }
    }

    protected void setItem(int row, int column, ItemStack item) {
        if (row < 0 || row >= inventory.getSize() / 9 || column < 0 || column >= 9) {
            throw new IllegalArgumentException("Row or column out of bounds");
        }
        inventory.setItem(row * 9 + column, item);
    }
    protected ItemStack createItemStack(String customTag, Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(name));
            meta.lore(Arrays.stream(lore).map(Component::text).toList());
            meta.getPersistentDataContainer().set(new NamespacedKey(VClans.getInstance(), "customTag"), PersistentDataType.STRING, customTag);
            item.setItemMeta(meta);
        }
        return item;
    }
    protected abstract void initializeItems();

    private int getInventorySize(int rows) {
        if (rows < 1 || rows > 6) {
            throw new IllegalArgumentException("Rows must be between 1 and 6");
        }
        return rows * 9;
    }
}
