package com.example.alber.posttest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
    private static String showMsg = "\n";
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
                            params.put("membertype_id", "2");

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
                                params.put("identifier", null);
                                params.put("membertype", "2");  //必填
                                params.put("name", null);
                                params.put("nickname", null);
                                params.put("password", txtPwd.getText().toString()); //必填
                                params.put("localpicture", "images\\usr\\pic001.jpg");
                                params.put("dbpicture", "images\\usr\\pic020.jpg");

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
                                    params.put("username", txtEmail.getText().toString() + ";" + "2");//帳號+會員類型 為唯一
                                    params.put("password", txtPwd.getText().toString());
                                    params.put("membertype_id", "2");   //本站會員
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
}
