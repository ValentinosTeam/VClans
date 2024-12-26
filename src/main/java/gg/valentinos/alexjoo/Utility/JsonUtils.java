package gg.valentinos.alexjoo.Utility;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import gg.valentinos.alexjoo.VClans;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class JsonUtils {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static String toJson(Object object) {
        return JsonParser.parseString(new Gson().toJson(object)).getAsJsonObject().getAsString();
    }

    public static void toJsonFile(Object object, String fileName) {
        File jsonFile = new File(VClans.getInstance().getDataFolder(), fileName);
        String json = JsonParser.parseString(new Gson().toJson(object)).getAsJsonObject().getAsString();
        try (FileWriter writer = new FileWriter(jsonFile)) {
            writer.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static <T> T fromJsonFile(String fileName, Class<T> clazz) {

        File jsonFile = new File(VClans.getInstance().getDataFolder(), fileName);
        if (!jsonFile.exists()) {
            try {
                jsonFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return gson.fromJson(jsonFile.toString(), clazz);
    }


}

