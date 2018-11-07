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
    
    // private Text title = new Text();
    // private Text descriptionDate = new Text();
    // private Text description = new Text();
    // private JFXButton actionButton = new JFXButton();
    //
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
        
        // scrollPane.setPrefHeight(250);
        
        // AnchorPane infoPane = new AnchorPane();
        // infoPane.setPrefWidth(Main.WIDTH);
        // infoPane.setPrefHeight(250);
        
        // title.setLayoutX(14);
        // title.setLayoutY(40);
        // title.setStrokeType(StrokeType.OUTSIDE);
        // title.setStrokeWidth(0);
        // title.setFont(new Font(36));
        // title.setId("text");
        // AnchorPane.setLeftAnchor(title, 16D);
        //
        // description.setText(DescriptionManager.getShowDescription(show, description::setText));
        // description.setLayoutX(14);
        // description.setLayoutY(70);
        // description.setStrokeType(StrokeType.OUTSIDE);
        // description.setStrokeWidth(0);
        // description.setWrappingWidth(Main.WIDTH / 2 - 20);
        // description.setFont(new Font(18));
        // description.setId("text");
        // AnchorPane.setLeftAnchor(description, 16D);
        //
        // descriptionDate.setLayoutX(Main.WIDTH / 2 + 10);
        // descriptionDate.setLayoutY(32);
        // descriptionDate.setStrokeType(StrokeType.OUTSIDE);
        // descriptionDate.setStrokeWidth(0);
        // // descriptionDate.setWrappingWidth(210);
        // descriptionDate.setFont(new Font(18));
        // descriptionDate.setId("text");
        // AnchorPane.setTopAnchor(descriptionDate, 16D);
        
        // JFXButton closeButton = new JFXButton();
        // closeButton.setGraphic(CROSS_ICON);
        // closeButton.setOnMouseClicked(e -> closeTab());
        // AnchorPane.setRightAnchor(closeButton, 10D);
        // AnchorPane.setTopAnchor(closeButton, 10D);
        //
        // actionButton.setId("button");
        // actionButton.setFont(new Font(20));
        // AnchorPane.setRightAnchor(actionButton, 28D);
        // AnchorPane.setBottomAnchor(actionButton, 28D);
        //
        // infoPane.getChildren().addAll(title, description, descriptionDate, closeButton, actionButton);
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
