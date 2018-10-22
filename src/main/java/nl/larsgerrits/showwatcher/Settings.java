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
        Path settingsPath = Paths.get(System.getProperty("user.home")).resolve(Reference.SHOW_WATCHER_MAP).resolve(Reference.SETTINGS_FILE);
        Map<String, String> settings = readSettings(settingsPath);
        
        if (settings.containsKey("basePath")) BASE_PATH = Paths.get(settings.get("basePath"));
        else BASE_PATH = Paths.get(System.getProperty("user.home")).resolve(Reference.SHOW_WATCHER_MAP);
        
        CACHE_PATH = BASE_PATH.resolve(Reference.CACHE_MAP);
        
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
            Files.lines(path, Charset.forName("UTF-8")).filter(s -> !s.trim().startsWith("#")).forEach(s -> parseLineToSettings(s, settings));
        }
        catch (IOException ignored) { }
        return settings;
    }
    
    private static void parseLineToSettings(String setting, Map<String, String> settings)
    {
        String[] keyValue = setting.split("=");
        settings.put(keyValue[0].trim(), keyValue[1].trim());
    }
    
    private Settings() { }
}
