package nl.larsgerrits.showwatcher.api.eztv;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import nl.larsgerrits.showwatcher.show.TVShow;
import nl.larsgerrits.showwatcher.show.Torrent;
import nl.larsgerrits.showwatcher.util.HTTPUtils;

import java.util.List;

public final class EZTVApi
{
    private static final Gson gson = new GsonBuilder().registerTypeAdapter(List.class, new EZTVDeserializer()).create();
    
    private EZTVApi() {}
    
    @SuppressWarnings("unchecked")
    public static List<Torrent> request(TVShow show)
    {
        String jsonResponse = HTTPUtils.GET("https://eztv.ag/api/get-torrents?imdb_id=" + show.getImdbId().replace("tt", ""));
        return gson.fromJson(jsonResponse, List.class);
    }
}
