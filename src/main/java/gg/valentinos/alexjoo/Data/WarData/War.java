package gg.valentinos.alexjoo.Data.WarData;

import gg.valentinos.alexjoo.Data.ClanData.Clan;
import gg.valentinos.alexjoo.Data.ClanData.ClanChunk;
import gg.valentinos.alexjoo.Data.LogType;
import gg.valentinos.alexjoo.Handlers.ClanHandler;
import gg.valentinos.alexjoo.Handlers.WarHandler;
import gg.valentinos.alexjoo.Utility.TaskScheduler;
import gg.valentinos.alexjoo.VClans;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

import static gg.valentinos.alexjoo.VClans.Log;
import static gg.valentinos.alexjoo.VClans.sendFormattedMessage;

public class War {
    private String initiatorClanId;
    private String targetClanId;
    private long declarationTime;
    private WarState state;
    private String loserClanId = null;

    private final ClanHandler clanHandler;
    private final TaskScheduler scheduler;
    private final WarHandler warHandler;

    public War(Clan initiatorClan, Clan targetClan) {
        this.initiatorClanId = initiatorClan.getId();
        this.targetClanId = targetClan.getId();
        this.declarationTime = System.currentTimeMillis();
        this.state = WarState.DECLARED;

        this.clanHandler = VClans.getInstance().getClanHandler();
        this.scheduler = VClans.getInstance().getTaskScheduler();
        this.warHandler = VClans.getInstance().getWarHandler();
        Log("Created a new war between " + initiatorClan.getName() + " and " + targetClan.getName());
    }

    public void changeState(WarState newState) {
        switch (newState) {
            case DECLARED:
                declareWar();
                break;
            case IN_PROGRESS:
                startWar();
                break;
            case ENDED:
                endWar();
                break;
            default:
                break;
        }
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
            endWar();
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
            endWar();
        }

    }

    private void declareWar() {
        Log("Declaring war.");
        resetChunkOccupationStates();
        state = WarState.DECLARED;
        scheduler.runTaskLater(this::startWar, warHandler.GRACE_PERIOD * 20);
    }
    private void startWar() {
        if (state != WarState.DECLARED) return;
        Log("Starting war");
        state = WarState.IN_PROGRESS;
        Clan initiatorClan = clanHandler.getClanById(initiatorClanId);
        Clan targetClan = clanHandler.getClanById(targetClanId);
        for (ClanChunk chunk : initiatorClan.getChunks()) {
            scheduler.runTaskTimer(new ChunkOccupationTask(this, initiatorClan, targetClan, chunk), 0, 20);
        }
        for (ClanChunk chunk : targetClan.getChunks()) {
            scheduler.runTaskTimer(new ChunkOccupationTask(this, targetClan, initiatorClan, chunk), 0, 20);
        }
        scheduler.runTaskLater(this::endWar, warHandler.WAR_DURATION * 20);
    }
    private void endWar() {
        if (state != WarState.IN_PROGRESS) return;
        Log("Ending war");
        state = WarState.ENDED;
        Clan initiatorClan = clanHandler.getClanById(initiatorClanId);
        Clan targetClan = clanHandler.getClanById(targetClanId);
        initiatorClan.setLastWarTime(System.currentTimeMillis());
        targetClan.setLastWarTime(System.currentTimeMillis());
        resetChunkOccupationStates();

        if (loserClanId != null) {
            Clan loserClan = clanHandler.getClanById(loserClanId);
            for (UUID uuid : loserClan.getMemberUUIDs()) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    sendFormattedMessage(player, "You lost the war and the clan is gone...", LogType.INFO); // TODO: Make this message configurable
                }
            }
            // TODO: message the winners
            clanHandler.disbandClan(loserClan);
            VClans.getInstance().getChunkHandler().updateChunkRadarForAll();
            //TODO: Compensate winner with money
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
            chunk.setOccupationProgress(VClans.getInstance().getWarHandler().MAX_DEFENCE_HP);
        }
        for (ClanChunk chunk : targetClan.getChunks()) {
            if (VClans.getInstance().getChunkHandler().chunkShouldBeSecured(chunk)) {
                chunk.setOccupationState(ChunkOccupationState.SECURED);
            } else {
                chunk.setOccupationState(ChunkOccupationState.CONTROLLED);
            }
            chunk.setOccupationProgress(VClans.getInstance().getWarHandler().MAX_DEFENCE_HP);
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

}
