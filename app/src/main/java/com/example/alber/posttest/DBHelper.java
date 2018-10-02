package com.example.alber.posttest;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "MYLOCALDB";
    private static final String TABLE_NAME_MEMBER = "mbr_member";
    private static final String TABLE_NAME_FRIENDSHIP = "mbr_friendship";
    private static final String TABLE_NAME_SORT = "mbr_sort";
    private static final String TABLE_NAME_SUBSORT = "mbr_subsort";
    private static final String TABLE_NAME_SORTBUDGET = "mbr_sortbudget";
    private static final String TABLE_NAME_SUBSORTBUDGET = "mbr_subsortbudget";
    private static final String TABLE_NAME_ACCOUNTTYPE = "sys_accounttype";
    private static final String TABLE_NAME_ACCOUNT = "mbr_account";
    private static final String TABLE_NAME_PROJECT = "mbr_project";
    private static final String TABLE_NAME_INVOICE = "mbr_invoice";
    private static final String TABLE_NAME_MEMBERINVOICE = "mbr_memberinvoice";
    private static final String TABLE_NAME_ACCOUNTING = "mbr_accounting";


    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);//交給父類別
    }

    public DBHelper(Context context) {//自訂建構子
        this(context, DB_NAME, null, DB_VERSION);//交給預設建構子
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String CREATE_TABLE_mbr_member = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_MEMBER +
                "(_id INTEGER PRIMARY KEY ," +   //注意不要 AUTOINCREMENT
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
                "(_id INTERGER(4)," +    //注意不須設為主鍵,不要 AUTOINCREMENT
                "member_id INTEGER(4)," +
                "friend_id INTEGER(4)," +
                "nickname VARCHAR(20)," +
                "syncstatus INTEGER(4) DEFAULT 1," +
                "renew_time DATETIME,"+
                "PRIMARY KEY(member_id,friend_id)," +
                "FOREIGN KEY(member_id) REFERENCES mbr_member(_id)," +
                "FOREIGN KEY(friend_id) REFERENCES mbr_member(_id))";
        db.execSQL(CREATE_TABLE_mbr_friendship);

        final String CREATE_TABLE_mbr_sort = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_SORT +
                "(_id INTEGER PRIMARY KEY," +    //注意不要 AUTOINCREMENT
                "type BOOLEAN," +
                "name VARCHAR(32)," +
                "icon VARCHAR(64)," +
                "renew_time DATETIME," +
                "member_id INTEGER(4))";
        db.execSQL(CREATE_TABLE_mbr_sort);

        final String CREATE_TABLE_mbr_subsort = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_SUBSORT +
                "(_id INTEGER PRIMARY KEY," +    //注意不要 AUTOINCREMENT
                "name VARCHAR(32)," +
                "type BOOLEAN," +
                "icon VARCHAR(64)," +
                "renew_time DATETIME," +
                "member_id INTEGER(4)," +
                "sort_id INTERGER(4)," +
                "FOREIGN KEY(member_id) REFERENCES mbr_member(_id)," +
                "FOREIGN KEY(sort_id) REFERENCES mbr_sort(_id))";
                db.execSQL(CREATE_TABLE_mbr_subsort);

        final String CREATE_TABLE_mbr_sortbudget = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_SORTBUDGET +
                "(_id INTEGER PRIMARY KEY," +   //注意不要 AUTOINCREMENT
                "month INTEGER," +
                "budget INTEGER," +
                "amount INTEGER," +
                "renew_time DATETIME," +
                "sort_id INTEGER," +
                "FOREIGN KEY(sort_id) REFERENCES mbr_sort(_id))";
        db.execSQL(CREATE_TABLE_mbr_sortbudget);

        final String CREATE_TABLE_mbr_subsortbudget = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_SUBSORTBUDGET +
                "(_id INTEGER PRIMARY KEY," +    //注意不要 AUTOINCREMENT
                "month INTEGER," +
                "budget INTEGER," +
                "amount INTEGER," +
                "renew_time DATETIME," +
                "subsort_id INTEGER," +
                "FOREIGN KEY(subsort_id) REFERENCES mbr_subsort(_id))";
        db.execSQL(CREATE_TABLE_mbr_subsortbudget);

        final String CREATE_TABLE_sys_accounttype = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_ACCOUNTTYPE +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT ," +
                "name VARCHAR(32)," +
                "renew_time DATETIME)";
        db.execSQL(CREATE_TABLE_sys_accounttype);

        final String CREATE_TABLE_mbr_account = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_ACCOUNT +
                "(_id INTEGER PRIMARY KEY," +    //注意不要 AUTOINCREMENT
                "name VARCHAR(20)," +
                "initialAmount INTEGER," +
                "balance INTEGER," +
                "FX VARCHAR(10)," +
                "renew_time DATETIME," +
                "accounttype_id INTEGER," +
                "member_id INTEGER," +
                "FOREIGN KEY(accounttype_id) REFERENCES sys_accounttype(_id)," +
                "FOREIGN KEY(member_id) REFERENCES mbr_member(_id))";
        db.execSQL(CREATE_TABLE_mbr_account);

        final String CREATE_TABLE_mbr_project = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_PROJECT +
                "(_id INTEGER PRIMARY KEY," +   //注意不要 AUTOINCREMENT
                "name VARCHAR(20)," +
                "renew_time DATETIME," +
                "member_id INTEGER," +
                "FOREIGN KEY(member_id) REFERENCES mbr_member(_id))";
        db.execSQL(CREATE_TABLE_mbr_project);

        final String CREATE_TABLE_mbr_invoice = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_INVOICE +
                "(number CHAR(10) PRIMARY KEY ," +
                "renew_time DATETIME)";
        db.execSQL(CREATE_TABLE_mbr_invoice);

        final String CREATE_TABLE_mbr_memberinvoice = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_MEMBERINVOICE +
                "(_id INTEGER," +   //注意不須設為主鍵,不要 AUTOINCREMENT
                "number CHAR(10)," +
                "createtime DATETIME," +
                "amount INTERGER," +
                "renew_time DATETIME,"+
                "member_id INTEGER," +
                "PRIMARY KEY(number,member_id,createtime)," +
                "FOREIGN KEY(number) REFERENCES mbr_invoice(number),"+
                "FOREIGN KEY(member_id) REFERENCES mbr_member(_id))";
        db.execSQL(CREATE_TABLE_mbr_memberinvoice);

        final String CREATE_TABLE_mbr_accounting = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_ACCOUNTING +
                "(_id INTEGER PRIMARY KEY," +   //注意不要 AUTOINCREMENT
                "purchasedate DATE," +
                "type BOOLEAN," +
                "amount INTEGER," +
                "localpicture VARCHAR(64)," +
                "dbpicture VARCHAR(64)," +
                "comment VARCHAR(32)," +
                "renew_time DATETIME," +
                "account_id INTEGER," +
                "invoicenumber VARCHAR(10)," +
                "member_id INTEGER," +
                "project_id INTEGER," +
                "sort_id INTEGER," +
                "subsort_id INTEGER," +
                "FOREIGN KEY(invoicenumber) REFERENCES mbr_invoice(invoicenumber)," +
                "FOREIGN KEY(member_id) REFERENCES mbr_member(_id)," +
                "FOREIGN KEY(project_id) REFERENCES mbr_project(_id)," +
                "FOREIGN KEY(sort_id) REFERENCES mbr_sort(_id)," +
                "FOREIGN KEY(subsort_id) REFERENCES mbr_subsort(_id))";
        db.execSQL(CREATE_TABLE_mbr_accounting);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
       /* final String DROP_TABLE_sys_sort="DROP TABLE "+TABLE_NAME_SORT;
        db.execSQL(DROP_TABLE_sys_sort);
        final String DROP_TABLE_sys_subsort="DROP TABLE "+TABLE_NAME_SUBSORT;
        db.execSQL(DROP_TABLE_sys_subsort);*/
    }
}

