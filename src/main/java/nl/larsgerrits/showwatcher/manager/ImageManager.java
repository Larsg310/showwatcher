package nl.larsgerrits.showwatcher.manager;

import javafx.application.Platform;
import javafx.scene.image.Image;
import nl.larsgerrits.showwatcher.Settings;
import nl.larsgerrits.showwatcher.Threading;
import nl.larsgerrits.showwatcher.api_impl.tmdb.TMDBApi;
import nl.larsgerrits.showwatcher.show.TVEpisode;
import nl.larsgerrits.showwatcher.show.TVSeason;
import nl.larsgerrits.showwatcher.show.TVShow;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public final class ImageManager
{
    private static final String IMAGE_URL = "https://image.tmdb.org/t/p/w780";
    private static final Image DEFAULT_IMAGE = new Image("@../../image/default.png");
    private static final Image DEFAULT_IMAGE_WIDE = new Image("@../../image/default_wide.png");
    
    private ImageManager() {}
    
    public static void getPosterURLForTVShow(TVShow show, @Nonnull Consumer<Image> callback)
    {
        Path path = Settings.CACHE_PATH.resolve(String.format("%s.png", show.getImdbId()));
        
        if (Files.exists(path))
        {
            callback.accept(new Image("file:" + path));
        }
        else
        {
            callback.accept(DEFAULT_IMAGE);
            Threading.IMAGE_THREAD.execute(() -> {
                String posterPath = TMDBApi.getShowPoster(show);
                downloadPoster(callback, path, posterPath);
            });
        }
    }
    
    public static void getPosterURLForTVSeason(@Nonnull TVSeason season, @Nonnull Consumer<Image> callback)
    {
        Path path = Settings.CACHE_PATH.resolve(String.format("%s_%d.png", season.getShow().getImdbId(), season.getSeasonNumber()));
        
        if (Files.exists(path))
        {
            callback.accept(new Image("file:" + path));
        }
        else
        {
            callback.accept(DEFAULT_IMAGE);
            
            Threading.IMAGE_THREAD.execute(() -> {
                String posterPath = TMDBApi.getSeasonPoster(season);
                downloadPoster(callback, path, posterPath);
            });
        }
    }
    
    public static void getPosterURLForTVEpisode(TVEpisode episode, @Nonnull Consumer<Image> callback)
    {
        Path path = Settings.CACHE_PATH.resolve(String.format("%s_%d_%d.png", episode.getSeason().getShow().getImdbId(), episode.getSeason().getSeasonNumber(), episode.getEpisodeNumber()));
        
        if (Files.exists(path))
        {
            callback.accept(new Image("file:" + path));
        }
        else
        {
            callback.accept(DEFAULT_IMAGE_WIDE);
            
            Threading.IMAGE_THREAD.execute(() -> {
                String posterPath = TMDBApi.getEpisodePoster(episode);
                downloadPoster(callback, path, posterPath);
            });
        }
    }
    
    private static void downloadPoster(@Nonnull Consumer<Image> onDownloadCallback, Path path, String posterPath)
    {
        if (StringUtils.isNotEmpty(posterPath))
        {
            boolean downloaded = saveImage(IMAGE_URL + posterPath, path.toString());
            Platform.runLater(() -> {
                if (downloaded) onDownloadCallback.accept(new Image("file:" + path));
                else onDownloadCallback.accept(DEFAULT_IMAGE_WIDE);
            });
        }
    }
    
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static boolean saveImage(String webUrl, String dest)
    {
        try
        {
            URL url = new URL(webUrl);
            InputStream is = url.openStream();
            
            if (!Files.exists(Settings.CACHE_PATH)) Files.createDirectory(Settings.CACHE_PATH);
            
            File file = new File(dest);
            file.createNewFile();
            OutputStream os = new FileOutputStream(dest);
            
            byte[] b = new byte[2048];
            int length;
            
            while ((length = is.read(b)) != -1) os.write(b, 0, length);
            
            is.close();
            os.close();
            
            return true;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
    }
}
