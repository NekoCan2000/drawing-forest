
package drawingforest_player;
import java.io.Serializable;

//送信する時のクラス
public class Message implements Serializable {
    public static enum Mode {
        CHAT, ANSWER, CLICK, PRESS, DRAW, RESET
    };

    // 送信する文字列
    public String message;
    // 送信するデータのモード
    public Mode mode;
    // 座標系を送信するようのdouble
    public double x1, y1, x2, y2, size;

    public Message(Mode m) {
        mode = m;
    }

    public Message(Mode m, String str) {
        message = str;
        mode = m;
    }

    public Message(String str) {
        this(Mode.CHAT, str);
    }

    public Message(Mode m, double size, double x, double y, String c) {
        mode = m;
        x1 = x;
        y1 = y;
        this.size = size;
        message = c;
    }

    public Message(Mode m, double size, double x1, double y1, double x2, double y2, String c) {
        this(m, size, x1, y1, c);
        this.x2 = x2;
        this.y2 = y2;
    }
}