package nl.larsgerrits.showwatcher.show;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class Torrent
{
    private final String title;
    private final String magnetUrl;
    private final int season;
    private final int episode;
    private final int seeds;
    private final int peers;
    private final long sizeInBytes;
    
    public Torrent(String title, String magnetUrl, int season, int episode, int seeds, int peers, long sizeInBytes)
    {
        this.title = title;
        this.magnetUrl = magnetUrl;
        this.season = season;
        this.episode = episode;
        this.seeds = seeds;
        this.peers = peers;
        this.sizeInBytes = sizeInBytes;
    }
    
    public int getEpisode()
    {
        return episode;
    }
    
    public String getMagnetUrl()
    {
        return magnetUrl;
    }
    
    public int getPeers()
    {
        return peers;
    }
    
    public int getSeason()
    {
        return season;
    }
    
    public int getSeeds()
    {
        return seeds;
    }
    
    public String getTitle()
    {
        return title;
    }
    
    public long getSizeInBytes()
    {
        return sizeInBytes;
    }
    
    @Override
    public String toString()
    {
        return new ToStringBuilder(this).append("title", title).append("season", season).append("episode", episode).toString();
    }
}
