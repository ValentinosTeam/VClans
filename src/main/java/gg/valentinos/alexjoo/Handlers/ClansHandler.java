package gg.valentinos.alexjoo.Handlers;

import com.google.gson.reflect.TypeToken;
import gg.valentinos.alexjoo.Data.Clan;
import gg.valentinos.alexjoo.Data.Clans;
import gg.valentinos.alexjoo.Utility.JsonUtils;
import gg.valentinos.alexjoo.VClans;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;

public class ClansHandler {

    private Clans clans;
    private static final Logger logger = VClans.getInstance().getLogger();
    private static final FileConfiguration config = VClans.getInstance().getConfig();

    public ClansHandler() {
        loadClans();
    }

    public String createClan(UUID player, String name) {
        // This method will create a clan return null if successful, error message if not
        logger.info("Player " + Bukkit.getPlayer(player).getName() + " is trying to create a clan with name " + name);
        if (clans.clanExists(name)) {
            logger.warning("Clan with name " + name + " already exists.");
            return Objects.requireNonNull(config.getString("commands.clan.create.messages.already-exists")).replace("{name}",name);
        }
        if (name.length() > 16) {
            logger.warning("Clan name is too long.");
            return Objects.requireNonNull(config.getString("commands.clan.create.messages.too-long"));
        }
        if (name.length() < 3) {
            logger.warning("Clan name is too short.");
            return Objects.requireNonNull(config.getString("commands.clan.create.messages.too-short"));
        }
        if (clans.getClanByMember(player) != null) {
            logger.warning("Player is already in a clan.");
            return Objects.requireNonNull(config.getString("commands.clan.create.messages.already-in-clan"));
        }
        if (!name.matches("^[a-zA-Z0-9_]*$")) {
            logger.warning("Clan name contains invalid characters.");
            return Objects.requireNonNull(config.getString("commands.clan.create.messages.invalid-characters"));
        }

        // checks done, create the clan
        ArrayList<UUID> members = new ArrayList<>();
        ArrayList<UUID> owners = new ArrayList<>();
        owners.add(player);
        Clan clan = new Clan(name, members, owners, new ArrayList<>());
        clans.addClan(clan);
        saveClans();
        logger.fine("Player " + Bukkit.getPlayer(player).getName() + " has successfully created a clan with name " + name);
        return null;
    }

    public String deleteClan(UUID player, Clan clan) {
        // This method will delete a clan return true if successful, false if not

        // TODO: Check if player is an admin. if true then allow delete clan
        // Check if the player is the owner of the clan
        if (!clan.getOwners().contains(player)) {
            logger.warning("Player " + Bukkit.getPlayer(player).getName() + " is not the owner of the clan.");
            return config.getString("commands.default.messages.not-owner");
        }

        clans.getClans().remove(clan);
        saveClans();
        return null;
    }

    public String disbandClan(UUID player) {
        // This method will disband a clan return true if successful, false if not
        logger.info("Player " + Bukkit.getPlayer(player).getName() + " is trying to disband a clan.");
        Clan clan = clans.getClanByMember(player);
        String error = isPlayerOwner(player, clan);
        if (error != null) {
            return error;
        }
        if (clan.getOwners().size() > 1){
            logger.warning("Player is not the only owner of the clan.");
            return config.getString("commands.clan.disband.messages.not-only-owner");
        }

        return deleteClan(player, clan);
    }

    public String invitePlayer(UUID playerUUID, String targetName) {
        // This method will invite a player to a clan return true if successful, false if not
        Player player = Bukkit.getPlayer(playerUUID);
        assert player != null;
        OfflinePlayer target = player.getServer().getOfflinePlayer(targetName);

        logger.info("Player " + player.getName() + " is trying to invite player " + targetName + " to the clan.");

        if (!target.hasPlayedBefore()) {
            logger.warning("Player attempted to invite a player that has never joined the server before.");
            return config.getString("commands.default.messages.never-joined");
        } else if (target.equals(player)) {
            logger.warning("Player attempted to invite themselves.");
            return config.getString("commands.clan.invite.messages.invite-self");
        }

        Clan clan = clans.getClanByOwner(playerUUID);
        if (clan == null) {
            logger.warning("Player does not own a clan to invite players.");
            return config.getString("commands.clan.invite.messages.not-owner");
        }
        if (clan.getMembers().contains(target.getUniqueId())) {
            logger.warning("Player is already in the clan.");
            return config.getString("commands.clan.invite.messages.already-in-the-clan");
        }
        if (clan.getInvites().contains(target.getUniqueId())) {
            logger.warning("Player is already invited to the clan.");
            return config.getString("commands.clan.invite.messages.already-invited");
        }
        if (clans.getClanByMember(target.getUniqueId()) != null) {
            logger.warning("Player is already in a clan.");
            return config.getString("commands.clan.invite.messages.already-in-a-clan");
        }
        clan.inviteMember(target.getUniqueId());
        saveClans();
        return null;
    }

    public String joinClan(UUID player, String clanName) {
        // This method will join a player to a clan return true if successful, false if not
        logger.info("Player " + Bukkit.getPlayer(player).getName() + " is trying to join a clan with name " + clanName);
        Clan clan = clans.getClanByName(clanName);
        if (clan == null) {
            logger.warning("Clan with name " + clanName + " does not exist.");
            return config.getString("commands.clan.join.messages.clan-not-exist").replace("{clan}", clanName);
        }
        if (clan.getMembers().contains(player)) {
            logger.warning("Player is already in the clan.");
            return config.getString("commands.clan.join.messages.already-in-the-clan").replace("{clan}", clanName);
        }
        if (!clan.getInvites().contains(player)) {
            logger.warning("Player is not invited to the clan.");
            return config.getString("commands.clan.join.messages.not-invited").replace("{clan}", clanName);
        }
        if (clans.getClanByMember(player) != null) {
            logger.warning("Player is already in a clan.");
            return config.getString("commands.clan.join.messages.already-in-a-clan").replace("{clan}", clanName);
        }
        clan.addMember(player);
        saveClans();
        return null;
    }

