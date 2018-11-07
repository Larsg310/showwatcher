package nl.larsgerrits.showwatcher.api_impl;

import nl.larsgerrits.showwatcher.api_impl.ettv.ETTVApi;
import nl.larsgerrits.showwatcher.api_impl.eztv.EZTVApi;
import nl.larsgerrits.showwatcher.api_impl.piratebay.PirateBayApi;
import nl.larsgerrits.showwatcher.show.TVEpisode;
import nl.larsgerrits.showwatcher.show.Torrent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class TorrentCollector
{
    private static final List<Function<TVEpisode, List<Torrent>>> torrentCollectors = new ArrayList<>();
    
    static
    {
        torrentCollectors.add(EZTVApi::request);
        torrentCollectors.add(ETTVApi::request);
        torrentCollectors.add(PirateBayApi::request);
    }
    
    public static Torrent getTorrent(TVEpisode episode)
    {
        for (Function<TVEpisode, List<Torrent>> collector : torrentCollectors)
        {
            List<Torrent> torrents = collector.apply(episode);
            if (torrents != null && !torrents.isEmpty())
            {
                torrents.sort(Comparator.comparing(Torrent::getSeeds));
                System.out.println(torrents.get(0).getMagnetUrl());
                return torrents.get(0);
            }
        }
        return null;
    }
}
