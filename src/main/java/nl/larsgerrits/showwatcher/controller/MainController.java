package nl.larsgerrits.showwatcher.controller;

import com.google.common.collect.Maps;
import com.jfoenix.controls.JFXMasonryPane;
import com.jfoenix.controls.JFXTabPane;
import javafx.fxml.FXML;
import nl.larsgerrits.showwatcher.Threading;
import nl.larsgerrits.showwatcher.components.poster.PosterShow;
import nl.larsgerrits.showwatcher.components.tab.TabAddShow;
import nl.larsgerrits.showwatcher.components.tab.TabShow;
import nl.larsgerrits.showwatcher.manager.ShowManager;
import nl.larsgerrits.showwatcher.show.TVShow;

import java.util.Map;

public class MainController
{
    public static Map<TVShow, TabShow> SHOW_TAB_MAP = Maps.newHashMap();
    
    @FXML
    public JFXTabPane tabPane;
    
    @FXML
    private JFXMasonryPane masonryPane;
    
    public void initialize()
    {
        tabPane.getTabs().set(1, new TabAddShow(this));
        
        // ShowManager.getTVShows().stream()/*.sorted(Comparator.comparing(TVShow::isCompletelyWatched))*/.forEach(s -> {
        ShowManager.getTVShows().stream()/*.sorted(Comparator.comparing(TVShow::isCompletelyWatched))*/.forEach(s -> {
            PosterShow poster = new PosterShow(s);
            masonryPane.getChildren().add(poster);
            poster.setOnMouseClicked(e -> {
                TabShow showTab = SHOW_TAB_MAP.get(s);
                
                if (showTab == null)
                {
                    showTab = new TabShow(tabPane, s);
                    // ShowManager.checkForNewUpdates(s);
                    SHOW_TAB_MAP.put(s, showTab);
                }
                
                if (!tabPane.getTabs().contains(showTab))
                {
                    tabPane.getTabs().add(showTab);
                }
                
                tabPane.getSelectionModel().select(showTab);
            });
        });
        // Threading.LOADING_THREAD.submit(() -> ShowManager.getTVShows().forEach(ShowManager::checkForNewUpdates));
        ShowManager.getTVShows().forEach(s -> Threading.LOADING_THREAD.execute(() -> ShowManager.checkForNewUpdates(s)));
        // TVEpisode episode = Objects.requireNonNull(ShowManager.getShow("tt4016454")).getSeason(4).getEpisode(18);
        // DownloadManager.downloadEpisode(episode);
        
        tabPane.getSelectionModel().select(2);
    }
}
