package gg.valentinos.alexjoo.Data.WarData;

import gg.valentinos.alexjoo.Data.ClanData.Clan;
import gg.valentinos.alexjoo.VClans;
import org.bukkit.entity.Player;

public class PeaceTreaty {

    private Player creator;
    private Clan creatorClan;
    private Clan targetClan;
    private int amountOffered;
    private int amountRequested;

    private boolean isVaultEnabled;

    public PeaceTreaty(Player creator, Clan targetClan, int amount) {
        this.creator = creator;
        Clan creatorClan = VClans.getInstance().getClanHandler().getClanByMember(creator.getUniqueId());
        if (creatorClan == null) {
            throw new IllegalArgumentException("Initiator player is not in a clan.");
        }
        this.creatorClan = creatorClan;
        this.targetClan = targetClan;
        if (amount >= 0) {
            this.amountOffered = 0;
            this.amountRequested = amount;
        } else {
            this.amountOffered = -amount;
            this.amountRequested = 0;
        }
        this.isVaultEnabled = VClans.getInstance().getVaultHandler().getEconomy() != null;
    }

    public void payOutTreaty(Player acceptor) {
        if (amountOffered == 0 && amountRequested == 0) {
            return;
        }
        if (!isVaultEnabled) return;
        if (amountOffered > 0) {
            VClans.getInstance().getVaultHandler().withdrawPlayer(creator, amountOffered);
            VClans.getInstance().getVaultHandler().depositPlayer(acceptor, amountOffered);
        } else if (amountRequested > 0) {
            VClans.getInstance().getVaultHandler().withdrawPlayer(acceptor, amountRequested);
            VClans.getInstance().getVaultHandler().depositPlayer(creator, amountRequested);
        }
    }
    public boolean canCreatorPay() {
        if (!isVaultEnabled) return true;
        if (amountOffered <= 0) return true;
        double balance = VClans.getInstance().getVaultHandler().getEconomy().getBalance(creator);
        return balance >= amountOffered;
    }
    public boolean canTargetPay(Player player) {
        if (!isVaultEnabled) return true;
        if (amountRequested <= 0) return true;
        double balance = VClans.getInstance().getVaultHandler().getEconomy().getBalance(player);
        return balance >= amountRequested;
    }

    public Player getCreator() {
        return creator;
    }
    public Clan getCreatorClan() {
        return creatorClan;
    }
    public Clan getTargetClan() {
        return targetClan;
    }
    public int getAmountOffered() {
        return amountOffered;
    }
    public int getAmountRequested() {
        return amountRequested;
    }
}
