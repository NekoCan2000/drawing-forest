
package drawingforest_player;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.*;
import java.net.*;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;

public class Main extends Application {

    // 基礎となる土台
    public static Stage stage;
    // 送受信用のソケット
    public static Socket socket = null;
    // InputStreamのデータ丸ごと送れるクラス
    public static ObjectInputStream oInStream = null;
    // OutputStreamのデータ丸ごと受信できるクラス
    public static ObjectOutputStream oOutputStream = null;

    @Override // 起動時に呼ばれるメソッド
    public void start(Stage stage) throws Exception {
        System.out.println("Start");
        // 接続画面を読み込み
        Parent root = FXMLLoader.load(getClass().getResource("ConnectFXML.fxml"));
        // Connect.javaに処理も移動

        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.show();
        Main.stage = stage;

        // Windowの閉じるボタンが押されたときに呼ばれるメソッド
        // の処理をラムダ式で記述
        stage.setOnCloseRequest((e -> {
            // スレッドのループを解除
            System.out.println("close");
            try {
                socket.close();
            } catch (Exception exc) {
                System.out.println("socket Network Error :" + exc);
                System.exit(1);
            }
            System.exit(0);
        }));
    }
    public static void main(String[] args) {
        launch(args);
    }
}
