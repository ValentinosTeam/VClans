package gg.valentinos.alexjoo.GUIs;

import gg.valentinos.alexjoo.Data.Clan;
import gg.valentinos.alexjoo.Data.ClanRank;
import gg.valentinos.alexjoo.Data.LogType;
import gg.valentinos.alexjoo.Handlers.ClanHandler;
import gg.valentinos.alexjoo.VClans;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static gg.valentinos.alexjoo.VClans.Log;
import static gg.valentinos.alexjoo.VClans.SendMessage;

public class RankGui extends AbstractGui {
    private Clan clan;
    private final ClanHandler clanHandler;
    private ClanRank playerRank;

    private ClanRank newRank;

    public RankGui() {
        super("Rank Manager", 3);
        clanHandler = VClans.getInstance().getClanHandler();
    }

    @Override
    protected void initializeItems() {
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
        newRank = new ClanRank("New Rank", "newrank");
        newRank.setPriority(1);
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
        List<Map.Entry<String, ClanRank>> sortedRanks = ranks.entrySet().stream().sorted(Comparator.comparingInt((Map.Entry<String, ClanRank> entry) -> entry.getValue().getPriority()).reversed()).toList();
        int pos = 9;
        for (Map.Entry<String, ClanRank> entry : sortedRanks) {
            String id = entry.getKey();
            ClanRank rank = entry.getValue();

            int memberCount = clan.getMemberUUIDsFromRank(id).size();
            int priority = rank.getPriority();
            String title = rank.getTitle();
            Material material = getRankMaterial(priority, memberCount);
            ItemStack item = createItemStack("rank-"+id, material, LegacyComponentSerializer.legacyAmpersand().deserialize(title), Component.text("Click this to see information about this rank."), Component.text("Priority: ", TextColor.color(100,100,100)).append(Component.text(priority, TextColor.color(255, 244, 0))), Component.text("Members: ", TextColor.color(100,100,100)).append(Component.text(memberCount)));
            setItem(pos, item);
            pos++;
        }
    }
    private void initializeRankEditor(ClanRank rank){
        // top menu bar
        inventory.clear();
        setItem(0, 0, createItemStack("saveRank", Material.EMERALD_BLOCK, "Save Changes", "Click this to save the changes below."));
        setItem(0, 1, createItemStack("setTitle", Material.NAME_TAG, LegacyComponentSerializer.legacyAmpersand().deserialize(newRank.getTitle()), Component.text("This is the current ranks title."), Component.text("Click this to set the title of the rank.")));
        setItem(0,8, createItemStack("goBack",Material.BARRIER, "Go Back", "Click this to go back.", "Any unsaved changes will be lost."));

        if (rank == null) { // rank is null, we are creating a new rank
            newRank = new ClanRank("New Rank", "newrank");
            newRank.setPriority(1);
            setItem(0, 4, createItemStack("priority", Material.OAK_HANGING_SIGN, "Priority number: " + playerRank.getPriority(), "This number determines the hierarchy of the rank.", "The higher the number, the higher the rank."));
        }
        else{ // rank is not null, we are editing an existing rank
            newRank = new ClanRank(rank.getTitle(), rank.getId());
            newRank.setPriority(rank.getPriority());
            newRank.setPermissions(rank.copyPermissions());
            setItem(0, 4, createItemStack("priority", Material.OAK_HANGING_SIGN, "Priority number: " + rank.getPriority(), "This number determines the hierarchy of the rank.", "The higher the number, the higher the rank."));
        }
        if (rank != null && (rank.getPriority() == 99 || rank.getPriority() == 0)) {
            return;
        }

        setItem(0, 2, createItemStack("priority-10", Material.SOUL_LANTERN, "decrease priority by 10", "-10 to the priority number."));
        setItem(0, 3, createItemStack("priority-1", Material.SOUL_TORCH, "decrease priority by 1", "-1 to the priority number."));
        // the priority number is set further down when we read the rank.
        setItem(0, 5, createItemStack("priority+1", Material.TORCH, "increase priority by 1", "+1 to the priority number."));
        setItem(0, 6, createItemStack("priority+10", Material.LANTERN, "increase priority by 10", "+10 to the priority number."));
        ItemStack playerHead = createItemStack("rankCopy" ,Material.PLAYER_HEAD, "Your Rank", "Your rank is: " + playerRank.getTitle(), "Clicking this will copy your ranks configuration to the rank you are editing.");
        SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
        if (skullMeta != null) {
            skullMeta.setOwningPlayer(player);
            playerHead.setItemMeta(skullMeta);
        }
        setItem(0, 7, playerHead);

        initializeRankPermissions(newRank.getPermissions());
        updatePriorityNumber();
    }
    private Inventory createAnvilUI(){
        keepAlive = true;
        InventoryView inventoryView = player.openAnvil(null, true);
        keepAlive = false;
        if (inventoryView == null) {
            Log("Couldn't load anvil UI", LogType.SEVERE);
            player.closeInventory();
            return null;
        }
        return inventoryView.getTopInventory();
    }
    private void initializeTitleEditor(){
        inventory = createAnvilUI();
        if (inventory == null) return;
        ItemStack item = createItemStack("currentTitle", Material.NAME_TAG, Component.text(newRank.getTitle()), Component.text("The current title is: ").append(LegacyComponentSerializer.legacyAmpersand().deserialize(newRank.getTitle())), Component.text("Input the new name in the anvil."));
        inventory.setItem(0, item);
    }
    private void initializeRankIdEditor(){
        inventory = createAnvilUI();
        if (inventory == null) return;
        ItemStack item = createItemStack("currentId", Material.NETHER_STAR, Component.text(newRank.getTitle()), Component.text("The current rank ID is: ").append(Component.text(newRank.getTitle())), Component.text("Input a valid ID in the anvil."));
        inventory.setItem(0, item);
    }
    private void initializeRankPermissions(HashMap<String, Boolean> perms) {
        int pos = 9;
        for (Map.Entry<String, Boolean> entry : perms.entrySet()) {
            String permission = entry.getKey();
            boolean value = entry.getValue();
            ItemStack item = createPermissionBlock(permission, value);
            setItem(pos, item);
            pos++;
        }
    }
    private void updatePriorityNumber(){
        if (newRank.getPriority() < 1){
            newRank.setPriority(1);
        }
        if (newRank.getPriority() > 98){
            newRank.setPriority(98);
        }
        setItem(0, 4, createItemStack("priority", Material.OAK_HANGING_SIGN, Component.text("Priority number: ").append(Component.text(newRank.getPriority(), TextColor.color(255,215,0))), Component.text("This number determines the hierarchy of the rank."), Component.text("The higher the number, the higher the rank.")));
    }
    private ItemStack createPermissionBlock(String permission, boolean value){
        String customTag = "perm-"+permission;
        Material material;
        Component lore;
        Component name = Component.text(unCamelCase(permission) + ": ");
        TextColor color = value ? TextColor.color(0, 255, 0) : TextColor.color(255, 0, 0);
        name = name.append(Component.text(value, color));
        if (!player.hasPermission(permission) || permission.equals("canDisband")) {
            material = value ? Material.GREEN_CONCRETE : Material.RED_CONCRETE;
            lore = Component.text("You cannot change this permission.")
                    .color(TextColor.color(255, 0, 0));
        }
        else{
            material = value ? Material.GREEN_WOOL : Material.RED_WOOL;
            lore = Component.text("Click this to toggle this permission.");
        }
        return createItemStack(customTag, material, name, lore);
    }
    private static @NotNull Material getRankMaterial(int priority, int memberCount) {
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
        if (e.getWhoClicked() == player) {
            e.setCancelled(true);
            if (e.isRightClick() || e.isShiftClick()) {
                return;
            }
            ItemStack item = e.getCurrentItem();
            ItemStack cursorItem = player.getItemOnCursor();
            if (item == null || item.getType() == Material.AIR) {
                if (!cursorItem.getType().isAir()){
                    initializeRankManager();
                    player.setItemOnCursor(null);
                }
                return;
            }
            Component displayName = item.getItemMeta().displayName();

            String customTag = item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(VClans.getInstance(), "customTag"), PersistentDataType.STRING);
            if (customTag == null) return;
            if (displayName == null) {
                Log("Display name is null", LogType.SEVERE);
                player.closeInventory();
                return;
            }
            HashMap<String, Boolean> newPermissions;
            if (e.getInventory().getType() == InventoryType.CHEST){
                if (!customTag.startsWith("rank-")){
                    if (!cursorItem.getType().isAir()){
                        player.setItemOnCursor(null);
                        initializeRankManager();
                        return;
                    }
                }
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
                    case "saveRank":
                        if (!cursorItem.getType().isAir()){
                            initializeRankManager();
                            player.setItemOnCursor(null);
                        }
                        else{
                            if (clan.getRankById(newRank.getId()) != null) {
                                // rank exists overwrite it
                                clanHandler.addRank(clan, newRank);
                                keepAlive = false;
                                initializeRankManager();
                            }
                            else{
                                // rank doesn't exist, open anvil gui to set the id.
                                initializeRankIdEditor();
                            }
                        }
                        break;
                    case "setTitle":
                        // open an anvil ui to set the title
                        if (!cursorItem.getType().isAir()){
                            initializeRankManager();
                            player.setItemOnCursor(null);
                        }
                        else{
                            initializeTitleEditor();
                        }
                        break;
                    case "rankCopy":
                        // copy the configuration of the players current rank into the rank being edited
                        HashMap<String, Boolean> playerPermissionsCopy = playerRank.copyPermissions();
                        newPermissions = ClanRank.createDefaultPermissions();
                        for (String key : playerPermissionsCopy.keySet()) {
                            if (playerPermissionsCopy.get(key) && !key.equals("canDisband")) {
                                newPermissions.put(key, true);
                            }
                        }
                        newRank.setPermissions(newPermissions);
                        initializeRankPermissions(newPermissions);
                        break;
                    default:
                        if (customTag.startsWith("rank-")) {
                            String rankId = customTag.substring(5);
                            ClanRank rank = clan.getRankById(rankId);
                            if (cursorItem.getType().isAir()) { // selecting the rank as normal
                                if (rank != null) {
                                    SendMessage(player, clan.getRankInfo(rank), LogType.NULL);
                                } else {
                                    SendMessage(player, Component.text("Rank not found.", TextColor.color(255,0,0)), LogType.SEVERE);
                                }
                                player.closeInventory();
                            }
                            else { // has an item like deletion or editing equipped
                                ItemMeta cursorItemMeta = cursorItem.getItemMeta();
                                if (cursorItemMeta == null) {
                                    SendMessage(player, Component.text("The item meta is null. You shouldn't be able to see this message").color(TextColor.color(255,0,0)), LogType.SEVERE);
                                    return;
                                }
                                String heldCustomTag = cursorItemMeta.getPersistentDataContainer().get(new NamespacedKey(VClans.getInstance(), "customTag"), PersistentDataType.STRING);
                                if (heldCustomTag != null){
                                    Log("Player " + player.getName() + " clicked on " + heldCustomTag, LogType.INFO);
                                    ClanRank targetRank = clan.getRankById(rankId);
                                    if (targetRank == null) {
                                        SendMessage(player, Component.text("Rank not found.", TextColor.color(255,0,0)), LogType.SEVERE);
                                        return;
                                    }
                                    if (playerRank.getPriority() <= targetRank.getPriority() && playerRank.getPriority() != 99) {
                                        // cant edit this rank due to priority too low
                                        VClans.sendFormattedMessage(player, VClans.getInstance().getConfig().getString("commands.clan.rank.messages.rank-priority-low"), LogType.INFO);
                                        return;
                                    }
                                    player.setItemOnCursor(null);
                                    if (heldCustomTag.equals("editRank")) {
                                        newRank = targetRank;
                                        initializeRankEditor(newRank);
                                    } else if (heldCustomTag.equals("deleteRank")) {
                                        //TODO delete rank
                                        if (targetRank.getPriority() != 99 && targetRank.getPriority() != 0) {
                                            clanHandler.removeRank(clan, targetRank.getId());
                                        }
                                        initializeRankManager();
                                    }
                                }
                            }
                        }
                        else if (customTag.startsWith("perm-")){
                            // should toggle the permission, lime or red concrete and true or false permission
                            String permission = customTag.substring(5);
                            newPermissions = newRank.copyPermissions();
                            boolean value = newPermissions.get(permission);
                            if (item.getType() != Material.LIME_CONCRETE && item.getType() != Material.RED_CONCRETE){
                                newPermissions.put(permission, !value);
                                ItemStack newItem = createPermissionBlock(permission, !value);
                                setItem(e.getSlot(), newItem);
                            }
                            newRank.setPermissions(newPermissions);
                        }
                        else if (customTag.startsWith("priority")){
                            // should change the priority, +1 or -1
                            if (customTag.equals("priority")){
                                SendMessage(player, Component.text("This ranks priority number is: " + newRank.getPriority()), LogType.INFO);
                                return;
                            }
                            else{
                                String priorityChange = customTag.substring(8);
                                int newPriority = newRank.getPriority();
                                switch (priorityChange) {
                                    case "+1" -> newPriority++;
                                    case "-1" -> newPriority--;
                                    case "+10" -> newPriority += 10;
                                    case "-10" -> newPriority -= 10;
                                }
                                newRank.setPriority(newPriority);
                                updatePriorityNumber();
                            }
                        }
                        break;
                }
            }
            else if (e.getInventory().getType() == InventoryType.ANVIL){
                if (!cursorItem.getType().isAir()){
                    initializeRankManager();
                    player.setItemOnCursor(null);
                }
                switch (customTag) {
                    case "currentTitle":
                        // copy the current title into the anvil text
                        ItemStack temp = createItemStack("currentTitle", Material.NAME_TAG, Component.text(newRank.getTitle()), Component.text("Your current title is: ").append(LegacyComponentSerializer.legacyAmpersand().deserialize(newRank.getTitle())), Component.text("Input the new name in the anvil."));
                        inventory.setItem(0, temp);
                        break;
                    case "titleResult":
                        // set the new title to newTitle and go back to the rank editor
                        String name = LegacyComponentSerializer.legacyAmpersand().serialize(displayName);
                        newRank.setTitle(name);
                        inventory.clear();
                        inventory = Bukkit.createInventory(null, InventoryType.CHEST, title);
                        initializeRankEditor(newRank);
                        keepAlive = true;
                        player.openInventory(inventory);
                        keepAlive = false;
                        break;
                    case "idResult":
                        String id = PlainTextComponentSerializer.plainText().serialize(displayName);
                        newRank.setId(id);
                        inventory.clear();
                        inventory = Bukkit.createInventory(null, InventoryType.CHEST, title);
                        clanHandler.addRank(clan, newRank);
                        initializeRankManager();
                        keepAlive = true;
                        player.openInventory(inventory);
                        keepAlive = false;
                        break;
                    default:
                        Log("Somethings wrong i can feel it", LogType.SEVERE);
                        break;
                }
            }
        }
    }
    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent e) {
        ItemStack item = inventory.getItem(0);
        if (e.getViewers().contains(player) && item != null) {
            if (item.getType() == Material.NAME_TAG){
                e.getView().setRepairCost(0);
                ItemStack result = createItemStack("titleResult", Material.NAME_TAG, Component.text(newRank.getTitle()), Component.text("This will be the new name of the rank."));
                ItemMeta meta = result.getItemMeta();
                if (meta != null) {
                    String newName = e.getView().getRenameText();
                    if (newName == null) return;
                    Component name;
                    name = LegacyComponentSerializer.legacyAmpersand().deserialize(newName);
                    meta.displayName(name);
                    result.setItemMeta(meta);
                    String strippedName = stripMessage(newName);
                    if (strippedName == null || strippedName.length() < 3 || strippedName.length() > 30) {
                        e.setResult(null);
                    }
                    else{
                        e.setResult(result);
                    }
                }
            }
            else if (item.getType() == Material.NETHER_STAR){
                e.getView().setRepairCost(0);
                ItemStack result = createItemStack("idResult", Material.NETHER_STAR, Component.text(newRank.getTitle()), Component.text("This will be the ID of the rank."));
                ItemMeta meta = result.getItemMeta();
                if (meta != null) {
                    String newName = e.getView().getRenameText();
                    if (newName == null) return;
                    meta.displayName(Component.text(newName));
                    result.setItemMeta(meta);
                    if (stringValid(newName) && newName.length() > 3 && newName.length() < 16 && clan.getRankById(newName) == null) {
                        e.setResult(result);
                    }
                    else{
                        e.setResult(null);
                    }
                }
            }
        }
    }

    private static String stripMessage(String str) {
        // Remove all non-alphanumeric characters and underscores
        final List<String> allowedCodes = List.of(
                "&1", "&2", "&3", "&4", "&5", "&6", "&7", "&8", "&9", "&a", "&b", "&c", "&d", "&e", "&f", "&l", "&m", "&n", "&o", "&r"
        );
        final List<String> forbiddenCodes = List.of(
                "&k", "&0"
        );
        if (str == null) return "";
        for (String code : forbiddenCodes) {
            if (str.contains(code)) {
                return null;
            }
        }
        for (String code : allowedCodes) {
            str = str.replace(code, "");
        }
        return str.replaceAll("\\s+", ""); // Remove extra spaces
//        return str;
    }
    private static boolean stringValid(String str){
        return str != null && str.matches("[a-z0-9]+");
    }
    private static String unCamelCase(String str) {
        // Insert spaces before each uppercase letter
        String spaced = str.replaceAll("([a-z])([A-Z])", "$1 $2");
        // Capitalize the first letter of each word
        String[] words = spaced.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            result.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1).toLowerCase())
                    .append(" ");
        }
        return result.toString().trim();
    }
}
