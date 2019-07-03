package nl.larsgerrits.showwatcher.download;

import nl.larsgerrits.showwatcher.api_impl.torrent.eztv.EZTVApi;
import nl.larsgerrits.showwatcher.api_impl.torrent.kat.KickassApi;
import nl.larsgerrits.showwatcher.api_impl.torrent.piratebay.PirateBayApi;
import nl.larsgerrits.showwatcher.show.TVEpisode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TorrentCollector
{
    private static final List<Function<TVEpisode, List<Torrent>>> torrentCollectors = new ArrayList<>();
    
    static
    {
        torrentCollectors.add(KickassApi::request);
        torrentCollectors.add(EZTVApi::request);
        // torrentCollectors.add(ETTVApi::request);
        torrentCollectors.add(PirateBayApi::request);
        
    }
    
    public static Torrent getTorrent(TVEpisode episode)
    {
        List<Torrent> torrents = torrentCollectors.stream().flatMap(f -> f.apply(episode).stream()).collect(Collectors.toList());
        if (!torrents.isEmpty())
        {
            System.out.print(torrents.size() + " ");
            torrents.sort(Comparator.comparing(Torrent::getSeedsPlusPeers).reversed());
            System.out.println(torrents.get(0).getMagnetUrl());
            return torrents.get(0);
        }
        
        return null;
    }
}
