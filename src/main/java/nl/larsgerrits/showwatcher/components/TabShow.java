package nl.larsgerrits.showwatcher.components;

import com.google.common.base.Strings;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTabPane;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import nl.larsgerrits.showwatcher.Main;
import nl.larsgerrits.showwatcher.Threading;
import nl.larsgerrits.showwatcher.manager.DescriptionManager;
import nl.larsgerrits.showwatcher.manager.DownloadManager;
import nl.larsgerrits.showwatcher.manager.ShowManager;
import nl.larsgerrits.showwatcher.show.TVEpisode;
import nl.larsgerrits.showwatcher.show.TVSeason;
import nl.larsgerrits.showwatcher.show.TVShow;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.io.IOException;
import java.util.Date;

public class TabShow extends Tab
{
    public final FontAwesomeIconView CROSS_ICON = new FontAwesomeIconView(FontAwesomeIcon.TIMES, "40px");
    public final FontAwesomeIconView PLAY = new FontAwesomeIconView(FontAwesomeIcon.PLAY, "20px");
    public final FontAwesomeIconView DOWNLOAD = new FontAwesomeIconView(FontAwesomeIcon.DOWNLOAD, "20px");
    
    private TVShow show;
    private JFXTabPane showTabPane;
    
    private Text title = new Text();
    private Text descriptionDate = new Text();
    private Text description = new Text();
    private JFXButton actionButton = new JFXButton();
    private JFXTabPane seasonTabPane = new JFXTabPane();
    
    public TabShow(JFXTabPane showTabPane, TVShow show)
    {
        this.show = show;
        this.showTabPane = showTabPane;
        show.setSeasonAdded(this::newSeasonAdded);
        
        setText(show.getTitle());
        setClosable(true);
        
        BorderPane borderPane = new BorderPane();
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPrefWidth(1784);
        scrollPane.setPrefHeight(250);
        
        AnchorPane infoPane = new AnchorPane();
        infoPane.setPrefWidth(Main.WIDTH);
        infoPane.setPrefHeight(250);
        
        title.setLayoutX(14);
        title.setLayoutY(40);
        title.setStrokeType(StrokeType.OUTSIDE);
        title.setStrokeWidth(0);
        title.setFont(new Font(36));
        title.setId("text");
        AnchorPane.setLeftAnchor(title, 16D);
        
        description.setText(DescriptionManager.getShowDescription(show, description::setText));
        description.setLayoutX(14);
        description.setLayoutY(70);
        description.setStrokeType(StrokeType.OUTSIDE);
        description.setStrokeWidth(0);
        description.setWrappingWidth(Main.WIDTH / 2 - 20);
        description.setFont(new Font(18));
        description.setId("text");
        AnchorPane.setLeftAnchor(description, 16D);
        
        descriptionDate.setLayoutX(Main.WIDTH / 2 + 10);
        descriptionDate.setLayoutY(32);
        descriptionDate.setStrokeType(StrokeType.OUTSIDE);
        descriptionDate.setStrokeWidth(0);
        descriptionDate.setWrappingWidth(210);
        descriptionDate.setFont(new Font(18));
        descriptionDate.setId("text");
        AnchorPane.setTopAnchor(descriptionDate, 16D);
        
        JFXButton closeButton = new JFXButton();
        closeButton.setGraphic(CROSS_ICON);
        closeButton.setOnMouseClicked(e -> closeTab());
        AnchorPane.setRightAnchor(closeButton, 10D);
        AnchorPane.setTopAnchor(closeButton, 10D);
        
        actionButton.setId("button");
        actionButton.setFont(new Font(20));
        AnchorPane.setRightAnchor(actionButton, 16D);
        AnchorPane.setBottomAnchor(actionButton, 28D);
        
        infoPane.getChildren().addAll(title, description, descriptionDate, closeButton, actionButton);
        scrollPane.setContent(infoPane);
        borderPane.setTop(scrollPane);
        
        seasonTabPane.setPrefWidth(1784);
        seasonTabPane.setPrefHeight(750);
        seasonTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> onSeasonTabSelected(newTab));
        BorderPane.setAlignment(seasonTabPane, Pos.CENTER);
        
        for (TVSeason season : show.getSeasons())
        {
            TabSeason tab = new TabSeason(season, this::onEpisodeSelected);
            seasonTabPane.getTabs().add(tab);
        }
        
        borderPane.setCenter(seasonTabPane);
        
