package gg.valentinos.alexjoo.Listeners;

import gg.valentinos.alexjoo.Data.ClanData.Clan;
import gg.valentinos.alexjoo.Data.ClanData.ClanMember;
import gg.valentinos.alexjoo.VClans;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
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

import static gg.valentinos.alexjoo.VClans.Log;

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

        Log("Player: " + chat.getPlayerPrefix(sender) + " " + sender.getName());

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
            Component original = event.message();
            Component modified = Component.text("[Clan Chat] ", TextColor.color(clan.getColor().get(0), clan.getColor().get(1), clan.getColor().get(2)))
                    .append(original.color(NamedTextColor.GRAY));
            event.message(modified);
        }

//        event.renderer(this); // this will override the default chat renderer
    }

    @Override
    public @NotNull Component render(@NotNull Player source, @NotNull Component sourceDisplayName, @NotNull Component message, @NotNull Audience viewer) {
        Chat chat = VClans.getInstance().getVaultHandler().getChat();
        if (chat == null) return Component.empty();

        // Get prefix
        String prefixText = chat.getPlayerPrefix(null, source);
        if (prefixText == null) prefixText = "";

        // Define custom RGB color
        TextColor prefixColor = TextColor.color(125, 125, 125);
        if (!prefixText.isEmpty()) {
            Clan clan = VClans.getInstance().getClanHandler().getClanByMember(source.getUniqueId());
            prefixColor = TextColor.color(clan.getColor().get(0), clan.getColor().get(1), clan.getColor().get(2));
        }


        // Build [prefix] with color
        Component coloredPrefix = Component.text(prefixText).color(prefixColor);
        Component prefixWrapped = Component.text("[")
                .color(NamedTextColor.GRAY)
                .append(coloredPrefix)
                .append(Component.text("]", NamedTextColor.GRAY));

        // Build <name> with color
        Component nameComponent = Component.text("<" + source.getName() + ">", NamedTextColor.WHITE);

        // Separator :
        Component separator = Component.text(": ", NamedTextColor.GRAY);

        // Final format: [prefix] <name>: message
        return prefixWrapped
                .append(Component.space())
                .append(nameComponent)
                .append(separator)
                .append(message);
    }
}
