package nl.larsgerrits.showwatcher.api_impl.info.trakt;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import nl.larsgerrits.showwatcher.gson.trakt.TraktEpisodeDeserializer;
import nl.larsgerrits.showwatcher.gson.trakt.TraktPopularDeserializer;
import nl.larsgerrits.showwatcher.gson.trakt.TraktSeasonDeserializer;
import nl.larsgerrits.showwatcher.gson.trakt.TraktShowDeserializer;
import nl.larsgerrits.showwatcher.show.TVSeason;
import nl.larsgerrits.showwatcher.show.TVShow;
import nl.larsgerrits.showwatcher.util.HTTPUtils;

import javax.annotation.Nullable;
import java.util.*;

public final class TraktApi
{
    private static final Map<String, String> traktHeaders = new HashMap<>();
    
    private static final Gson showGson = new GsonBuilder().registerTypeAdapter(TraktShow.class, new TraktShowDeserializer()).create();
    private static final Gson seasonGson = new GsonBuilder().registerTypeAdapter(List.class, new TraktSeasonDeserializer()).create();
    private static final Gson episodeGson = new GsonBuilder().registerTypeAdapter(TraktEpisode.class, new TraktEpisodeDeserializer()).create();
    private static final Gson popularGson = new GsonBuilder().registerTypeAdapter(List.class, new TraktPopularDeserializer()).create();
    
    private static final Map<TVShow, TraktShow> showCache = new HashMap<>();
    private static final Map<TVShow, List<TraktSeason>> showSeasonCache = new HashMap<>();
    private static final Map<TVSeason, List<TraktEpisode>> seasonEpisodeCache = new HashMap<>();
    private static final List<TVShow> popular = new ArrayList<>();
    
    static
    {
        traktHeaders.put("Content-Type", "application/json");
        traktHeaders.put("trakt-api-key", "d37bc084cad26a17a8a4ae8bf01eb73262e9ae15823351257f87c335c69f466d");
        traktHeaders.put("trakt-api-version", "2");
    }
    
    private TraktApi() {}
    
    public static TraktShow getShow(TVShow show)
    {
        TraktShow traktShow = showCache.get(show);
        if (traktShow == null)
        {
            traktShow = requestShow(show);
            showCache.put(show, traktShow);
        }
        return traktShow;
    }
    
    @SuppressWarnings("unchecked")
    private static TraktShow requestShow(TVShow show)
    {
        String jsonResponse = HTTPUtils.get("https://api.trakt.tv/shows/" + show.getImdbId() + "?extended=full", traktHeaders);
        return showGson.fromJson(jsonResponse, TraktShow.class);
    }
    
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
    public static TraktSeason getSeason(TVShow show, int season)
    {
        return getSeasons(show).stream().filter(s -> s.getSeasonNumber() == season).findFirst().orElse(null);
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
            String url = "https://api.trakt.tv/shows/" + season.getShow().getImdbId() + "/seasons/" + season.getSeasonNumber() + "/episodes/" + (episode+1) + "?extended=full";
            String jsonResponse = HTTPUtils.get(url, traktHeaders);
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
    
    @SuppressWarnings("unchecked")
    public static List<TVShow> getPopularShows(int page)
    {
        List<TVShow> shows = requestPopular(page);
        if (shows != null) popular.addAll(shows);
        
        return new ArrayList<>(popular);
        
        // if (!popular.isEmpty()) return popular.get(page);
        //
        // List<TVShow> shows = requestPopular();
        // if (shows != null) popular.addAll(shows);
        // return popular;
    }
    
    @SuppressWarnings("unchecked")
    private static List<TVShow> requestPopular(int page)
    {
        return popularGson.fromJson(HTTPUtils.get("https://api.trakt.tv/shows/trending?page=" + (page + 1) + "&limit=100", traktHeaders), List.class);
    }
}
