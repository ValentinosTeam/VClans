package gg.valentinos.alexjoo.Listeners;

import gg.valentinos.alexjoo.Data.Clan;
import gg.valentinos.alexjoo.Data.ClanMember;
import gg.valentinos.alexjoo.VClans;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ChatListener implements Listener, ChatRenderer {
    private Chat chat;
    private String format;

    public ChatListener(Chat chat) {
        this.chat = chat;
        format = VClans.getInstance().getConfig().getString("settings.chat-format");
        //TODO: lets add minimessage support
        if (format == null) format = "[{prefix}] <{name}>: ";
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        if (chat == null) return;
        Player sender = event.getPlayer();

        // if using clan chat, only clan members are recipients
        Clan clan = VClans.getInstance().getVaultHandler().getClanChat(sender);
        if (clan != null) {
            HashMap<UUID, ClanMember> clanMembers = clan.getMembers();

            Set<Audience> recipients = event.viewers();
            recipients.clear();

            for (Map.Entry<UUID, ClanMember> clanMemberEntry : clanMembers.entrySet()) {
                UUID uuid = clanMemberEntry.getKey();
                Player player = Bukkit.getPlayer(uuid);
                if (player == null || !player.isOnline()) continue;

                recipients.add(player);
            }
        }
//        event.renderer(this); // Tell the event to use our renderer
    }

    @Override
    public @NotNull Component render(@NotNull Player source, @NotNull Component sourceDisplayName, @NotNull Component message, @NotNull Audience viewer) {
        String prefix = chat.getPlayerPrefix(source);

        Component displayName = Component.text("")
                .append(Component.text(prefix, NamedTextColor.GRAY))
                .append(sourceDisplayName);


        // Final message format: DisplayName: Message
        return Component.text("")
                .append(displayName)
                .append(Component.text(": ", NamedTextColor.GRAY))
                .append(message);
    }
}
