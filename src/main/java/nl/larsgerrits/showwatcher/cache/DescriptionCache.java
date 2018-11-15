package nl.larsgerrits.showwatcher.cache;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import nl.larsgerrits.showwatcher.Settings;
import nl.larsgerrits.showwatcher.show.TVEpisode;
import nl.larsgerrits.showwatcher.show.TVSeason;
import nl.larsgerrits.showwatcher.show.TVShow;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

public class DescriptionCache
{
    public static final String NO_DESCRIPTION = "No description found!";
    
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    private static Map<String, ShowDescription> cache = new HashMap<>();
    
    static
    {
        loadFromFile(Settings.CACHE_PATH.resolve("desc.json"));
    }
    
    public static void addShowDescription(TVShow show, String description)
    {
        if (!hasShowDescription(show))
        {
            cache.put(show.getImdbId(), new ShowDescription(description));
        }
        else
        {
            cache.get(show.getImdbId()).description = description;
        }
    }
    
    public static String getShowDescription(TVShow show)
    {
        if (!hasShowDescription(show)) return "";
        return cache.get(show.getImdbId()).description;
    }
    
    public static boolean hasShowDescription(TVShow show)
    {
        return cache.containsKey(show.getImdbId());
    }
    
    public static void addSeasonDescription(TVSeason season, String description)
    {
        if (!hasShowDescription(season.getShow()))
        {
            addShowDescription(season.getShow(), "");
        }
        
        if (!hasSeasonDescription(season))
        {
            cache.get(season.getShow().getImdbId()).seasons.put(season.getSeasonNumber(), new SeasonDescription(description));
        }
        else
        {
            cache.get(season.getShow().getImdbId()).seasons.get(season.getSeasonNumber()).description = description;
        }
    }
    
    public static String getSeasonDescription(TVSeason season)
    {
        if (!hasSeasonDescription(season)) return NO_DESCRIPTION;
        return cache.get(season.getShow().getImdbId()).seasons.get(season.getSeasonNumber()).description;
    }
    
    public static boolean hasSeasonDescription(TVSeason season)
    {
        return hasShowDescription(season.getShow()) && cache.get(season.getShow().getImdbId()).seasons.containsKey(season.getSeasonNumber());
    }
    
    public static void addEpisodeDescription(TVEpisode episode, String description)
    {
        if (!hasSeasonDescription(episode.getSeason()))
        {
            addSeasonDescription(episode.getSeason(), "");
        }
        cache.get(episode.getSeason().getShow().getImdbId()).seasons.get(episode.getSeason().getSeasonNumber()).episodes.put(episode.getEpisodeNumber(), description);
    }
    
    public static String getEpisodeDescription(TVEpisode episode)
    {
        if (!hasEpisodeDescription(episode)) return NO_DESCRIPTION;
        return cache.get(episode.getSeason().getShow().getImdbId()).seasons.get(episode.getSeason().getSeasonNumber()).episodes.get(episode.getEpisodeNumber());
    }
    
    public static boolean hasEpisodeDescription(TVEpisode episode)
    {
        return hasSeasonDescription(episode.getSeason()) && cache.get(episode.getSeason().getShow().getImdbId()).seasons.get(episode.getSeason().getSeasonNumber()).episodes.containsKey(episode.getEpisodeNumber());
    }
    
    public static String toJsonString()
    {
        
        return GSON.toJson(cache);
    }
    
    public static void loadFromFile(Path path)
    {
        try
        {
            cache = GSON.fromJson(Files.lines(path, UTF_8).collect(Collectors.joining()), new TypeToken<Map<String, ShowDescription>>() {}.getType());
        }
        catch (Exception e)
        {
            // e.printStackTrace();
        }
    }
    
    private static class ShowDescription
    {
        private String description;
        private final Map<Integer, SeasonDescription> seasons = new HashMap<>();
        
        public ShowDescription(String description)
        {
            this.description = description;
        }
    }
    
    private static class SeasonDescription
    {
        private String description;
        private final Map<Integer, String> episodes = new HashMap<>();
        
        public SeasonDescription(String description)
        {
            this.description = description;
        }
    }
}
