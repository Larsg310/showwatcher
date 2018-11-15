package nl.larsgerrits.showwatcher.manager;

import com.google.common.base.Strings;
import javafx.application.Platform;
import nl.larsgerrits.showwatcher.Settings;
import nl.larsgerrits.showwatcher.Threading;
import nl.larsgerrits.showwatcher.api_impl.trakt.TraktApi;
import nl.larsgerrits.showwatcher.api_impl.trakt.TraktEpisode;
import nl.larsgerrits.showwatcher.cache.DescriptionCache;
import nl.larsgerrits.showwatcher.show.TVEpisode;
import nl.larsgerrits.showwatcher.show.TVShow;
import nl.larsgerrits.showwatcher.util.FileUtils;

import java.util.function.Consumer;

public final class DescriptionManager
{
    private static final String NO_DESCRIPTION = "No description found currently...";
    
    private DescriptionManager() {}
    
    public static void getShowDescription(TVShow show, Consumer<String> callback)
    {
        if (DescriptionCache.hasShowDescription(show))
        {
            callback.accept(DescriptionCache.getShowDescription(show));
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
        String description;
        if (DescriptionCache.hasEpisodeDescription(episode) && !Strings.isNullOrEmpty(description = DescriptionCache.getEpisodeDescription(episode)))
        {
            callback.accept(description);
        }
        else
        {
            callback.accept(NO_DESCRIPTION);
            Threading.IMAGE_THREAD.execute(() -> {
                TraktEpisode e = TraktApi.getEpisode(episode.getSeason(), episode.getEpisodeNumber());
                if (e != null && !Strings.isNullOrEmpty(e.getOverview()))
                {
                    Platform.runLater(() -> callback.accept(e.getOverview()));
                    DescriptionCache.addEpisodeDescription(episode, e.getOverview());
                }
            });
        }
    }
    
    public static void close()
    {
        // List<TVEpisode> episodes = ShowManager.getTVShows().stream().flatMap(s -> s.getSeasons().stream()).flatMap(s -> s.getEpisodes().stream()).collect(Collectors.toList());
        // Threading.DOWNLOAD_THREAD.execute(() -> episodes.forEach(e -> {
        //     getEpisodeDescription(e, s -> {}, episodes.size());
        //     FileUtils.write(Settings.CACHE_PATH.resolve("desc.json"), DescriptionCache.toJsonString());
        // }));
        FileUtils.write(Settings.CACHE_PATH.resolve("desc.json"), DescriptionCache.toJsonString());
    }
    
}
