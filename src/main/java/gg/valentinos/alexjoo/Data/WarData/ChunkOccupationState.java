package gg.valentinos.alexjoo.Data.WarData;

public enum ChunkOccupationState {
    SECURED, // Owned by defending clan, and cannot be captured.
    CONTROLLED, // Owned by defending clan, but can be captured (because at least one of the adjacent chunks is not owned by defending clan or is captured by attacking clan).
    CAPTURING, // Currently controlled by defending clan, but being captured by attacking clan.
    CONTESTED, // Is either being CAPTURING or LIBERATING, but both clans have presence in the chunk and therefore progress is halted.
    CAPTURED, // Owned by the attacking clan.
    LIBERATING, // Owned by attacking clan, but being liberated by defending clan. When fully liberated, it returns to CONTROLLED/SECURED state.
}
