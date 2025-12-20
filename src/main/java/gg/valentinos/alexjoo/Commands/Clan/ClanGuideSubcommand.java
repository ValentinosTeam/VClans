package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.CommandAction;
import gg.valentinos.alexjoo.Commands.SubCommand;
import gg.valentinos.alexjoo.Data.LogType;
import gg.valentinos.alexjoo.Handlers.GuideBookHandler;
import gg.valentinos.alexjoo.VClans;
import net.kyori.adventure.key.Key;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ClanGuideSubcommand extends SubCommand {

    private final GuideBookHandler guideBookHandler;

    public ClanGuideSubcommand() {
        super("clan", "guide", List.of("not-a-book", "no-book"));
        maxArgs = 2;
        hasToBePlayer = true;
        successSound = Key.key("minecraft:item.book.page_turn");
        successVolume = 1;
        this.guideBookHandler = VClans.getInstance().getGuideBookHandler();
    }

    @Override
    public CommandAction getAction(CommandSender sender, String[] args) {
        return () -> {
            String chapter = "navigation";
            if (args.length == 2) {
                chapter = args[1];
            }
            //TODO: TESTING ------------------
            guideBookHandler.reloadBooks();
            //TODO: TESTING ------------------
            Player player = (Player) sender;
            guideBookHandler.openBook(player, chapter);
        };
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            if (guideBookHandler.areBooksReady()) return List.of("guide");
        } else if (args.length == 2) {
            if (guideBookHandler.areBooksReady()) return guideBookHandler.getBookKeys();
        }
        return List.of();
    }

    @Override
    protected boolean hasSpecificErrors(CommandSender sender, String[] args) {
        if (!guideBookHandler.areBooksReady()) {
            sendFormattedPredefinedMessage(sender, "no-book", LogType.WARNING);
            return true;
        }

        String chapter = "navigation";
        if (args.length == 2) {
            chapter = args[1];

            if (!guideBookHandler.getBookKeys().contains(chapter)) {
                sendFormattedPredefinedMessage(sender, "not-a-book", LogType.WARNING);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean suggestCommand(CommandSender sender) {
        return true;
    }

    @Override
    protected void loadReplacementValues(CommandSender sender, String[] args) {
    }

}