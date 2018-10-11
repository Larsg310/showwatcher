package nl.larsgerrits.showwatcher;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import nl.larsgerrits.showwatcher.api.tmdb.IDMapper;
import nl.larsgerrits.showwatcher.manager.ShowManager;

import java.io.IOException;

public class Main extends Application
{
    @Override
    public void start(Stage stage) throws IOException
    {
        stage.setOnCloseRequest(e -> {
            Threading.close();
            IDMapper.close();
            ShowManager.close();
        });
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        
        Scene scene = new Scene(loader.load());
        
        scene.getStylesheets().add(getClass().getResource("/css/stylesheet.css").toString());
        
        stage.setScene(scene);
        stage.setTitle("Show Watcher");
        stage.show();
    }
    
    public static void main(String[] args)
    {
        launch(args);
    }
}
