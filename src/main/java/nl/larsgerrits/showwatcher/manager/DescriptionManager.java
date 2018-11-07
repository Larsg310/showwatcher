package nl.larsgerrits.showwatcher.manager;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import javafx.application.Platform;
import nl.larsgerrits.showwatcher.Threading;
import nl.larsgerrits.showwatcher.api_impl.trakt.TraktApi;
import nl.larsgerrits.showwatcher.api_impl.trakt.TraktEpisode;
import nl.larsgerrits.showwatcher.show.TVEpisode;
import nl.larsgerrits.showwatcher.show.TVShow;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class DescriptionManager
{
    private static final String NO_DESCRIPTION = "No description found currently...";
    
    private static Map<String, String> showDescription = new HashMap<>();
    private static Table<String, Integer, String> seasonDescriptionTable = HashBasedTable.create();
    private static Table<String, Integer, String> episodeDescriptionTable = HashBasedTable.create();
    
    private DescriptionManager() {}
    
    public static void getShowDescription(TVShow show, Consumer<String> callback)
    {
        if (showDescription.containsKey(show.getImdbId()))
        {
            callback.accept(showDescription.get(show.getImdbId()));
        }
        else
        {
            callback.accept(NO_DESCRIPTION);
        }
    }
    
    // Threading.IMAGE_THREAD.execute(() -> {
    //     try
    //     {
    //         Response<Show> response = FileUtils.TRAKT_TV.shows().summary(show.getImdbId(), Extended.FULL).execute();
    //         if (response.isSuccessful() && response.body() != null)
    //         {
    //             if (response.body().overview != null) showDescription.put(show.getImdbId(), response.body().overview);
    //         }
    //     }
    //     catch (IOException e)
    //     {
    //         e.printStackTrace();
    //     }
    //     finally
    //     {
    //         if (showDescription.containsKey(show.getImdbId()))
    //         {
    //             Platform.runLater(() -> callback.accept(showDescription.get(show.getImdbId())));
    //         }
    //     }
    //
    // });
    
    public static void getEpisodeDescription(TVEpisode episode, Consumer<String> callback)
    {
        String rowKey = episode.getSeason().getShow().getImdbId() + "_" + episode.getSeason().getSeasonNumber();
        
        if (episodeDescriptionTable.contains(rowKey, episode.getEpisodeNumber()))
        {
            callback.accept(episodeDescriptionTable.get(rowKey, episode.getEpisodeNumber()));
        }
        else
        {
            callback.accept(NO_DESCRIPTION);
            Threading.IMAGE_THREAD.execute(() -> {
                TraktEpisode e = TraktApi.getEpisode(episode.getSeason(), episode.getEpisodeNumber());
                if (e != null)
                {
                    Platform.runLater(() -> callback.accept(e.getOverview()));
                    episodeDescriptionTable.put(rowKey, episode.getEpisodeNumber(), e.getOverview());
                }
            });
        }
        
        // Threading.IMAGE_THREAD.execute(() -> {
        //     try
        //     {
        //         Response<Episode> response = FileUtils.TRAKT_TV.episodes().summary(episode.getSeason().getShow().getImdbId(), episode.getSeason().getSeasonNumber(), episode.getEpisodeNumber(), Extended.FULL).execute();
        //         if (response.isSuccessful() && response.body() != null)
        //         {
        //             if (response.body().overview != null)
        //             {
        //                 episodeDescriptionTable.put(episode.getSeason().getShow().getImdbId() + "_" + episode.getSeason().getSeasonNumber(), episode.getEpisodeNumber(), response.body().overview);
        //             }
        //         }
        //     }
        //     catch (IOException e)
        //     {
        //         e.printStackTrace();
        //     }
        //     finally
        //     {
        //         if (episodeDescriptionTable.contains(episode.getSeason().getShow().getImdbId() + "_" + episode.getSeason().getSeasonNumber(), episode.getEpisodeNumber()))
        //         {
        //             callback.accept(episodeDescriptionTable.get(episode.getSeason().getShow().getImdbId() + "_" + episode.getSeason().getSeasonNumber(), episode.getEpisodeNumber()));
        //         }
        //     }
        //
        // });
        
    }
}
