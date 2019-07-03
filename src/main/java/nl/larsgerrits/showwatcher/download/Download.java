package nl.larsgerrits.showwatcher.download;

import com.frostwire.jlibtorrent.SessionManager;
import javafx.application.Platform;
import nl.larsgerrits.showwatcher.components.poster.PosterDownload;
import nl.larsgerrits.showwatcher.show.TVEpisode;

import java.nio.file.Path;
import java.util.function.Consumer;

public class Download
{
    private TVEpisode episode;
    private Torrent torrent;
    
    private PosterDownload poster;
    
    private Consumer<Double> progressConsumer = (s) -> {};
    private SessionManager sessionManager;
    private Path saveDir;
    
    public Download(TVEpisode episode, Torrent torrent)
    {
        this.episode = episode;
        this.torrent = torrent;
    }
    
    public void setProgressConsumer(Consumer<Double> progressConsumer)
    {
        this.progressConsumer = progressConsumer;
    }
    
    public void setProgress(double progress)
    {
        Platform.runLater(() -> progressConsumer.accept(progress));
    }
    
    public void setPoster(PosterDownload poster)
    {
        this.poster = poster;
    }
    
    public void setSessionManager(SessionManager sessionManager)
    {
        this.sessionManager = sessionManager;
    }
    
    public void setSaveDir(Path saveDir)
    {
        this.saveDir = saveDir;
    }
    
    public Path getSaveDir()
    {
        return saveDir;
    }
    
    public int getPeers()
    {
        return torrent.getPeers();
    }
    
    public PosterDownload getPoster()
    {
        return poster;
    }
    
    public int getSeeds()
    {
        return torrent.getSeeds();
    }
    
    public SessionManager getSessionManager()
    {
        return sessionManager;
    }
    
    public Torrent getTorrent()
    {
        return torrent;
    }
    
    public TVEpisode getEpisode()
    {
        return episode;
    }
}
