package nl.larsgerrits.showwatcher.components.tab;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import nl.larsgerrits.showwatcher.manager.DownloadManager;
import nl.larsgerrits.showwatcher.manager.ShowManager;
import nl.larsgerrits.showwatcher.show.TVEpisode;
import nl.larsgerrits.showwatcher.show.TVShow;

import java.util.List;
import java.util.stream.Collectors;

public class TabDownload extends Tab
{
    public static final FontAwesomeIconView DOWNLOAD = new FontAwesomeIconView(FontAwesomeIcon.DOWNLOAD, "20px");
    
    private VBox downloadQueue = new VBox();
    private VBox downloadablePanes = new VBox();
    
    public TabDownload()
    {
        DownloadManager.addDownloadAddedCallback(downloadQueue.getChildren()::add);
        DownloadManager.addDownloadFinishedCallback(downloadQueue.getChildren()::remove);
        DownloadManager.addDownloadFailedCallback(downloadQueue.getChildren()::remove);
        DownloadManager.addDownloadCanceledCallback(downloadQueue.getChildren()::remove);
    
        DownloadManager.addDownloadablePaneCallback(downloadablePanes.getChildren()::add);
        DownloadManager.removeDownloadablePaneCallback(downloadablePanes.getChildren()::remove);
        
        setGraphic(DOWNLOAD);
        
        AnchorPane pane = new AnchorPane();
        pane.setId("bordered");
        pane.getStyleClass().addAll("anchor-pane");
        
        downloadQueue.setFillWidth(false);
        
        AnchorPane.setTopAnchor(downloadQueue, 0D);
        AnchorPane.setBottomAnchor(downloadQueue, 0D);
        AnchorPane.setLeftAnchor(downloadQueue, 0D);
        
        ScrollPane downloadQueuePane = new ScrollPane();
        downloadQueuePane.setId("downloadQueue");
        downloadQueuePane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        downloadQueuePane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        downloadQueuePane.setContent(downloadQueue);
        
        AnchorPane.setTopAnchor(downloadQueuePane, 0D);
        AnchorPane.setBottomAnchor(downloadQueuePane, 0D);
        AnchorPane.setLeftAnchor(downloadQueuePane, 0D);
        
        downloadablePanes.setFillWidth(false);
        downloadablePanes.getStyleClass().add("anchor-pane-2");
        
        AnchorPane.setTopAnchor(downloadablePanes, 0D);
        AnchorPane.setRightAnchor(downloadablePanes, 0D);
        AnchorPane.setBottomAnchor(downloadablePanes, 0D);
    
        ScrollPane downloadableListPane = new ScrollPane();
        downloadableListPane.setId("downloadablePanes");
        downloadableListPane.getStyleClass().add("anchor-pane-2");
    
        downloadableListPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        downloadableListPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        downloadableListPane.setContent(downloadablePanes);
        
        AnchorPane.setTopAnchor(downloadableListPane, 0D);
        AnchorPane.setBottomAnchor(downloadableListPane, 0D);
        AnchorPane.setRightAnchor(downloadableListPane, 0D);
        
        pane.getChildren().addAll(downloadQueuePane, downloadableListPane/*, downloadAll*/);
        setContent(pane);
    }
    
    private void downloadAll()
    {
        List<TVShow> shows = ShowManager.getTVShows();
        shows.forEach(ShowManager::checkForNewUpdates);
        List<TVEpisode> downloadableEpisodes = shows.stream()//
                                                    .flatMap(s -> s.getSeasons().stream())//
                                                    .flatMap(s -> s.getEpisodes().stream())//
                                                    .filter(e -> e.getVideoFilePath() == null)//
                                                    .filter(TVEpisode::isReleased)//
                                                    .collect(Collectors.toList());
        
        if (!downloadableEpisodes.isEmpty())
        {
            downloadableEpisodes.forEach(DownloadManager::downloadEpisode);
        }
    }
}
