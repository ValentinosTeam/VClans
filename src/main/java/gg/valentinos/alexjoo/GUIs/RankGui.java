package gg.valentinos.alexjoo.GUIs;

import gg.valentinos.alexjoo.Data.Clan;
import gg.valentinos.alexjoo.Data.ClanRank;
import gg.valentinos.alexjoo.Data.LogType;
import gg.valentinos.alexjoo.Handlers.ClanHandler;
import gg.valentinos.alexjoo.VClans;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import static gg.valentinos.alexjoo.VClans.SendMessage;

public class RankGui extends AbstractGui {
    private Clan clan;
    private ClanHandler clanHandler;
    private ClanRank rank;

    public RankGui() {
        super("Rank Manager", 3);
    }

    @Override
    protected void initializeItems() {
        this.clanHandler = VClans.getInstance().getClansHandler();
        this.clan = clanHandler.getClanByMember(player.getUniqueId());
        if (clan == null) {
            SendMessage(player, Component.text("You are not in a clan, you shouldn't see this.").color(TextColor.color(255,0,0)), LogType.SEVERE);
            player.closeInventory();
            return;
        }
        this.rank = clan.getRank(player.getUniqueId());
        initializeRankManager();
    }

    private void initializeRankManager(){
        // top menu bar
        inventory.clear();
        if (rank.canCreateRank())
            setItem(0, 0, createItemStack("createRank", Material.CRAFTING_TABLE, "Create Rank", "Click to create a new rank"));
        if (rank.canDeleteRank())
            setItem(0,1, createItemStack("deleteRank", Material.TNT, "Delete Rank", "Click this and select a rank to delete it."));
        if (rank.canEditRank())
            setItem(0, 2, createItemStack("editRank" ,Material.WRITABLE_BOOK, "Edit Rank", "Click this and select a rank to edit it."));
        ItemStack playerHead = createItemStack("showInfo" ,Material.PLAYER_HEAD, "Your Rank", "Your rank is: " + rank.getTitle(), "Click this to send information about your rank in the chat.");
        SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
        if (skullMeta != null) {
            skullMeta.setOwningPlayer(player);
            playerHead.setItemMeta(skullMeta);
        }
        setItem(0, 7, playerHead);
        setItem(0,8, createItemStack("closeWindow",Material.BARRIER, "Close", "Click this to close the GUI."));
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
                String customTag = item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(VClans.getInstance(), "customTag"), PersistentDataType.STRING);
                player.sendMessage("Player " + player.getName() + " clicked on " + customTag);
                switch (customTag) {
                    case "createRank":
                        break;
                    case "deleteRank":
                        break;
                    case "editRank":
                        break;
                    case "showInfo":
                        SendMessage(player, rank.getRankInfo(), LogType.NULL);
                        player.closeInventory();
                        break;
                    case "closeWindow":
                        player.closeInventory();
                        break;
                    default:
                        break;
                }
            }
            else{
                player.sendMessage("Player clicked on nothing.");
            }
        }
    }
}
