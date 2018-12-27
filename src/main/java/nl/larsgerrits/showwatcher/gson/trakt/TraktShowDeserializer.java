package nl.larsgerrits.showwatcher.gson.trakt;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import nl.larsgerrits.showwatcher.api_impl.info.trakt.TraktShow;

import java.lang.reflect.Type;

public class TraktShowDeserializer implements JsonDeserializer<TraktShow>
{
    @SuppressWarnings("Duplicates")
    @Override
    public TraktShow deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        String overview = json.getAsJsonObject().get("overview").getAsString();
        
        return new TraktShow(overview);
    }
}
