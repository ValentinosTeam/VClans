package gg.valentinos.alexjoo.Handlers;

import com.github.sirblobman.combatlogx.api.event.PlayerPreTagEvent;
import com.github.sirblobman.combatlogx.api.object.TagType;
import gg.valentinos.alexjoo.Data.ClanData.Clan;
import gg.valentinos.alexjoo.VClans;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

public class CombatLogXHandler implements Listener {

    public CombatLogXHandler() {
    }

    @EventHandler
    public void onPlayerPreTag(PlayerPreTagEvent event) {
        if (event.getTagType() != TagType.PLAYER) return;
        Player attacker = (Player) event.getEnemy();
        Player target = event.getPlayer();
        if (attacker == null) return;
        Clan attackerClan = VClans.getInstance().getClanHandler().getClanByMember(attacker.getUniqueId());
        if (attackerClan != null && attackerClan.isPlayerMember(target.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    public boolean isEnabled() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        return pluginManager.isPluginEnabled("CombatLogX");
    }
}
