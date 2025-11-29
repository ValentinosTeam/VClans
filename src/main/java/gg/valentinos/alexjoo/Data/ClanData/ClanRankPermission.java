package gg.valentinos.alexjoo.Data.ClanData;

import java.util.HashMap;
import java.util.Map;

public enum ClanRankPermission {

    CAN_DISBAND("canDisband"),
    CAN_INVITE("canInvite"),
    CAN_KICK("canKick"),
    CAN_EDIT_RANK("canEditRank"),
    CAN_CREATE_RANK("canCreateRank"),
    CAN_DELETE_RANK("canDeleteRank"),
    CAN_CHANGE_RANK("canChangeRank"),
    CAN_CLAIM_CHUNKS("canClaimChunks"),
    CAN_UNCLAIM_CHUNKS("canUnclaimChunks"),
    CAN_COLOR("canColor"),
    CAN_UPGRADE("canUpgrade"),
    CAN_DOWNGRADE("canDowngrade"),
    CAN_SET_PREFIX("canSetPrefix"),
    CAN_RENAME("canRename"),
    CAN_DECLARE_WAR("canDeclareWar"),
    CAN_OFFER_PEACE("canOfferPeace");

    private final String key;

    ClanRankPermission(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }

    private static final Map<String, ClanRankPermission> BY_KEY = new HashMap<>();

    static {
        for (ClanRankPermission perm : values()) {
            BY_KEY.put(perm.key, perm);
        }
    }

    public static ClanRankPermission fromKey(String key) {
        return BY_KEY.get(key);
    }
}
/*
!clanHandler.hasPermission(player, ClanRankPermission.)

 */