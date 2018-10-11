package nl.larsgerrits.showwatcher.manager;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import nl.larsgerrits.showwatcher.show.TVEpisode;
import nl.larsgerrits.showwatcher.show.TVSeason;
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
    
    public static String getShowDescription(TVShow show, Consumer<String> callback)
    {
        if (showDescription.containsKey(show.getImdbId()))
        {
            return showDescription.get(show.getImdbId());
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
        
        return NO_DESCRIPTION;
    }
    
    public static String getSeasonDescription(TVSeason season, Consumer<String> callback)
    {
        if (seasonDescriptionTable.contains(season.getTVShow().getImdbId(), season.getSeasonNumber()))
        {
            return seasonDescriptionTable.get(season.getTVShow().getImdbId(), season.getSeasonNumber());
        }
        
        // Threading.IMAGE_THREAD.execute(() -> {
        //     try
        //     {
        //         Response<List<Season>> response = FileUtils.TRAKT_TV.seasons().summary(season.getTVShow().getImdbId(), Extended.FULL).execute();
        //         if (response.isSuccessful())
        //         {
        //             assert response.body() != null;
        //             for (Season seasonInfo : response.body())
        //             {
        //                 if (seasonInfo.overview != null) seasonDescriptionTable.put(season.getTVShow().getImdbId(), seasonInfo.number, seasonInfo.overview);
        //             }
        //         }
        //     }
        //     catch (IOException e)
        //     {
        //         e.printStackTrace();
        //     }
        //     finally
        //     {
        //         if (seasonDescriptionTable.contains(season.getTVShow().getImdbId(), season.getSeasonNumber()))
        //         {
        //             callback.accept(seasonDescriptionTable.get(season.getTVShow().getImdbId(), season.getSeasonNumber()));
        //         }
        //     }
        //
        // });
        //
        return NO_DESCRIPTION;
    }
    
    public static String getEpisodeDescription(TVEpisode episode, Consumer<String> callback)
    {
        if (episodeDescriptionTable.contains(episode.getSeason().getTVShow().getImdbId() + "_" + episode.getSeason().getSeasonNumber(), episode.getEpisodeNumber()))
        {
            return episodeDescriptionTable.get(episode.getSeason().getTVShow().getImdbId() + "_" + episode.getSeason().getSeasonNumber(), episode.getEpisodeNumber());
        }
        
        // Threading.IMAGE_THREAD.execute(() -> {
        //     try
        //     {
        //         Response<Episode> response = FileUtils.TRAKT_TV.episodes().summary(episode.getSeason().getTVShow().getImdbId(), episode.getSeason().getSeasonNumber(), episode.getEpisodeNumber(), Extended.FULL).execute();
        //         if (response.isSuccessful() && response.body() != null)
        //         {
        //             if (response.body().overview != null)
        //             {
        //                 episodeDescriptionTable.put(episode.getSeason().getTVShow().getImdbId() + "_" + episode.getSeason().getSeasonNumber(), episode.getEpisodeNumber(), response.body().overview);
        //             }
        //         }
        //     }
        //     catch (IOException e)
        //     {
        //         e.printStackTrace();
        //     }
        //     finally
        //     {
        //         if (episodeDescriptionTable.contains(episode.getSeason().getTVShow().getImdbId() + "_" + episode.getSeason().getSeasonNumber(), episode.getEpisodeNumber()))
        //         {
        //             callback.accept(episodeDescriptionTable.get(episode.getSeason().getTVShow().getImdbId() + "_" + episode.getSeason().getSeasonNumber(), episode.getEpisodeNumber()));
        //         }
        //     }
        //
        // });
        
        return NO_DESCRIPTION;
    }
}
