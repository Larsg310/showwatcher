package nl.larsgerrits.showwatcher.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import nl.larsgerrits.showwatcher.data.EpisodeData;
import nl.larsgerrits.showwatcher.data.SeasonData;
import nl.larsgerrits.showwatcher.show.TVEpisode;
import nl.larsgerrits.showwatcher.show.TVSeason;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FileUtils
{
    // public static final TraktV2 TRAKT_TV = new TraktV2("d37bc084cad26a17a8a4ae8bf01eb73262e9ae15823351257f87c335c69f466d", "ba0b07289702f71b73c3427503c5d62598bcd2d83e62d8e0d9b4496a4699c328", "http://www.google.com");
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    private static final Path DIRECTORY = Paths.get("G:", "TV Series");
    public static final Path CACHE_DIRECTORY = DIRECTORY.resolve("_cache");
    private static final Path SEASON_INFO = Paths.get("season_info.json");
    private static final Pattern DIACRITICS_AND_FRIENDS = Pattern.compile("[\\p{InCombiningDiacriticalMarks}\\p{IsLm}\\p{IsSk}]+");
    
    public static void loadShowsFromDisk(BiFunction<Path, SeasonData, TVSeason> seasonDataConsumer)
    {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(DIRECTORY))
        {
            for (Path path : directoryStream)
            {
                if (Files.exists(path.resolve(SEASON_INFO)))
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
        Path seasonPath = DIRECTORY.resolve(fixFileName(data.getTitle()) + "_season_" + data.getSeason());
        boolean result = false;
        
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
        
        return result;
    }
    
    private static SeasonData parseSeasonData(Path path)
    {
        try
        {
            String content = new String(Files.readAllBytes(path.resolve(SEASON_INFO)));
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
        
        File dir = new File(DIRECTORY + File.separator + fixFileName(season.getTVShow().getTitle()) + "_season_" + season.getSeasonNumber());
        
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
     * //                         String fileName = "episode_" + String.format("%02d", episode.number) + "_" + fixFileName(episode.title) + ".mkv";
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
    public static String fixFileName(String filename)
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
        write(CACHE_DIRECTORY.resolve("_id_map.json"), GSON.toJson(imdbToTmdb));
    }
    
    @SuppressWarnings("unchecked")
    public static void loadIdMap(Map<String, Integer> imdbToTmdb)
    {
        try
        {
            Map<String, Double> jsonMap = GSON.fromJson(Files.lines(CACHE_DIRECTORY.resolve("_id_map.json"), Charset.forName("UTF-8")).collect(Collectors.joining()), Map.class);
            for (Map.Entry<String, Double> entry : jsonMap.entrySet())
            {
                imdbToTmdb.put(entry.getKey(), (int) entry.getValue().doubleValue());
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    private static void write(Path path, String content)
    {
        try (BufferedWriter writer = Files.newBufferedWriter(path, Charset.forName("UTF-8")))
        {
            writer.write(content);
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }
    
    public static Path getSaveDir(TVEpisode episode)
    {
        return DIRECTORY.resolve(fixFileName(episode.getSeason().getTVShow().getTitle()) + "_season_" + episode.getSeason().getSeasonNumber());
    }
    
    public static String getEpisodeFileName(int episode, String title)
    {
        return "episode_" + String.format("%02d", episode) + "_" + fixFileName(title) + ".mkv";
    }
}