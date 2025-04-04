package gg.valentinos.alexjoo.GUIs;

import gg.valentinos.alexjoo.Data.LogType;
import gg.valentinos.alexjoo.VClans;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.List;

import static gg.valentinos.alexjoo.VClans.Log;

public abstract class AbstractGui implements Listener {
    protected Component title;
    protected Inventory inventory;
    protected Player player;
    protected boolean keepAlive = false;

    public AbstractGui(String title, int rows) {
        this.title = Component.text(title);
        this.inventory = Bukkit.createInventory(null, getInventorySize(rows), this.title);
        Bukkit.getPluginManager().registerEvents(this, VClans.getInstance());
    }

    public void openInventory(Player player) {
        this.player = player;
        initializeItems();
        player.openInventory(inventory);
    }
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        //TODO: fix when the player closes the inventory by pressing esc
        if (keepAlive && event.getPlayer() == player && event.getInventory().equals(inventory)) {
            Log("Inventory closed, but keep alive is true " + event.getInventory().getType(), LogType.INFO);
            return;
        }
        if (event.getInventory().equals(inventory)) {
            HandlerList.unregisterAll(this);
        }
        // Prevent held item from being dropped in the inventory if he has a custom tag.
        ItemStack cursorItem = player.getItemOnCursor();
        ItemMeta cursorItemMeta = cursorItem.getItemMeta();
        if (cursorItemMeta == null) {
            return;
        }
        String customTag = cursorItemMeta.getPersistentDataContainer().get(new NamespacedKey(VClans.getInstance(), "customTag"), PersistentDataType.STRING);
        if (!cursorItem.getType().isAir() && customTag != null) {
            player.setItemOnCursor(null);
        }

    }

    protected void setItem(int row, int column, ItemStack item) {
        if (row < 0 || row >= inventory.getSize() / 9 || column < 0 || column >= 9) {
            throw new IllegalArgumentException("Row or column out of bounds");
        }
        inventory.setItem(row * 9 + column, item);
    }
    protected void setItem(int pos, ItemStack item) {
        if (pos >= inventory.getSize()) {
            throw new IllegalArgumentException("Pos out of bounds");
        }
        inventory.setItem(pos, item);
    }
    protected static ItemStack createItemStack(String customTag, Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(name));
            meta.lore(Arrays.stream(lore).map(line -> Component.text(line, TextColor.color(240,240,240))).toList());
            meta.getPersistentDataContainer().set(new NamespacedKey(VClans.getInstance(), "customTag"), PersistentDataType.STRING, customTag);
            item.setItemMeta(meta);
        }
        return item;
    }
    protected static ItemStack createItemStack(String customTag, Material material, Component name, Component... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(name);
            meta.lore(Arrays.stream(lore).map(line -> line.color() == null ? line.color(TextColor.color(240,240,240)) : line).toList());
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
