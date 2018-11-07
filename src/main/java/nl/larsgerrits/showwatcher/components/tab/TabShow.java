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
            System.out.println(season.getSeasonNumber());
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
    
    private void setActionButtonSeason(TVSeason season)
    {
        // if (season.getPath() == null)
        // {
        //     actionButton.setText("Download Season");
        //     actionButton.setGraphic(DOWNLOAD);
        //
        //     actionButton.setDisable(false);
        //
        //     actionButton.setOnMouseClicked(e -> {
        //         ShowManager.saveSeasonToDisk(season);
        //         for (TVEpisode episode : season)
        //         {
        //             if (episode.getVideoFile().isEmpty() && episode.getReleaseDate().getTime() > 0 && episode.getReleaseDate().getTime() < System.currentTimeMillis())
        //             {
        //                 DownloadManager.downloadEpisode(episode);
        //             }
        //         }
        //         setActionButtonSeason(season);
        //     });
        // }
        // else
        // {
        //     actionButton.setText("Watch Season");
        //     actionButton.setGraphic(PLAY);
        //
        //     if (season.isWatched()) actionButton.setDisable(true);
        //     else
        //     {
        //         actionButton.setDisable(false);
        //         actionButton.setOnMouseClicked(e -> {
        //             for (TVEpisode episode : season)
        //             {
        //                 if (!episode.isWatched() && StringUtils.isNotEmpty(episode.getVideoFile()) && episode.getSeason().getPath() != null)
        //                 {
        //                     // try
        //                     // {
        //                     //     Desktop.getDesktop().open(episode.getSeason().getPath().resolve(episode.getVideoFile()).toFile()); //TODO: Add builtin video player
        //                     // }
        //                     // catch (IOException ex)
        //                     // {
        //                     //     ex.printStackTrace();
        //                     // }
        //                     // break;
        //                     episode.setWatched(true);
        //                     episode.getSeason().setDirty(true);
        //                 }
        //             }
        //         });
        //     }
        // }
    }
    
    private void setActionButtonEpisode(TVEpisode episode)
    {
        // if (Strings.isNullOrEmpty(episode.getVideoFile()))
        // {
        //     actionButton.setText("Download Episode");
        //     actionButton.setGraphic(DOWNLOAD);
        //
        //     actionButton.setDisable(false);
        //
        //     actionButton.setOnMouseClicked(e -> {
        //         // ShowManager.saveSeasonToDisk(season);
        //         DownloadManager.downloadEpisode(episode);
        //         setActionButtonEpisode(episode);
        //     });
        // }
        // else
        // {
        //     actionButton.setText("Watch Episode");
        //     actionButton.setGraphic(PLAY);
        //
        //     if (episode.getSeason().getPath() == null || episode.getReleaseDate().getTime() > System.currentTimeMillis())
        //     {
        //         actionButton.setDisable(true);
        //     }
        //     else
        //     {
        //         actionButton.setDisable(false);
        //         actionButton.setOnMouseClicked(e -> {
        //             if (episode.getSeason().getPath() != null)
        //             {
        //                 try
        //                 {
        //                     Desktop.getDesktop().open(episode.getSeason().getPath().resolve(episode.getVideoFile()).toFile()); //TODO: Add builtin video player
        //                 }
        //                 catch (IOException ex)
        //                 {
        //                     ex.printStackTrace();
        //                 }
        //             }
        //         });
        //     }
        // }
    }
    
    private void newSeasonAdded(TVSeason season)
    {
        TabSeason tab = new TabSeason(season, this::onEpisodeSelected);
        Platform.runLater(() -> {
            seasonTabPane.getTabs().add(tab);
            seasonTabPane.getTabs().sort(Comparator.comparing(t -> ((TabSeason) t).getSeason().getSeasonNumber()));
        });
    }
    
    // title.setText(show.getTitle() + ": Episode " + episode.getSeason().getSeasonNumber() + "x" + String.format("%02d", episode.getEpisodeNumber()));
    //
    // Date date = episode.getReleaseDate();
    // // descriptionDate.setWrappingWidth(270);
    // descriptionDate.setText(String.format("Release Date: %02d/%02d/%04d %02d:%02d", date.getDate(), date.getMonth() + 1, date.getYear() + 1900, date.getHours()+1, date.getMinutes()));
    // // descriptionDate.setText();
    //
    // description.setText(DescriptionManager.getEpisodeDescription(episode, description::setText));
    //
    // setActionButtonEpisode(episode);
    
    // @FXML
    // public void initialize()
    // {
    //     for (TVSeason season : show.getSeasons())
    //     {
    //         TabSeason poster = new TabSeason(season);
    //         tabPane.getTabs().add(poster);
    //     }
    //
    //     setText(show.getTitle());
    //
    //     descriptionTitle.setText(show.getTitle());
    //     description.setText(DescriptionManager.getShowDescription(show, s -> description.setText(s)));
    //
    //     watchedButton.setOnMouseClicked(e -> {
    //         show.setWatched(true);
    //         // list.getChildren().stream().map(n->(SeasonPoster)n).forEach(SeasonPoster::updateWatchedClass);
    //     });
    //
    //     Date releaseDate = show.getSeasons().get(0).getReleaseDate();
    //     descriptionDate.setText("Release Date: " + releaseDate.getDate() + "/" + (releaseDate.getMonth() + 1) + "/" + (releaseDate.getYear() + 1900));
    //
    //     // scrollPane.setVvalue(scrollPane.getVmin());
    // }
    
    public TVShow getShow()
    {
        return show;
    }
    
    // private void setOnSeasonClicked(SeasonPoster poster)
    // {
    //     poster.setOnMouseClicked(e -> {
    //         masonryPane.getChildren().clear();
    //         for (TVEpisode episode : poster.getSeason().getEpisodes())
    //         {
    //             PosterEpisode episodePoster = new PosterEpisode(episode);
    //             masonryPane.getChildren().add(episodePoster);
    //             setOnEpisodeClicked(episodePoster);
    //             episodePoster.updateWatchedClass();
    //         }
    //         masonryPane.setVisible(true);
    //
    //         Date releaseDate = poster.getSeason().getReleaseDate();
    //         descriptionDate.setText("Release Date: " + releaseDate.getDate() + "/" + (releaseDate.getMonth() + 1) + "/" + (releaseDate.getYear() + 1900));
    //
    //         descriptionTitle.setText("Season " + poster.getSeason().getSeasonNumber());
    //         description.setText(DescriptionManager.getSeasonDescription(poster.getSeason(), s -> description.setText(s)));
    //         torrentHealth.setVisible(false);
    //         watchedButton.setOnMouseClicked(e2 -> {
    //             poster.getSeason().setWatched(true);
    //             poster.updateWatchedClass();
    //             masonryPane.getChildren().stream().map(n -> (PosterEpisode) n).forEach(PosterEpisode::updateWatchedClass);
    //         });
    //         poster.updateWatchedClass();
    //     });
    // }
    
    // private void setOnEpisodeClicked(PosterEpisode poster)
    // {
    //     poster.setOnMouseClicked(e -> {
    //         descriptionTitle.setText("Episode " + poster.getEpisode().getEpisodeNumber() + ": " + poster.getEpisode().getTitle());
    //         description.setText(DescriptionManager.getEpisodeDescription(poster.getEpisode(), s -> description.setText(s)));
    //         torrentHealth.setValue(50 / 10);
    //         torrentHealth.setVisible(true);
    //
    //         Date releaseDate = poster.getEpisode().getReleaseDate();
    //         descriptionDate.setText("Release Date: " + releaseDate.getDate() + "/" + (releaseDate.getMonth() + 1) + "/" + (releaseDate.getYear() + 1900));
    //
    //         list.getChildren().stream().map(p -> (SeasonPoster) p).filter(p -> p.getSeason() == poster.getEpisode().getSeason()).forEach(SeasonPoster::updateWatchedClass);
    //         watchedButton.setOnMouseClicked(e3 -> {
    //             poster.getEpisode().setWatched(true);
    //             poster.updateWatchedClass();
    //
    //             try
    //             {
    //                 if (StringUtils.isNotEmpty(poster.getEpisode().getVideoFile()) && poster.getEpisode().getSeason().getPath() != null)
    //                 {
    //                     Desktop.getDesktop().open(poster.getEpisode().getSeason().getPath().resolve(poster.getEpisode().getVideoFile()).toFile());
    //                 }
    //             }
    //             catch (IOException e1)
    //             {
    //                 e1.printStackTrace();
    //             }
    //         });
    //     });
    // }
    
    // @Override
    // public void setStage(Stage stage)
    // {
    //     super.setStage(stage);
    //     stage.widthProperty().addListener((v, o, n) -> {
    //         descriptionPane.setPrefWidth(n.intValue());
    //         episodeGrid.setPrefWidth(n.intValue() - 400);
    //         episodeGrid.prefHeight(n.intValue() - 250);
    //     });
    //
    //     list.getChildren()//
    //         .stream()//
    //         .filter(n -> n instanceof SeasonPoster)//
    //         .map(n -> (SeasonPoster) n)//
    //         .forEach(p -> p.setStage(stage));
    // }
}
