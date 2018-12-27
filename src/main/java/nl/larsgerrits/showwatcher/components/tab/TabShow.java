package nl.larsgerrits.showwatcher.components.tab;

import com.jfoenix.controls.JFXTabPane;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import nl.larsgerrits.showwatcher.Main;
import nl.larsgerrits.showwatcher.Threading;
import nl.larsgerrits.showwatcher.components.pane.PaneAction;
import nl.larsgerrits.showwatcher.components.pane.PaneDescription;
import nl.larsgerrits.showwatcher.show.TVEpisode;
import nl.larsgerrits.showwatcher.show.TVSeason;
import nl.larsgerrits.showwatcher.show.TVShow;

import java.util.Comparator;

public class TabShow extends Tab
{
    private TVShow show;
    
    private JFXTabPane showTabPane;
    
    private JFXTabPane seasonTabPane = new JFXTabPane();
    
    private PaneDescription infoPane = new PaneDescription(Main.WIDTH * 2 / 3, 250);
    private PaneAction actionPane = new PaneAction(this, Main.WIDTH / 3, 250);
    
    public TabShow(JFXTabPane showTabPane, TVShow show)
    {
        this.show = show;
        this.showTabPane = showTabPane;
        
        setText(show.getTitle());
        setClosable(true);
        
        BorderPane borderPane = new BorderPane();
        
        AnchorPane pane = new AnchorPane();
        
        AnchorPane.setLeftAnchor(infoPane, 0D);
        AnchorPane.setTopAnchor(infoPane, 0D);
        AnchorPane.setBottomAnchor(infoPane, 0D);
        
        AnchorPane.setRightAnchor(actionPane, 0D);
        AnchorPane.setTopAnchor(actionPane, 0D);
        AnchorPane.setBottomAnchor(actionPane, 0D);
        
        pane.getChildren().addAll(infoPane, actionPane);
        borderPane.setTop(pane);
        
        seasonTabPane.setPrefWidth(1784);
        seasonTabPane.setPrefHeight(750);
        seasonTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> onSeasonTabSelected(newTab));
        BorderPane.setAlignment(seasonTabPane, Pos.CENTER);
        
        for (TVSeason season : show.getSeasons())
        {
            TabSeason tab = new TabSeason(season, this::onEpisodeSelected);
            seasonTabPane.getTabs().add(tab);
        }
        seasonTabPane.getTabs().sort(Comparator.comparing(t -> ((TabSeason) t).getSeason().getSeasonNumber()));
        show.setSeasonAdded(this::newSeasonAdded);
        
        borderPane.setCenter(seasonTabPane);
        
        setContent(borderPane);
    
        // ShowManager.checkForNewUpdates(show);
    }
    
    private void onSeasonTabSelected(Tab newTab)
    {
        if (newTab instanceof TabSeason)
        {
            ((TabSeason) newTab).onSelected();
            onSeasonSelected(((TabSeason) newTab).getSeason());
        }
    }
    
    @SuppressWarnings("deprecation")
    private void onSeasonSelected(TVSeason season)
    {
        infoPane.onSeasonSelected(season);
        actionPane.onSeasonSelected(season);
    }
    
    public void onEpisodeSelected(TVEpisode episode)
    {
        infoPane.onEpisodeSelected(episode);
        actionPane.onEpisodeSelected(episode);
        actionPane.setWatchedButtonIcon(episode);
    }
    
    public void closeTab()
    {
        showTabPane.getSelectionModel().select(2);
        Threading.LOADING_THREAD.execute(() -> {
            try
            {
                Thread.sleep(150);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            Platform.runLater(() -> showTabPane.getTabs().remove(this));
        });
        
    }
    
    private void newSeasonAdded(TVSeason season)
    {
        TabSeason tab = new TabSeason(season, this::onEpisodeSelected);
        Platform.runLater(() -> {
            seasonTabPane.getTabs().add(tab);
            seasonTabPane.getTabs().sort(Comparator.comparing(t -> ((TabSeason) t).getSeason().getSeasonNumber()));
        });
    }
    
    public TVShow getShow()
    {
        return show;
    }
}
