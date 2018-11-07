package nl.larsgerrits.showwatcher.show;

import com.google.common.base.Objects;

import java.nio.file.Path;
import java.util.Date;

public class TVEpisode
{
    private String title;
    private int episodeNumber;
    private Path videoFile;
    private Date releaseDate;
    private boolean watched;
    private TVSeason season;
    
    public TVEpisode(String title, int episodeNumber, Path videoFile, Date releaseDate, TVSeason season, boolean watched)
    {
        this.title = title;
        this.episodeNumber = episodeNumber;
        this.videoFile = videoFile;
        this.releaseDate = releaseDate;
        this.season = season;
        this.watched = watched;
    }
    
    public void setVideoFile(Path videoFile)
    {
        this.videoFile = videoFile;
        season.setDirty(true);
    }
    
    public void setReleaseDate(Date releaseDate)
    {
        this.releaseDate = releaseDate;
    }
    
    public int getEpisodeNumber()
    {
        return episodeNumber;
    }
    
    public Path getVideoFile()
    {
        return videoFile;
    }
    
    public Date getReleaseDate()
    {
        return releaseDate;
    }
    
    public TVSeason getSeason()
    {
        return season;
    }
    
    public String getTitle()
    {
        return title;
    }
    
    public boolean isWatched()
    {
        return watched;
    }
    
    public void setWatched(boolean watched)
    {
        this.watched = watched;
        season.setDirty(true);
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hashCode(episodeNumber, season);
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TVEpisode episode = (TVEpisode) o;
        return episodeNumber == episode.episodeNumber && Objects.equal(season, episode.season);
    }
}
