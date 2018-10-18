package nl.larsgerrits.showwatcher;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public final class Settings
{
    @Nonnull
    public static Path BASE_PATH;
    @Nonnull
    public static Path CACHE_PATH;
    
    static
    {
        Path settingsPath = Paths.get(System.getProperty("user.home")).resolve("showwatcher").resolve("_settings.txt");
        Map<String, String> settings = readSettings(settingsPath);
        
        if (settings.containsKey("basePath")) BASE_PATH = Paths.get(settings.get("basePath"));
        else BASE_PATH = Paths.get(System.getProperty("user.home")).resolve("showwatcher");
        
        CACHE_PATH = BASE_PATH.resolve("_cache");
    
        System.out.println(BASE_PATH);
        //        System.out.println(System.getProperty("user.home"));
        //        System.getProperties().list(System.out);
        //        System.out.println();
    }
    
    private static Map<String, String> readSettings(Path path)
    {
        Map<String, String> settings = new HashMap<>();
        try
        {
            Files.lines(path, Charset.forName("UTF-8")).forEach(s -> parseSetting(s, settings));
        }
        catch (IOException ignored) { }
        return settings;
    }
    
    private static void parseSetting(String setting, Map<String, String> settings)
    {
        String[] keyValue = setting.split("=");
        settings.put(keyValue[0], keyValue[1]);
    }
    
    private Settings() { }
}
