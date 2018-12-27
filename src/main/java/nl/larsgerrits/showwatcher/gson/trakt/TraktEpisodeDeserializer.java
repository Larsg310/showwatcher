package nl.larsgerrits.showwatcher.gson.trakt;

import com.google.gson.*;
import nl.larsgerrits.showwatcher.api_impl.info.trakt.TraktEpisode;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.Date;

public class TraktEpisodeDeserializer implements JsonDeserializer<TraktEpisode>
{
    @SuppressWarnings("Duplicates")
    @Override
    public TraktEpisode deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        JsonObject obj = json.getAsJsonObject();
        
        if (!obj.get("title").isJsonNull())
        {
            String title = obj.get("title").getAsString();
            int episodeNumber = obj.get("number").getAsInt();
            String overview = obj.get("overview").isJsonNull() ? "" : obj.get("overview").getAsString();
            
            JsonElement dateElement = obj.get("first_aired");
            Date date = dateElement.isJsonNull() ? new Date(0) : Date.from(Instant.parse(dateElement.getAsString()));
            
            return new TraktEpisode(title, episodeNumber, date, overview);
        }
        else return null;
    }
}
