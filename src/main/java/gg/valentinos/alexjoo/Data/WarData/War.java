package gg.valentinos.alexjoo.Data.WarData;

import gg.valentinos.alexjoo.Data.ClanData.Clan;
import gg.valentinos.alexjoo.Data.ClanData.ClanChunk;
import gg.valentinos.alexjoo.Data.LogType;
import gg.valentinos.alexjoo.Handlers.ClanHandler;
import gg.valentinos.alexjoo.Handlers.VaultHandler;
import gg.valentinos.alexjoo.Handlers.WarHandler;
import gg.valentinos.alexjoo.Utility.Decorator;
import gg.valentinos.alexjoo.Utility.TaskScheduler;
import gg.valentinos.alexjoo.VClans;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.entity.Player;

import static gg.valentinos.alexjoo.VClans.Log;
import static gg.valentinos.alexjoo.VClans.sendFormattedMessage;

public class War {
    private String initiatorClanId;
    private String targetClanId;
    private long declarationTime;
    private WarState state;
    private String loserClanId = null;

    private transient PeaceTreaty peaceTreaty;
    private transient ClanHandler clanHandler;
    private transient TaskScheduler scheduler;
    private transient WarHandler warHandler;

    public War(Clan initiatorClan, Clan targetClan) {
        this.initiatorClanId = initiatorClan.getId();
        this.targetClanId = targetClan.getId();
        this.declarationTime = System.currentTimeMillis();
        this.state = WarState.DECLARED;

        initHandlers();
        Log("Created a new war between " + initiatorClan.getName() + " and " + targetClan.getName());
    }

    public void checkWarEndCondition() {
        Clan initiatorClan = clanHandler.getClanById(initiatorClanId);
        Clan targetClan = clanHandler.getClanById(targetClanId);
        boolean lostWar = true;
        for (ClanChunk chunk : initiatorClan.getChunks()) {
            if (!chunk.getIsLost()) {
                lostWar = false;
                break;
            }
        }
        if (lostWar) {
            loserClanId = initiatorClanId;
            endWar(false);
            return;
        }


        lostWar = true;
        for (ClanChunk chunk : targetClan.getChunks()) {
            if (!chunk.getIsLost()) {
                lostWar = false;
                break;
            }
        }
        if (lostWar) {
            loserClanId = targetClanId;
            endWar(false);
        }

    }

