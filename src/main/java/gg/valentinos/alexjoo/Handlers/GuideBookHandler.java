package gg.valentinos.alexjoo.Handlers;

import gg.valentinos.alexjoo.Data.LogType;
import gg.valentinos.alexjoo.VClans;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static gg.valentinos.alexjoo.VClans.Log;

public class GuideBookHandler {

    private final HashMap<String, Book> books;
    private final MiniMessage mm;
    private boolean booksReady;

    public GuideBookHandler() {
        this.books = new HashMap<>();
        this.mm = MiniMessage.miniMessage();
        this.booksReady = loadBooks();
    }

    public void openBook(Player player, String id) {
        Book book = books.get(id);
        if (book == null) return;
        player.openBook(book);
    }
    public Book getBook(String id) {
        return books.get(id);
    }
    public List<String> getBookKeys() {
        return new ArrayList<>(books.keySet());
    }
    public boolean areBooksReady() {
        return booksReady;
    }
//    public void reloadBooks() {
//        VClans.getInstance().reloadConfig();
//        this.booksReady = loadBooks();
//    }

    private boolean loadBooks() {
        ConfigurationSection config = VClans.getInstance().getConfig().getConfigurationSection("guide-books");
        if (config == null) return false;
        String title = config.getString("title") == null ? "Guide Book" : config.getString("title");
        String author = config.getString("author") == null ? "Valentinos" : config.getString("author");
        Log("Title: " + title);

        if (title == null) {
            Log("guide-books.title config not found", LogType.SEVERE);
            return false;
        }
        if (author == null) {
            Log("guide-books.author config not found", LogType.SEVERE);
            return false;
        }
        ConfigurationSection booksSection = config.getConfigurationSection("books");
        if (booksSection == null) {
            Log("guide-books.books config not found", LogType.SEVERE);
            return false;
        }
        Set<String> bookKeys = booksSection.getKeys(false);
        if (bookKeys.isEmpty()) {
            Log("no chapters found", LogType.SEVERE);
            return false;
        }

        Component titleComponent = mm.deserialize(title);
        Component authorComponent = mm.deserialize(author);

        for (String key : bookKeys) {
            Book book = createBook(key, titleComponent, authorComponent);
            if (book == null) {
                Log("Couldn't load the book guide-books.books." + key, LogType.SEVERE);
                return false;
            }
            books.put(key, book);
        }

        if (!books.containsKey("navigation")) {
            Log("guide-books.books.navigation config not found", LogType.SEVERE);
            return false;
        }
        return true;
    }
    private Book createBook(String bookKey, Component title, Component author) {
        List<String> pages = VClans.getInstance().getConfig().getStringList("guide-books.books." + bookKey);
        if (pages.isEmpty()) {
            Log("guide-books.books." + bookKey + " has no pages", LogType.SEVERE);
            return null;
        }
        List<Component> pageComponents = new ArrayList<>();
        for (String page : pages) {
            Component pageComponent = mm.deserialize(page);
            pageComponents.add(pageComponent);
        }
        return Book.book(title, author, pageComponents);
    }
}
