package nl.larsgerrits.showwatcher.api_impl.trakt;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import nl.larsgerrits.showwatcher.gson.trakt.TraktEpisodeDeserializer;
import nl.larsgerrits.showwatcher.gson.trakt.TraktSeasonDeserializer;
import nl.larsgerrits.showwatcher.show.TVSeason;
import nl.larsgerrits.showwatcher.show.TVShow;
import nl.larsgerrits.showwatcher.util.HTTPUtils;

import javax.annotation.Nullable;
import java.util.*;

public final class TraktApi
{
    private static final Map<String, String> traktHeaders = new HashMap<>();
    private static final Gson seasonGson = new GsonBuilder().registerTypeAdapter(List.class, new TraktSeasonDeserializer()).create();
    private static final Gson episodeGson = new GsonBuilder().registerTypeAdapter(TraktEpisode.class, new TraktEpisodeDeserializer()).create();
    private static final Map<TVShow, List<TraktSeason>> showSeasonCache = new HashMap<>();
    private static final Map<TVSeason, List<TraktEpisode>> seasonEpisodeCache = new HashMap<>();
    
    static
    {
        traktHeaders.put("Content-Type", "application/json");
        traktHeaders.put("trakt-api-key", "d37bc084cad26a17a8a4ae8bf01eb73262e9ae15823351257f87c335c69f466d");
        traktHeaders.put("trakt-api-version", "2");
    }
    
    private TraktApi() {}
    
    public static List<TraktSeason> getSeasons(TVShow show)
    {
        List<TraktSeason> traktSeasons = showSeasonCache.get(show);
        if (traktSeasons == null)
        {
            traktSeasons = requestSeasons(show);
            showSeasonCache.put(show, traktSeasons);
        }
        return Optional.ofNullable(traktSeasons).orElseGet(ArrayList::new);
    }
    
    @SuppressWarnings("unchecked")
    private static List<TraktSeason> requestSeasons(TVShow show)
    {
        String jsonResponse = HTTPUtils.get("https://api.trakt.tv/shows/" + show.getImdbId() + "/seasons?extended=full", traktHeaders);
        return seasonGson.fromJson(jsonResponse, List.class);
    }
    
    @Nullable
    public static TraktEpisode getEpisode(TVSeason season, int episode)
    {
        return getEpisodes(season).stream().filter(s -> s.getEpisodeNumber() == episode).findFirst().orElse(null);
    }
    
    public static List<TraktEpisode> getEpisodes(TVSeason season)
    {
        List<TraktEpisode> traktEpisodes = seasonEpisodeCache.get(season);
        if (traktEpisodes == null)
        {
            traktEpisodes = requestEpisodes(season);
            seasonEpisodeCache.put(season, traktEpisodes);
        }
        return Optional.ofNullable(traktEpisodes).orElseGet(ArrayList::new);
    }
    
    @SuppressWarnings("unchecked")
    private static List<TraktEpisode> requestEpisodes(TVSeason season)
    {
        List<TraktEpisode> episodes = new ArrayList<>();
        for (int episode = 0; episode < season.getTotalEpisodes(); episode++)
        {
            String jsonResponse = HTTPUtils.get("https://api.trakt.tv/shows/" + season.getShow().getImdbId() + "/seasons/" + season.getSeasonNumber() + "/episodes/" + episode + "?extended=full", traktHeaders);
            if (!Strings.isNullOrEmpty(jsonResponse))
            {
                TraktEpisode e = episodeGson.fromJson(jsonResponse, TraktEpisode.class);
                if (e != null)
                {
                    episodes.add(e);
                }
            }
        }
        return episodes;
    }
}