    public void declareWar() {
        Log("Declaring war.");
        resetChunkOccupationStates();
        state = WarState.DECLARED;
        warHandler.saveWars();
        Clan initiatorClan = clanHandler.getClanById(initiatorClanId);
        Clan targetClan = clanHandler.getClanById(targetClanId);
        for (Player player : initiatorClan.getOnlinePlayers()) {
            Decorator.PlaySound(player, Key.key("minecraft:item.goat_horn.sound.2"), 1);
            Decorator.Broadcast(player, Component.text("War Declared!").color(TextColor.color(255, 0, 0)), "Your clan declared war on " + targetClan.getName(), 5);
        }
        for (Player player : targetClan.getOnlinePlayers()) {
            Decorator.PlaySound(player, Key.key("minecraft:item.goat_horn.sound.2"), 1);
            Decorator.Broadcast(player, Component.text("War Declared!").color(TextColor.color(255, 0, 0)), initiatorClan.getName() + " declared war on your clan", 5);
        }
    }
    public void startWar() {
        Log("Starting war");
        state = WarState.IN_PROGRESS;
        loadTasks();
        warHandler.saveWars();
        Clan initiatorClan = clanHandler.getClanById(initiatorClanId);
        Clan targetClan = clanHandler.getClanById(targetClanId);
        for (Player player : initiatorClan.getOnlinePlayers()) {
            Decorator.PlaySound(player, Key.key("minecraft:entity.ender_dragon.growl"), 1);
            Decorator.Broadcast(player, Component.text("War Started!").color(TextColor.color(255, 0, 0)), Component.text("Chunk protections are now compromised"), 5);
        }
        for (Player player : targetClan.getOnlinePlayers()) {
            Decorator.PlaySound(player, Key.key("minecraft:entity.ender_dragon.growl"), 1);
            Decorator.Broadcast(player, Component.text("War Started!").color(TextColor.color(255, 0, 0)), Component.text("Chunk protections are now compromised"), 5);
        }
    }
    public void endWar(boolean isPeace) {
        Log("Ending war");
        state = WarState.ENDED;
        Clan initiatorClan = clanHandler.getClanById(initiatorClanId);
        Clan targetClan = clanHandler.getClanById(targetClanId);
        initiatorClan.setLastWarTime(System.currentTimeMillis());
        targetClan.setLastWarTime(System.currentTimeMillis());
        resetChunkOccupationStates();
        warHandler.saveWars();

        if (loserClanId != null) {
            Clan loserClan = clanHandler.getClanById(loserClanId);
            Clan winningClan;
            if (loserClan.getId().equals(initiatorClanId)) {
                winningClan = targetClan;
            } else {
                winningClan = initiatorClan;
            }
            for (Player player : loserClan.getOnlinePlayers()) {
                sendFormattedMessage(player, "You lost the war and the clan is gone...", LogType.INFO); // TODO: Make this message configurable
                Decorator.PlaySound(player, Key.key("minecraft:entity.ender_dragon.death"), 1);
                Decorator.Broadcast(player, Component.text("Lost the war.").color(TextColor.color(255, 0, 0)), Component.text("Your clan got disbanded..."), 5);
                Decorator.SummonLightning(player);
            }
            VaultHandler vaultHandler = VClans.getInstance().getVaultHandler();
            int playersOnlineAmount = winningClan.getOnlinePlayers().size();
            int totalSum = VClans.getInstance().getClanTierHandler().getTotalPriceSum(loserClan.getTier());
            double rewardPerPlayer = (double) totalSum / playersOnlineAmount;
            for (Player player : winningClan.getOnlinePlayers()) {
                Decorator.PlaySound(player, Key.key("minecraft:item.goat_horn.sound.0"), 1);
                Decorator.Broadcast(player, Component.text("Won the war!").color(TextColor.color(0, 255, 0)), Component.text("The enemy clan got disbanded"), 5);
                Decorator.SummonFirework(player, Color.GREEN, 1, true, true, FireworkEffect.Type.BALL_LARGE);
                if (rewardPerPlayer > 0) {
                    sendFormattedMessage(player, "You won the war and got a reward of $" + rewardPerPlayer + "!", LogType.INFO); // TODO: Make this message configurable
                    vaultHandler.depositPlayer(player, rewardPerPlayer);
                } else {
                    sendFormattedMessage(player, "You won the war!", LogType.INFO); // TODO: Make this message configurable
                }
            }
            clanHandler.disbandClan(loserClan);
        } else { // No winners or losers
            Key key;
            String message;
            if (isPeace) {
                key = Key.key("minecraft:entity.player.levelup");
                message = "Peace has been achieved.";
            } else {
                key = Key.key("minecraft:item.goat_horn.sound.6");
                message = "";
            }
            for (Player player : initiatorClan.getOnlinePlayers()) {
                sendFormattedMessage(player, "The war ended by time out, no winners or losers.", LogType.INFO); // TODO: Make this message configurable
                Decorator.PlaySound(player, key, 1);
                Decorator.Broadcast(player, Component.text("War is over.").color(TextColor.color(200, 200, 200)), Component.text(message), 5);
            }
            for (Player player : targetClan.getOnlinePlayers()) {
                sendFormattedMessage(player, "The war ended by time out, no winners or losers.", LogType.INFO); // TODO: Make this message configurable
                Decorator.PlaySound(player, key, 1);
                Decorator.Broadcast(player, Component.text("War is over.").color(TextColor.color(200, 200, 200)), Component.text(message), 5);
            }
        }
        VClans.getInstance().getChunkHandler().updateChunkRadarForAll();
    }
    public void loadTasks() {
        if (state != WarState.IN_PROGRESS) return; // do not start the chunk occupation tasks if war isnt active.
        Clan initiatorClan = clanHandler.getClanById(initiatorClanId);
        Clan targetClan = clanHandler.getClanById(targetClanId);
        for (ClanChunk chunk : initiatorClan.getChunks()) {
            scheduler.runTaskTimer(new ChunkOccupationTask(this, initiatorClan, targetClan, chunk), 0, 20);
        }
        for (ClanChunk chunk : targetClan.getChunks()) {
            scheduler.runTaskTimer(new ChunkOccupationTask(this, targetClan, initiatorClan, chunk), 0, 20);
        }
    }

    private void resetChunkOccupationStates() {
        Clan initiatorClan = clanHandler.getClanById(initiatorClanId);
        Clan targetClan = clanHandler.getClanById(targetClanId);
        for (ClanChunk chunk : initiatorClan.getChunks()) {
            if (VClans.getInstance().getChunkHandler().chunkShouldBeSecured(chunk)) {
                chunk.setOccupationState(ChunkOccupationState.SECURED);
            } else {
                chunk.setOccupationState(ChunkOccupationState.CONTROLLED);
            }
            chunk.setOccupationProgress(VClans.getInstance().getWarHandler().CHUNK_HEALTH_POINTS);
        }
        for (ClanChunk chunk : targetClan.getChunks()) {
            if (VClans.getInstance().getChunkHandler().chunkShouldBeSecured(chunk)) {
                chunk.setOccupationState(ChunkOccupationState.SECURED);
            } else {
                chunk.setOccupationState(ChunkOccupationState.CONTROLLED);
            }
            chunk.setOccupationProgress(VClans.getInstance().getWarHandler().CHUNK_HEALTH_POINTS);
        }
    }

    public String getInitiatorClanId() {
        return initiatorClanId;
    }
    public void setInitiatorClanId(String initiatorClanId) {
        this.initiatorClanId = initiatorClanId;
    }
    public String getTargetClanId() {
        return targetClanId;
    }
    public void setTargetClanId(String targetClanId) {
        this.targetClanId = targetClanId;
    }
    public long getDeclarationTime() {
        return declarationTime;
    }
    public void setDeclarationTime(long declarationTime) {
        this.declarationTime = declarationTime;
    }
    public WarState getState() {
        return state;
    }
    public void setState(WarState state) {
        this.state = state;
    }
    public void initHandlers() {
        this.clanHandler = VClans.getInstance().getClanHandler();
        this.scheduler = VClans.getInstance().getTaskScheduler();
        this.warHandler = VClans.getInstance().getWarHandler();
    }
    public PeaceTreaty getPeaceTreaty() {
        return peaceTreaty;
    }
    public void setPeaceTreaty(PeaceTreaty peaceTreaty) {
        this.peaceTreaty = peaceTreaty;
    }
}
