
package drawingforest_player;

import java.io.*;
import java.net.*;
import javafx.fxml.Initializable;
import java.util.*;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;

public class Connect implements Initializable {
    @FXML
    TextField IP;
    @FXML
    TextField Port;
    @FXML
    TextField Name;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    @FXML
    public void Decide() throws Exception {
        // 名前入力欄が空白なら接続させないif文
        String name = Name.getText();
        if (name.trim().length() == 0) {
            Name.setText("");
            Name.setPromptText("Input Nick Name!!!");
            return;
        }

        //10文字以上の名前なら接続させないif文
        if (name.length() > 10) {
            Name.setText("");
            Name.setPromptText("Please 10 Charactors Name");
            return;
        }

        // 接続情報が空白なら接続させないif文
        if(IP.getText().trim().length() * Port.getText().trim().length() == 0){
            return ;
        }

        System.out.println("Connecting...IP Address:" + IP.getText() + "/ Port:" + Port.getText());
        try {
            // 入力情報から接続する
            Main.socket = new Socket(IP.getText(), Integer.parseInt(Port.getText()));
            //Main.socket = new Socket(InetAddress.getLocalHost().getHostAddress(), 2000); // 自分がサーバの場合に楽するための文
            Main.oInStream = new ObjectInputStream(Main.socket.getInputStream());
            Main.oOutputStream = new ObjectOutputStream(Main.socket.getOutputStream());
        } catch (Exception e) {
            System.out.println("Connect Error :" + e);
            return;
        }

        try {
            // サーバに名前を送信
            Main.oOutputStream.writeObject(new Message(name));
        } catch (Exception e) {
            System.out.println("output Network Error :" + e);
            return ;
        }

        // ゲーム画面に移動
        Parent root = FXMLLoader.load(getClass().getResource("Game.fxml"));
        // Player.javaに処理も移動

        Main.stage.setScene(new Scene(root));
        Main.stage.show();
    }
}