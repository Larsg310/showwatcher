package nl.larsgerrits.showwatcher;

import com.google.common.base.Strings;
import nl.larsgerrits.showwatcher.util.FileUtils;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Settings
{
    @Nonnull
    public static Path DEFAULT_PATH;
    
    @Nonnull
    public static List<Path> BASE_PATHS = new ArrayList<>();
    
    @Nonnull
    public static Path CACHE_PATH;
    
    @Nonnull
    public static Path COLLECTIONS_PATH;
    
    static
    {
        DEFAULT_PATH = Paths.get(System.getProperty("user.home")).resolve(Reference.SHOW_WATCHER_MAP);
        
        Path settingsPath = DEFAULT_PATH.resolve(Reference.SETTINGS_FILE);
        Map<String, String> settings = readSettings(settingsPath);
        
        if (settings.containsKey("basePath"))
        {
            String basePath = settings.get("basePath");
            String[] basePaths = basePath.split(";");
            for (String path : basePaths)
            {
                if (!Strings.isNullOrEmpty(path))
                {
                    BASE_PATHS.add(Paths.get(path));
                }
            }
            
            // BASE_PATH = Paths.get(settings.get("basePath"));
        }
        
        CACHE_PATH = BASE_PATHS.get(0).resolve(Reference.CACHE_MAP);
        COLLECTIONS_PATH = BASE_PATHS.get(0).resolve(Reference.COLLECTIONS_MAP);
    }
    
    private static Map<String, String> readSettings(Path path)
    {
        Map<String, String> settings = new HashMap<>();
        if (Files.exists(path))
        {
            try
            {
                Files.lines(path, Charset.forName("UTF-8")).filter(s -> !s.trim().startsWith("#")).forEach(s -> parseLineToSettings(s, settings));
            }
            catch (IOException e) { e.printStackTrace();}
        }
        else
        {
            if (Files.notExists(path)) FileUtils.write(path, "basePath=" + Settings.DEFAULT_PATH);
        }
        return settings;
    }
    
    private static void parseLineToSettings(String setting, Map<String, String> settings)
    {
        String[] keyValue = setting.split("=");
        settings.put(keyValue[0].trim(), keyValue[1].trim());
    }
    
    private Settings() { }
}
