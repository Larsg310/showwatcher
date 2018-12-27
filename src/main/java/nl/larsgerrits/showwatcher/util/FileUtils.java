package nl.larsgerrits.showwatcher.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import nl.larsgerrits.showwatcher.Reference;
import nl.larsgerrits.showwatcher.Settings;
import nl.larsgerrits.showwatcher.data.EpisodeData;
import nl.larsgerrits.showwatcher.data.SeasonData;
import nl.larsgerrits.showwatcher.gson.show.ShowCollectionDeserializer;
import nl.larsgerrits.showwatcher.show.TVEpisodeCollection;
import nl.larsgerrits.showwatcher.show.TVEpisode;
import nl.larsgerrits.showwatcher.show.TVSeason;

import javax.annotation.Nonnull;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FileUtils
{
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Gson COLLECTION_GSON = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(TVEpisodeCollection.class, new ShowCollectionDeserializer()).create();
    
    private static final Pattern DIACRITICS_AND_FRIENDS = Pattern.compile("[\\p{InCombiningDiacriticalMarks}\\p{IsLm}\\p{IsSk}]+");
    
    public static void loadShowsFromDisk(@Nonnull BiFunction<Path, SeasonData, TVSeason> seasonDataConsumer)
    {
        try (DirectoryStream<Path> showPaths = Files.newDirectoryStream(Settings.BASE_PATH))
        {
            for (Path path : showPaths)
            {
                if (Files.exists(path.resolve(Reference.SEASON_INFO_FILE)))
                {
                    SeasonData data = parseSeasonData(path);
                    if (data != null)
                    {
                        boolean result = checkSeasonData(data);
                        TVSeason season = seasonDataConsumer.apply(path, data);
                        if (result) season.setDirty(true);
                    }
                }
            }
        }
        catch (IOException e) { e.printStackTrace();}
    }
    
    private static boolean checkSeasonData(SeasonData data)
    {
        Path seasonPath = Settings.BASE_PATH.resolve(String.format("%s_season_%d", getSimplifiedName(data.getTitle()), data.getSeason()));
        boolean result = false;
        
        if (data.getEpisodeData() != null)
        {
            for (EpisodeData episode : data.getEpisodeData())
            {
                if (!Files.exists(seasonPath.resolve(episode.getFileName())))
                {
                    episode.setFileName("");
                    result = true;
                }
                String correctFileName = getEpisodeFileName(episode.getEpisode(), episode.getTitle());
                if (Files.exists(seasonPath.resolve(correctFileName)))
                {
                    episode.setFileName(correctFileName);
                    result = true;
                }
            }
        }
        Path folderJpg = seasonPath.resolve("folder.jpg");
        
        try
        {
            if (Files.notExists(folderJpg))
            {
                Path imgPng = Settings.CACHE_PATH.resolve(data.getImdbId() + "_" + data.getSeason() + ".png");
                
                if (Files.exists(imgPng))
                {
                    Files.copy(imgPng, folderJpg);
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        
        return result;
    }
    
    public static List<TVEpisodeCollection> loadShowCollectionsFromDisk()
    {
        List<TVEpisodeCollection> showCollections = new ArrayList<>();
        
        if (!Files.exists(Settings.COLLECTIONS_PATH)) return showCollections;
        
        try (DirectoryStream<Path> showPaths = Files.newDirectoryStream(Settings.COLLECTIONS_PATH))
        {
            for (Path path : showPaths)
            {
                String json = Files.lines(path, Charset.forName("UTF-8")).collect(Collectors.joining());
                TVEpisodeCollection collection = COLLECTION_GSON.fromJson(json, TVEpisodeCollection.class);
                showCollections.add(collection);
            }
        }
        catch (IOException e) { e.printStackTrace();}
        
        return showCollections;
    }
    
    private static SeasonData parseSeasonData(Path path)
    {
        try
        {
            String content = new String(Files.readAllBytes(path.resolve(Reference.SEASON_INFO_FILE)));
            if (!content.isEmpty())
            {
                return GSON.fromJson(content, SeasonData.class);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }
    
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static Path writeSeasonToDisk(TVSeason season)
    {
        List<EpisodeData> episodeData = new ArrayList<>();
        for (TVEpisode episode : season.getEpisodes())
        {
            episodeData.add(new EpisodeData(episode.getEpisodeNumber(), episode.getTitle(), episode.getVideoFile() == null ? "" : episode.getVideoFile().getFileName().toString(), episode.getReleaseDate() == null ? 0L : episode.getReleaseDate().getTime(), episode.getWatched().get()));
        }
        SeasonData data = new SeasonData(season.getShow().getTitle(), season.getShow().getImdbId(), season.getSeasonNumber(), season.getTotalEpisodes(), season.getReleaseDate() == null ? 0L : season.getReleaseDate().getTime(), episodeData);
        
        File dir = new File(Settings.BASE_PATH + File.separator + getSimplifiedName(season.getShow().getTitle()) + "_season_" + season.getSeasonNumber());
        
        if (!dir.exists()) dir.mkdir();
        
        Path path = dir.toPath().resolve(Reference.SEASON_INFO_FILE);
        try (FileWriter writer = new FileWriter(path.toFile()))
        {
            writer.write(GSON.toJson(data));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return path;
    }
    
    public static String getSimplifiedName(String filename)
    {
        return stripDiacritics(filename.toLowerCase()).replace(' ', '_')//
                                                      .replace('-', '_')//
                                                      .replaceAll("[^a-zA-Z_\\d]+", "")//
                                                      .replaceAll("_{2,}", "_");
    }
    
    private static String stripDiacritics(String str)
    {
        str = Normalizer.normalize(str, Normalizer.Form.NFD);
        str = DIACRITICS_AND_FRIENDS.matcher(str).replaceAll("");
        return str;
    }
    
    public static void saveIdMap(Map<String, Integer> imdbToTmdb)
    {
        
        write(Settings.CACHE_PATH.resolve(Reference.ID_MAP_FILE), GSON.toJson(imdbToTmdb));
    }
    
    @SuppressWarnings("unchecked")
    public static void loadIdMap(Map<String, Integer> imdbToTmdb)
    {
        try
        {
            Map<String, Double> jsonMap = GSON.fromJson(Files.lines(Settings.CACHE_PATH.resolve(Reference.ID_MAP_FILE), Charset.forName("UTF-8")).collect(Collectors.joining()), Map.class);
            for (Map.Entry<String, Double> entry : jsonMap.entrySet())
            {
                imdbToTmdb.put(entry.getKey(), entry.getValue().intValue());
            }
        }
        catch (IOException e) {e.printStackTrace(); }
    }
    
    public static void write(Path path, String content)
    {
        try
        {
            if (Files.notExists(path)) Files.createFile(path);
            try (BufferedWriter writer = Files.newBufferedWriter(path, Charset.forName("UTF-8")))
            {
                writer.write(content);
            }
        }
        catch (IOException e) {e.printStackTrace(); }
    }
    
    public static Path getSaveDir(TVEpisode episode)
    {
        return Settings.BASE_PATH.resolve(String.format("%s_season_%d", getSimplifiedName(episode.getSeason().getShow().getTitle()), episode.getSeason().getSeasonNumber()));
    }
    
    public static String getEpisodeFileName(int episode, String title)
    {
        return String.format("episode_%02d_%s.mkv", episode, getSimplifiedName(title));
    }
}