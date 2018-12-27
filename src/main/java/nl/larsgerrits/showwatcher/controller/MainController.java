package nl.larsgerrits.showwatcher.controller;

import com.google.common.collect.Maps;
import com.jfoenix.controls.JFXMasonryPane;
import com.jfoenix.controls.JFXTabPane;
import javafx.fxml.FXML;
import nl.larsgerrits.showwatcher.components.poster.PosterShow;
import nl.larsgerrits.showwatcher.components.tab.TabAdd;
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
        tabPane.getSelectionModel().select(2);
        
        ShowManager.getTVShows().forEach(s -> {
            PosterShow poster = new PosterShow(s);
            masonryPane.getChildren().add(poster);
            poster.setOnMouseClicked(e -> {
                TabShow showTab = SHOW_TAB_MAP.get(s);
                
                if (showTab == null){
                    showTab = new TabShow(tabPane, s);
                    ShowManager.checkForNewUpdates(s);
                    SHOW_TAB_MAP.put(s, showTab);
                }
                
                if(!tabPane.getTabs().contains(showTab)){
                    tabPane.getTabs().add(showTab);
                }
                
                tabPane.getSelectionModel().select(showTab);
            });
        });
        // ShowManager.getTVShows().forEach(ShowManager::checkForNewUpdates);
        
        tabPane.getTabs().set(1, new TabAdd(this));
    }
}
