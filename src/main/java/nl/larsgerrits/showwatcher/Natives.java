package nl.larsgerrits.showwatcher;

import com.sun.javafx.PlatformUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.Consumer;

public class Natives
{
    private static final String JLIBTORRENT_VERSION = "1.2.0.18";
    
    private static final String JLIBTORRENT_WINDOWS = "natives/x86_64/jlibtorrent-" + JLIBTORRENT_VERSION + ".dll";
    private static final String JLIBTORRENT_MAC = "natives/x86_64/libjlibtorrent-" + JLIBTORRENT_VERSION + ".dylib";
    private static final String JLIBTORRENT_LINUX = "natives/x86_64/libjlibtorrent-" + JLIBTORRENT_VERSION + ".so";
    
    private Natives() { }
    
    public static void loadNatives()
    {
        try
        {
            String jlibtorrentNatives = getJLibTorrentNativesLocation();
            loadLibraryFromJar(jlibtorrentNatives, s -> System.setProperty("jlibtorrent.jni.path", s));
        }
        catch (Exception e)
        {
            System.out.println("[ERROR] Error loading natives, downloading disabled!");
            e.printStackTrace();
        }
    }
    
    private static String getJLibTorrentNativesLocation()
    {
        if (PlatformUtil.isWindows()) return JLIBTORRENT_WINDOWS;
        if (PlatformUtil.isLinux()) return JLIBTORRENT_LINUX;
        if (PlatformUtil.isMac()) return JLIBTORRENT_MAC;
        return "";
    }
    
    public static void loadLibraryFromJar(String path, Consumer<String> toLoad)
    {
        Path nativesPath = Settings.DEFAULT_PATH.resolve(path);
        
        if (!Files.exists(nativesPath))
        {
            extractNativesFromJar(nativesPath, path);
        }
        
        toLoad.accept(nativesPath.toString());
    }
    
    private static void extractNativesFromJar(Path diskPath, String jarPath)
    {
        try
        {
            Files.createDirectories(diskPath.getParent());
            Files.createFile(diskPath);
            
            try (InputStream is = Natives.class.getResourceAsStream("/" + jarPath))
            {
                Files.copy(is, diskPath, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        catch (IOException | NullPointerException e)
        {
            e.printStackTrace();
        }
    }
}
