package nl.larsgerrits.showwatcher.manager;

import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.model.tv.TvEpisode;
import info.movito.themoviedbapi.model.tv.TvSeries;
import nl.larsgerrits.showwatcher.Settings;
import nl.larsgerrits.showwatcher.Threading;
import nl.larsgerrits.showwatcher.api.tmdb.IDMapper;
import nl.larsgerrits.showwatcher.show.TVEpisode;
import nl.larsgerrits.showwatcher.show.TVShow;
import nl.larsgerrits.showwatcher.util.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import static nl.larsgerrits.showwatcher.util.FileUtils.*;

public final class ImageManager
{
    private static final String DEFAULT_IMAGE = "@../../image/default.png";
    private static final String DEFAULT_IMAGE_WIDE = "@../../image/default_wide.png";
    private static TmdbApi TMDB_API = new TmdbApi("83fed95ccc330d5b194e5039d40387d6");
    
    public ImageManager()
    {
    }
    
    public static String getPosterURLForTVShow(TVShow show, Consumer<String> onDownloadCallback)
    {
        Path path = Settings.CACHE_PATH.resolve(show.getImdbId() + ".png");
        
        if (Files.exists(path)) return "file:" + path;
        
        Threading.IMAGE_THREAD.execute(() -> {
            
            int tmdbId = IDMapper.getTmdbId(show.getImdbId());
            try
            {
                TvSeries tvSeries = TMDB_API.getTvSeries().getSeries(tmdbId, "");
                
                if (StringUtils.isNotEmpty(tvSeries.getPosterPath()))
                {
                    String webUrl = "https://image.tmdb.org/t/p/w780" + tvSeries.getPosterPath();
                    
                    boolean downloaded = saveImage(webUrl, path.toString());
                    if (onDownloadCallback != null)
                    {
                        if (downloaded) onDownloadCallback.accept("file:" + path);
                        else onDownloadCallback.accept(DEFAULT_IMAGE_WIDE);
                    }
                }
            }
            catch (Exception ignored) { }
        });
        
        return DEFAULT_IMAGE;
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
    
    // public static String getPosterURLForTVSeason(TVSeason season, Consumer<String> onDownloadCallback)
    // {
    //     String path = FileUtils.CACHE_DIRECTORY.getPath() + File.separator + season.getTVShow().getImdbId() + "_" + season.getSeasonNumber() + ".png";
    //
    //     if (Files.exists(Paths.get(path))) return "file:" + path;
    //
    //     Threading.IMAGE_THREAD.execute(() -> {
    //
    //         int tmdbId = IMDB_TO_TMDB.getOrDefault(season.getTVShow().getImdbId(), -1);
    //         if (tmdbId <= -1)
    //         {
    //             FindResults result = TMDB_API.getFind().find(season.getTVShow().getImdbId(), TmdbFind.ExternalSource.imdb_id, "en-US");
    //             tmdbId = result.getTvResults().get(0).getId();
    //             IMDB_TO_TMDB.put(season.getTVShow().getImdbId(), tmdbId);
    //         }
    //         try
    //         {
    //             TvSeason tvSeason = TMDB_API.getTvSeasons().getSeason(tmdbId, season.getSeasonNumber(), "en", TmdbTvSeasons.SeasonMethod.images);
    //
    //             Optional<Artwork> artwork = tvSeason.getImages().getPosters().stream().max(Comparator.comparing(Artwork::getVoteCount));
    //             if (artwork.isPresent())
    //             {
    //                 String webUrl = "https://image.tmdb.org/t/p/w780" + artwork.get().getFilePath();
    //
    //                 // System.out.println(webUrl);
    //                 boolean downloaded = saveImage(webUrl, path);
    //                 if (onDownloadCallback != null)
    //                 {
    //                     if (downloaded) onDownloadCallback.accept("file:" + path);
    //                     else onDownloadCallback.accept(DEFAULT_IMAGE);
    //                 }
    //             }
    //         }
    //         catch (ResponseStatusException ignored) { }
    //     });
    //
    //     return DEFAULT_IMAGE;
    // }
    //
    public static String getPosterURLForTVEpisode(TVEpisode episode, Consumer<String> onDownloadImage)
    {
        Path path = Settings.CACHE_PATH.resolve(episode.getSeason().getTVShow().getImdbId() + "_" + episode.getSeason().getSeasonNumber() + "_" + episode.getEpisodeNumber() + ".png");
        
        if (Files.exists(path)) return "file:" + path;
        
        Threading.IMAGE_THREAD.execute(() -> {
            int tmdbId = IDMapper.getTmdbId(episode.getSeason().getTVShow().getImdbId());
            try
            {
                TvEpisode tvEpisode = TMDB_API.getTvEpisodes().getEpisode(tmdbId, episode.getSeason().getSeasonNumber(), episode.getEpisodeNumber(), "en-US");
                
                String stillPath = tvEpisode.getStillPath();
                if (StringUtils.isNotEmpty(stillPath))
                {
                    String webUrl = "https://image.tmdb.org/t/p/w780" + stillPath;
                    
                    // System.out.println(webUrl);
                    boolean downloaded = saveImage(webUrl, path.toString());
                    if (onDownloadImage != null)
                    {
                        if (downloaded) onDownloadImage.accept("file:" + path);
                        else onDownloadImage.accept(DEFAULT_IMAGE_WIDE);
                    }
                }
            }
            catch (Exception ignored) { }
        });
        
        return DEFAULT_IMAGE_WIDE;
    }
}
