package gg.valentinos.alexjoo.Utility;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import gg.valentinos.alexjoo.Data.Clan;
import gg.valentinos.alexjoo.Data.Cooldown;
import gg.valentinos.alexjoo.Data.PlayerCooldownsMap;
import gg.valentinos.alexjoo.VClans;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class JsonUtils {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static void checkDataFolder() {
        File dataFolder = VClans.getInstance().getDataFolder();
        if (!dataFolder.exists()) {
            VClans.getInstance().getLogger().info("Data folder missing, creating one now at " + dataFolder.getAbsolutePath());
            dataFolder.mkdirs(); // Ensure the directory exists
        }
    }

    private static File readJsonFile(String fileName){
        checkDataFolder();

        File jsonFile = new File(VClans.getInstance().getDataFolder(), fileName);
        if (!jsonFile.exists()) {
            try {
                jsonFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return jsonFile;
    }

    public static void toJsonFile(Object object, String fileName) {
        checkDataFolder();

        File jsonFile = new File(VClans.getInstance().getDataFolder(), fileName);

        try (FileWriter writer = new FileWriter(jsonFile)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(object, writer); // Serialize the object and write it to the file
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static PlayerCooldownsMap deserializeCooldowns(String fileName) {
        Type type = new TypeToken<Map<String, List<Cooldown>>>() {
        }.getType();
        FileReader reader = null;
        Map<String, List<Cooldown>> tempMap;
        try {
            reader = new FileReader(readJsonFile(fileName));
            tempMap = gson.fromJson(reader, type);
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        PlayerCooldownsMap playerCooldownsMap = new PlayerCooldownsMap();
        playerCooldownsMap.setPlayerCooldownsMap(new HashMap<>());

        if (tempMap != null) {
            for (Map.Entry<String, List<Cooldown>> entry : tempMap.entrySet()) {
                UUID playerId = UUID.fromString(entry.getKey());
                HashSet<Cooldown> cooldowns = new HashSet<>(entry.getValue());
                playerCooldownsMap.getPlayerCooldownsMap().put(playerId, cooldowns);
            }
        }

        return playerCooldownsMap;
    }

    public static List<Clan> deserializeClans(String fileName) {
        Type type = new TypeToken<List<Clan>>() {
        }.getType();
        FileReader reader = null;
        List<Clan> clans;
        try {
            reader = new FileReader(readJsonFile(fileName));
            clans = gson.fromJson(reader, type);
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return clans;
    }
}

