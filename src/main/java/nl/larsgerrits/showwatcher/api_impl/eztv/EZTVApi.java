package nl.larsgerrits.showwatcher.api_impl.eztv;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import nl.larsgerrits.showwatcher.gson.eztv.EZTVDeserializer;
import nl.larsgerrits.showwatcher.show.TVEpisode;
import nl.larsgerrits.showwatcher.show.TVShow;
import nl.larsgerrits.showwatcher.download.Torrent;
import nl.larsgerrits.showwatcher.util.HTTPUtils;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class EZTVApi
{
    private static final Gson gson = new GsonBuilder().registerTypeAdapter(List.class, new EZTVDeserializer()).create();
    
    private static Map<TVShow, List<Torrent>> torrentMap = new HashMap<>();
    
    private EZTVApi() {}
    
    @SuppressWarnings("unchecked")
    public static List<Torrent> request(TVEpisode episode)
    {
        List<Torrent> torrents = torrentMap.get(episode.getSeason().getShow());
        if (torrents == null)
        {
            String jsonResponse = HTTPUtils.get("https://eztv.ag/api/get-torrents?imdb_id=" + episode.getSeason().getShow().getImdbId().replace("tt", ""));
            torrents = gson.fromJson(jsonResponse, List.class);
            torrentMap.put(episode.getSeason().getShow(), torrents);
        }
        
        torrents.sort(Comparator.comparing(Torrent::getSeeds));
        
        List<Torrent> episodeTorrents = torrents.stream()//
                                                .filter(t -> t.getSeason() == episode.getSeason().getSeasonNumber())//
                                                .filter(t -> t.getEpisode() == episode.getEpisodeNumber())//
                                                .collect(Collectors.toList());//
        
        if (!episodeTorrents.isEmpty()) return episodeTorrents;
        else
        {
            String testId = "s" + String.format("%02d", episode.getSeason().getSeasonNumber()) + "e" + String.format("%02d", episode.getEpisodeNumber());
            return torrents.stream().filter(t -> t.getTitle().toLowerCase().contains(testId)).collect(Collectors.toList());
        }
    }
}
