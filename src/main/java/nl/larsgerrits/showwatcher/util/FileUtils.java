package nl.larsgerrits.showwatcher.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import nl.larsgerrits.showwatcher.Reference;
import nl.larsgerrits.showwatcher.Settings;
import nl.larsgerrits.showwatcher.data.EpisodeData;
import nl.larsgerrits.showwatcher.data.SeasonData;
import nl.larsgerrits.showwatcher.gson.show.ShowCollectionDeserializer;
import nl.larsgerrits.showwatcher.show.TVEpisode;
import nl.larsgerrits.showwatcher.show.TVEpisodeCollection;
import nl.larsgerrits.showwatcher.show.TVSeason;

import javax.annotation.Nonnull;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FileUtils
{
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Gson COLLECTION_GSON = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(TVEpisodeCollection.class, new ShowCollectionDeserializer()).create();
    
    private static final Pattern DIACRITICS_AND_FRIENDS = Pattern.compile("[\\p{InCombiningDiacriticalMarks}\\p{IsLm}\\p{IsSk}]+");
    private static final Pattern EPISODE = Pattern.compile(".*([Ss](\\d{1,2})[Ee](\\d{1,2})).*");
    
    public static void loadShowsFromDisks(@Nonnull BiFunction<Path, SeasonData, TVSeason> seasonDataConsumer)
    {
        for (Path basePath : Settings.BASE_PATHS)
        {
            try (DirectoryStream<Path> showPaths = Files.newDirectoryStream(basePath))
            {
                for (Path path : showPaths)
                {
                    if (Files.exists(path.resolve(Reference.SEASON_INFO_FILE)))
                    {
                        SeasonData data = parseSeasonData(path);
                        if (data != null)
                        {
                            data.setBasePath(basePath);
                            boolean result = checkSeasonData(data);
                            TVSeason season = seasonDataConsumer.apply(path, data);
                            if (result) season.setDirty(true);
                        }
                    }
                }
            }
            catch (IOException e) { e.printStackTrace();}
        }
    }
    
    private static boolean checkSeasonData(SeasonData data)
    {
        Path seasonPath = data.getBasePath().resolve(String.format("%s_season_%d", getSimplifiedName(data.getTitle()), data.getSeason()));
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
                else
                {
                    try (DirectoryStream<Path> stream = Files.newDirectoryStream(seasonPath))
                    {
                        for (Path entry : stream)
                        {
                            String fileName = entry.getFileName().toString();
                            Matcher matcher = EPISODE.matcher(fileName);
                            if (matcher.matches())
                            {
                                String seasonText = matcher.group(2);
                                String episodeText = matcher.group(3);
                                
                                int seasonNumber = Integer.parseInt(seasonText);
                                int episodeNumber = Integer.parseInt(episodeText);
                                
                                if (seasonNumber == data.getSeason() && episodeNumber == episode.getEpisode())
                                {
                                    Path newPath = Paths.get(entry.toString().replace(fileName, correctFileName));
                                    System.out.println();
                                    Files.move(entry, newPath);
                                    episode.setFileName(correctFileName);
                                    result = true;
                                }
                            }
                        }
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
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
            episodeData.add(new EpisodeData(episode.getEpisodeNumber(), episode.getTitle(), episode.getVideoFilePath() == null ? "" : episode.getVideoFilePath().getFileName().toString(), episode.getReleaseDate() == null ? 0L : episode.getReleaseDate().getTime(), episode.getWatched().get()));
        }
        SeasonData data = new SeasonData(season.getShow().getTitle(), season.getShow().getImdbId(), season.getSeasonNumber(), season.getTotalEpisodes(), season.getReleaseDate() == null ? 0L : season.getReleaseDate().getTime(), episodeData);
        
        Path seasonPath = season.getPath();
        if (seasonPath == null) seasonPath = getSaveDir(season);
        if (seasonPath == null) return null;
        try
        {
            if (Files.notExists(seasonPath)) Files.createDirectory(seasonPath);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
        
        Path path = seasonPath.resolve(Reference.SEASON_INFO_FILE);
        
        try (FileWriter writer = new FileWriter(path.toFile()))
        {
            writer.write(GSON.toJson(data));
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
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
    
    public static Path getSaveDir(TVSeason season)
    {
        for (Path basePath : Settings.BASE_PATHS)
        {
            try
            {
                FileStore fileStore = Files.getFileStore(basePath);
                if (fileStore.getUsableSpace() < 10000000000L) continue;
            }
            catch (IOException e)
            {
                continue;
            }
            return basePath.resolve(String.format("%s_season_%d", getSimplifiedName(season.getShow().getTitle()), season.getSeasonNumber()));
        }
        return null;
    }
    
    public static String getEpisodeFileName(int episode, String title)
    {
        return String.format("episode_%02d_%s.mkv", episode, getSimplifiedName(title));
    }
}