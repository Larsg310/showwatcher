package nl.larsgerrits.showwatcher.manager;

import com.google.common.base.Strings;
import javafx.application.Platform;
import nl.larsgerrits.showwatcher.Threading;
import nl.larsgerrits.showwatcher.api_impl.info.trakt.TraktApi;
import nl.larsgerrits.showwatcher.api_impl.info.trakt.TraktEpisode;
import nl.larsgerrits.showwatcher.api_impl.info.trakt.TraktSeason;
import nl.larsgerrits.showwatcher.data.EpisodeData;
import nl.larsgerrits.showwatcher.data.SeasonData;
import nl.larsgerrits.showwatcher.show.TVEpisode;
import nl.larsgerrits.showwatcher.show.TVSeason;
import nl.larsgerrits.showwatcher.show.TVShow;
import nl.larsgerrits.showwatcher.util.FileUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

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
                TVEpisode episode = new TVEpisode(episodeData.getTitle(), episodeData.getEpisode(), Strings.isNullOrEmpty(episodeData.getFileName()) ? null : path.resolve(episodeData.getFileName()), date, season, episodeData.isWatched());
                season.addEpisode(episode, false);
            }
        }
        show.addSeason(season, false);
        return season;
    }
    
    public static void checkForNewUpdates(TVShow show)
    {
        Threading.API_THREAD.execute(() -> {
            System.out.println("Updating " + show.getTitle());
            List<TraktSeason> tmdbSeasons = TraktApi.getSeasons(show);
            for (TraktSeason season : tmdbSeasons)
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
                        if (tvSeason.getTotalEpisodes() != season.getEpisodeCount())
                        {
                            tvSeason.setTotalEpisodes(season.getEpisodeCount());
                        }
                        // if(tvSeason.)
                    }
                }
            }
            for (TVSeason season : show)
            {
                if (!season.isFullyDownloaded())
                {
                    List<TraktEpisode> tmdbEpisodes = TraktApi.getEpisodes(season);
                    for (TraktEpisode episode : tmdbEpisodes)
                    {
                        if (!season.hasEpisode(episode.getEpisodeNumber()))
                        {
                            TVEpisode tvEpisode = new TVEpisode(episode.getTitle(), episode.getEpisodeNumber(), null, episode.getReleaseDate(), season, false);
                            season.addEpisode(tvEpisode, true);
                            // season.setDirty(true);
                        }
                        else
                        {
                            TVEpisode tvEpisode = season.getEpisode(episode.getEpisodeNumber());
                            
                            tvEpisode.setReleaseDate(episode.getReleaseDate());
                            tvEpisode.setTitle(episode.getTitle());
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
        tvShows.stream().flatMap(show -> show.getSeasons().stream())//
               .filter(TVSeason::isDirty)//
               .filter(s -> s.getPath() != null).forEach(ShowManager::saveSeasonToDisk);
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
    
    public static void searchShows(String search, Consumer<TVShow> addShow)
    {
        if (Strings.isNullOrEmpty(search))
        {
            Threading.SEARCH_THREAD.execute(() -> {
                for (int page = 0; page < 1; page++)
                {
                    List<TVShow> popular = TraktApi.getPopularShows(page);
                    Set<String> existingIds = ShowManager.getTVShowMap().keySet();
                    popular.stream().filter(s -> !existingIds.contains(s.getImdbId())).forEach(s -> Platform.runLater(() -> addShow.accept(s))
                    );
                }
            });
        }
    }
}