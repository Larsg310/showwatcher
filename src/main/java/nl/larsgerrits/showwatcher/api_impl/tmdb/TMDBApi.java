package nl.larsgerrits.showwatcher.api_impl.tmdb;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import nl.larsgerrits.showwatcher.gson.tmdb.TMDBEpisodePosterDeserializer;
import nl.larsgerrits.showwatcher.gson.tmdb.TMDBSeasonPosterDeserializer;
import nl.larsgerrits.showwatcher.gson.tmdb.TMDBShowPosterDeserializer;
import nl.larsgerrits.showwatcher.show.TVEpisode;
import nl.larsgerrits.showwatcher.show.TVSeason;
import nl.larsgerrits.showwatcher.show.TVShow;
import nl.larsgerrits.showwatcher.util.HTTPUtils;

public final class TMDBApi
{
    private static final Gson showPosterGson = new GsonBuilder().registerTypeAdapter(String.class, new TMDBShowPosterDeserializer()).create();
    private static final Gson seasonPosterGson = new GsonBuilder().registerTypeAdapter(String.class, new TMDBSeasonPosterDeserializer()).create();
    private static final Gson episodePosterGson = new GsonBuilder().registerTypeAdapter(String.class, new TMDBEpisodePosterDeserializer()).create();
    public static final String API_KEY = "?api_key=83fed95ccc330d5b194e5039d40387d6";
    
    private TMDBApi() {}
    
    public static String getShowPoster(TVShow show)
    {
        int tmdbId = IDMapper.getTmdbId(show);
        String jsonResponse = HTTPUtils.get("https://api.themoviedb.org/3/tv/" + tmdbId + API_KEY);
        return showPosterGson.fromJson(jsonResponse, String.class);
    }
    
    public static String getSeasonPoster(TVSeason season)
    {
        int tmdbId = IDMapper.getTmdbId(season);
        String jsonResponse = HTTPUtils.get("https://api.themoviedb.org/3/tv/" + tmdbId + "/season/" + season.getSeasonNumber() + API_KEY + "&append_to_response=images");
        return seasonPosterGson.fromJson(jsonResponse, String.class);
    }
    
    public static String getEpisodePoster(TVEpisode episode)
    {
        int tmdbId = IDMapper.getTmdbId(episode);
        String jsonResponse = HTTPUtils.get("https://api.themoviedb.org/3/tv/" + tmdbId + "/season/" + episode.getSeason().getSeasonNumber() + "/episode/" + episode.getEpisodeNumber() + API_KEY);
        return episodePosterGson.fromJson(jsonResponse, String.class);
    }
}
