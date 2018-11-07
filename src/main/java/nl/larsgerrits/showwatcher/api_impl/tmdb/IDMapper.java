package nl.larsgerrits.showwatcher.api_impl.tmdb;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import nl.larsgerrits.showwatcher.gson.TMDBFindDeserializer;
import nl.larsgerrits.showwatcher.show.TVEpisode;
import nl.larsgerrits.showwatcher.show.TVSeason;
import nl.larsgerrits.showwatcher.show.TVShow;
import nl.larsgerrits.showwatcher.util.FileUtils;
import nl.larsgerrits.showwatcher.util.HTTPUtils;

import java.util.HashMap;
import java.util.Map;

public class IDMapper
{
    private static final Gson findGson = new GsonBuilder().registerTypeAdapter(Integer.class, new TMDBFindDeserializer()).create();
    
    private static Map<String, Integer> imdbToTmdb = new HashMap<>();
    
    static {
        FileUtils.loadIdMap(imdbToTmdb);
    }
    
    public static void close()
    {
        FileUtils.saveIdMap(imdbToTmdb);
    }
    
    public static int getTmdbId(TVEpisode episode){
        return getTmdbId(episode.getSeason());
    }

    public static int getTmdbId(TVSeason season){
        return getTmdbId(season.getShow());
    }

    public static int getTmdbId(TVShow show){
        return getTmdbId(show.getImdbId());
    }
    
    public static int getTmdbId(String imdbId)
    {
        int tmdbId = imdbToTmdb.getOrDefault(imdbId, -1);
        if (tmdbId == -1)
        {
            tmdbId = request(imdbId);
            imdbToTmdb.put(imdbId, tmdbId);
        }
        return tmdbId;
    }
    
    @SuppressWarnings("unchecked")
    private static int request(String imdbId)
    {
        String jsonResponse = HTTPUtils.get("https://api.themoviedb.org/3/find/" + imdbId + "?api_key=83fed95ccc330d5b194e5039d40387d6&external_source=imdb_id");
        
        if (!Strings.isNullOrEmpty(jsonResponse))
        {
            return findGson.fromJson(jsonResponse, Integer.class);
        }
        return -1;
    }
}
