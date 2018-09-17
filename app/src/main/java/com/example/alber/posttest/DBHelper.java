package com.example.alber.posttest;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "MYLOCALDB";
    private static final String TABLE_NAME_MEMBER = "mbr_member";
    private static final String TABLE_NAME_FRIENDSHIP = "mbr_friendship";

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);//交給父類別
    }

    public DBHelper(Context context) {//自訂建構子
        this(context, DB_NAME, null, DB_VERSION);//交給預設建構子
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String CREATE_TABLE_mbr_member = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_MEMBER +
                "(id INTEGER PRIMARY KEY ," +   //注意不要 AUTOINCREMENT
                "toid VARCHAR(8)," +
                "account VARCHAR(50)," +
                "identifier VARCHAR(22)," +
                "membertype_id TINYINT(1)," +   //不用外來鍵
                "name NVARCHAR(20)," +
                "password VARCAHR(128)," +
                "localpicture VARCHAR(64)," +
	            "dbpicture VARCHAR(64)," +
                "renew_time DATETIME)";
        db.execSQL(CREATE_TABLE_mbr_member);

        final String CREATE_TABLE_mbr_friendship = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_FRIENDSHIP +
                "(id INTERGER(4)," +    //注意不須設為主鍵
                "member_id INTEGER(4)," +
                "friend_id INTEGER(4)," +
                "nickname VARCHAR(20)," +
                "syncstatus INTEGER(4) DEFAULT 1," +
                "renew_time TIMESTAMP(4),"+
                "PRIMARY KEY(member_id,friend_id)," +
                "FOREIGN KEY(member_id) REFERENCES mbr_member(id)," +
                "FOREIGN KEY(friend_id) REFERENCES mbr_member(id))";
        db.execSQL(CREATE_TABLE_mbr_friendship);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
       /* final String DROP_TABLE_sys_sort="DROP TABLE "+TABLE_NAME_SORT;
        db.execSQL(DROP_TABLE_sys_sort);
        final String DROP_TABLE_sys_subsort="DROP TABLE "+TABLE_NAME_SUBSORT;
        db.execSQL(DROP_TABLE_sys_subsort);*/
    }
}

