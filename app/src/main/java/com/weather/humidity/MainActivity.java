package com.weather.humidity;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.weather.humidity.Dao.SqlHelper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
/*
 第三方库用于获取网络上的天气 在在app文件下的build.gradle文件里 要加上
dependencies {
     compile  'org.jsoup:jsoup:1.9.2'
}
 */
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class MainActivity extends AppCompatActivity implements View.OnClickListener { //实现点击监听器接口
    private TextView tempaure; //显示温度
    private TextView humidity; //显示湿度
    private Button open_btn; //加湿器打开按钮
    private Button refresh; //刷新
    private int Temp; //温度数据
    private int Hum;  //湿度数据
    private static int count = 0; //计数器，查看使用了多少次
    private SQLiteDatabase db;
    //更新ui,异步更新
    final  Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){ //处理获取的天气状况
            super.handleMessage(msg);
            if(msg.what == 1){
                Map<Integer,Integer> info = (HashMap)msg.obj;
                tempaure.setText(info.get(1)+"℃"); //更新温度显示
                humidity.setText(info.get(2)+"%");  //更新适度
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//因为不是所有的系统都可以设置颜色的，在4.4以下就不可以。。有的说4.1，所以在设置的时候要检查一下系统版本是否是4.1以上
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        }
        setContentView(R.layout.activity_main);
        new IsInternet().checkNetwork(this);
        initView();

    }

    //初始化界面
    public void initView(){
        tempaure = (TextView) findViewById(R.id.tempure); //获取页面上的控件
        humidity = (TextView) findViewById(R.id.Humidity);
        open_btn = (Button)findViewById(R.id.open);
        refresh = (Button)findViewById(R.id.refresh);
        open_btn.setOnClickListener(this);
        //刷新重新获取
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getWeather();
            }
        });
        getWeather();
    }
    //获取天气信息 同时进行异步更新 使天气同时显示
    public void getWeather(){
        new Thread(new weather()).start();

    }
    class weather implements Runnable{ //新建一个线程异步加载数据 实现ui同步更新
        public void run(){
            try{
                //调用jsoup的接口来实现对网络数据的获取   程序应用接口API
                Document doc = Jsoup.connect("http://weather.sina.com.cn/zhenjiang")
                        .get(); //获取网页内容
                String temp = doc.select("div.slider_degree").text();
                String Th = doc.select("p[class=slider_detail]").text();
                //正则表达式获取数字数据
                String regex = "-{0,1}[\\d]{2,4}";
                Pattern p = Pattern.compile(regex);
                Matcher m = p.matcher(temp);

                //获取温度
                if(m.find()){
                    Temp = Integer.parseInt(m.group());
                }
                //获取湿度
                m = p.matcher(Th);
                if(m.find()){
                    Hum = Integer.parseInt(m.group());
                }
                //简单哈希表来储存数据 <Key,value>的类型按照需要来修改
                Map<Integer,Integer> info = new HashMap<>();
                info.put(1,Temp);info.put(2,Hum);
                //消息，相当于mfc中的消息循环，把消息发送给处理消息的handler
                Message msg = new Message();
                msg.obj = info;
                msg.what = 1;
                handler.sendMessage(msg);
            }catch (Exception e){
                //日志记录
                Log.i("error",e.toString());
            }
        }
    }




    //当湿度小于室内湿度时
    //当点击按钮时触发的信息
    @Override
    public void onClick(View v) {
        ReadCount();
        count ++;//每次点击一次就表示使用一次
        Log.i("2",String.valueOf(count));
        if(count ==3){ //等于1 只是为了测试要如果判断使用了x次将1换成x即可。同时建议将使用次数用数据库或者文件来保存和读取

            //警告对话框
            AlertDialog dlg = new AlertDialog.Builder(this)
                    .setTitle("提示") //设置对话框标题
                    .setMessage("已使用"+count+"次请及时清理加湿器") //提示信息
                    .setNegativeButton("确定",new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which){

                }
            }).create();
            dlg.show(); //显示警告对话框
            count = 0;
        }
        UpdateCount(count);
    }
    public void ReadCount(){
        db = (new SqlHelper(this)).getReadableDatabase();
        Cursor c = db.rawQuery("select * from count",null);
        while(c.moveToNext()){
            count = c.getInt(0);
            Log.i("1",String.valueOf(count));
        }
        db.close();
    }
    public void UpdateCount(int counter){
        db = (new SqlHelper(this)).getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("counter",counter);
        db.execSQL("update count set counter="+counter);
        db.close();
    }

}

