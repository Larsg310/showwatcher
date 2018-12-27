package nl.larsgerrits.showwatcher;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import nl.larsgerrits.showwatcher.api_impl.info.tmdb.IDMapper;
import nl.larsgerrits.showwatcher.manager.DescriptionManager;
import nl.larsgerrits.showwatcher.manager.ShowManager;

public class Main extends Application
{
    public static int WIDTH = 1784;
    
    @Override
    public void start(Stage stage) throws Exception
    {
        stage.setOnCloseRequest(e -> {
            IDMapper.close();
            ShowManager.close();
            Threading.close();
            DescriptionManager.close();
        });
    
        
        stage.widthProperty().addListener((obs, o, n) -> WIDTH = n.intValue());
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        
        Scene scene = new Scene(loader.load());
        
        scene.getStylesheets().add(getClass().getResource("/css/dark_theme.css").toString());
        
        stage.setScene(scene);
        stage.setTitle("Show Watcher");
        stage.show();
        
    }
    
    public static void main(String[] args)
    {
        Natives.loadNatives();
        launch(args);
    }
}
