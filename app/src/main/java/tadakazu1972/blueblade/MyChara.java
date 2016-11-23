package tadakazu1972.blueblade;

/**
 * Created by tadakazu on 2016/11/20.
 */

public class MyChara {
    protected float x, y;   // display x, y
    protected float wx, wy; // world x, y
    protected float vx, vy; // move x, y
    protected float ay;     // gravity
    protected float l, r, t, b; // collision detection
    protected int index;    // animation index
    protected int base_index; //animation base index
    protected int status;   // character status

    public MyChara(){
        x = 2 * 8.0f;
        y = 2 * 8.0f;
        wx = 2 * 8.0f;
        wy = 2 * 8.0f;
        vx = 0.0f;
        vy = 0.0f;
        ay = 1.0f;
        l = x;
        r = x + 7.0f;
        t = y;
        b = y + 7.0f;
        index = 0;
        base_index = 0;
        status = 0;
    }

    public void move(int touchDirection, Map map, MainActivity ac) {
        int x1, x2, y1, y2, dx1, dx2, dy1;
        if (touchDirection == 1) { //上
            vx = 0.0f;
            vy = -2.0f;
            base_index = 4;
        } else if (touchDirection == 3){ //下
            vx = 0.0f;
            vy = 2.0f;
            base_index = 0;
        } else if (touchDirection == 2) {
            vx = 2.0f;
            vy = 0.0f;
            base_index = 2;
        } else if  (touchDirection == 4) {
            vx = -2.0f;
            vy = 0.0f;
            base_index = 6;
        } else if (touchDirection == 0) {
            vy = 0.0f;
            vx = 0.0f;
        }
        //当たり判定用マップ座標算出
        x1 = (int) (wx +1.0f+ vx) / 8; if (x1 < 0) x1 = 0; if (x1 > 254) x1 = 254;
        y1 = (int) (wy +1.0f+ vy) / 8; if (y1 < 0) y1 = 0; if (y1 > 299) y1 = 299;
        x2 = (int) (wx +6.0f+ vx) / 8; if (x2 > 254) x2 = 254; if (x2 < 0) x2 = 0;
        y2 = (int) (wy +6.0f+ vy) / 8; if (y2 > 299) y2 = 299; if (y2 < 0) y2 = 0;
        //カベ判定
        if (map.MAP[y1][x1] > 4 || map.MAP[y1][x2] > 4 || map.MAP[y2][x1] > 4 || map.MAP[y2][x2] > 4) {
            vx = 0.0f;
            vy = 0.0f;
            // Adjust mapy
            if (ac.mapy > -3.0f){
                ac.mapy = 0.0f;
            } else if (ac.mapy < -6.0f){
                ac.mapy = -8.0f;
            }
        }
        //ワールド座標更新
        wx = wx + vx;
        if (wx < 0.0f) wx = 0.0f;
        if (wx > 800.0f) wx = 800.0f;
        wy = wy + vy;
        if (wy < 0.0f) wy = 0.0f;
        if (wy > 800.0f) wy = 800.0f;
        //ワールド当たり判定移動
        l = wx + 1.0f + vx;
        r = wx + 6.0f + vx;
        t = wy + 1.0f + vy;
        b = wy + 6.0f + vy;
        //画面座標更新
        x = x + vx;
        if ( x > 128.0f ) {
            if ( ac.baseX != 80) {
                x = 128.0f;
                ac.mapx = ac.mapx - vx;
                if ( wx / 8.0f > ac.baseX + 17 ) { //画面表示限界の１つ分前を超えたら代入
                    ac.baseX = ac.baseX + 1; if (ac.baseX > 80) { ac.baseX = 80; }// because baseX + (0 to 9) = Map Array's Maximum
                    ac.mapx = 0.0f;
                }
            } else if (ac.mapx != -8.0f) { //右端の限界まで引っ張り出していなければmapxを動かす
                x = 128.0f;
                ac.mapx = ac.mapx - vx;
            } else {
                if ( x > 152.0f ) x = 152.0f; //画面外へ飛び出し防止
            }
        }
        if (x < 16.0f) {
            if ( ac.baseX != 0) {
                x = 16.0f;
                ac.mapx = ac.mapx - vx;
                if ( wx / 8.0f < ac.baseX + 1 ) {
                    ac.baseX = ac.baseX - 1; if (ac.baseX < 0) { ac.baseX = 0; }
                    ac.mapx = 0.0f;
                }
            } else {
                if (x < 0.0f) x = 0.0f;
            }
        }
        y = y + vy;
        if (y > 128.0f) {
            if ( ac.baseY != 80) {
                y = 128.0f;
                ac.mapy = ac.mapy - vy;
                if ( wy / 8.0f > ac.baseY + 17 ) {
                    ac.baseY = ac.baseY + 1; if (ac.baseY > 80) { ac.baseY = 80; }
                    ac.mapy = 0.0f;
                }
            } else if ( ac.mapy != -8.0f ) { //最下段の限界まで引っ張りだしていなければmapyを動かす
                y = 128.0f;
                ac.mapy = ac.mapy - vy;
            } else {
                if ( y > 152.0f ) y = 152.0f; //画面外へ飛び出し防止
            }
        }
        if (y < 16.0f) {
            if ( ac.baseY != 0) {
                y = 16.0f;
                ac.mapy = ac.mapy - vy;
                if ( wy / 8.0f < ac.baseY + 1 ) {
                    ac.baseY = ac.baseY - 1; if (ac.baseY < 0) { ac.baseY = 0; }
                    ac.mapy = 0.0f;
                }
            } else { //最上段まで移動可能
                if ( y < 0.0f ) y = 0.0f; //画面外へ飛び出し防止
            }
        }
        //アニメーションインデックス変更処理
        index++;
        if (index > 19) index = 0;
    }
}
