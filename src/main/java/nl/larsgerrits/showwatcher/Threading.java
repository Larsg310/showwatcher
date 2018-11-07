package nl.larsgerrits.showwatcher;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Threading
{
    public static ExecutorService IMAGE_THREAD = Executors.newSingleThreadExecutor();
    public static ExecutorService DOWNLOAD_THREAD = Executors.newSingleThreadExecutor();
    public static ExecutorService LOADING_THREAD = Executors.newSingleThreadExecutor();
    public static ExecutorService FILE_THREAD = Executors.newSingleThreadExecutor();
    public static ExecutorService API_THREAD = Executors.newSingleThreadExecutor();
    public static ExecutorService TEST_THREAD = Executors.newSingleThreadExecutor();
    
    
    public static void close(){
        IMAGE_THREAD.shutdown();
        DOWNLOAD_THREAD.shutdown();
        LOADING_THREAD.shutdown();
        FILE_THREAD.shutdown();
        API_THREAD.shutdown();
        TEST_THREAD.shutdown();
    }
}
