package drawingforest_server;

import java.util.*;
import drawingforest_player.Message;

public class GameManager {

    ServerLogController server;

    //お題一覧
    String[][] quiz = {
    {"りんご", "みかん", "ばなな"},
    {"めざまし", "ふとん", "まくら", "ゆめ"},
    {"えんぴつ", "けしごむ", "じょうぎ", "ふでばこ"},
    {"らいおん", "きりん", "ねこ", "ねずみ", "うし", "とら", "うさぎ", "りゅう", "へび", "うま", "ひつじ", "さる", "とり", "いぬ", "いのしし"},
    {"きって", "てがみ"}
    };
    ArrayList<String> Quiz = new ArrayList<>();

    // 現在の問
    public String question;

    public GameManager(ServerLogController Server) {
        server = Server;
        Start();
    }

    void Start() {
        Random ram = new Random();
        ArrayList<Integer> nums = new ArrayList<>();

        // 0～人数までの数が要素として入ったリストを作成
        for (int i = 0; i < server.players.size(); i++) {
            nums.add(i);
        }

        // 半数を抜き取る
        for (int i = 0; i < server.players.size() / 2; i++) {
            nums.remove(ram.nextInt(nums.size()));
        }

        // クイズ一覧を作成
        if (Quiz.isEmpty()) {
            for (String[] s : quiz) {
                Quiz.addAll(Arrays.asList(s));
            }
        }

        // 問題をランダムで決める
        question = Quiz.get(new Random().nextInt(Quiz.size()));
        Quiz.remove(question);

        Message m = new Message(Message.Mode.PRESS, question);
        //全員に送信
        for (int i = 0; i < server.players.size(); i++) {
            //x1が0なら解答者
            m.x1 = 0;
            for (int num : nums) { //出題者リストを探索
                if (num == i) {
                    nums.remove(new Integer(num));//探索量を減らす
                    //x1が-1なら出題者
                    m.x1 = -1;
                    break;
                }
            }

            // メッセージを送信
            try {
                server.players.get(i).oos.writeObject(m);
            } catch (Exception e) {
                server.KillThread(server.players.get(i));
            }
        }
    }

    //クイズの答えと参照するメソッド
    public boolean Answer(String ans) {
        return ans.equals(question);
    }

    // リタイア時
    public void Surrender() {
        // 正解発表
        server.All(new Message("正解は:" + question));
        // ゲーム再スタート
        Start();
    }
}
