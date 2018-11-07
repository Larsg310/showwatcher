package nl.larsgerrits.showwatcher.util;

import nl.larsgerrits.showwatcher.Settings;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

public final class HTTPUtils
{
    private static final int RATE_LIMIT_RESPONSE = 429;
    
    private HTTPUtils() {}
    
    public static String get(String url)
    {
        return get(url, null);
    }
    
    public static String get(String url, Map<String, String> headers)
    {
        try
        {
            HttpURLConnection con = getConnection(url, headers);
            if (con.getResponseCode() == RATE_LIMIT_RESPONSE)
            {
                int retryAfter = Integer.valueOf(con.getHeaderField("Retry-After"));
                con.disconnect();
                Thread.sleep(retryAfter * 1000 + 1000);
                con = getConnection(url, headers);
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
    
    public static Stream<String> getFromZip(String urlString)
    {
        try
        {
            Path path = Settings.CACHE_PATH.resolve("_ettv.txt");
            
            if (Files.notExists(path))
            {
                HttpURLConnection con = getConnection(urlString, null);
                
                ReadableByteChannel rbc = Channels.newChannel(con.getInputStream());
                
                Path gzipPath = Settings.CACHE_PATH.resolve("_ettv.txt.gz");
                
                FileOutputStream fos = new FileOutputStream(gzipPath.toFile());
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                fos.close();
                rbc.close();
                
                FileInputStream fis = new FileInputStream(gzipPath.toFile());
                GZIPInputStream gis = new GZIPInputStream(fis);
                ReadableByteChannel in = Channels.newChannel(gis);
                
                WritableByteChannel out = Channels.newChannel(new FileOutputStream(path.toFile()));
                ByteBuffer buffer = ByteBuffer.allocate(65536);
                while (in.read(buffer) != -1)
                {
                    buffer.flip();
                    out.write(buffer);
                    buffer.clear();
                }
                
                out.close();
                in.close();
                gis.close();
                fis.close();
                
                Files.delete(gzipPath);
            }
            
            return Files.lines(path);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return Stream.of();
    }
    
    private static HttpURLConnection getConnection(String url, Map<String, String> headers) throws IOException
    {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        
        if (headers != null) headers.forEach(con::setRequestProperty);
        
        con.connect();
        return con;
    }
}
