package nl.larsgerrits.showwatcher.show;

import com.google.common.base.Objects;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class TVSeason implements Iterable<TVEpisode>
{
    private TVShow show;
    private int seasonNumber;
    private List<TVEpisode> episodes = new ArrayList<>();
    private Path path;
    private int totalEpisodes;
    private Date releaseDate;
    
    private boolean dirty = false;
    private Consumer<TVEpisode> episodeAdded;
    
    public TVSeason(TVShow show, int seasonNumber, Path path, int totalEpisodes, Date releaseDate)
    {
        this.show = show;
        this.seasonNumber = seasonNumber;
        this.path = path;
        this.totalEpisodes = totalEpisodes;
        this.releaseDate = releaseDate;
    }
    
    public void addEpisode(TVEpisode episode, boolean notify)
    {
        episodes.add(episode);
        setDirty(true);
        if (notify) getEpisodeAdded().accept(episode);
    }
    
    public void setTotalEpisodes(int totalEpisodes)
    {
        this.totalEpisodes = totalEpisodes;
    }
    
    private Consumer<TVEpisode> getEpisodeAdded()
    {
        return episodeAdded == null ? t -> {} : episodeAdded;
    }
    
    public void setEpisodeAdded(Consumer<TVEpisode> seasonAdded)
    {
        this.episodeAdded = seasonAdded;
    }
    
    public boolean hasEpisode(int number)
    {
        return episodes.stream().anyMatch(episode -> episode.getEpisodeNumber() == number);
    }
    
    public TVEpisode getEpisode(int number)
    {
        return episodes.stream().filter(episode -> episode.getEpisodeNumber() == number).findFirst().orElse(null);
    }
    
    @Override
    @Nonnull
    public Iterator<TVEpisode> iterator()
    {
        return episodes.iterator();
    }
    
    public List<TVEpisode> getEpisodes()
    {
        return episodes;
    }
    
    public Path getPath()
    {
        return path;
    }
    
    public void setPath(Path path)
    {
        this.path = path;
    }
    
    public Date getReleaseDate()
    {
        return releaseDate;
    }
    
    public int getSeasonNumber()
    {
        return seasonNumber;
    }
    
    public TVShow getShow()
    {
        return show;
    }
    
    public int getTotalEpisodes()
    {
        return Math.max(totalEpisodes, episodes.size());
    }
    
    public boolean isDirty()
    {
        return dirty;
    }
    
    public void setDirty(boolean dirty)
    {
        this.dirty = dirty;
        // if (dirty)
        // {
        //     Threading.FILE_THREAD.execute(() -> {
        //         ShowManager.saveSeasonToDisk(this);
        //         this.dirty = false;
        //     });
        // }
    }
    
    public boolean isFullyDownloaded()
    {
        if (episodes.isEmpty()) return false;
        if (episodes.size() < getTotalEpisodes()) return false;
        return episodes.stream().allMatch(episode -> episode.getVideoFilePath() != null);
    }
    
    public boolean isWatched()
    {
        return episodes.stream().allMatch(e -> e.getWatched().get());
    }
    
    public void setWatched(boolean watched)
    {
        episodes.forEach(e -> e.getWatched().set(watched));
        setDirty(true);
    }
    
    public void setReleaseDate(Date releaseDate)
    {
        this.releaseDate = releaseDate;
        setDirty(true);
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hashCode(show, seasonNumber);
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TVSeason tvSeason = (TVSeason) o;
        return seasonNumber == tvSeason.seasonNumber && Objects.equal(show, tvSeason.show);
    }
    
    @Override
    public String toString()
    {
        return "TVSeason{" + "show=" + show + ", seasonNumber=" + seasonNumber + ", episodes=" + episodes + ", path=" + path + ", totalEpisodes=" + totalEpisodes + ", releaseDate=" + releaseDate + '}';
    }
}
