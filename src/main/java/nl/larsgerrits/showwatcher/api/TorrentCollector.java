package nl.larsgerrits.showwatcher.api;

import nl.larsgerrits.showwatcher.api.eztv.EZTVApi;
import nl.larsgerrits.showwatcher.api.piratebay.PirateBayApi;
import nl.larsgerrits.showwatcher.show.TVEpisode;
import nl.larsgerrits.showwatcher.show.TVShow;
import nl.larsgerrits.showwatcher.show.Torrent;

import java.util.*;

public class TorrentCollector
{
    private static final Map<TVShow, List<Torrent>> showTorrentCache = new HashMap<>();
    
    public static Torrent getTorrent(TVEpisode episode)
    {
        List<Torrent> torrents = showTorrentCache.get(episode.getSeason().getTVShow());
        if (torrents == null)
        {
            torrents = EZTVApi.request(episode.getSeason().getTVShow());
            // torrents.addAll(PirateBayApi.request(episode));
            showTorrentCache.put(episode.getSeason().getTVShow(), torrents);
        }
        torrents.sort(Comparator.comparing(Torrent::getSeeds));
        
        // for (Torrent torrent : torrents)
        // {
        //     if (torrent.getSeason() == episode.getSeason().getSeasonNumber() && torrent.getEpisode() == episode.getEpisodeNumber())
        //     {
        //         return torrent;
        //     }
        //     if (torrent.getSeason() == 0 && torrent.getEpisode() == 0)
        //     {
        //         String testId = "s" + String.format("%02d", episode.getSeason().getSeasonNumber()) + "e" + String.format("%02d", episode.getEpisodeNumber());
        //         System.out.println(testId);
        //         if (torrent.getTitle().toLowerCase().contains(testId))
        //         {
        //             return torrent;
        //         }
        //     }
        
        Optional<Torrent> optionalTorrent = torrents.stream()//
                                                    .filter(torrent -> torrent.getSeason() == episode.getSeason().getSeasonNumber())//
                                                    .filter(torrent -> torrent.getEpisode() == episode.getEpisodeNumber())//
                                                    .max(Comparator.comparing(Torrent::getSeeds));
        
        return optionalTorrent.orElse(PirateBayApi.request(episode));
    }
}
