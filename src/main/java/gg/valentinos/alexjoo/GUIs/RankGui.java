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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static gg.valentinos.alexjoo.VClans.Log;
import static gg.valentinos.alexjoo.VClans.SendMessage;

public class RankGui extends AbstractGui {
    private Clan clan;
    private ClanHandler clanHandler;
    private ClanRank playerRank;
    private enum Mode {
        CREATE,
        DELETE,
        EDIT,
        NONE
    }

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
        this.playerRank = clan.getRank(player.getUniqueId());
        initializeRankManager();
    }

    private void initializeRankManager(){
        // top menu bar
        inventory.clear();
        HashMap<String, Boolean> permissions = playerRank.getPermissions();
        if (permissions.get("canCreateRank"))
            setItem(0, 0, createItemStack("createRank", Material.CRAFTING_TABLE, "Create Rank", "Click to create a new rank"));
        if (permissions.get("canDeleteRank"))
            setItem(0,1, createItemStack("deleteRank", Material.TNT, "Delete Rank", "Click this and select a rank to delete it."));
        if (permissions.get("canEditRank"))
            setItem(0, 2, createItemStack("editRank" ,Material.WRITABLE_BOOK, "Edit Rank", "Click this and select a rank to edit it."));
        ItemStack playerHead = createItemStack("showInfo" ,Material.PLAYER_HEAD, "Your Rank", "Your rank is: " + playerRank.getTitle(), "Click this to send information about your rank in the chat.");
        SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
        if (skullMeta != null) {
            skullMeta.setOwningPlayer(player);
            playerHead.setItemMeta(skullMeta);
        }
        setItem(0, 7, playerHead);
        setItem(0,8, createItemStack("closeWindow",Material.BARRIER, "Close", "Click this to close the GUI."));

        // rank list
        HashMap<String, ClanRank> ranks = clan.getRanks();
        int pos = 9;
        for (Map.Entry<String, ClanRank> entry : ranks.entrySet()) {
            String id = entry.getKey();
            ClanRank rank = entry.getValue();

            int memberCount = clan.getMemberUUIDsFromRank(id).size();
            int priority = rank.getPriority();
            String title = rank.getTitle();
            Material material = getMaterial(priority, memberCount);
            ItemStack item = createItemStack("rank-"+id, material, title, "Click this to see information about this rank.", "Members: " + memberCount, "Priority: " + priority);
            setItem(pos, item);
            pos++;
        }
    }
    private void initializeRankEditor(ClanRank rank){
        // top menu bar
        inventory.clear();
        setItem(0, 0, createItemStack("saveRank", Material.EMERALD_BLOCK, "Save Changes", "Click this to save the changes below."));
        setItem(0, 1, createItemStack("setTitle", Material.NAME_TAG, "Set Title", "Click this to set the title of the rank."));
        setItem(0, 2, createItemStack("priority-10", Material.SOUL_LANTERN, "-10", "-10 to the priority number."));
        setItem(0, 3, createItemStack("priority-1", Material.SOUL_TORCH, "-1", "-1 to the priority number."));
        // the priority number is set further down when we read the rank.
        setItem(0, 5, createItemStack("priority+1", Material.TORCH, "+1", "+1 to the priority number."));
        setItem(0, 6, createItemStack("priority+10", Material.LANTERN, "+10", "+10 to the priority number."));
        ItemStack playerHead = createItemStack("rankCopy" ,Material.PLAYER_HEAD, "Your Rank", "Your rank is: " + playerRank.getTitle(), "Clicking this will copy your ranks configuration to the rank you are editing.");
        SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
        if (skullMeta != null) {
            skullMeta.setOwningPlayer(player);
            playerHead.setItemMeta(skullMeta);
        }
        setItem(0, 7, playerHead);
        setItem(0,8, createItemStack("goBack",Material.BARRIER, "Close", "Click this to go back.", "Any unsaved changes will be lost."));

        // rank configuration
        HashMap<String, Boolean> permissions;
        if (rank == null) { // rank is null, we are creating a new rank
            permissions = ClanRank.createDefaultPermissions();
            setItem(0, 4, createItemStack("priority", Material.OAK_HANGING_SIGN, "Priority number: " + playerRank.getPriority(), "This number determines the hiarchy of the rank.", "The higher the number, the higher the rank."));
        }
        else{ // rank is not null, we are editing an existing rank
            permissions = rank.getPermissions();
            setItem(0, 4, createItemStack("priority", Material.OAK_HANGING_SIGN, "Priority number: " + rank.getPriority(), "This number determines the hiarchy of the rank.", "The higher the number, the higher the rank."));
        }
        int pos = 9;
        for (Map.Entry<String, Boolean> entry : permissions.entrySet()) {
            String permission = entry.getKey();
            boolean value = entry.getValue();
            ItemStack item;
            if (value) {
                item = createItemStack(permission, Material.LIME_CONCRETE, permission, "Click this to disable this permission.");
            } else {
                item = createItemStack(permission, Material.RED_CONCRETE, permission, "Click this to enable this permission.");
            }
            setItem(pos, item);
            pos++;
        }

    }

    private static @NotNull Material getMaterial(int priority, int memberCount) {
        Material material;
        if (priority == 99){
            material = Material.GOLD_BLOCK;
        } else if (priority == 0) {
            if (memberCount > 0) {
                material = Material.GOLD_NUGGET;
            }
            else {
                material = Material.IRON_NUGGET;
            }
        }else{
            if (memberCount > 0) {
                material = Material.GOLD_INGOT;
            }
            else {
                material = Material.IRON_INGOT;
            }
        }
        return material;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getView().title().equals(Component.text(title)) && e.getWhoClicked() == player) {
            e.setCancelled(true);
            ItemStack item = e.getCurrentItem();
            ItemStack cursorItem = player.getItemOnCursor();
            if (item == null || item.getType() == Material.AIR) {
                return;
            }
//            Player player = (Player) e.getWhoClicked();
            String customTag = item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(VClans.getInstance(), "customTag"), PersistentDataType.STRING);
            if (customTag != null) {
                player.sendMessage("Player " + player.getName() + " clicked on " + customTag);
                switch (customTag) {
                    case "createRank":
                        if (!cursorItem.getType().isAir()){
                            initializeRankManager();
                            player.setItemOnCursor(null);
                        }
                        else{
                            initializeRankEditor(null);
                        }
                        break;
                    case "deleteRank", "editRank":
                        if (!cursorItem.getType().isAir()){
                            initializeRankManager();
                            player.setItemOnCursor(null);
                        }
                        else{
                            e.setCancelled(false);
                        }
                        break;
                    case "showInfo":
                        SendMessage(player, clan.getRankInfo(playerRank), LogType.NULL);
                        player.closeInventory();
                        break;
                    case "closeWindow":
                        player.closeInventory();
                        break;
                    case "goBack":
                        player.setItemOnCursor(null);
                        initializeRankManager();
                        break;
                    default:
                        if (customTag.startsWith("rank-")) {
                            String rankId = customTag.substring(5);
                            ClanRank rank = clan.getRankById(rankId);
                            if (cursorItem.getType().isAir()) {
                                if (rank != null) {
                                    SendMessage(player, clan.getRankInfo(rank), LogType.NULL);
                                } else {
                                    player.sendMessage("Rank not found.");
                                }
                                player.closeInventory();
                            }
                            else{
                                ItemMeta cursorItemMeta = cursorItem.getItemMeta();
                                if (cursorItemMeta == null) {
                                    Log("The item meta is null.", LogType.SEVERE);
                                    return;
                                }
                                String heldCustomTag = cursorItemMeta.getPersistentDataContainer().get(new NamespacedKey(VClans.getInstance(), "customTag"), PersistentDataType.STRING);
                                if (heldCustomTag != null){
                                    Log("Player " + player.getName() + " clicked on " + heldCustomTag, LogType.INFO);
                                    if (heldCustomTag.equals("editRank")) {
                                        //TODO: do checks if player can perform changes based on permissions and priority
//                                        ClanHandler clanHandler = VClans.getInstance().getClansHandler();
                                        ClanRank targetRank = clan.getRankById(rankId);
                                        if (targetRank == null) {
                                            player.sendMessage("Rank not found.");
                                            return;
                                        }
                                        if (playerRank.getPriority() <= targetRank.getPriority()){
                                            //TODO: cant edit this rank due to priority too low
                                            return;
                                        }
                                        player.setItemOnCursor(null);
                                        initializeRankEditor(rank);
                                    } else if (heldCustomTag.equals("deleteRank")) {
                                        player.setItemOnCursor(null);
                                        //TODO delete rank
                                    }
                                }
                            }
                        } else {
                            player.sendMessage("Unknown item clicked.");
                        }
                        break;
                }
            }
            else{
                player.sendMessage("Player clicked on nothing.");
            }
        }
    }
}
