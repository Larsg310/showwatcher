package nl.larsgerrits.showwatcher.api.piratebay;

import nl.larsgerrits.showwatcher.show.TVEpisode;
import nl.larsgerrits.showwatcher.show.Torrent;
import nl.larsgerrits.showwatcher.util.FileUtils;

import java.util.List;

public final class PirateBayApi
{
    private PirateBayApi() {}
    
    @SuppressWarnings("unchecked")
    public static Torrent request(TVEpisode e)
    {
        Query query = constructQuery(e);
        System.out.println(query.TranslateToUrl());
        List<Torrent> torrents = PirateBayScraper.search(query, e.getSeason().getSeasonNumber(), e.getEpisodeNumber());
        if (torrents.size() > 0) return torrents.get(0);
        return null;
    }
    
    private static Query constructQuery(TVEpisode episode)
    {
        return new Query(FileUtils.fixFileName(episode.getSeason().getTVShow().getTitle()).replace("_", "%20") + "%20s" + String.format("%02d", episode.getSeason().getSeasonNumber()) + "e" + String.format("%02d", episode.getEpisodeNumber()), 0);
    }
    
}
