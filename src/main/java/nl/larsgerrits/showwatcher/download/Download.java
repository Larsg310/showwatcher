package nl.larsgerrits.showwatcher.download;

import com.jfoenix.controls.JFXProgressBar;
import javafx.application.Platform;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import nl.larsgerrits.showwatcher.controller.MainController;
import nl.larsgerrits.showwatcher.show.TVEpisode;

public class Download extends AnchorPane
{
    private JFXProgressBar progressBar = new JFXProgressBar();
    private Tooltip tooltip = new Tooltip("0%");
    
    public Download(TVEpisode episode)
    {
        setId("bordered");
        setPrefHeight(70);
        setPrefWidth(200);
        
        Text text = new Text(episode.getSeason().getShow().getTitle() + " " + episode.getSeason().getSeasonNumber() + "x" + episode.getEpisodeNumber());
        text.setFont(new Font(20));
        text.setId("text");
        AnchorPane.setTopAnchor(text, 4D);
        AnchorPane.setLeftAnchor(text, 8D);
        
        progressBar.setPrefHeight(20);
        progressBar.setPrefWidth(140);
        progressBar.setId("bordered");
        MainController.bindTooltip(progressBar, tooltip);
        AnchorPane.setBottomAnchor(progressBar, 8D);
        AnchorPane.setLeftAnchor(progressBar, 8D);
        
        getChildren().addAll(text, progressBar);
    }
    
    public void setProgress(double progress)
    {
        Platform.runLater(() -> {
            progressBar.setProgress(progress);
            tooltip.setText("Progress: " + String.format("%.2f", progress * 100).replace(',', '.') + '%');
        });
    }
}
