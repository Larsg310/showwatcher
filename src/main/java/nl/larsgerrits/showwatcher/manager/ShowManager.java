package nl.larsgerrits.showwatcher.manager;

import nl.larsgerrits.showwatcher.Threading;
import nl.larsgerrits.showwatcher.api_impl.tmdb.TMDBApi;
import nl.larsgerrits.showwatcher.api_impl.tmdb.TMDBEpisode;
import nl.larsgerrits.showwatcher.api_impl.tmdb.TMDBSeason;
import nl.larsgerrits.showwatcher.data.EpisodeData;
import nl.larsgerrits.showwatcher.data.SeasonData;
import nl.larsgerrits.showwatcher.show.TVEpisode;
import nl.larsgerrits.showwatcher.show.TVSeason;
import nl.larsgerrits.showwatcher.show.TVShow;
import nl.larsgerrits.showwatcher.util.FileUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ShowManager
{
    private static Map<String, TVShow> tvShowMap = new HashMap<>();
    private static List<TVShow> tvShows = new ArrayList<>();
    
    public ShowManager()
    {
    }
    
    public static List<TVShow> getTVShows()
    {
        if (tvShows.isEmpty())
        {
            FileUtils.loadShowsFromDisk(ShowManager::onSeasonDataLoad);
        }
        return tvShows;
    }
    
    public static Map<String, TVShow> getTVShowMap()
    {
        if (tvShowMap.isEmpty())
        {
            FileUtils.loadShowsFromDisk(ShowManager::onSeasonDataLoad);
        }
        return tvShowMap;
    }
    
    private static TVSeason onSeasonDataLoad(Path path, SeasonData seasonData)
    {
        TVShow show = tvShowMap.get(seasonData.getImdbId());
        if (show == null)
        {
            show = new TVShow(seasonData.getTitle(), seasonData.getImdbId());
            tvShowMap.put(seasonData.getImdbId(), show);
            tvShows.add(show);
            tvShows.sort(Comparator.comparing(TVShow::getTitle));
        }
        
        TVSeason season = new TVSeason(show, seasonData.getSeason(), path, seasonData.getTotalEpisodes(), seasonData.getReleaseDate());
        if (seasonData.getEpisodeData() != null)
        {
            for (EpisodeData episodeData : seasonData.getEpisodeData())
            {
                Date date = new Date(episodeData.getReleaseDate());
                TVEpisode episode = new TVEpisode(episodeData.getTitle(), episodeData.getEpisode(), episodeData.getFileName(), date, season, episodeData.isWatched());
                season.addEpisode(episode, false);
            }
            //            System.out.println(season.getTVShow().getTitle() + ": " + season.getSeasonNumber());
        }
        show.addSeason(season, false);
        return season;
    }
    
    public static void checkForNewUpdates(TVShow show)
    {
        Threading.LOADING_THREAD.execute(() -> {
            List<TMDBSeason> tmdbSeasons = TMDBApi.getSeasons(show);
            for (TMDBSeason season : tmdbSeasons)
            {
                if (season.getSeasonNumber() != 0)
                {
                    if (!show.hasSeason(season.getSeasonNumber()))
                    {
                        TVSeason tvSeason = new TVSeason(show, season.getSeasonNumber(), null, season.getEpisodeCount(), season.getReleaseDate());
                        show.addSeason(tvSeason, true);
                    }
                    else
                    {
                        TVSeason tvSeason = show.getSeason(season.getSeasonNumber());
                        if (tvSeason.getReleaseDate().getTime() == 0)
                        {
                            tvSeason.setReleaseDate(season.getReleaseDate());
                        }
                    }
                }
            }
            for (TVSeason season : show)
            {
                if (!season.isFullyDownloaded())
                {
                    List<TMDBEpisode> tmdbEpisodes = TMDBApi.getEpisodes(season);
                    for (TMDBEpisode episode : tmdbEpisodes)
                    {
                        if (!season.hasEpisode(episode.getEpisodeNumber()))
                        {
                            TVEpisode tvEpisode = new TVEpisode(episode.getTitle(), episode.getEpisodeNumber(), "", episode.getReleaseDate(), season, false);
                            season.addEpisode(tvEpisode, true);
                        }
                    }
                }
            }
        });
    }
    
    public static void saveSeasonToDisk(TVSeason season)
    {
        Path path = FileUtils.writeSeasonToDisk(season);
        season.setPath(path);
    }
    
    public static void close()
    {
        tvShows.stream()//
               .flatMap(show -> show.getSeasons().stream())//
               .filter(TVSeason::isDirty)//
               .forEach(ShowManager::saveSeasonToDisk);
    }
    
    @Nullable
    public static TVShow getShow(String imdbId)
    {
        
        return getTVShowMap().get(imdbId);
    }
    
    @Nonnull
    public static List<TVShow> getShows(String... imdbIds)
    {
        List<TVShow> shows = new ArrayList<>();
        for (String imdbId : imdbIds)
        {
            TVShow show = getTVShowMap().get(imdbId);
            if (show != null) shows.add(show);
        }
        return shows;
    }
}
