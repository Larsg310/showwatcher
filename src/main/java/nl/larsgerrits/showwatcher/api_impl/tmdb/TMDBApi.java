package nl.larsgerrits.showwatcher.api_impl.tmdb;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import nl.larsgerrits.showwatcher.gson.TMDBEpisodeDeserializer;
import nl.larsgerrits.showwatcher.gson.TMDBSeasonDeserializer;
import nl.larsgerrits.showwatcher.show.TVSeason;
import nl.larsgerrits.showwatcher.show.TVShow;
import nl.larsgerrits.showwatcher.util.HTTPUtils;

import java.util.*;

public final class TMDBApi
{
    private static final Gson seasonGson = new GsonBuilder().registerTypeAdapter(List.class, new TMDBSeasonDeserializer()).create();
    private static final Gson episodeGson = new GsonBuilder().registerTypeAdapter(List.class, new TMDBEpisodeDeserializer()).create();
    private static final Map<TVShow, List<TMDBSeason>> showSeasonCache = new HashMap<>();
    private static final Map<TVSeason, List<TMDBEpisode>> seasonEpisodeCache = new HashMap<>();
    
    private TMDBApi() {}
    
    public static List<TMDBSeason> getSeasons(TVShow show)
    {
        List<TMDBSeason> tmdbSeasons = showSeasonCache.get(show);
        if (tmdbSeasons == null)
        {
            tmdbSeasons = requestSeasons(show);
            showSeasonCache.put(show, tmdbSeasons);
        }
        return Optional.ofNullable(tmdbSeasons).orElseGet(ArrayList::new);
    }
    
    @SuppressWarnings("unchecked")
    private static List<TMDBSeason> requestSeasons(TVShow show)
    {
        int tmdbId = IDMapper.getTmdbId(show.getImdbId());
        String jsonResponse = HTTPUtils.get("https://api.themoviedb.org/3/tv/" + tmdbId + "?api_key=83fed95ccc330d5b194e5039d40387d6");
        return seasonGson.fromJson(jsonResponse, List.class);
    }
    
    public static List<TMDBEpisode> getEpisodes(TVSeason season)
    {
        List<TMDBEpisode> tmdbEpisodes = seasonEpisodeCache.get(season);
        if (tmdbEpisodes == null)
        {
            tmdbEpisodes = requestEpisodes(season);
            seasonEpisodeCache.put(season, tmdbEpisodes);
        }
        return Optional.ofNullable(tmdbEpisodes).orElseGet(ArrayList::new);
    }
    
    @SuppressWarnings("unchecked")
    private static List<TMDBEpisode> requestEpisodes(TVSeason season)
    {
        int tmdbId = IDMapper.getTmdbId(season.getTVShow().getImdbId());
        String jsonResponse = HTTPUtils.get("https://api.themoviedb.org/3/tv/" + tmdbId + "/season/" + season.getSeasonNumber() + "?api_key=83fed95ccc330d5b194e5039d40387d6");
        return episodeGson.fromJson(jsonResponse, List.class);
    }
}
