package drawingforest_server;

import java.io.*;
import java.net.*;
import drawingforest_player.Message;

public class ServerThread extends Thread {

    ServerLogController server;
    // playerとの通信用ソケット
    public Socket socket = null;
    // 送信用ストリーム
    public ObjectOutputStream oos = null;
    // 受信用ストリーム
    public ObjectInputStream ois = null;
    // playerの名前
    public String name;
    // お絵描き時の位置情報を保持
    double x, y;

    public ServerThread(Socket s, ServerLogController Server) throws Exception {
        socket = s;
        server = Server;
        oos = new ObjectOutputStream(socket.getOutputStream());
        ois = new ObjectInputStream(socket.getInputStream());
        server.print("connected IPAddress:" + socket.getRemoteSocketAddress());

    }

    @Override // 受信用メソッド
    public void run() {
        try {
            name = ((Message) ois.readObject()).message;
        } catch (Exception e) {
            server.print(e.toString());
            return;
        }

        // ログイン情報を通知
        Message message = new Message("Login :" + name);
        server.All(message);
        server.print(message.message);

        // 人数次第で処理を変更
        server.Login(this);

        while (true) {
            try { // 受信待機
                message = (Message) ois.readObject();
            } catch (Exception e) {
                // エラー時スレッドの削除をするようサーバに要請
                server.KillThread(this);
                try { // ソケットを閉じる
                    socket.close();
                } catch (Exception exc) {
                    server.print(exc.toString());
                }
                break;
            }

            // 受信すると
            switch (message.mode) {
                case CHAT:
                    if (!message.message.equals("SURRENDER")) {
                        // ただのチャットは名前を追加して表示
                        message.message = name + ":" + message.message;
                        server.print(message.message);
                    } else if (server.gm != null) {
                        server.gm.Surrender(); // メッセージがSURRENDERならリタイア
                    }
                case CLICK:
                case RESET:
                    // 全員に送信する
                    server.All(message);
                    break;
                case ANSWER:
                    if (server.gm == null) {
                        return;
                    }
                    if (server.gm.Answer(message.message.split("@")[1])) {
                        server.All(new Message(name + "さんが正解しました！"));
                        server.gm.Surrender();
                    }

                    message.message = name + message.message;
                    server.All(message);
                    break;
                case PRESS:
                    // 始めに押した位置を保存
                    x = message.x1;
                    y = message.y1;
                    break;
                case DRAW:
                    // 線を引く情報を全員に送信
                    message.x2 = x;
                    message.y2 = y;
                    server.All(message);
                    // 今回のx1,y1を保存
                    x = message.x1;
                    y = message.y1;
                    break;
            } // switch() end }
        } // while end }

    } // run () end }
} // class ServerThread end }
