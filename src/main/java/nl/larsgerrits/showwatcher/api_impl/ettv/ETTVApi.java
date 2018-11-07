package nl.larsgerrits.showwatcher.api_impl.ettv;

import nl.larsgerrits.showwatcher.manager.ShowManager;
import nl.larsgerrits.showwatcher.show.TVEpisode;
import nl.larsgerrits.showwatcher.show.TVShow;
import nl.larsgerrits.showwatcher.show.Torrent;
import nl.larsgerrits.showwatcher.util.FileUtils;
import nl.larsgerrits.showwatcher.util.HTTPUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ETTVApi
{
    private static final Pattern SEASON_EPISODE_PATTERN = Pattern.compile(".*S(\\d{2})E(\\d{2}).*");
    
    private static final String TRACKERS = String.join("&tr=", new String[]{//
            "udp%3A%2F%2Ftracker.coppersurfer.tk%3A6969%2Fannounce",//
            "udp%3A%2F%2F9.rarbg.to%3A2710%2Fannounce",//
            "udp%3A%2F%2F9.rarbg.me%3A2710%2Fannounce",//
            "udp%3A%2F%2FIPv6.open-internet.nl%3A6969%2Fannounce",//
            "udp%3A%2F%2Ftracker.internetwarriors.net%3A1337%2Fannounce",//
            "udp%3A%2F%2Ftracker.opentrackr.org%3A1337%2Fannounce",//
            "udp%3A%2F%2Fp4p.arenabg.com%3A1337%2Fannounce",//
            "udp%3A%2F%2Feddie4.nl%3A6969%2Fannounce",//
            "udp%3A%2F%2Fshadowshq.yi.org%3A6969%2Fannounce",//
            "udp%3A%2F%2Ftracker.leechers-paradise.org%3A6969%2Fannounce",//
            "udp%3A%2F%2Fexplodie.org%3A6969%2Fannounce",//
            "udp%3A%2F%2Ftracker.tiny-vps.com%3A6969%2Fannounce",//
            "udp%3A%2F%2Finferno.demonoid.pw%3A3391%2Fannounce",//
            "udp%3A%2F%2Fipv4.tracker.harry.lu%3A80%2Fannounce",//
            "udp%3A%2F%2Fpeerfect.org%3A6969%2Fannounce",//
            "udp%3A%2F%2Ftracker.pirateparty.gr%3A6969%2Fannounce",//
            "udp%3A%2F%2Ftracker.vanitycore.co%3A6969%2Fannounce",//
            "udp%3A%2F%2Fopen.stealth.si%3A80%2Fannounce",//
            "udp%3A%2F%2Ftracker.torrent.eu.org%3A451",//
            "udp%3A%2F%2Ftracker.zer0day.to%3A1337%2Fannounce",//
            "udp%3A%2F%2Ftracker.open-internet.nl%3A6969%2Fannounce"//
    });
    
    private static final Map<TVShow, List<Torrent>> torrents = new HashMap<>();
    
    private static List<Torrent> getTorrentList()
    {
        Stream<Torrent> result = HTTPUtils.getFromZip("https://www.ettv.tv/dumps/ettv_full.txt.gz").filter(s -> s.contains("|TV|")).map(ETTVApi::parse).filter(Objects::nonNull)/*.filter(Objects::nonNull)*/;
        return result.collect(Collectors.toList());
    }
    
    private static Map<TVShow, List<Torrent>> getTorrentMap()
    {
        if (torrents.isEmpty())
        {
            for (Torrent torrent : getTorrentList())
            {
                List<Torrent> torrentList = torrents.computeIfAbsent((TVShow) torrent.getData(), k -> new ArrayList<>());
                torrentList.add(torrent);
            }
        }
        return torrents;
    }
    
    public static List<Torrent> request(TVEpisode episode)
    {
        return getTorrentMap().getOrDefault(episode.getSeason().getShow(), new ArrayList<>())//
                              .stream()//
                              .filter(t -> t.getSeason() == episode.getSeason().getSeasonNumber())//
                              .filter(t -> t.getEpisode() == episode.getEpisodeNumber())//
                              .collect(Collectors.toList());
    }
    
    private static Torrent parse(String line)
    {
        String[] parts = line.split("\\|");
        
        String title = parts[1];
        Matcher matcher = SEASON_EPISODE_PATTERN.matcher(title);
        
        TVShow show = ShowManager.getTVShows().stream().filter(s -> FileUtils.getSimplifiedName(s.getTitle()).equals(getSimplifiedName(matcher, title))).findFirst().orElse(null);
        
        if (show != null)
        {
            String magnetUrl = createMagnet(parts[0], title);
            int[] seasonEpisode = getSeasonEpisodeData(matcher, title);
            
            return new Torrent(title, magnetUrl, seasonEpisode[0], seasonEpisode[1], 0, 0, 0, show);
        }
        return null;
    }
    
    private static String getSimplifiedName(Matcher matcher, String title)
    {
        if (matcher.matches())
        {
            // System.out.println(name);
            return FileUtils.getSimplifiedName(title.substring(0, matcher.start(1) - 2).replace(".", "_"));
        }
        return "";
    }
    
    private static String createMagnet(String hash, String title)
    {
        return "magnet:?xt=urn:btih:" + hash + "&dn=" + title + "&tr=" + TRACKERS;
    }
    
    private static int[] getSeasonEpisodeData(Matcher matcher, String title)
    {
        if (matcher.matches())
        {
            // System.out.println(matcher.group(1) + ", " + matcher.group(2));
            // matcher.
            int season = Integer.valueOf(matcher.group(1));
            int episode = Integer.valueOf(matcher.group(2));
            return new int[]{season, episode};
        }
        return new int[]{0, 0};
    }
}
