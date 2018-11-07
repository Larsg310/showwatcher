package nl.larsgerrits.showwatcher.download;

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
    
    private final Object data;
    
    public Torrent(String title, String magnetUrl, int season, int episode, int seeds, int peers, long sizeInBytes)
    {
        this(title, magnetUrl, season, episode, seeds, peers, sizeInBytes, null);
    }
    
    public Torrent(String title, String magnetUrl, int season, int episode, int seeds, int peers, long sizeInBytes, Object data)
    {
        this.title = title;
        this.magnetUrl = magnetUrl;
        this.season = season;
        this.episode = episode;
        this.seeds = seeds;
        this.peers = peers;
        this.sizeInBytes = sizeInBytes;
        this.data = data;
    }
    
    public Object getData()
    {
        return data;
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
    
    public long getSizeInBytes()
    {
        return sizeInBytes;
    }
    
    public String getTitle()
    {
        return title;
    }
    
    @Override
    public String toString()
    {
        return new ToStringBuilder(this).append("title", title).append("season", season).append("episode", episode).toString();
    }
}
