package nl.larsgerrits.showwatcher.components.poster;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXProgressBar;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import nl.larsgerrits.showwatcher.download.Download;
import nl.larsgerrits.showwatcher.manager.DownloadManager;
import nl.larsgerrits.showwatcher.manager.ImageManager;
import nl.larsgerrits.showwatcher.show.TVEpisode;

import javax.annotation.Nonnull;

public class PosterDownload extends AnchorPane
{
    private final Download download;
    
    private ImageView imageView = new ImageView();
    private Text textEpisode = new Text();
    private Text textTitle = new Text();
    private JFXProgressBar progressBar = new JFXProgressBar();
    private Text textProgress = new Text();
    private Text textPeerSeeds = new Text();
    private JFXButton cancelButton = new JFXButton();
    
    public PosterDownload(@Nonnull Download download)
    {
        this.download = download;
        download.setPoster(this);
        
        setPrefHeight(155);
        setPrefWidth(500 - 2);
        setId("poster");
        
        imageView.setFitWidth(188);
        imageView.setPickOnBounds(true);
        imageView.setPreserveRatio(true);
        
        AnchorPane.setLeftAnchor(imageView, 10D);
        AnchorPane.setRightAnchor(imageView, 10D);
        AnchorPane.setTopAnchor(imageView, 10D);
        
        TVEpisode episode = download.getEpisode();
        ImageManager.getPosterURLForTVEpisode(episode, this::setImage);
        
        textEpisode.setId("text");
        textEpisode.setLayoutX(214);
        textEpisode.setFont(new Font(18));
        textEpisode.setText(episode.getSeason().getShow().getTitle() + " " + episode.getSeason().getSeasonNumber() + "x" + String.format("%02d", episode.getEpisodeNumber()));
        textEpisode.setWrappingWidth(550);
        
        AnchorPane.setTopAnchor(textEpisode, 4D);
        
        textTitle.setId("text");
        textTitle.setLayoutX(214);
        textTitle.setFont(new Font(14));
        textTitle.setText("Title: " + episode.getTitle());
        textTitle.setWrappingWidth(300);
        
        AnchorPane.setTopAnchor(textTitle, 30D);
        
        progressBar.setPrefHeight(20);
        progressBar.setPrefWidth(140);
        progressBar.setId("bordered");
        
        AnchorPane.setBottomAnchor(progressBar, 8D);
        AnchorPane.setLeftAnchor(progressBar, 212D);
        
        textProgress.setId("text");
        textProgress.setText("0.00%");
        textProgress.setLayoutX(360);
        textProgress.setFont(new Font(14));
        download.setProgressConsumer(this::setProgress);
        
        AnchorPane.setBottomAnchor(textProgress, 10D);
        
        textPeerSeeds.setId("text");
        textPeerSeeds.setText(download.getPeers() + " peers / " + download.getSeeds() + " seeds");
        textPeerSeeds.setLayoutX(214);
        textPeerSeeds.setFont(new Font(14));
        
        AnchorPane.setBottomAnchor(textPeerSeeds, 30D);
        
        cancelButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.TIMES, "20px"));
        cancelButton.setOnMouseClicked(e -> DownloadManager.cancelDownload(this));
        
        AnchorPane.setTopAnchor(cancelButton, 4D);
        AnchorPane.setRightAnchor(cancelButton, 4D);
        
        getChildren().addAll(imageView, textEpisode, textTitle, progressBar, textProgress, textPeerSeeds, cancelButton);
    }
    
    private void setImage(Image image)
    {
        imageView.setImage(image);
        double imageHeight = 188 * (image.getHeight() / image.getWidth());
        // textEpisode.setLayoutX(26 + image.getWidth());
        
        setPrefHeight(20 + imageHeight);
    }
    
    private void setProgress(double progress)
    {
        progressBar.setProgress(progress);
        textProgress.setText(String.format("%02.2f%%", progress * 100D).replace(',', '.'));
    }
    
    public Download getDownload()
    {
        return download;
    }
}
