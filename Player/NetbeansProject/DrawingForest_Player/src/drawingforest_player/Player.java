
package drawingforest_player;

import java.net.*;
import java.util.*;

import javafx.fxml.*;
import javafx.animation.AnimationTimer;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.canvas.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.paint.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;

public class Player implements Initializable {

    @FXML // ラベルを追加する親オブジェクト
    HBox hbox;
    @FXML // 解答者の解答を流すパネル
    AnchorPane ap;
    @FXML // お絵描きするキャンバス
    Canvas canvas;
    @FXML // チャットの入力欄
    TextField chatBox;
    @FXML // 表示するチャット欄
    Label chatLabel;
    @FXML // ペンの大きさスライダー
    Slider sizer;
    @FXML // 色ペン用のパレット
    ColorPicker cPicker;

    boolean change = true;
    double size = 2;
    ArrayList<Text> answers = new ArrayList<>();
    ArrayList<Text> addList = new ArrayList<>();
    ObservableList<Node> children;
    // チャット欄に表示する文字列
    String chatStr = "";

    GraphicsContext gc;

    class Recieve extends Thread {

        @Override // 受信スレッド
        public void run() {
            Message m;
            while (true) {
                try {
                    // 受信待機
                    m = (Message) Main.oInStream.readObject();
                } catch (Exception e) {
                    break;// socketが閉じたら処理を終える
                }
                switch (m.mode) {
                case CHAT:
                    chatStr = chatStr + m.message + "\n";
                    System.out.println(chatStr);
                    String[] s = chatStr.split("\n");
                    if (s.length >= 19) {
                        chatStr = "";
                        for (int i = 1; i < s.length; i++) {
                            chatStr = chatStr + s[i] + "\n";
                        }
                    }
                    break;
                case ANSWER:
                    Text t = new Text(m.message);
                    t.setFont(Font.font(null, 25));
                    t.setFill(Color.ALICEBLUE);
                    t.setStroke(Color.CORNFLOWERBLUE);
                    answers.add(t);
                    t.setLayoutY(ap.getHeight() - 10);
                    t.setLayoutX(new Random().nextInt((int) ap.getWidth() - m.message.length() * 20));
                    break;
                case CLICK:
                    DrawPoint(m.size, m.x1, m.y1,m.message);
                    break;
                case DRAW:
                    DrawLine(m.size, m.x1, m.y1, m.x2, m.y2, m.message);
                    break;
                case RESET:
                    Reset();
                    break;
                case PRESS: // Pressはplayerは使わないため役職決め用に使用
                    change = true;
                    String str;
                    if (m.x1 == 0) {// x1が0なら回答者
                        System.out.println("You are ANSWER");
                        str = "あなたは解答者です\n解答は先頭に半角で@をつけてひらがなで答えてね";
                    } else {// x1がその他なら出題者
                        System.out.println("You are QUIZER:" + m.message);
                        str = "あなたはお題を描きます\nお題:"+m.message;
                    }
                    Text tex = new Text(str);
                    tex.setFont(new Font(20.0));
                    tex.setTextAlignment(TextAlignment.CENTER);
                    addList.add(tex);
                    break;
                }// switch end }

            } // while() end }
        } // run() end }

    }// class Receive end }

    @Override // 一度だけ呼ばれるメソッド
    public void initialize(URL url, ResourceBundle rb) {
        gc = canvas.getGraphicsContext2D();
        cPicker.setValue(Color.BLACK);
        Reset();
        children = ap.getChildren();

        // アニメーションクラス
        new AnimationTimer() {
            @Override // ゲームループメソッド
            public void handle(long now) {
                Text t;
                // チャット欄ではchatStrを常に表示する
                chatLabel.setText(chatStr);

                if (!addList.isEmpty()) { // クイズの通知が来たら
                    if (change) {
                        while (!hbox.getChildren().isEmpty()) {
                            hbox.getChildren().remove(hbox.getChildren().get(0));
                        }
                        change = false;
                    }
                    t = addList.get(0);
                    hbox.getChildren().add(t);
                    addList.remove(t);
                }

                // 解答を受信したら生成
                while (!answers.isEmpty()) {
                    children.add(answers.get(0));
                    answers.remove(0);
                }

                // 解答を下から上に移動させる文（children 0 はキャンバスのため除外）
                for (int i = 1; i < children.size(); i++) {
                    t = (Text) children.get(i);
                    t.setLayoutY(t.getLayoutY() - 2);
                    if (t.getLayoutY() < 10) {
                        children.remove(t);
                    }
                }

            } // handle(long now) end }
        }.start();

        // 受信スレッドの作成と開始
        new Recieve().start();
    } // initialize() end }

    // サーバに送信するメソッド
    void Send(Message message) {
        try {
            // 引数のメッセージを送信
            Main.oOutputStream.writeObject(message);
        } catch (Exception e) {
            System.out.println("Player in send() output Error :" + e);
        }
    }

    @FXML // 送信ボタンが押された時に呼ばれるメソッド
    public void InputChat() {
        String str = chatBox.getText();
        // 空白入力を防止if文
        if (str.trim().length() != 0) {
            if (!str.substring(0, 1).equals("@")) {
                Send(new Message(str));
                System.out.println("chat:"+str);
            } else {
                Send(new Message(Message.Mode.ANSWER, str));
                System.out.println("answer:"+str);
            }
        }
        chatBox.setText("");
        chatBox.setPromptText("解答は先頭に@を付けてひらがなで");
    }

    @FXML // クリックされた時
    void Clicked(MouseEvent e) {
        size = sizer.getValue() + 1;
        Send(new Message(Message.Mode.CLICK, size, e.getX() - size / 2, e.getY() - size / 2, cPicker.getValue().toString()));
    }

    @FXML // ドラッグされた時
    void Dragged(MouseEvent e) {
        Send(new Message(Message.Mode.DRAW, size, e.getX() - size / 2, e.getY() - size / 2, cPicker.getValue().toString()));
    }

    @FXML // 長押しされた時
    void Pressed(MouseEvent e) {
        size = sizer.getValue() + 1;
        Send(new Message(Message.Mode.PRESS, size, e.getX() - size / 2, e.getY() - size / 2, cPicker.getValue().toString()));
    }

    @FXML // Resetボタン押された時
    void CanvasReset() {
        Send(new Message(Message.Mode.RESET));
    }

    @FXML // リタイアボタンを押された時
    void Surrender() {
        Send(new Message("SURRENDER"));
    }

    // 円を描くメソッド
    void DrawPoint(double s, double x, double y,String c) {
        gc.setFill(Color.valueOf(c));
        gc.fillOval(x, y, s, s);
    }

    // 線を引くメソッド
    void DrawLine(double size, double x1, double y1, double x2, double y2, String c) {
        gc.setStroke(Color.valueOf(c));
        gc.setLineWidth(size);
        gc.strokeLine(x1, y1, x2, y2);
    }

    // キャンバスを白くする
    void Reset() {
        Paint p = gc.getFill();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setFill(p);
    }
}