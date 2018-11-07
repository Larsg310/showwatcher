package nl.larsgerrits.showwatcher.gson.eztv;

import com.google.gson.*;
import nl.larsgerrits.showwatcher.download.Torrent;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class EZTVDeserializer implements JsonDeserializer<List<Torrent>>
{
    @Override
    public List<Torrent> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        List<Torrent> torrents = new ArrayList<>();
        if (json.getAsJsonObject().has("torrents"))
        {
            JsonArray torrentsArray = json.getAsJsonObject().get("torrents").getAsJsonArray();
            
            for (JsonElement e : torrentsArray)
            {
                JsonObject torrentObj = e.getAsJsonObject();
                
                String title = torrentObj.get("title").getAsString();
                String magnet_url = torrentObj.get("magnet_url").getAsString();
                int season = torrentObj.get("season").getAsInt();
                int episode = torrentObj.get("episode").getAsInt();
                int seeds = torrentObj.get("seeds").getAsInt();
                int peers = torrentObj.get("seeds").getAsInt();
                long size_bytes = torrentObj.get("size_bytes").getAsLong();
                
                torrents.add(new Torrent(title, magnet_url, season, episode, seeds, peers, size_bytes));
            }
        }
        return torrents;
    }
}
