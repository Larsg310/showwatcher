package nl.larsgerrits.showwatcher.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import nl.larsgerrits.showwatcher.Reference;
import nl.larsgerrits.showwatcher.Settings;
import nl.larsgerrits.showwatcher.data.EpisodeData;
import nl.larsgerrits.showwatcher.data.SeasonData;
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
    
    private static final Pattern DIACRITICS_AND_FRIENDS = Pattern.compile("[\\p{InCombiningDiacriticalMarks}\\p{IsLm}\\p{IsSk}]+");
    
    public static void loadShowsFromDisk(@Nonnull BiFunction<Path, SeasonData, TVSeason> seasonDataConsumer)
    {
        try (DirectoryStream<Path> showPaths = Files.newDirectoryStream(Settings.BASE_PATH))
        {
            for (Path path : showPaths)
            {
                if (Files.exists(path.resolve(Reference.SEASON_INFO)))
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
        catch (IOException ignored) { }
    }
    
    private static boolean checkSeasonData(SeasonData data)
    {
        Path seasonPath = Settings.BASE_PATH.resolve(String.format("%s_season_%d", getSimplefiedName(data.getTitle()), data.getSeason()));
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
        return result;
    }
    
    private static SeasonData parseSeasonData(Path path)
    {
        try
        {
            String content = new String(Files.readAllBytes(path.resolve(Reference.SEASON_INFO)));
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
            episodeData.add(new EpisodeData(episode.getEpisodeNumber(), episode.getTitle(), episode.getFileName(), episode.getReleaseDate() == null ? 0L : episode.getReleaseDate().getTime(), episode.isWatched()));
        }
        SeasonData data = new SeasonData(season.getTVShow().getTitle(), season.getTVShow().getImdbId(), season.getSeasonNumber(), season.getTotalEpisodes(), season.getReleaseDate() == null ? 0L : season.getReleaseDate().getTime(), episodeData);
        
        File dir = new File(Settings.BASE_PATH + File.separator + getSimplefiedName(season.getTVShow().getTitle()) + "_season_" + season.getSeasonNumber());
        
        if (!dir.exists()) dir.mkdir();
        
        Path path = dir.toPath().resolve("season_info.json");
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
    
    /**
     * // @SuppressWarnings("ResultOfMethodCallIgnored")
     * // private static void fixSeasonMetadata(SeasonData info, File dir)
     * // {
     * //     try
     * //     {
     * //         Map<Integer, File> toRename = new HashMap<>();
     * //
     * //         Response<List<Episode>> response = TRAKT_TV.seasons().season(info.getImdbId(), info.getSeason(), Extended.FULLEPISODES).execute();
     * //
     * //         if (response.isSuccessful())
     * //         {
     * //             Map<Integer, String> map = new HashMap<>();
     * //
     * //             File[] episodeFiles = dir.listFiles(f -> f.getTitle().startsWith("episode_"));
     * //             assert episodeFiles != null;
     * //             for (File f : episodeFiles)
     * //             {
     * //                 String[] details = f.getTitle().replace(".mkv", "").split("_");
     * //                 int number = Integer.parseInt(details[1]);
     * //                 // System.out.println(Arrays.toString(details));
     * //                 if (details.length == 2)
     * //                 {
     * //                     // System.out.println(f.getPath().replace(".mkv", ""));
     * //                     toRename.put(number, f);
     * //                 }
     * //                 map.put(number, f.getTitle());
     * //             }
     * //
     * //             List<EpisodeData> episodeData = new ArrayList<>();
     * //             assert response.body() != null;
     * //             for (com.uwetrottmann.trakt5.entities.Episode episode : response.body())
     * //             {
     * //                 if (episode.title != null && !episode.title.isEmpty())
     * //                 {
     * //                     EpisodeData ep = new EpisodeData();
     * //                     ep.setEpisode(episode.number);
     * //                     ep.setTitle(episode.title);
     * //                     if (episode.first_aired != null)
     * //                     {
     * //                         ep.setReleaseDate(episode.first_aired.withOffsetSameInstant(ZoneOffset.of(OffsetDateTime.now().getOffset().toString())));
     * //                     }
     * //                     if (map.containsKey(episode.number)) ep.setFileName(map.get(episode.number));
     * //                     if (toRename.keySet().contains(episode.number))
     * //                     {
     * //                         String fileName = "episode_" + String.format("%02d", episode.number) + "_" + getSimplefiedName(episode.title) + ".mkv";
     * //                         ep.setFileName(fileName);
     * //                         File f = toRename.get(episode.number);
     * //                         String newPath = f.getParent() + "\\" + fileName;
     * //                         f.renameTo(new File(newPath));
     * //                     }
     * //                     episodeData.add(ep);
     * //                 }
     * //             }
     * //             info.setEpisodeData(episodeData);
     * //         }
     * //
     * //         Response<Show> showResponse = TRAKT_TV.shows().summary(info.getImdbId(), null).execute();
     * //         if (showResponse.isSuccessful())
     * //         {
     * //             assert showResponse.body() != null;
     * //             info.setTitle(showResponse.body().title);
     * //         }
     * //
     * //         String json = GSON.toJson(info);
     * //
     * //         try (FileWriter writer = new FileWriter(Paths.get(dir.getPath() + File.separator + "season_info.json").toFile()))
     * //         {
     * //             writer.write(json);
     * //         }
     * //     }
     * //     catch (IOException e)
     * //     {
     * //         e.printStackTrace();
     * //     }
     * // }
     * //
     */
    public static String getSimplefiedName(String filename)
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
    
    public static void writeIdMap(Map<String, Integer> imdbToTmdb)
    {
        write(Settings.CACHE_PATH.resolve(Reference.ID_MAP), GSON.toJson(imdbToTmdb));
    }
    
    @SuppressWarnings("unchecked")
    public static void loadIdMap(Map<String, Integer> imdbToTmdb)
    {
        try
        {
            Map<String, Double> jsonMap = GSON.fromJson(Files.lines(Settings.CACHE_PATH.resolve(Reference.ID_MAP), Charset.forName("UTF-8")).collect(Collectors.joining()), Map.class);
            for (Map.Entry<String, Double> entry : jsonMap.entrySet())
            {
                imdbToTmdb.put(entry.getKey(), (int) entry.getValue().doubleValue());
            }
        }
        catch (IOException ignored) { }
    }
    
    private static void write(Path path, String content)
    {
        try (BufferedWriter writer = Files.newBufferedWriter(path, Charset.forName("UTF-8")))
        {
            writer.write(content);
        }
        catch (IOException ignored) { }
    }
    
    public static Path getSaveDir(TVEpisode episode)
    {
        return Settings.BASE_PATH.resolve(getSimplefiedName(episode.getSeason().getTVShow().getTitle()) + "_season_" + episode.getSeason().getSeasonNumber());
    }
    
    public static String getEpisodeFileName(int episode, String title)
    {
        return "episode_" + String.format("%02d", episode) + "_" + getSimplefiedName(title) + ".mkv";
    }
}