package nl.larsgerrits.showwatcher.api_impl;

import nl.larsgerrits.showwatcher.api_impl.eztv.EZTVApi;
import nl.larsgerrits.showwatcher.api_impl.piratebay.PirateBayApi;
import nl.larsgerrits.showwatcher.show.TVEpisode;
import nl.larsgerrits.showwatcher.show.TVShow;
import nl.larsgerrits.showwatcher.show.Torrent;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class TorrentCollector
{
    private static final Map<TVShow, List<Torrent>> showTorrentCache = new HashMap<>();
    
//    private static final Predicate<Torrent> t = t -> t.getTitle().toLowerCase().matches("s(\\d\\d)e(\\d\\d)");
    
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
        
        System.out.println(torrents);
        
        Optional<Torrent> optionalTorrent = torrents.stream()//
                                                    .filter(torrent -> torrent.getSeason() == episode.getSeason().getSeasonNumber())//
                                                    .filter(torrent -> torrent.getEpisode() == episode.getEpisodeNumber())//
                                                    .max(Comparator.comparing(Torrent::getSeeds));
        
        if (optionalTorrent.isPresent()) return optionalTorrent.get();
        else
        {
            String testId = "s" + String.format("%02d", episode.getSeason().getSeasonNumber()) + "e" + String.format("%02d", episode.getEpisodeNumber());
            optionalTorrent = torrents.stream().filter(t -> t.getTitle().toLowerCase().contains(testId)).max(Comparator.comparing(Torrent::getSeeds));
            return optionalTorrent.orElseGet(() -> PirateBayApi.request(episode));
        }
    }
}
