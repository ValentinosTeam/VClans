package gg.valentinos.alexjoo.Utility;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import gg.valentinos.alexjoo.VClans;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class JsonUtils {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public JsonUtils() {
        File dataFolder = VClans.getInstance().getDataFolder();
        if (!dataFolder.exists()) {
            // Ensure the data folder exists
            dataFolder.mkdirs();
        }
        VClans.getInstance().getLogger().info("1. Data folder: " + dataFolder.getAbsolutePath());
    }

    private static String toJson(Object object) {
        return JsonParser.parseString(new Gson().toJson(object)).getAsJsonObject().getAsString();
    }

    public static void toJsonFile(Object object, String fileName) {
        File dataFolder = VClans.getInstance().getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs(); // Ensure the directory exists
        }
        VClans.getInstance().getLogger().info("2. Data folder: " + dataFolder.getAbsolutePath());

        File jsonFile = new File(dataFolder, fileName);

        try (FileWriter writer = new FileWriter(jsonFile)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(object, writer); // Serialize the object and write it to the file
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static <T> T fromJsonFile(String fileName, Class<T> clazz) {
        File dataFolder = VClans.getInstance().getDataFolder();
        if (!dataFolder.exists()) {
            // Ensure the data folder exists
            dataFolder.mkdirs();
        }
        VClans.getInstance().getLogger().info("3. Data folder: " + dataFolder.getAbsolutePath());

        File jsonFile = new File(dataFolder, fileName);
        if (!jsonFile.exists()) {
            try {
                jsonFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (FileReader reader = new FileReader(jsonFile)) {
            return new Gson().fromJson(reader, clazz);
        } catch (IOException e) {
            e.printStackTrace();
            return null; // Return null or handle the error appropriately
        }
    }


}

