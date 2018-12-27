package nl.larsgerrits.showwatcher.components.pane;

import com.jfoenix.controls.JFXButton;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import nl.larsgerrits.showwatcher.components.tab.TabShow;
import nl.larsgerrits.showwatcher.manager.DownloadManager;
import nl.larsgerrits.showwatcher.manager.ShowManager;
import nl.larsgerrits.showwatcher.show.TVEpisode;
import nl.larsgerrits.showwatcher.show.TVSeason;

import java.awt.*;
import java.io.IOException;

public class PaneAction extends AnchorPane
{
    public static final FontAwesomeIconView EYE_CLOSED = new FontAwesomeIconView(FontAwesomeIcon.EYE_SLASH, "40px");
    public static final FontAwesomeIconView EYE_OPENED = new FontAwesomeIconView(FontAwesomeIcon.EYE, "40px");
    public static final FontAwesomeIconView CROSS_ICON = new FontAwesomeIconView(FontAwesomeIcon.TIMES, "40px");
    
    public static final FontAwesomeIconView DOWNLOAD = new FontAwesomeIconView(FontAwesomeIcon.DOWNLOAD, "20px");
    public static final FontAwesomeIconView PLAY = new FontAwesomeIconView(FontAwesomeIcon.PLAY, "20px");
    
    private JFXButton actionButton = new JFXButton();
    private JFXButton watchedButton = new JFXButton();
    
    public PaneAction(TabShow tab, int width, int height)
    {
        setPrefWidth(width);
        setPrefHeight(height);
        
        getStyleClass().addAll("anchor-pane");
        
        JFXButton closeButton = new JFXButton();
        closeButton.setGraphic(CROSS_ICON);
        closeButton.setOnMouseClicked(e -> tab.closeTab());
        AnchorPane.setRightAnchor(closeButton, 10D);
        AnchorPane.setTopAnchor(closeButton, 10D);
        
        actionButton.setText("");
        actionButton.setGraphic(PLAY);
        actionButton.setId("button");
        actionButton.setFont(new Font(20));
        AnchorPane.setRightAnchor(actionButton, 10D);
        
        AnchorPane.setRightAnchor(watchedButton, 60D);
        AnchorPane.setTopAnchor(watchedButton, 10D);
        
        AnchorPane.setBottomAnchor(actionButton, 28D);
        
        getChildren().addAll(watchedButton, closeButton, actionButton);
    }
    
    public void setWatchedButtonIcon(TVEpisode episode)
    {
        watchedButton.setGraphic(episode.getWatched().get() ? EYE_CLOSED : EYE_OPENED);
        watchedButton.setOnMouseClicked(e -> {
            episode.getWatched().set(!episode.getWatched().get());
            watchedButton.setGraphic(episode.getWatched().get() ? EYE_CLOSED : EYE_OPENED);
        });
    }
    
    public void onSeasonSelected(TVSeason season)
    {
        setActionButtonDownloadSeason(season);
    }
    
    private void setActionButtonDownloadSeason(TVSeason season)
    {
        actionButton.setDisable(season.getPath() != null);
        actionButton.setVisible(season.getPath() == null);
        
        if (season.getPath() == null)
        {
            actionButton.setGraphic(DOWNLOAD);
            actionButton.setText("Download Season");
            actionButton.setOnMouseClicked(e -> {
                ShowManager.saveSeasonToDisk(season);
                setActionButtonDownloadSeason(season);
            });
        }
    }
    
    public void onEpisodeSelected(TVEpisode episode)
    {
        setActionButtonDownloadEpisode(episode);
    }
    
    private void setActionButtonDownloadEpisode(TVEpisode episode)
    {
        if (episode.getSeason().getPath() == null)
        {
            setActionButtonDownloadSeason(episode.getSeason());
        }
        else
        {
            if (episode.getVideoFile() != null)
            {
                actionButton.setDisable(false);
                actionButton.setVisible(true);
                actionButton.setGraphic(PLAY);
                actionButton.setText("Play Episode");
                actionButton.setOnMouseClicked(e -> {
                    episode.getWatched().set(true);
                    try
                    {
                        Desktop.getDesktop().open(episode.getVideoFile().toFile());
                    }
                    catch (IOException e1)
                    {
                        e1.printStackTrace();
                    }
                });
            }
            else
            {
                actionButton.setDisable(!episode.isReleased());
                actionButton.setVisible(true);
                actionButton.setGraphic(DOWNLOAD);
                actionButton.setText("Download Episode");
                actionButton.setOnMouseClicked(e -> {
                    DownloadManager.downloadEpisode(episode);
                    actionButton.setDisable(true);
                    actionButton.setGraphic(PLAY);
                    actionButton.setText("Play Episode");
                    
                });
            }
        }
    }
}
