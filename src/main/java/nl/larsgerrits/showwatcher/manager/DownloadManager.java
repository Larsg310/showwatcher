package nl.larsgerrits.showwatcher.manager;

import com.frostwire.jlibtorrent.AlertListener;
import com.frostwire.jlibtorrent.SessionManager;
import com.frostwire.jlibtorrent.TorrentInfo;
import com.frostwire.jlibtorrent.alerts.*;
import javafx.application.Platform;
import nl.larsgerrits.showwatcher.Threading;
import nl.larsgerrits.showwatcher.api_impl.TorrentCollector;
import nl.larsgerrits.showwatcher.download.Download;
import nl.larsgerrits.showwatcher.show.TVEpisode;
import nl.larsgerrits.showwatcher.show.Torrent;
import nl.larsgerrits.showwatcher.util.FileUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class DownloadManager
{
    private static Consumer<Download> downloadAddedCallback;
    private static Consumer<Download> downloadFinishedCallback;
    
    private DownloadManager() {}
    
    public static void setDownloadAddedCallback(Consumer<Download> downloadCallback)
    {
        DownloadManager.downloadAddedCallback = downloadCallback;
    }
    
    public static void setDownloadFinishedCallback(Consumer<Download> downloadFinishedCallback)
    {
        DownloadManager.downloadFinishedCallback = downloadFinishedCallback;
    }
    
    public static void downloadEpisode(TVEpisode episode)
    {
        // downloadQueue.add(episode);
        Threading.DOWNLOAD_THREAD.execute(() -> {
            Torrent torrent = TorrentCollector.getTorrent(episode);
            
            String info = String.format("%s Episode %dx%02d", episode.getSeason().getShow().getTitle(), episode.getSeason().getSeasonNumber(), episode.getEpisodeNumber());
            if (torrent != null)
            {
                System.out.println("Downloading " + info);
                Download download = downloadMagnet(episode, torrent.getMagnetUrl());
                if (downloadAddedCallback != null) Platform.runLater(() -> downloadAddedCallback.accept(download));
            }
            else
            {
                System.out.println("Failed downloading " + info);
                downloadFinishedCallback.accept(null);
            }
        });
    }
    
    private static Download downloadMagnet(TVEpisode episode, String magnetUrl)
    {
        Download download = new Download(episode);
        try
        {
            SessionManager manager = new SessionManager();
            
            Path saveDir = FileUtils.getSaveDir(episode);
            
            if (Files.notExists(saveDir)) Files.createDirectory(saveDir);
            
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
                            episode.setVideoFile(episode.getSeason().getPath().resolve(fileName));
                            if (downloadFinishedCallback != null) Platform.runLater(() -> downloadFinishedCallback.accept(download));
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
            
            manager.addListener(showListener);
            manager.start();
            
            waitForNodesInDHT(manager);
            byte[] data = manager.fetchMagnet(magnetUrl, 60);
            TorrentInfo ti = TorrentInfo.bdecode(data);
            
            // System.out.println(Entry.bdecode(data).toString());
            
            manager.download(ti, saveDir.toFile());
            
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        return download;
    }
    
    private static void waitForNodesInDHT(final SessionManager s) throws InterruptedException
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
        
        boolean r = signal.await(60, TimeUnit.SECONDS);
        if (!r) System.exit(0);
    }
}
