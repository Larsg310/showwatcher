package nl.larsgerrits.showwatcher.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public final class HTTPUtils
{
    private static final int RATE_LIMIT_RESPONSE = 429;
    
    private HTTPUtils() {}
    
    public static String get(String url)
    {
        try
        {
            HttpURLConnection con = getConnection(url);
            if (con.getResponseCode() == RATE_LIMIT_RESPONSE)
            {
                int retryAfter = Integer.valueOf(con.getHeaderField("Retry-After"));
                con.disconnect();
                Thread.sleep(retryAfter * 1000 + 1000);
                con = getConnection(url);
            }
            
            if (con.getResponseCode() == HttpURLConnection.HTTP_OK)
            {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                
                while ((inputLine = in.readLine()) != null) response.append(inputLine);
                in.close();
                
                return response.toString();
            }
        }
        catch (IOException | InterruptedException e) {e.printStackTrace();}
        
        return "";
    }
    
    private static HttpURLConnection getConnection(String url) throws IOException
    {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        con.connect();
        return con;
    }
}
