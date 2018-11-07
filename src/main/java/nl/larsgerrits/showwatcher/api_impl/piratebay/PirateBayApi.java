package nl.larsgerrits.showwatcher.api_impl.piratebay;

import nl.larsgerrits.showwatcher.show.TVEpisode;
import nl.larsgerrits.showwatcher.show.Torrent;
import nl.larsgerrits.showwatcher.util.FileUtils;

import java.util.List;

public final class PirateBayApi
{
    private PirateBayApi() {}

    @SuppressWarnings("unchecked")
    public static List<Torrent> request(TVEpisode e)
    {
        Query query = constructQuery(e);
        System.out.println("URL: "+query.TranslateToUrl());
        return PirateBayScraper.search(query, e.getSeason().getSeasonNumber(), e.getEpisodeNumber());
    }

    private static Query constructQuery(TVEpisode episode)
    {
        return new Query(FileUtils.getSimplifiedName(episode.getSeason().getShow().getTitle()).replace("_", "%20") + "%20s" + String.format("%02d", episode.getSeason().getSeasonNumber()) + "e*" + episode.getEpisodeNumber(), 0);
    }

}
