
package drawingforest_server;

import drawingforest_player.Message;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.text.Font;

public class ServerLogController implements Initializable {

    //アプリケーションのポート番号
    private final int PORTNUMBER = 2000;
    //最大接続数
    private final int MAXCONNECTS = 10;
    
    // 参加しているplayerと通信するスレッドリスト
    ArrayList<ServerThread> players = new ArrayList<>();
    // サーバソケット本家
    ServerSocket serverSocket = null;
    // ゲームの進行を管理するクラス
    GameManager gm;
    @FXML //コンソール画面
    private Label label;
    private String logStr = "";
    private Font font;

    //受信するためだけのクラス
    class Recieve extends Thread {

        ServerLogController slc;

        Recieve(ServerLogController SLC) {
            slc = SLC;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    // 接続待機
                    Socket socket = serverSocket.accept();
                    // スレッドの生成
                    ServerThread st = new ServerThread(socket, slc);
                    // Playerスレッドリストに追加
                    players.add(st);
                    // スレッド開始
                    st.start();

                } catch (Exception e) {
                    System.out.println("Server Network Error :" + e);
                    if (serverSocket.isClosed()) {
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        font = label.getFont();
        try {
            // ポート番号と最大人数を設定
            serverSocket = new ServerSocket(PORTNUMBER, MAXCONNECTS);
            // IPアドレスとポート番号を表示
            print("Server's IP Address : " + InetAddress.getLocalHost().getHostAddress());
            print("Server's Port Number:" + PORTNUMBER);
        } catch (Exception e) {
            System.out.println("Server socket Error :" + e);
            System.exit(1);
        }

        new Recieve(this).start();
    }

    public void closeServer() {
        try {
            serverSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(ServerLogController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void print(String str) {
        double height = label.getPrefHeight();
        logStr = logStr + str + "\n";
        String[] strs = logStr.split("\n");
        if (strs.length * font.getSize() >= height) {
            logStr = "";
            for (int i = 1; i < strs.length; i++) {
                logStr = logStr + strs[i] + "\n";
            }
        }

        /*
         *javaFXの同期に関係なくjavaFXのオブジェクトを変更するとエラーする
         *runnableクラスから間接的に同期させる
         */
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                System.out.println("logStr:" + logStr);
                label.setText(logStr);
            }
        };
        Platform.runLater(runnable);
    }

    // Player全員に引数の文字列を送信するメソッド
    public void All(Message message) {
        LinkedList<ServerThread> delete = new LinkedList<>();
        for (ServerThread st : players) {
            try {
                // 引数の文字列を送信
                st.oos.writeObject(message);
            } catch (IOException e) {
                // エラーしたら削除リストに追加
                delete.add(st);
            }
        }
        //削除リストのスレッドを削除
        while(!delete.isEmpty()) {
            KillThread(delete.get(0));
            delete.remove(0);
        }
        // ガベージコレクション保険でnullを代入
        delete = null;
    }

    // スレッド削除メソッド
    public void KillThread(ServerThread st) {
        players.remove(st);
        All(new Message("Logout :" + st.name));
        print("Logout :"+st.name);
        if (players.size() == 1) {
            Login(players.get(0));
            gm = null;
        }
    }

    // 人数の増減ごとに表示を設定
    void Login(ServerThread st) {
        if (players.size() != 2) {
            Message m = new Message(Message.Mode.PRESS, "参加者を待っています");
            m.x1 = players.size() == 1 ? -1 : 0;
            try {
                st.oos.writeObject(m);
            } catch (Exception e) {
                KillThread(players.size() == 1 ? players.get(0) : st);
            }
        } else if (gm == null) {
            // 初めて2人になった時ゲーム開始
            print("Let's Paint");
            gm = new GameManager(this);
        }
    }
}
