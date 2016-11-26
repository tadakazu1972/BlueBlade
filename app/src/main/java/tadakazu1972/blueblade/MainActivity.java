package tadakazu1972.blueblade;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.opencsv.CSVParser;
import com.opencsv.CSVReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Integer.parseInt;

public class MainActivity extends AppCompatActivity implements Runnable, android.view.View.OnTouchListener {
    private MainActivity mainActivity;
    private View view;
    private SurfaceView surfaceView;
    private Thread thread;
    private volatile boolean isThreadRun;
    public int deviceWidth; //Device's size
    public int deviceHeight; //Device's size
    final float VIEW_WIDTH = 320.0f;
    final float VIEW_HEIGHT = 320.0f;
    private int width;
    private int height;
    private float scale;
    private float scaleX;
    private float scaleY;
    protected Map map;
    protected float mapx;
    protected float mapy;
    protected float m;
    protected int baseX; // for drawing Map Array's baseX
    protected int baseY; // for drawing Map Array's baseY
    private Bitmap[] sBg = new Bitmap[4];
    private Bitmap[] sMap = new Bitmap[32];
    private Bitmap[] sArthur = new Bitmap[8];
    protected MyChara myChara;
    private int touchDirection;
    boolean repeatFlg;
    private Button btnUp;
    private Button btnRight;
    private Button btnDown;
    private Button btnLeft;
    protected Paint paintDamage = new Paint();
    protected Paint paintMonsterHp = new Paint();
    protected int damageIndex;
    //ダメージ時にモンスターHP表示して３秒後に消すタイマー
    private Timer monsterHpTimer = null;
    private TimerTask monsterHpTimerTask = null;
    private Handler monsterHpTimerHandler = new Handler();
    //開発中パラメータ表示用
    protected Paint paint0 = new Paint();

    public MainActivity(){
        super();
        // Create MAP DATA
        mapx = 0.0f;
        mapy = 0.0f;
        map = new Map();
        m = 0.0f;
        baseX = 0;
        baseY = 0;
        // Create MyChara
        myChara = new MyChara();
        touchDirection=0;
        repeatFlg=false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = this;
        view = this.getWindow().getDecorView();
        setContentView(R.layout.activity_main);
        surfaceView = (SurfaceView)findViewById(R.id.MainSurfaceView);
        getDisplaySize();
        createButton();
        //マップ生成
        loadCSV("map01.csv");
        //タイマー生成
        monsterHpTimer = new Timer();
        monsterHpTimerTask = new monsterHpTimerTask();
        monsterHpTimer.schedule(monsterHpTimerTask, 3000, 3000); //3秒後
    }

