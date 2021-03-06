package nl.larsgerrits.showwatcher.gson.tmdb;

import com.google.gson.*;

import java.lang.reflect.Type;

public class TMDBShowPosterDeserializer implements JsonDeserializer<String>
{
    @Override
    public String deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        JsonObject obj = json.getAsJsonObject();
        JsonElement e = obj.get("poster_path");
        if (!e.isJsonNull())
        {
            return e.getAsString();
        }
        return "";
    }
}
