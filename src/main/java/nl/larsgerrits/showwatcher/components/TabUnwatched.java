package nl.larsgerrits.showwatcher.components;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.scene.control.Tab;
import nl.larsgerrits.showwatcher.Threading;
import nl.larsgerrits.showwatcher.manager.ShowManager;
import nl.larsgerrits.showwatcher.show.TVEpisode;
import nl.larsgerrits.showwatcher.show.TVSeason;
import nl.larsgerrits.showwatcher.show.TVShow;

import java.util.List;

public class TabUnwatched extends Tab
{
    private final FontAwesomeIconView EYE = new FontAwesomeIconView(FontAwesomeIcon.EYE, "20px");
    
    public TabUnwatched()
    {
        setGraphic(EYE);
        
        // TVShow show = ShowManager.getShow("tt2357547");
        
        // if (show != null)
        // {
        //     try
        //     {
        //         // search search = search.newSearch(show.getTitle().toLowerCase() + " s" + String.format("%02d", 2) + "e" + String.format("%02d", 1), Tv.ALL, search.SortOption.MOST_SEEDERS);
        //         Query query = new Query("black%20mirror%20s" + String.format("%02d", 3) + "e" + String.format("%02d", 1), 0, TorrentCategory.All, QueryOrder.ByDefault);
        //         List<Torrent> torrents = PirateBayScraper.search(query, 2, 1);
        //         System.out.println(query.TranslateToUrl());
        //         System.out.println(torrents.get(0).toString());
        //         System.out.println();
        //     }
        //     catch (IOException e)
        //     {
        //         e.printStackTrace();
        //     }
        // }
        
        List<TVShow> shows = ShowManager.getTVShows();
        for (TVShow show : shows)
        {
            Threading.TEST_THREAD.execute(() -> {
                for (TVSeason season : show)
                {
                    for (TVEpisode episode : season)
                    {
                        if (episode.getFileName().isEmpty() && episode.getReleaseDate().getTime() != 0 && episode.getReleaseDate().getTime() < System.currentTimeMillis())
                        {
                            System.out.println(show.getTitle() + ": " + season.getSeasonNumber() + "x" + String.format("%02d", episode.getEpisodeNumber()));
                            // DownloadManager.downloadEpisode(episode);
                        }
                    }
                }
            });
        }
    }
}