    @Override
    public void onResume(){
        super.onResume();
        // Create Sprite
        createSprite();
        isThreadRun = true;
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void onPause(){
        super.onPause();
        isThreadRun = false;
        while(true){
            try{
                thread.join();
                break;
            } catch(InterruptedException e){

            }
        }
        thread = null;
    }

    @Override
    public void run(){
        SurfaceHolder holder = surfaceView.getHolder();
        while(isThreadRun){
            if(holder.getSurface().isValid()){
                synchronized (this){
                    // Draw Screen
                    draw(holder);
                }
                // Moving
                moveCharacters();
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent e){
        //onTouchは画面を押した時と離した時の両方のイベントを取得する
        int action = e.getAction();
        switch(action){
            //ボタンから指が離れた時
            case MotionEvent.ACTION_UP:
                //連続イベントフラグをfalse
                repeatFlg = false;
                touchDirection = 0;
                //ゲームスタート
                //if (mainSurfaceView.gs==0) mainSurfaceView.gs=1;
                break;
            case MotionEvent.ACTION_DOWN:
                switch(v.getId()){
                    case R.id.btnUp:
                        touchDirection = 1;
                        break;
                    case R.id.btnRight:
                        touchDirection = 2;
                        break;
                    case R.id.btnDown:
                        touchDirection = 3;
                        break;
                    case R.id.btnLeft:
                        touchDirection = 4;
                        break;
                }
        }
        return false;
    }

    public void getDisplaySize(){
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        deviceWidth = size.x;
        deviceHeight = size.y;
        scaleX = deviceWidth / VIEW_WIDTH;
        scaleY = deviceHeight / VIEW_HEIGHT;
        scale = scaleX > scaleY ? scaleY : scaleX;
    }

    public void createButton(){
        /*OnClickだと動きっぱなしになるので不採用
        view.findViewById(R.id.btnUp).setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                myChara.base_index = 6;
                touchDirection = 1;
            }
        });
        view.findViewById(R.id.btnRight).setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                touchDirection = 2;
            }
        });
        view.findViewById(R.id.btnDown).setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                myChara.base_index = 0;
                touchDirection = 3;
            }
        });
        view.findViewById(R.id.btnLeft).setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                touchDirection = 4;
            }
        });*/
        //OnTouchのほうがボタンを離すと止まる動きを実現できるのでこちらを採用
        // attach button from res/layout/activity_main.xml
        btnUp = (Button)findViewById(R.id.btnUp);
        btnRight = (Button)findViewById(R.id.btnRight);
        btnDown = (Button)findViewById(R.id.btnDown);
        btnLeft = (Button)findViewById(R.id.btnLeft);
        // set OnTouchListener
        btnUp.setOnTouchListener(this);
        btnRight.setOnTouchListener(this);
        btnDown.setOnTouchListener(this);
        btnLeft.setOnTouchListener(this);
    }

    public void createSprite(){
        Resources res = this.getResources();
        if (sBg[0] == null) {
            // Back Ground
            sBg[0] = BitmapFactory.decodeResource(res, R.drawable.black);
            // Map
            sMap[0] = BitmapFactory.decodeResource(res, R.drawable.field01);
            sMap[1] = BitmapFactory.decodeResource(res, R.drawable.earth01);
            sMap[2] = BitmapFactory.decodeResource(res, R.drawable.earth02);
            sMap[3] = BitmapFactory.decodeResource(res, R.drawable.grass);
            sMap[4] = BitmapFactory.decodeResource(res, R.drawable.dungeonfloor);
            sMap[5] = BitmapFactory.decodeResource(res, R.drawable.tree);
            sMap[6] = BitmapFactory.decodeResource(res, R.drawable.stone);
            sMap[7] = BitmapFactory.decodeResource(res, R.drawable.sea);
            sMap[8] = BitmapFactory.decodeResource(res, R.drawable.redbrick);
            sMap[9] = BitmapFactory.decodeResource(res, R.drawable.stonebrick);
            sMap[10] = BitmapFactory.decodeResource(res, R.drawable.rock);
            sMap[11] = BitmapFactory.decodeResource(res, R.drawable.brick);
            sMap[12] = BitmapFactory.decodeResource(res, R.drawable.pillar);
            sMap[13] = BitmapFactory.decodeResource(res, R.drawable.fire);
            sMap[14] = BitmapFactory.decodeResource(res, R.drawable.black);
            sMap[15] = BitmapFactory.decodeResource(res, R.drawable.door);
            sMap[16] = BitmapFactory.decodeResource(res, R.drawable.descend);
            sMap[17] = BitmapFactory.decodeResource(res, R.drawable.descend);
            sMap[18] = BitmapFactory.decodeResource(res, R.drawable.treasure);
            sMap[19] = BitmapFactory.decodeResource(res, R.drawable.rod);
            sMap[20] = BitmapFactory.decodeResource(res, R.drawable.redrod);
            sMap[21] = BitmapFactory.decodeResource(res, R.drawable.face2);
            sMap[22] = BitmapFactory.decodeResource(res, R.drawable.face);
            // Arthur
            sArthur[0] = BitmapFactory.decodeResource(res, R.drawable.knight01);
            sArthur[1] = BitmapFactory.decodeResource(res, R.drawable.knight02);
            sArthur[2] = BitmapFactory.decodeResource(res, R.drawable.knight03);
            sArthur[3] = BitmapFactory.decodeResource(res, R.drawable.knight04);
            sArthur[4] = BitmapFactory.decodeResource(res, R.drawable.knight05);
            sArthur[5] = BitmapFactory.decodeResource(res, R.drawable.knight06);
            sArthur[6] = BitmapFactory.decodeResource(res, R.drawable.knight07);
            sArthur[7] = BitmapFactory.decodeResource(res, R.drawable.knight08);
        }
        //ダメージ時モンスターHP表示用Paint設定
        paintMonsterHp.setTextSize(12);
        paintMonsterHp.setColor(Color.RED);
        paintMonsterHp.setTextAlign(Paint.Align.CENTER);
        //paintMonsterHp.setTypeface(Typeface.DEFAULT_BOLD);
        //開発時各種パラメーター確認用
        paint0.setTextSize(7);
        paint0.setColor(Color.WHITE);
        paint0.setTextAlign(Paint.Align.LEFT);
        paint0.setTypeface(Typeface.DEFAULT_BOLD);
    }

    public class monsterHpTimerTask extends TimerTask {
        @Override
        public void run(){
            monsterHpTimerHandler.post( new Runnable(){
                public void run(){
                }
            });
        }
    }

    public void draw(SurfaceHolder holder){
        Canvas canvas = holder.lockCanvas();
        if (canvas != null){
            //Adjust Screen Size
            canvas.scale(scale*2, scale*2);
            // Draw Back
            for (int y=0;y<21;y++){
                for (int x=0;x<20;x++){
                    canvas.drawBitmap(sBg[0], x*8.0f, y*8.0f, null);
                }
            }
            // Draw MAP
            // Draw Next Map
            // 上辺
            if (baseY>0){
                for (int x=0;x<20;x++){
                    canvas.drawBitmap(sMap[map.MAP[baseY-1][baseX+x]], x*8.0f+mapx, -8.0f+mapy, null);
                }
            }
            // 右辺
            if (baseX<79){
                for (int y=0;y<20;y++){
                    canvas.drawBitmap(sMap[map.MAP[baseY+y][baseX+20]], 160.0f+mapx, y*8.0f+mapy, null);
                }
            }
            // 左辺
            if (baseX>0){
                for (int y=0;y<20;y++){
                    canvas.drawBitmap(sMap[map.MAP[baseY+y][baseX-1]], -8.0f+mapx, y*8.0f+mapy, null);
                }
            }
            // 下辺
            if (baseY<79){
                for (int x=0;x<20;x++){
                    canvas.drawBitmap(sMap[map.MAP[baseY+20][baseX+x]], x*8.0f+mapx, 160.0f+mapy, null);
                }
            }
            // Draw Main Map
            int index = 0;
            for (int y=0;y<20;y++){
                for (int x=0;x<20;x++){
                    canvas.drawBitmap(sMap[map.MAP[baseY+y][baseX+x]], x*8.0f+mapx, y*8.0f+mapy, null);
                }
            }
            // Draw MyChara
            int i = myChara.base_index + myChara.index / 10;
            if (i > 7) i = 0;
            canvas.drawBitmap(sArthur[i], myChara.x, myChara.y, null);

            // Draw Text
            canvas.drawText("mapx="+String.valueOf(mapx), 2, 10, paint0);
            canvas.drawText("mapy="+String.valueOf(mapy), 2, 16, paint0);
            canvas.drawText("x="+String.valueOf(myChara.x), 2, 22, paint0);
            canvas.drawText("y="+String.valueOf(myChara.y), 2, 28, paint0);
            canvas.drawText("wx="+String.valueOf(myChara.wx), 2, 34, paint0);
            canvas.drawText("wy="+String.valueOf(myChara.wy), 2, 40, paint0);
            canvas.drawText("baseX="+String.valueOf(baseX), 2, 46, paint0);
            canvas.drawText("baseY="+String.valueOf(baseY), 2, 52, paint0);
            int mx = (int)myChara.x/8;
            int my = (int)myChara.y/8;
            canvas.drawText("MAP[y][x]="+String.valueOf(map.MAP[mx][my]), 2, 100, paint0);
        }
        holder.unlockCanvasAndPost(canvas);
    }

    public void moveCharacters(){
        myChara.move( touchDirection, map, this);
    }

    public void loadCSV(String filename){
        InputStream is = null;
        try {
            try {
                //assetsフォルダ内のcsvファイル読込
                is = getAssets().open(filename);
                InputStreamReader ir = new InputStreamReader(is, "UTF-8");
                CSVReader csvreader = new CSVReader(ir, CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER, 0);//0行目から
                String[] csv;
                int y = 0;
                while((csv = csvreader.readNext()) != null){
                    for (int x=0;x<100;x++){
                        //読み込んだデータをある程度のレンジに変換して格納
                        int data = 0;
                        int _data = parseInt(csv[x]);
                        /*if ( _data <= 30) { data = 0;
                        } else if ( _data > 30 && _data <=60) { data = 1;
                        } else if ( _data > 60 && _data <=100) { data = 2;
                        } else if ( _data >100 && _data <=130) { data = 3;
                        } else if ( _data >130 && _data <=160) { data = 4;
                        } else if ( _data >160 && _data <=190) { data = 5;
                        } else if ( _data >190 && _data <=900) { data = 6;
                        } else if ( _data >900) { data = 7;}*/
                        map.MAP[y][x] = _data;
                    }
                    y++; if (y>100){ y=0;}
                }
                //データ代入
            } finally {
                if (is != null) is.close();
                Toast.makeText(this, "マップデータ読込完了", Toast.LENGTH_SHORT).show();
            }
        } catch(Exception e){
            Toast.makeText(this, "CSV読込エラー", Toast.LENGTH_SHORT).show();
        }
    }
}