        setContent(borderPane);
    }
    
    private void closeTab()
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
        title.setText(show.getTitle() + ": Season " + season.getSeasonNumber());
        
        Date date = season.getReleaseDate();
        descriptionDate.setText("Release Date: " + String.format("%02d", date.getDate()) + "/" + String.format("%02d", date.getMonth() + 1) + "/" + (date.getYear() + 1900));
        
        description.setText(DescriptionManager.getSeasonDescription(season, description::setText));
        
        setActionButtonSeason(season);
    }
    
    private void setActionButtonSeason(TVSeason season)
    {
        if (season.getPath() == null)
        {
            actionButton.setText("Download Season");
            actionButton.setGraphic(DOWNLOAD);
            
            actionButton.setDisable(false);
            
            actionButton.setOnMouseClicked(e -> {
                ShowManager.saveSeasonToDisk(season);
                for (TVEpisode episode : season)
                {
                    if (episode.getFileName().isEmpty() && episode.getReleaseDate().getTime() > 0 && episode.getReleaseDate().getTime() < System.currentTimeMillis())
                    {
                        DownloadManager.downloadEpisode(episode);
                    }
                }
                setActionButtonSeason(season);
            });
        }
        else
        {
            actionButton.setText("Watch Season");
            actionButton.setGraphic(PLAY);
            
            if (season.isWatched()) actionButton.setDisable(true);
            else
            {
                actionButton.setDisable(false);
                actionButton.setOnMouseClicked(e -> {
                    for (TVEpisode episode : season)
                    {
                        if (!episode.isWatched() && StringUtils.isNotEmpty(episode.getFileName()) && episode.getSeason().getPath() != null)
                        {
                            try
                            {
                                Desktop.getDesktop().open(episode.getSeason().getPath().resolve(episode.getFileName()).toFile()); //TODO: Add builtin video player
                            }
                            catch (IOException ex)
                            {
                                ex.printStackTrace();
                            }
                            break;
                        }
                    }
                });
            }
        }
    }
    
    private void setActionButtonEpisode(TVEpisode episode)
    {
        if (Strings.isNullOrEmpty(episode.getFileName()))
        {
            actionButton.setText("Download Episode");
            actionButton.setGraphic(DOWNLOAD);
            
            actionButton.setDisable(false);
            
            actionButton.setOnMouseClicked(e -> {
                // ShowManager.saveSeasonToDisk(season);
                DownloadManager.downloadEpisode(episode);
                setActionButtonEpisode(episode);
            });
        }
        else
        {
            actionButton.setText("Watch Episode");
            actionButton.setGraphic(PLAY);
            
            if (episode.getSeason().getPath() == null || episode.getReleaseDate().getTime() > System.currentTimeMillis())
            {
                actionButton.setDisable(true);
            }
            else
            {
                actionButton.setDisable(false);
                actionButton.setOnMouseClicked(e -> {
                    if (episode.getSeason().getPath() != null)
                    {
                        try
                        {
                            Desktop.getDesktop().open(episode.getSeason().getPath().resolve(episode.getFileName()).toFile()); //TODO: Add builtin video player
                        }
                        catch (IOException ex)
                        {
                            ex.printStackTrace();
                        }
                    }
                });
            }
        }
    }
    
    private void newSeasonAdded(TVSeason season)
    {
        TabSeason tab = new TabSeason(season, this::onEpisodeSelected);
        Platform.runLater(() -> seasonTabPane.getTabs().add(tab));
    }
    
    @SuppressWarnings("deprecation")
    public void onEpisodeSelected(TVEpisode episode)
    {
        title.setText(show.getTitle() + ": Episode " + episode.getSeason().getSeasonNumber() + "x" + String.format("%02d", episode.getEpisodeNumber()));
        
        Date date = episode.getReleaseDate();
        descriptionDate.setText("Release Date: " + date.getDate() + "/" + (date.getMonth() + 1) + "/" + (date.getYear() + 1900));
        
        description.setText(DescriptionManager.getEpisodeDescription(episode, description::setText));
        
        setActionButtonEpisode(episode);
    }
    
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
    //                 if (StringUtils.isNotEmpty(poster.getEpisode().getFileName()) && poster.getEpisode().getSeason().getPath() != null)
    //                 {
    //                     Desktop.getDesktop().open(poster.getEpisode().getSeason().getPath().resolve(poster.getEpisode().getFileName()).toFile());
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
