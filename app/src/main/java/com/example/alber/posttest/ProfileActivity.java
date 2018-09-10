package com.example.alber.posttest;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private Button btnReturn2, btnUpdate;
    private EditText txtAccount, txtToid, txtName;
    private static String account, toid, name;

    private final ProfileUpdateHandler profileUpdateHandler = new ProfileUpdateHandler(ProfileActivity.this);

    private class MyMessages {
        public static final int Error = 0;
        public static final int Connecting = 1;
        public static final int Progressing = 2;
        public static final int Disconnect = 3;
    }

    private static SharedPreferences myGetSharedPreferences(Context context) {
        return context.getSharedPreferences("jwt_token", MODE_PRIVATE);
    }

    private static class ProfileUpdateHandler extends Handler {
        private final WeakReference<ProfileActivity> mActivity;    //弱引用

        private ProfileUpdateHandler(ProfileActivity activity) {
            mActivity = new WeakReference<ProfileActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            ProfileActivity activity = mActivity.get();    //獲取弱引用的對象
            if (activity != null) {
                try {
                    switch (msg.what) {
                        case ProfileActivity.MyMessages.Progressing:
                            Bundle bundle = msg.getData();
                            JSONObject jsonObject = new JSONObject(bundle.getString("patch_jsonString"));
                            int responseCode = jsonObject.getInt("responseCode");
                            String strMsg;
                            if(responseCode == 204){
                                strMsg = "更新成功！\n" + jsonObject.toString();
                                SharedPreferences myPref = myGetSharedPreferences(activity.getApplicationContext());    //靜態方法
                                String strPayload = myPref.getString("PAYLOAD", "");//讀取已儲存的SharedPref
                                JSONObject jsonPayload = MainActivity.StringToJSON(strPayload);  //轉成JSON
                                toid = activity.txtToid.getText().toString();   //覆寫
                                name = activity.txtName.getText().toString();
                                jsonPayload.put("toid",toid);   //覆寫
                                jsonPayload.put("name",name);
                                SharedPreferences.Editor editor = myPref.edit();
                                editor.putString("PAYLOAD",jsonPayload.toString());    //存入
                                editor.apply();
                            }
                            else{
                                strMsg = "更新失敗！\n" + jsonObject.toString();
                            }

                            Toast.makeText(activity, strMsg, Toast.LENGTH_LONG).show();
                            break;
                        default:
                            break;
                    }
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
        setContentView(R.layout.activity_profile);
        btnReturn2 = findViewById(R.id.btnReturn2);
        btnUpdate = findViewById(R.id.btnUpdate);
        txtAccount = findViewById(R.id.txtAccount);
        txtToid = findViewById(R.id.txtToid);
        txtName = findViewById(R.id.txtName);

        try {
            //帶入資料
            SharedPreferences myPref = getSharedPreferences("jwt_token", MODE_PRIVATE);
            String strPayload = myPref.getString("PAYLOAD", "");//讀取已儲存的SharedPref
            JSONObject jsonPayload = MainActivity.StringToJSON(strPayload);  //轉成JSON
            account = jsonPayload.getString("username");
            toid = jsonPayload.getString("toid");
            name = jsonPayload.getString("name");

            txtAccount.setText(account.substring(0,account.length()-2));   //去除分號之後
            txtToid.setText(toid);
            txtName.setText(name);

        } catch (Exception e) {
            e.printStackTrace();
        }

        //返回
        btnReturn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //更新
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //先檢查有無修改
                final String strToid = txtToid.getText().toString().toUpperCase();
                final String strName = txtName.getText().toString();

                if(strToid.equals(toid)&&strName.equals(name)) { //未修改
                    Toast.makeText(getBaseContext(), "資料未變更，未更新", Toast.LENGTH_LONG).show();
                }
                else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                SharedPreferences myPref = getSharedPreferences("jwt_token", MODE_PRIVATE);
                                String token = myPref.getString("token", "");//讀取已儲存的Token
                                String strPayload = myPref.getString("PAYLOAD", "");//讀取已儲存的SharedPref
                                JSONObject jsonPayload = MainActivity.StringToJSON(strPayload);  //轉成JSON
                                Map<String, String> params = new HashMap<>();
                                params.put("toid", strToid);
                                params.put("name", strName);
                                Message message = new Message();
                                Bundle bundle = new Bundle();

                                message.what = ProfileActivity.MyMessages.Progressing;
                                String path = MainActivity.Path.member + jsonPayload.getString("user_id") + "/";
                                JSONObject jsonObject = HttpUtils.Patch(path, token, params);
                                bundle.putString("patch_jsonString", jsonObject.toString());    //轉成String
                                message.setData(bundle);
                                profileUpdateHandler.sendMessage(message);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {    //清除所有Handler動作

//        refreshHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
