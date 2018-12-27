package nl.larsgerrits.showwatcher.api_impl.torrent.kat;

import nl.larsgerrits.showwatcher.download.Torrent;
import nl.larsgerrits.showwatcher.show.TVEpisode;
import nl.larsgerrits.showwatcher.util.FileUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class KickassApi
{
    public static List<Torrent> request(TVEpisode episode)
    {
        List<Torrent> torrents = new ArrayList<>();
        String query = constructSearch(episode);
        try
        {
            Search search = Search.newSearch(query);
            // System.out.println(search.getSearchURL());
            for (nl.larsgerrits.showwatcher.api_impl.torrent.kat.Torrent t : search)
            {
                Torrent torrent = new Torrent(t.title(), t.magnetLink(), episode.getSeason().getSeasonNumber(), episode.getEpisodeNumber(), t.seeders(), t.leeches(), t.size());
                torrents.add(torrent);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return torrents;
    }
    
    private static String constructSearch(TVEpisode episode)
    {
        return FileUtils.getSimplifiedName(episode.getSeason().getShow().getTitle()).replace("_", " ") + " s" + String.format("%02d", episode.getSeason().getSeasonNumber()) + "e" + String.format("%02d", episode.getEpisodeNumber());
    }
    
}
