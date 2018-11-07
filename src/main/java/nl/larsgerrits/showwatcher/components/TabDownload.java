package nl.larsgerrits.showwatcher.components;

import com.jfoenix.controls.JFXButton;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import nl.larsgerrits.showwatcher.download.Download;
import nl.larsgerrits.showwatcher.manager.DownloadManager;
import nl.larsgerrits.showwatcher.manager.ShowManager;
import nl.larsgerrits.showwatcher.show.TVEpisode;
import nl.larsgerrits.showwatcher.show.TVShow;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TabDownload extends Tab
{
    private final FontAwesomeIconView DOWNLOAD = new FontAwesomeIconView(FontAwesomeIcon.DOWNLOAD, "20px");
    
    private ScrollPane scrollPane = new ScrollPane();
    private VBox downloads = new VBox();
    private JFXButton downloadAll = new JFXButton();
    
    private List<TVEpisode> toDownload = new ArrayList<>();
    
    public TabDownload()
    {
        DownloadManager.setDownloadAddedCallback(downloads.getChildren()::add);
        DownloadManager.setDownloadFinishedCallback(this::downloadFinished);
        setGraphic(DOWNLOAD);
        
        AnchorPane pane = new AnchorPane();
        pane.setId("bordered");
        pane.getStyleClass().addAll("anchor-pane");
        
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        //        scrollPane.setPrefHeight(1000);
        scrollPane.setPrefWidth(300);
        scrollPane.setLayoutX(10);
        scrollPane.setLayoutY(40);
        scrollPane.setFitToWidth(true);
        
        downloadAll.setId("button");
        downloadAll.setText("Download All");
        downloadAll.setOnMouseClicked(e -> downloadAll());
        AnchorPane.setTopAnchor(downloadAll, 16D);
        AnchorPane.setRightAnchor(downloadAll, 16D);
        
        // downloads.setPrefWidth(800);
        // downloads.setPrefHeight(1000);
        // AnchorPane.setTopAnchor(downloads, 10D);
        // AnchorPane.setLeftAnchor(downloads, 10D);
        
        // downloads.getChildren().add(new Download(ShowManager.getTVShows().get(0).getSeasons().get(2).getEpisodes().get(7), .70));
        // downloads.getChildren().add(new Download(ShowManager.getTVShows().get(0).getSeasons().get(5).getEpisodes().get(19), .70));
        
        downloads.setSpacing(10);
        
        scrollPane.setContent(downloads);
        pane.getChildren().addAll(scrollPane, downloadAll);
        setContent(pane);
    }
    
    private void downloadFinished(Download download)
    {
        if (download != null) downloads.getChildren().remove(download);
        if (!toDownload.isEmpty())
        {
            TVEpisode e = toDownload.remove(0);
            DownloadManager.downloadEpisode(e);
        }
    }
    
    private void downloadAll()
    {
        List<TVShow> shows = ShowManager.getTVShows();
        List<TVEpisode> episodes = shows.stream()//
                                        .flatMap(s -> s.getSeasons().stream())//
                                        .flatMap(s -> s.getEpisodes().stream())//
                                        .filter(e -> e.getVideoFile() == null)//
                                        .filter(e -> e.getReleaseDate().getTime() > 0)//
                                        .filter(e -> e.getReleaseDate().getTime() < System.currentTimeMillis())//
                                        .collect(Collectors.toList());
        
        toDownload.addAll(episodes);
        DownloadManager.downloadEpisode(toDownload.remove(0));
        // DownloadManager.downloadEpisodes(episodes);
    }
}