    public String leaveClan(UUID player) {
        // This method will leave a player from a clan return true if successful, false if not
        logger.info("Player " + Bukkit.getPlayer(player).getName() + " is trying to leave the clan.");
        Clan clan = clans.getClanByMember(player);
        if (clan == null) {
            logger.warning("Player is not in a clan.");
            return config.getString("commands.default.messages.not-in-clan");
        }
        if (clan.isOwner(player)) {
            logger.warning("Player is the owner of the clan.");
            return config.getString("commands.clan.leave.messages.owner-cant-leave");
        }
        clan.removeMember(player);
        return null;
    }

    public String stepDownPlayer(UUID player) {
        // This method will step down a player from a clan return true if successful, false if not
        logger.info("Player " + Bukkit.getPlayer(player).getName() + " is trying to step down from the clan.");
        Clan clan = clans.getClanByMember(player);
        String error = isPlayerOwner(player, clan);
        if (error != null) {
            return error;
        }
        if (clan.getOwners().size() == 1) {
            logger.warning("Player is the only owner of the clan.");
            return config.getString("commands.clan.step-down.messages.only-owner");
        }
        clan.stepDownOwner(player);
        return null;
    }

    public String kickPlayer(UUID playerUUID, String targetName) {
        // This method will kick a player from a clan return true if successful, false if not
        Player player = Bukkit.getPlayer(playerUUID);
        OfflinePlayer target = player.getServer().getOfflinePlayer(targetName);
        logger.info("Player " + player.getName() + " is trying to kick player " + targetName + " from the clan.");
        if (!target.hasPlayedBefore()) {
            return config.getString("commands.default.messages.never-joined").replace("{name}", targetName);
        }
        Clan clan = clans.getClanByMember(playerUUID);
        String error = isPlayerOwner(playerUUID, clan);
        if (error != null) {
            return error;
        }
        if (playerUUID.equals(target.getUniqueId())) {
            logger.warning("Player is trying to kick themselves.");
            return config.getString("commands.clan.kick.messages.cant-kick-yourself").replace("{name}", targetName);
        }
        if (!clan.getMembers().contains(target.getUniqueId())) {
            logger.warning("Player is not in the clan.");
            return config.getString("commands.clan.kick.messages.target-not-in-clan").replace("{name}", targetName);
        }
        if (clan.isOwner(target.getUniqueId())) {
            logger.warning("Player is the owner of the clan.");
            return config.getString("commands.clan.kick.messages.cant-kick-owner").replace("{name}", targetName);
        }
        clan.removeMember(target.getUniqueId());
        return null;
    }

    public void loadClans() {
        clans = new Clans();
        List<Clan> clansList = JsonUtils.deserializeClans("clans.json");
        if (clansList == null) {
            logger.warning("Clans file is empty. Creating new clans.json file.");
            saveClans();
            return;
        }
        clans.setClans(clansList);
    }

    public void saveClans() {
        JsonUtils.toJsonFile(clans.getClans(), "clans.json");
    }

    public String getInvitedClanNames(UUID uniqueId) {
        // This method will return the list of clans the player is invited to
        StringBuilder sb = new StringBuilder();
        for (Clan clan : clans.getClans()) {
            if (clan.getInvites().contains(uniqueId)) {
                sb.append(clan.getName()).append("\n");
            }
        }
        return sb.toString();
    }

    public List<UUID> getMembersOfPlayerClan(UUID player) {
        Clan clan = clans.getClanByMember(player);
        if (clan == null)
            return List.of();
        return clan.getMembers();
    }

    public String getClanNameOfMember(UUID uniqueId) {
        // This method will return the clan the player is in
        Clan clan = clans.getClanByMember(uniqueId);
        if (clan == null) {
            return null;
        }
        return clan.getName();
    }

    public List<String> getClanList() {
        // This method will return the list of clan names
        List<String> names = new ArrayList<>();
        for (Clan clan : clans.getClans()) {
            names.add(clan.getName());
        }
        return names;
    }

    public String getClanMembersList(String clanName) {
        Clan clan = clans.getClanByName(clanName);
        if (clan == null) {
            return "Clan does not exist.";
        }
        StringBuilder sb = new StringBuilder();
        for (UUID owner : clan.getOwners()) {
            sb.append(" - ").append(Bukkit.getOfflinePlayer(owner).getName()).append(" [Owner]\n");
        }
        for (UUID member : clan.getMembers()) {
            sb.append(" - ").append(Bukkit.getOfflinePlayer(member).getName()).append(" [Member] \n");
        }
        return sb.toString();
    }

    public List<UUID> getClanMemberUUIDs(String name) {
        Clan clan = clans.getClanByName(name);
        if (clan == null)
            return List.of();
        List<UUID> members = new ArrayList<>();
        members.addAll(clan.getMembers());
        members.addAll(clan.getOwners());

        return members;
    }

    private String isPlayerOwner(UUID player, Clan clan) {
        if (clan == null) {
            logger.warning("Player is not in a clan.");
            return config.getString("commands.default.messages.not-in-clan");
        }
        if (!clan.isOwner(player)) {
            logger.warning("Player is not the owner of the clan.");
            return config.getString("commands.default.messages.not-owner");
        }
        return null;
    }

}
