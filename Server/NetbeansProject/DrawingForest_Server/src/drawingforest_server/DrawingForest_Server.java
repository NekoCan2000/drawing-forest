
package drawingforest_server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class DrawingForest_Server extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ServerLog.fxml"));
        Parent root = fxmlLoader.load();
        
        Scene scene = new Scene(root);
        
        stage.setScene(scene);
        stage.show();
        
        ServerLogController slc = fxmlLoader.getController();
        //ウインドウ閉じる時の処理
        stage.setOnCloseRequest((e->{
            slc.closeServer();
        }));
    }

    
    public static void main(String[] args) {
        launch(args);
    }
    
}
