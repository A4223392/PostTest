package com.example.alber.posttest;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {


    private Button btnLogin, btnOther, btnClear, btnRegister, btnRefresh, btnProfile;
    private TextView txvStatus, txvRecord, txvExpired, txvOrig_iat;
    private EditText txtEmail, txtPwd, txtPwdConfirm;
    private Spinner spnType;
    private static String showMsg = "\n";
    private DBHelper DH;
    private SQLiteDatabase db;
    private final String DB_NAME = "MYLOCALDB";

    private final LoginHandler loginHandler = new LoginHandler(MainActivity.this);
    private final RegisterHandler registerHandler = new RegisterHandler(MainActivity.this);

    private final RefreshHandler refreshHandler = new RefreshHandler(MainActivity.this);

    private class MyMessages {
        public static final int Error = 0;
        public static final int Connecting = 1;
        public static final int Progressing = 2;
        public static final int Disconnect = 3;
    }

    public class Path {  //注意路徑有無斜線(endpoint)
        public static final String api_token_jwtauth = "https://www.177together.com/api-token-jwtauth";
        public static final String api_token_refresh = "https://www.177together.com/api-token-refresh/";
        public static final String member = "https://www.177together.com/api/member/";
        public static final String friendShip = "https://www.177together.com/api/friendship/";
        public static final String sort = "https://www.177together.com/api/sort/";
        public static final String subsort = "https://www.177together.com/api/subsort/";
        public static final String account = "https://www.177together.com/api/account/";
        public static final String project = "https://www.177together.com/api/project/";
    }

    //https://stackoverflow.com/questions/3806051/accessing-sharedpreferences-through-static-methods
    private static SharedPreferences myGetSharedPreferences(Context context) {
        return context.getSharedPreferences("jwt_token", MODE_PRIVATE);
    }

    //https://www.androiddesignpatterns.com/2013/01/inner-class-handler-memory-leak.html
    //http://www.jcodecraeer.com/a/anzhuokaifa/androidkaifa/2014/1106/1922.html
    //https://blog.csdn.net/Mr_Leixiansheng/article/details/67636817
    private static class LoginHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;    //弱引用

        private LoginHandler(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get();    //獲取弱引用的對象

            if (activity != null) {
                try {
                    switch (msg.what) {
                        case MyMessages.Connecting:
                            showMsg = "\n[Connecting]>>>";
                            break;
                        case MyMessages.Progressing:
                            showMsg = "[Progressing]>>>";

                            SharedPreferences myPref = myGetSharedPreferences(activity.getApplicationContext());    //靜態方法
                            String strPayload = myPref.getString("PAYLOAD", "");//讀取已儲存的SharedPref
                            JSONObject jsonPayload = StringToJSON(strPayload);  //轉成JSON

                            Bundle bundle = msg.getData();
                            JSONObject jsonObj = BundleToJson(bundle.getBundle("token_Bundle"));    //轉成JSON
                            showMsg +=
                                    "\n[ResponseCode]：" + String.valueOf(jsonObj.getInt("responseCode")) +
                                            "\n[Token]：" + jsonObj.getString("token") +
                                            "\n[PAYLOAD.user_id]：" + jsonPayload.getInt("user_id") +
                                            "\n[PAYLOAD.username]：" + jsonPayload.getString("username") +
                                            "\n[PAYLOAD.exp]：" + jsonPayload.getInt("exp") +
                                            "\n[PAYLOAD.orig_iat]：" + jsonPayload.getInt("orig_iat");
                            activity.txvStatus.setText("狀態：已登入 ; user_id = " + String.valueOf(jsonPayload.getInt("user_id")));
                            activity.txvStatus.setTextColor(Color.BLUE);

                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
                            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Taipei"));
                            Date expiredDate = new Date(jsonPayload.getInt("exp") * 1000L); //Unix timestamp 轉 Date
                            Date orig_iatDate = new Date(jsonPayload.getInt("orig_iat") * 1000L);
                            activity.txvExpired.setText("Token Expired：" + sdf.format(expiredDate));
                            activity.txvOrig_iat.setText("Token Issued At：" + sdf.format(orig_iatDate));

                            activity.btnLogin.setEnabled(false); //已登入不允許再登入及註冊
                            activity.btnRegister.setEnabled(false);
                            activity.btnProfile.setEnabled(true);
                            activity.txtEmail.setEnabled(false);
                            activity.txtPwd.setEnabled(false);
                            activity.txtPwdConfirm.setEnabled(false);
                            break;
                        case MyMessages.Disconnect:
                            showMsg = "\n[Disconnect]\n";
                            break;
                        case MyMessages.Error:
                            bundle = msg.getData();
                            String errorMsg = bundle.getString("errorMsg", "");
                            Toast.makeText(activity, errorMsg, Toast.LENGTH_LONG).show();
                            break;
                        default:
                            break;
                    }
                    activity.txvRecord.append(showMsg);
                    //super.handleMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class RegisterHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;    //弱引用

        private RegisterHandler(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get();    //獲取弱引用的對象
            if (activity != null) {
                try {
                    switch (msg.what) {
                        case MyMessages.Connecting:
                            showMsg = "\n[Connecting]>>>";
                            break;
                        case MyMessages.Progressing:
                            showMsg = "[Progressing]>>>";
                            SharedPreferences myPref = myGetSharedPreferences(activity.getApplicationContext());
                            String strPayload = myPref.getString("PAYLOAD", "");//讀取已儲存的SharedPref
                            JSONObject jsonPayload = StringToJSON(strPayload);  //轉成JSON

                            Bundle bundle = msg.getData();
                            JSONObject jsonObj = BundleToJson(bundle.getBundle("member_Bundle"));    //轉成JSON
                            showMsg +=
                                    "\n[ResponseCode]：" + String.valueOf(jsonObj.getInt("responseCode")) +
                                            "\n[Token]：" + jsonObj.getString("token") +
                                            "\n[PAYLOAD.exp]：" + jsonPayload.getInt("exp") +
                                            "\n[PAYLOAD.orig_iat]：" + jsonPayload.getInt("orig_iat");
                            showMsg += "\n{\n\"id\" : " + jsonObj.getInt("id") + ",\n" +
                                    "\"toid\" : \"" + jsonObj.getString("toid") + "\",\n" +
                                    "\"account\" : \"" + jsonObj.getString("account") + "\",\n" +
                                    "\"identifier\" : \"" + jsonObj.getString("identifier") + "\",\n" +
                                    "\"membertype\" : " + jsonObj.getInt("membertype") + ",\n" +
                                    "\"name\" : \"" + jsonObj.getString("name") + "\",\n" +
                                    "\"nickname\" : \"" + jsonObj.getString("nickname") + "\",\n" +
                                    "\"password\" : \"" + jsonObj.getString("password") + "\",\n" +
                                    "\"localpicture\" : \"" + jsonObj.getString("localpicture") + "\",\n" +
                                    "\"dbpicture\" : \"" + jsonObj.getString("dbpicture") + "\",\n}";
                            activity.txvStatus.setText("狀態：已登入 ; user_id = " + String.valueOf(jsonPayload.getInt("user_id")));
                            activity.txvStatus.setTextColor(Color.BLUE);

                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
                            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Taipei"));
                            Date expiredDate = new Date(jsonPayload.getInt("exp") * 1000L); //Unix timestamp 轉 Date
                            Date orig_iatDate = new Date(jsonPayload.getInt("orig_iat") * 1000L);
                            activity.txvExpired.setText("Token Expired：" + sdf.format(expiredDate));
                            activity.txvOrig_iat.setText("Token Issued At：" + sdf.format(orig_iatDate));
                            activity.btnLogin.setEnabled(false); //已登入不允許再登入及註冊
                            activity.btnRegister.setEnabled(false);
                            activity.btnProfile.setEnabled(true);
                            activity.txtEmail.setEnabled(false);
                            activity.txtPwd.setEnabled(false);
                            activity.txtPwdConfirm.setEnabled(false);

                            Toast.makeText(activity, "註冊成功！", Toast.LENGTH_SHORT).show();
                            break;
                        case MyMessages.Disconnect:
                            showMsg = "\n[Disconnect]\n";
                            break;
                        case MyMessages.Error:
                            bundle = msg.getData();
                            String errorMsg = bundle.getString("errorMsg", "");
                            Toast.makeText(activity, errorMsg, Toast.LENGTH_LONG).show();
                            break;
                        default:
                            break;
                    }
                    activity.txvRecord.append(showMsg);
                    //super.handleMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private static class RefreshHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;    //弱引用

        private RefreshHandler(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get();    //獲取弱引用的對象
            if (activity != null) {
                try {
                    switch (msg.what) {
                        case MyMessages.Connecting:
                            showMsg = "\n[Connecting]>>>";
                            break;
                        case MyMessages.Progressing:
                            showMsg = "[Progressing]>>>";
                            SharedPreferences myPref = myGetSharedPreferences(activity.getApplicationContext());
                            String strPayload = myPref.getString("PAYLOAD", "");//讀取已儲存的SharedPref
                            JSONObject jsonPayload = StringToJSON(strPayload);  //轉成JSON

                            Bundle bundle = msg.getData();
                            JSONObject jsonObj = BundleToJson(bundle.getBundle("token_Bundle"));
                            showMsg +=
                                    "\n[ResponseCode]：" + String.valueOf(jsonObj.getInt("responseCode")) +
                                            "\n[Token]：" + jsonObj.getString("token") +
                                            "\n[PAYLOAD.exp]：" + jsonPayload.getInt("exp") +
                                            "\n[PAYLOAD.orig_iat]：" + jsonPayload.getInt("orig_iat");
                            activity.txvStatus.setText("狀態：已登入 ; user_id = " + String.valueOf(jsonPayload.getInt("user_id")));
                            activity.txvStatus.setTextColor(Color.BLUE);

                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
                            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Taipei"));
                            Date expiredDate = new Date(jsonPayload.getInt("exp") * 1000L); //Unix timestamp 轉 Date
                            Date orig_iatDate = new Date(jsonPayload.getInt("orig_iat") * 1000L);
                            activity.txvExpired.setText("Token Expired：" + sdf.format(expiredDate));
                            activity.txvOrig_iat.setText("Token Issued At：" + sdf.format(orig_iatDate));
                            Toast.makeText(activity, "Token更新成功！", Toast.LENGTH_SHORT).show();
                            break;
                        case MyMessages.Disconnect:
                            showMsg = "\n[Disconnect]\n";
                            break;
                        case MyMessages.Error:
                            bundle = msg.getData();
                            String errorMsg = bundle.getString("errorMsg", "");
                            Toast.makeText(activity, errorMsg, Toast.LENGTH_LONG).show();
                            break;
                        default:
                            break;
                    }
                    activity.txvRecord.append(showMsg);
                    //super.handleMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);   //進入含有EditText的Activity時，不自動彈出虛擬鍵盤

        btnClear = findViewById(R.id.btnClear);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        btnOther = findViewById(R.id.btnOther);
        btnRefresh = findViewById(R.id.btnRefresh);
        btnProfile = findViewById(R.id.btnProfile);
        txvStatus = findViewById(R.id.txvStatus);
        txvRecord = findViewById(R.id.txvRecord);
        txvExpired = findViewById(R.id.txvExpired);
        txvOrig_iat = findViewById(R.id.txvOrig_iat);
        txtEmail = findViewById(R.id.txtEmail);
        txtPwd = findViewById(R.id.txtPwd);
        txtPwdConfirm = findViewById(R.id.txtPwdConfirm);
        spnType = findViewById(R.id.spnType);

        //檢查資料庫是否存在
        SharedPreferences dbStatusPref = getSharedPreferences("DBStatus", MODE_PRIVATE);
        boolean isFirst = dbStatusPref.getBoolean("isFirst", true);//第一次找不到為true
        if (isFirst) {
            SetUpLocalDB();//建立SQLite資料庫及資料表
            SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
            editor.putBoolean("isFirst", false);
            editor.apply();
        }

        //自動登入
        SharedPreferences myPref = getSharedPreferences("jwt_token", MODE_PRIVATE);  //此處使用預設的非靜態方法
        if (myPref.getString("token", "").equals("")) {
            txvStatus.setText("狀態：未登入");
            txvStatus.setTextColor(Color.BLACK);
        } else {
            try {
                String strPayload = myPref.getString("PAYLOAD", "");
                JSONObject jsonPayload = StringToJSON(strPayload);  //轉成JSON
                long exp = Long.valueOf(jsonPayload.getString("exp"));
                long now = System.currentTimeMillis() / 1000L;  //系統時間
                if (exp <= now) {    //逾期
                    txvStatus.setText("狀態：Token已逾期");
                    txvStatus.setTextColor(Color.RED);
                } else {
                    txvStatus.setText("狀態：自動登入; user_id = " + String.valueOf(jsonPayload.getInt("user_id")));
                    txvStatus.setTextColor(Color.BLUE);

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
                    sdf.setTimeZone(TimeZone.getTimeZone("Asia/Taipei"));
                    Date expiredDate = new Date(jsonPayload.getInt("exp") * 1000L); //Unix timestamp 轉 Date
                    Date orig_iatDate = new Date(jsonPayload.getInt("orig_iat") * 1000L);
                    txvExpired.setText("Token Expired：" + sdf.format(expiredDate));
                    txvOrig_iat.setText("Token Issued At：" + sdf.format(orig_iatDate));
                    btnLogin.setEnabled(false); //已登入不允許再登入及註冊
                    btnRegister.setEnabled(false);
                    btnProfile.setEnabled(true);
                    txtEmail.setEnabled(false);
                    txtPwd.setEnabled(false);
                    txtPwdConfirm.setEnabled(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //登入
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            loginHandler.sendEmptyMessage(MyMessages.Connecting);

                            Map<String, String> params = new HashMap<>();
                            //必填這三個欄位
                            params.put("username", txtEmail.getText().toString() + ";" + "2");  //xxx@xxx;2
                            params.put("password", txtPwd.getText().toString());    //MySQL392
                            params.put("membertype_id", spnType.getSelectedItem().toString());

                            Message message = new Message();
                            message.what = MyMessages.Progressing;
                            Bundle bundle = new Bundle();
                            JSONObject jsonObj = HttpUtils.GetToken(Path.api_token_jwtauth, params);
                            if(jsonObj.getInt("responseCode")!=HttpURLConnection.HTTP_OK){
                                message.what = MyMessages.Error;
                                bundle.putString("errorMsg", jsonObj.getString("non_field_errors"));    //"non_field_errors"為jwt預設"key"名稱
                                message.setData(bundle);
                                loginHandler.sendMessage(message);

                            }else{
                                DealToken(jsonObj.getString("token"));  //儲存Token
                                bundle.putBundle("token_Bundle", JsonToBundle(jsonObj));    //轉成Bundle
                                message.setData(bundle);
                                loginHandler.sendMessage(message);
                            }

                            loginHandler.sendEmptyMessage(MyMessages.Disconnect);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        //註冊
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (!(txtPwd.getText().toString().equals(txtPwdConfirm.getText().toString()))) {
                                Message message = new Message();
                                message.what = MyMessages.Error;
                                Bundle bundle = new Bundle();
                                bundle.putString("errorMsg", "兩次密碼不一致，請重新輸入！");
                                message.setData(bundle);
                                registerHandler.sendMessage(message);
                            } else {
                                registerHandler.sendEmptyMessage(MyMessages.Connecting);
                                Map<String, String> params = new HashMap<>();
                                params.put("account", txtEmail.getText().toString());    //必填
                                params.put("identifier", "85948764153");
                                params.put("membertype", spnType.getSelectedItem().toString());  //必填
                                params.put("name", "江建呈");
//                                params.put("nickname", null);
                                params.put("password", txtPwd.getText().toString()); //必填
//                                params.put("localpicture", "images\\usr\\pic001.jpg");
//                                params.put("dbpicture", "images\\usr\\pic020.jpg");

                                Message message = new Message();
                                message.what = MyMessages.Progressing;
                                Bundle bundle = new Bundle();
                                JSONObject jsonObj = HttpUtils.Register(Path.member, params);
                                if (jsonObj.getInt("responseCode") != HttpURLConnection.HTTP_CREATED) { //檢查responseCode
                                    message.what = MyMessages.Error;
                                    bundle.putString("errorMsg", jsonObj.getString("error_msg"));
                                    message.setData(bundle);
                                    registerHandler.sendMessage(message);

                                } else {    //註冊成功
                                    params = new HashMap<>();
                                    params.put("username", txtEmail.getText().toString() + ";" + spnType.getSelectedItem().toString());//帳號+會員類型 為唯一
                                    params.put("password", txtPwd.getText().toString());
                                    params.put("membertype_id", spnType.getSelectedItem().toString());
                                    JSONObject tokenJsonObj = HttpUtils.GetToken(Path.api_token_jwtauth, params);   //取得Token以利之後存取其他資源
                                    DealToken(tokenJsonObj.getString("token")); //儲存Token
                                    jsonObj.put("token", tokenJsonObj.getString("token"));   //加入註冊時回傳的json
                                    bundle.putBundle("member_Bundle", JsonToBundle(jsonObj));    //轉成Bundle
                                    message.setData(bundle);
                                    registerHandler.sendMessage(message);
                                }
                                registerHandler.sendEmptyMessage(MyMessages.Disconnect);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

            }
        });

        //其他功能
        btnOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(MainActivity.this,FriendshipActivity.class);
                startActivity(it);
            }
        });

        //更新token
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            refreshHandler.sendEmptyMessage(MyMessages.Connecting);
                            SharedPreferences myPref = getSharedPreferences("jwt_token", MODE_PRIVATE);
                            String token = myPref.getString("token", "");//讀取已儲存的Token

                            Map<String, String> params = new HashMap<>();
                            params.put("token", token);  //必填

                            Message message = new Message();
                            Bundle bundle = new Bundle();
                            JSONObject jsonObj = HttpUtils.RefreshToken(Path.api_token_refresh, params);
                            if (jsonObj.getInt("responseCode") != HttpURLConnection.HTTP_OK) { //檢查responseCode
                                message.what = MyMessages.Error;
                                bundle.putString("errorMsg", "尚未登入，請先登入！");
                                message.setData(bundle);
                                refreshHandler.sendMessage(message);
                            } else {  //更新成功
                                message.what = MyMessages.Progressing;
                                DealToken(jsonObj.getString("token"));  //儲存新Token
                                bundle.putBundle("token_Bundle", JsonToBundle(jsonObj));    //轉成Bundle
                                message.setData(bundle);
                                refreshHandler.sendMessage(message);
                            }

                            refreshHandler.sendEmptyMessage(MyMessages.Disconnect);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        //登出
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txvStatus.setText("狀態：未登入");
                txvStatus.setTextColor(Color.BLACK);
                txvRecord.setText("紀錄：");
                SharedPreferences.Editor editor = getSharedPreferences("jwt_token", MODE_PRIVATE).edit();
                editor.clear(); //清除Token
                editor.apply();
                txvExpired.setText("Token Expired：");
                txvOrig_iat.setText("Token Issued At：");

                btnLogin.setEnabled(true);
                btnRegister.setEnabled(true);
                btnProfile.setEnabled(false);

                txtEmail.setEnabled(true);
                txtPwd.setEnabled(true);
                txtPwdConfirm.setEnabled(true);
            }
        });

        //會員資料
        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(MainActivity.this,ProfileActivity.class);
                startActivity(it);
            }
        });
    }

    @Override
    protected void onDestroy() {    //清除所有Handler動作
        loginHandler.removeCallbacksAndMessages(null);
        registerHandler.removeCallbacksAndMessages(null);

        refreshHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private static Bundle JsonToBundle(JSONObject jsonObj) throws JSONException {
        Bundle bundle = new Bundle();
        Iterator iter = jsonObj.keys();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            String value = jsonObj.getString(key);
            bundle.putString(key, value);
        }
        return bundle;
    }

    private static JSONObject BundleToJson(Bundle bundle) throws JSONException {
        JSONObject jsonObj = new JSONObject();
        Set<String> keys = bundle.keySet();
        for (String key : keys) {
            jsonObj.put(key, JSONObject.wrap(bundle.get(key)));
        }
        return jsonObj;
    }

    public static JSONObject StringToJSON(String jsonString) throws JSONException {
        return new JSONObject(jsonString);
    }

    private void DealToken(String token) {
        SharedPreferences myPref = getSharedPreferences("jwt_token", MODE_PRIVATE);
        SharedPreferences.Editor editor = myPref.edit();
        editor.clear();  //舊的先清除
        editor.apply();
        String[] encodeArray = token.split("\\.");  //注意特殊字元
        String[] decodeArray = new String[2];   //[HEADER,PAYLOAD,VERIFY SIGNATURE] (VERIFY SIGNATURE 不處理)
        for (int i = 0; i < encodeArray.length - 1; i++) {
            byte[] data = Base64.decode(encodeArray[i], Base64.DEFAULT);
            decodeArray[i] = new String(data, StandardCharsets.UTF_8);
        }
        editor.putString("token", token)
                .putString("PAYLOAD", decodeArray[1])   //只需要PAYLOAD
                .apply();   //apply()為非同步寫入
        //http://android-deve.blogspot.com/2012/11/sharedpreferences-keyvalue.html
    }

    private void SetUpLocalDB() {
        // Android 使用 SQLite 資料庫的方法
        // http://jim690701.blogspot.tw/2012/06/android-sqlite.html
        // http://sweeteason.pixnet.net/blog/post/37364146-android-%E4%BD%BF%E7%94%A8-sqlite-%E8%B3%87%E6%96%99%E5%BA%AB%E7%9A%84%E6%96%B9%E6%B3%95

        //取得資料庫
        DBHelper DH = new DBHelper(this);
        db = DH.getReadableDatabase();
        //  db = openOrCreateDatabase(dbName, android.content.Context.MODE_PRIVATE, null);
        /*String TB_NAME;
        String[] col;
        String[] data;
        String cmd;
        Cursor cur;


        //新增sys_membertype資料
        TB_NAME = "sys_membertype";
        col = new String[]{"membertype_id", "name", "renew_time"};
        data = new String[]{"1", "管理員", datetime.getTime().toString(),
                "2", "本站帳號", datetime.getTime().toString(),
                "3", "Facebook", datetime.getTime().toString(),
                "4", "Google", datetime.getTime().toString()};
        AddData(TB_NAME, col, data);*/
    }

    public void AddData(String tableName, String[] columnName, String[] data) {
        //db = openOrCreateDatabase(DB_NAME, android.content.Context.MODE_PRIVATE, null);
        ContentValues cv ;
        for (int i = 0; i < data.length; ) {
            cv = new ContentValues(columnName.length);
            for (int j = 0; j < columnName.length; j++) {
                cv.put(columnName[j], data[i++]);
            }
            db.insert(tableName, null, cv);
        }
    }

    public void InitialMemberData(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DH = new DBHelper(MainActivity.this);
                    db = DH.getReadableDatabase();
                    SharedPreferences myPref = getSharedPreferences("jwt_token", MODE_PRIVATE);
                    String token = myPref.getString("token", "");    //讀取已儲存的Token
                    String strPayload = myPref.getString("PAYLOAD", "");
                    JSONObject jsonPayload = StringToJSON(strPayload);
                    String member_id = String.valueOf(jsonPayload.getInt("user_id"));   //讀取id
                    String path;
                    String TB_NAME;
                    String[] col;
                    String[] data;

                    //讀取並寫入本機分類
                    path = Path.sort + "?member_id=" + member_id;
                    JSONArray A = HttpUtils.Get(path, token);  //注意位置(最後一個是responseCode)
                    TB_NAME = "mbr_sort";
                    col = new String[]{"id", "type", "name", "icon", "renew_time", "member_id"};
                    data = new String[]{
                            A.getJSONObject(0).getString("id"), "0", "食品酒水", null, A.getJSONObject(0).getString("renew_time"), member_id,
                            A.getJSONObject(1).getString("id"), "0", "行車交通", null, A.getJSONObject(1).getString("renew_time"), member_id,
                            A.getJSONObject(2).getString("id"), "0", "居家生活", null, A.getJSONObject(2).getString("renew_time"), member_id,
                            A.getJSONObject(3).getString("id"), "0", "通訊媒體", null, A.getJSONObject(3).getString("renew_time"), member_id,
                            A.getJSONObject(4).getString("id"), "0", "教育教養", null, A.getJSONObject(4).getString("renew_time"), member_id,
                            A.getJSONObject(5).getString("id"), "0", "人際關係", null, A.getJSONObject(5).getString("renew_time"), member_id,
                            A.getJSONObject(6).getString("id"), "0", "休閒娛樂", null, A.getJSONObject(6).getString("renew_time"), member_id,
                            A.getJSONObject(7).getString("id"), "0", "醫療保健", null, A.getJSONObject(7).getString("renew_time"), member_id,
                            A.getJSONObject(8).getString("id"), "0", "財務金融", null, A.getJSONObject(8).getString("renew_time"), member_id,
                            A.getJSONObject(9).getString("id"), "0", "其他雜項", null, A.getJSONObject(9).getString("renew_time"), member_id,
                            A.getJSONObject(10).getString("id"), "0", "大型支出", null, A.getJSONObject(10).getString("renew_time"), member_id,
                            A.getJSONObject(11).getString("id"), "0", "電子發票", null, A.getJSONObject(11).getString("renew_time"), member_id,
                            A.getJSONObject(12).getString("id"), "1", "理財收入", null, A.getJSONObject(12).getString("renew_time"), member_id,
                            A.getJSONObject(13).getString("id"), "1", "其他收入", null, A.getJSONObject(13).getString("renew_time"), member_id,
                            A.getJSONObject(14).getString("id"), "1", "薪資報酬", null, A.getJSONObject(14).getString("renew_time"), member_id
                    };
                    AddData(TB_NAME, col, data);
                    Thread.sleep(500);

                    //讀取並寫入本機子分類
                    path = Path.subsort + "?member_id=" + member_id;
                    JSONArray B = HttpUtils.Get(path, token);  //注意位置(最後一個是responseCode)
                    TB_NAME = "mbr_subsort";
                    col = new String[]{"id", "type", "name", "icon", "renew_time", "member_id", "sort_id"};
                    data = new String[]{
                            B.getJSONObject(0).getString("id"), "0", "早餐", null, B.getJSONObject(0).getString("renew_time"), member_id, B.getJSONObject(0).getString("sort_id"),
                            B.getJSONObject(1).getString("id"), "0", "午餐", null, B.getJSONObject(1).getString("renew_time"), member_id, B.getJSONObject(1).getString("sort_id"),
                            B.getJSONObject(2).getString("id"), "0", "晚餐", null, B.getJSONObject(2).getString("renew_time"), member_id, B.getJSONObject(2).getString("sort_id"),
                            B.getJSONObject(3).getString("id"), "0", "飲品", null, B.getJSONObject(3).getString("renew_time"), member_id, B.getJSONObject(3).getString("sort_id"),
                            B.getJSONObject(4).getString("id"), "0", "水果", null, B.getJSONObject(4).getString("renew_time"), member_id, B.getJSONObject(4).getString("sort_id"),
                            B.getJSONObject(5).getString("id"), "0", "零食", null, B.getJSONObject(5).getString("renew_time"), member_id, B.getJSONObject(5).getString("sort_id"),
                            B.getJSONObject(6).getString("id"), "0", "公車", null, B.getJSONObject(6).getString("renew_time"), member_id, B.getJSONObject(6).getString("sort_id"),
                            B.getJSONObject(7).getString("id"), "0", "捷運", null, B.getJSONObject(7).getString("renew_time"), member_id, B.getJSONObject(7).getString("sort_id"),
                            B.getJSONObject(8).getString("id"), "0", "公共自行車", null, B.getJSONObject(8).getString("renew_time"), member_id, B.getJSONObject(8).getString("sort_id"),
                            B.getJSONObject(9).getString("id"), "0", "鐵路", null, B.getJSONObject(9).getString("renew_time"), member_id, B.getJSONObject(9).getString("sort_id"),
                            B.getJSONObject(10).getString("id"), "0", "計程車", null, B.getJSONObject(10).getString("renew_time"), member_id, B.getJSONObject(10).getString("sort_id"),
                            B.getJSONObject(11).getString("id"), "0", "燃料費", null, B.getJSONObject(11).getString("renew_time"), member_id, B.getJSONObject(11).getString("sort_id"),
                            B.getJSONObject(12).getString("id"), "0", "飛機", null, B.getJSONObject(12).getString("renew_time"), member_id, B.getJSONObject(12).getString("sort_id"),
                            B.getJSONObject(13).getString("id"), "0", "日常雜貨", null, B.getJSONObject(13).getString("renew_time"), member_id, B.getJSONObject(13).getString("sort_id"),
                            B.getJSONObject(14).getString("id"), "0", "嬰兒用品", null, B.getJSONObject(14).getString("renew_time"), member_id, B.getJSONObject(14).getString("sort_id"),
                            B.getJSONObject(15).getString("id"), "0", "寵物用品", null, B.getJSONObject(15).getString("renew_time"), member_id, B.getJSONObject(15).getString("sort_id"),
                            B.getJSONObject(16).getString("id"), "0", "治裝費", null, B.getJSONObject(16).getString("renew_time"), member_id, B.getJSONObject(16).getString("sort_id"),
                            B.getJSONObject(17).getString("id"), "0", "房租", null, B.getJSONObject(17).getString("renew_time"), member_id, B.getJSONObject(17).getString("sort_id"),
                            B.getJSONObject(18).getString("id"), "0", "電話費", null, B.getJSONObject(18).getString("renew_time"), member_id, B.getJSONObject(18).getString("sort_id"),
                            B.getJSONObject(19).getString("id"), "0", "網路費", null, B.getJSONObject(19).getString("renew_time"), member_id, B.getJSONObject(19).getString("sort_id"),
                            B.getJSONObject(20).getString("id"), "0", "多媒體", null, B.getJSONObject(20).getString("renew_time"), member_id, B.getJSONObject(20).getString("sort_id"),
                            B.getJSONObject(21).getString("id"), "0", "郵寄費", null, B.getJSONObject(21).getString("renew_time"), member_id, B.getJSONObject(21).getString("sort_id"),
                            B.getJSONObject(22).getString("id"), "0", "新聞雜誌", null, B.getJSONObject(22).getString("renew_time"), member_id, B.getJSONObject(22).getString("sort_id"),
                            B.getJSONObject(23).getString("id"), "0", "參考書籍", null, B.getJSONObject(23).getString("renew_time"), member_id, B.getJSONObject(23).getString("sort_id"),
                            B.getJSONObject(24).getString("id"), "0", "報名費", null, B.getJSONObject(24).getString("renew_time"), member_id, B.getJSONObject(24).getString("sort_id"),
                            B.getJSONObject(25).getString("id"), "0", "學雜費", null, B.getJSONObject(25).getString("renew_time"), member_id, B.getJSONObject(25).getString("sort_id"),
                            B.getJSONObject(26).getString("id"), "0", "補習費", null, B.getJSONObject(26).getString("renew_time"), member_id, B.getJSONObject(26).getString("sort_id"),
                            B.getJSONObject(27).getString("id"), "0", "聚餐", null, B.getJSONObject(27).getString("renew_time"), member_id, B.getJSONObject(27).getString("sort_id"),
                            B.getJSONObject(28).getString("id"), "0", "應酬", null, B.getJSONObject(28).getString("renew_time"), member_id, B.getJSONObject(28).getString("sort_id"),
                            B.getJSONObject(29).getString("id"), "0", "禮品", null, B.getJSONObject(29).getString("renew_time"), member_id, B.getJSONObject(29).getString("sort_id"),
                            B.getJSONObject(30).getString("id"), "0", "孝親", null, B.getJSONObject(30).getString("renew_time"), member_id, B.getJSONObject(30).getString("sort_id"),
                            B.getJSONObject(31).getString("id"), "0", "禮金", null, B.getJSONObject(31).getString("renew_time"), member_id, B.getJSONObject(31).getString("sort_id"),
                            B.getJSONObject(32).getString("id"), "0", "公益", null, B.getJSONObject(32).getString("renew_time"), member_id, B.getJSONObject(32).getString("sort_id"),
                            B.getJSONObject(33).getString("id"), "0", "電影動畫", null, B.getJSONObject(33).getString("renew_time"), member_id, B.getJSONObject(33).getString("sort_id"),
                            B.getJSONObject(34).getString("id"), "0", "音樂", null, B.getJSONObject(34).getString("renew_time"), member_id, B.getJSONObject(34).getString("sort_id"),
                            B.getJSONObject(35).getString("id"), "0", "書籍", null, B.getJSONObject(35).getString("renew_time"), member_id, B.getJSONObject(35).getString("sort_id"),
                            B.getJSONObject(36).getString("id"), "0", "遊戲", null, B.getJSONObject(36).getString("renew_time"), member_id, B.getJSONObject(36).getString("sort_id"),
                            B.getJSONObject(37).getString("id"), "0", "旅遊", null, B.getJSONObject(37).getString("renew_time"), member_id, B.getJSONObject(37).getString("sort_id"),
                            B.getJSONObject(38).getString("id"), "0", "展演", null, B.getJSONObject(38).getString("renew_time"), member_id, B.getJSONObject(38).getString("sort_id"),
                            B.getJSONObject(39).getString("id"), "0", "診療費", null, B.getJSONObject(39).getString("renew_time"), member_id, B.getJSONObject(39).getString("sort_id"),
                            B.getJSONObject(40).getString("id"), "0", "保健食品", null, B.getJSONObject(40).getString("renew_time"), member_id, B.getJSONObject(40).getString("sort_id"),
                            B.getJSONObject(41).getString("id"), "0", "美容美髮", null, B.getJSONObject(41).getString("renew_time"), member_id, B.getJSONObject(41).getString("sort_id"),
                            B.getJSONObject(42).getString("id"), "0", "手續費", null, B.getJSONObject(42).getString("renew_time"), member_id, B.getJSONObject(42).getString("sort_id"),
                            B.getJSONObject(43).getString("id"), "0", "勞健保費", null, B.getJSONObject(43).getString("renew_time"), member_id, B.getJSONObject(43).getString("sort_id"),
                            B.getJSONObject(44).getString("id"), "0", "人身保險", null, B.getJSONObject(44).getString("renew_time"), member_id, B.getJSONObject(44).getString("sort_id"),
                            B.getJSONObject(45).getString("id"), "0", "產險", null, B.getJSONObject(45).getString("renew_time"), member_id, B.getJSONObject(45).getString("sort_id"),
                            B.getJSONObject(46).getString("id"), "0", "稅務", null, B.getJSONObject(46).getString("renew_time"), member_id, B.getJSONObject(46).getString("sort_id"),
                            B.getJSONObject(47).getString("id"), "0", "分期付款", null, B.getJSONObject(47).getString("renew_time"), member_id, B.getJSONObject(47).getString("sort_id"),
                            B.getJSONObject(48).getString("id"), "0", "投資損益", null, B.getJSONObject(48).getString("renew_time"), member_id, B.getJSONObject(48).getString("sort_id"),
                            B.getJSONObject(49).getString("id"), "0", "其他", null, B.getJSONObject(49).getString("renew_time"), member_id, B.getJSONObject(49).getString("sort_id"),
                            B.getJSONObject(50).getString("id"), "0", "家電", null, B.getJSONObject(50).getString("renew_time"), member_id, B.getJSONObject(50).getString("sort_id"),
                            B.getJSONObject(51).getString("id"), "0", "家具", null, B.getJSONObject(51).getString("renew_time"), member_id, B.getJSONObject(51).getString("sort_id"),
                            B.getJSONObject(52).getString("id"), "0", "車輛", null, B.getJSONObject(52).getString("renew_time"), member_id, B.getJSONObject(52).getString("sort_id"),
                            B.getJSONObject(53).getString("id"), "0", "房產", null, B.getJSONObject(53).getString("renew_time"), member_id, B.getJSONObject(53).getString("sort_id"),
                            B.getJSONObject(54).getString("id"), "0", "電子發票", null, B.getJSONObject(54).getString("renew_time"), member_id, B.getJSONObject(54).getString("sort_id"),
                            B.getJSONObject(55).getString("id"), "0", "利息", null, B.getJSONObject(55).getString("renew_time"), member_id, B.getJSONObject(55).getString("sort_id"),
                            B.getJSONObject(56).getString("id"), "0", "股息", null, B.getJSONObject(56).getString("renew_time"), member_id, B.getJSONObject(56).getString("sort_id"),
                            B.getJSONObject(57).getString("id"), "0", "禮金", null, B.getJSONObject(57).getString("renew_time"), member_id, B.getJSONObject(57).getString("sort_id"),
                            B.getJSONObject(58).getString("id"), "0", "回饋金", null, B.getJSONObject(58).getString("renew_time"), member_id, B.getJSONObject(58).getString("sort_id"),
                            B.getJSONObject(59).getString("id"), "0", "補助金", null, B.getJSONObject(59).getString("renew_time"), member_id, B.getJSONObject(59).getString("sort_id"),
                            B.getJSONObject(60).getString("id"), "0", "薪水", null, B.getJSONObject(60).getString("renew_time"), member_id, B.getJSONObject(60).getString("sort_id"),
                            B.getJSONObject(61).getString("id"), "0", "獎金", null, B.getJSONObject(61).getString("renew_time"), member_id, B.getJSONObject(61).getString("sort_id")
                    };
                    AddData(TB_NAME, col, data);
                    Thread.sleep(500);

                    //讀取並寫入本機帳戶
                    path = Path.account + "?member_id=" + member_id;
                    JSONArray C = HttpUtils.Get(path, token);  //注意位置(最後一個是responseCode)
                    TB_NAME = "mbr_account";
                    col = new String[]{"id", "name", "initialamount", "balance", "FX", "renew_time", "accounttype_id", "member_id"};
                    data = new String[]{
                            C.getJSONObject(0).getString("id"), "現金", "0", "0", "1:1", C.getJSONObject(0).getString("renew_time"), "1", member_id,
                            C.getJSONObject(1).getString("id"), "銀行簽帳卡", "0", "0", "1:1", C.getJSONObject(1).getString("renew_time"), "2", member_id,
                            C.getJSONObject(2).getString("id"), "悠遊卡", "0", "0", "1:1", C.getJSONObject(2).getString("renew_time"), "3", member_id
                    };
                    AddData(TB_NAME, col, data);
                    Thread.sleep(500);

                    //讀取並寫入本機專案
                    path = Path.project + "?member_id=" + member_id;
                    JSONArray D = HttpUtils.Get(path, token);  //注意位置(最後一個是responseCode)
                    TB_NAME = "mbr_project";
                    col = new String[]{"id", "name", "renew_time", "member_id"};
                    data = new String[]{
                            D.getJSONObject(0).getString("id"), "一般", D.getJSONObject(0).getString("renew_time"), member_id,
                            D.getJSONObject(1).getString("id"), "出差", D.getJSONObject(1).getString("renew_time"), member_id,
                            D.getJSONObject(2).getString("id"), "旅遊", D.getJSONObject(2).getString("renew_time"), member_id,
                            D.getJSONObject(3).getString("id"), "電子發票", D.getJSONObject(3).getString("renew_time"), member_id
                    };
                    AddData(TB_NAME, col, data);
                    Thread.sleep(500);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
