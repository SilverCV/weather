package com.weather.humidity.Dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Silver on 2018/5/25.
 */
//由于使用数据较小，不需要将数据保存到数据库
public class SqlHelper extends SQLiteOpenHelper {
    public static String name = "count"; //数据库
    private static final int version = 1; //版本号
    public SqlHelper(Context context){
        super(context,name,null,version);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists count(counter int)");
        db.execSQL("insert into count(counter) values (0)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
    @Override
    public void onOpen(SQLiteDatabase db){
        super.onOpen(db); // 再次打开不需要重新创建数据库
    }
}
