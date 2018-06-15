package sieciowe.programowanie;

import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ConfigReader {
    private final static String path = "App.config";

    public static String getValue(String key) throws IOException {
        var file = openFile();
        var jsonObject = new JSONObject(file);
        return jsonObject.get(key).toString();
    }

    private static String openFile() throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)));
    }

}
