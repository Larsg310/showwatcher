package nl.larsgerrits.showwatcher.manager;

import com.frostwire.jlibtorrent.AlertListener;
import com.frostwire.jlibtorrent.SessionManager;
import com.frostwire.jlibtorrent.TorrentInfo;
import com.frostwire.jlibtorrent.alerts.*;
import javafx.application.Platform;
import nl.larsgerrits.showwatcher.Threading;
import nl.larsgerrits.showwatcher.components.pane.DownloadablePane;
import nl.larsgerrits.showwatcher.components.poster.PosterDownload;
import nl.larsgerrits.showwatcher.download.Download;
import nl.larsgerrits.showwatcher.download.Torrent;
import nl.larsgerrits.showwatcher.download.TorrentCollector;
import nl.larsgerrits.showwatcher.show.TVEpisode;
import nl.larsgerrits.showwatcher.show.TVShow;
import nl.larsgerrits.showwatcher.util.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public final class DownloadManager
{
    private static AtomicBoolean downloading = new AtomicBoolean(false);
    
    private static Consumer<PosterDownload> downloadStartedCallback = (p) -> {
        System.out.println("Download started");
    };
    private static Consumer<PosterDownload> downloadFinishedCallback = (p) -> {
        downloading.set(false);
        System.out.println("Download finished...");
        refresh();
    };
    private static Consumer<PosterDownload> downloadFailedCallback = (p) -> {
        downloading.set(false);
        System.out.println("Download failed...");
        refresh();
    };
    private static Consumer<PosterDownload> downloadCanceledCallback = (p) -> {
        System.out.println("Download cancelled...");
        refresh();
    };
    private static Consumer<DownloadablePane> addDownloadablePaneCallback = (p) -> {};
    private static Consumer<DownloadablePane> removeDownloadablePaneCallback = (p) -> {};
    
    private static List<Download> downloadList = new ArrayList<>();
    
    private static Map<TVShow, DownloadablePane> downloadablePaneMap = new HashMap<>();
    
    private DownloadManager() {}
    
    public static void addDownloadAddedCallback(Consumer<PosterDownload> downloadStartedCallback)
    {
        DownloadManager.downloadStartedCallback = downloadStartedCallback.andThen(DownloadManager.downloadStartedCallback);
    }
    
    public static void addDownloadFinishedCallback(Consumer<PosterDownload> downloadFinishedCallback)
    {
        DownloadManager.downloadFinishedCallback = downloadFinishedCallback.andThen(DownloadManager.downloadFinishedCallback);
    }
    
    public static void addDownloadFailedCallback(Consumer<PosterDownload> downloadFailedCallback)
    {
        DownloadManager.downloadFailedCallback = downloadFailedCallback.andThen(DownloadManager.downloadFailedCallback);
    }
    
    public static void addDownloadCanceledCallback(Consumer<PosterDownload> downloadCanceledCallback)
    {
        DownloadManager.downloadCanceledCallback = downloadCanceledCallback.andThen(DownloadManager.downloadCanceledCallback);
    }
    
    public static void addDownloadablePaneCallback(Consumer<DownloadablePane> addDownloadablePaneCallback)
    {
        DownloadManager.addDownloadablePaneCallback = addDownloadablePaneCallback.andThen(DownloadManager.addDownloadablePaneCallback);
    }
    
    public static void removeDownloadablePaneCallback(Consumer<DownloadablePane> removeDownloadablePaneCallback)
    {
        DownloadManager.removeDownloadablePaneCallback = removeDownloadablePaneCallback.andThen(DownloadManager.removeDownloadablePaneCallback);
    }
    
    public static void downloadEpisode(TVEpisode episode)
    {
        Threading.API_THREAD.execute(() -> {
            
            // downloadList.add(episode);
            
            Torrent torrent = TorrentCollector.getTorrent(episode);
            if (torrent != null)
            {
                Download download = new Download(episode, torrent);
                PosterDownload downloadPoster = new PosterDownload(download);
                
                Platform.runLater(() -> downloadStartedCallback.accept(downloadPoster));
                downloadList.add(download);
                
                refresh();
            }
        });
    }
    
    private static void refresh()
    {
        if (!downloading.get() && !downloadList.isEmpty())
        {
            downloading.set(true);
            Download download = downloadList.remove(0);
            
            Torrent torrent = download.getTorrent();
            
            String info = String.format("%s Episode %dx%02d", download.getEpisode().getSeason().getShow().getTitle(), download.getEpisode().getSeason().getSeasonNumber(), download.getEpisode().getEpisodeNumber());
            if (torrent != null)
            {
                System.out.println("Downloading " + info + "(" + download.getPeers() + " peers, " + download.getSeeds() + " seeds)");
                Threading.DOWNLOAD_THREAD.execute(() -> downloadMagnet(download, torrent));
            }
        }
    }
    
    private static void downloadMagnet(Download download, Torrent torrent)
    {
        TVEpisode episode = download.getEpisode();
        
        try
        {
            download.setSessionManager(new SessionManager());
            download.setSaveDir(FileUtils.getSaveDir(episode.getSeason()));
            
            if (download.getSaveDir() == null)
            {
                Platform.runLater(() -> downloadFailedCallback.accept(download.getPoster()));
                return;
            }
            
            if (Files.notExists(download.getSaveDir())) Files.createDirectory(download.getSaveDir());
            
            AlertListener showListener = new AlertListener()
            {
                private String fileName = FileUtils.getEpisodeFileName(episode.getEpisodeNumber(), episode.getTitle());
                private double prevProgress = -1D;
                
                @Override
                public int[] types()
                {
                    return null;
                }
                
                @Override
                public void alert(Alert<?> alert)
                {
                    AlertType type = alert.type();
                    switch (type)
                    {
                        case ADD_TORRENT:
                            ((AddTorrentAlert) alert).handle().renameFile(0, fileName);
                            ((AddTorrentAlert) alert).handle().resume();
                            break;
                        case TORRENT_FINISHED:
                            ((TorrentFinishedAlert) alert).handle().pause();
                            // System.out.println("Finished download!" + (fileName));
                            episode.setVideoFile(download.getSaveDir().resolve(fileName));
                            Platform.runLater(() -> downloadFinishedCallback.accept(download.getPoster()));
                            break;
                        case BLOCK_FINISHED:
                            double progress = ((BlockFinishedAlert) alert).handle().status().progress();
                            if (progress > prevProgress)
                            {
                                prevProgress = progress;
                                download.setProgress(progress);
                            }
                            break;
                        default:
                            break;
                    }
                }
            };
            
            download.getSessionManager().addListener(showListener);
            download.getSessionManager().start();
            
            if (waitForNodesInDHT(download.getSessionManager()))
            {
                byte[] data = download.getSessionManager().fetchMagnet(torrent.getMagnetUrl(), 60);
                
                TorrentInfo ti = TorrentInfo.bdecode(data);
                
                download.getSessionManager().download(ti, download.getSaveDir().toFile());
            }
        }
        catch (Exception e)
        {
            Platform.runLater(() -> downloadFailedCallback.accept(download.getPoster()));
            System.out.println("Download failed...");
            refresh();
        }
    }
    
    private static boolean waitForNodesInDHT(final SessionManager s) throws InterruptedException
    {
        final CountDownLatch signal = new CountDownLatch(1);
        
        final Timer timer = new Timer();
        timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                long nodes = s.stats().dhtNodes();
                if (nodes >= 10)
                {
                    signal.countDown();
                    timer.cancel();
                }
            }
        }, 0, 1000);
        
        return signal.await(60, TimeUnit.SECONDS);
    }
    
    public static void addToDownloadableList(TVEpisode episode)
    {
        TVShow show = episode.getSeason().getShow();
        DownloadablePane pane = downloadablePaneMap.computeIfAbsent(show, s -> {
            DownloadablePane newPane = new DownloadablePane(s);
            Platform.runLater(() -> addDownloadablePaneCallback.accept(newPane));
            return newPane;
        });
        pane.addEpisode(episode);
    }
    
    public static void cancelDownload(PosterDownload poster)
    {
        if (poster.getDownload().getSessionManager() != null)
        {
            downloading.set(false);
            Threading.DOWNLOAD_THREAD.execute(() -> {
                poster.getDownload().getSessionManager().stop();
                try
                {
                    TVEpisode episode = poster.getDownload().getEpisode();
                    String fileName = FileUtils.getEpisodeFileName(episode.getEpisodeNumber(), episode.getTitle());
                    Path filePath = poster.getDownload().getSaveDir().resolve(fileName);
                    if (Files.exists(filePath))
                    {
                        Files.delete(filePath);
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            });
        }
        else
        {
            downloadList.remove(poster.getDownload());
        }
        downloadCanceledCallback.accept(poster);
    }
}
