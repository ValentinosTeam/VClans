package gg.valentinos.alexjoo.Utility;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gg.valentinos.alexjoo.VClans;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class JsonUtils {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static void checkDataFolder() {
        File dataFolder = VClans.getInstance().getDataFolder();
        if (!dataFolder.exists()) {
            VClans.getInstance().getLogger().info("Data folder missing, creating one now at " + dataFolder.getAbsolutePath());
            dataFolder.mkdirs(); // Ensure the directory exists
        }
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

    public static <T> T fromJsonFile(String fileName, Class<T> clazz) {
        checkDataFolder();

        File jsonFile = new File(VClans.getInstance().getDataFolder(), fileName);
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

