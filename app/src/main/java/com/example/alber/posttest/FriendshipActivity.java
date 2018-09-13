package com.example.alber.posttest;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FriendshipActivity extends AppCompatActivity
    implements DialogInterface.OnClickListener {

    private Button btnReturn, btnGet, btnFriendList, btnSearchFriend, btnAddFriend;
    private static String showMsg = "\n";
    private TextView txvRecord, txvFriendName;
    private EditText txtToid;
    DBHelper DH;
    SQLiteDatabase db;
    Cursor cursor;
    private JSONObject jsonObjectSearchFriend = null;
    private Calendar datetime = Calendar.getInstance(Locale.TAIWAN);


    private final GetHandler getHandler = new GetHandler(FriendshipActivity.this);
    private final FriendListHandler friendListHandler = new FriendListHandler(FriendshipActivity.this);
    private final SearchFriendHandler searchFriendHandler = new SearchFriendHandler(FriendshipActivity.this);
    private final AddFriendHandler addFriendHandler = new AddFriendHandler(FriendshipActivity.this);

    private class MyMessages {
        public static final int Error = 0;
        public static final int Connecting = 1;
        public static final int Progressing = 2;
        public static final int Disconnect = 3;
    }

    private static class GetHandler extends Handler {
        private final WeakReference<FriendshipActivity> mActivity;    //弱引用

        private GetHandler(FriendshipActivity activity) {
            mActivity = new WeakReference<FriendshipActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            FriendshipActivity activity = mActivity.get();    //獲取弱引用的對象
            if (activity != null) {
                try {
                    switch (msg.what) {
                        case FriendshipActivity.MyMessages.Connecting:
                            showMsg = "\n[Connecting]>>>";
                            break;
                        case FriendshipActivity.MyMessages.Progressing:
                            showMsg = "[Progressing]>>>";
                            Bundle bundle = msg.getData();
                            JSONArray jsonArray = new JSONArray(bundle.getString("get_jsonArrayString"));
                            JSONObject jsonObject = jsonArray.getJSONObject(jsonArray.length() - 1);    //最後一個為responseCode
                            showMsg +=
                                    "\n[ResponseCode]：" + String.valueOf(jsonObject.getInt("responseCode")) +
                                            "\n" + jsonArray.toString();  //這邊直接輸出字串，未處理

                            Toast.makeText(activity, "讀取成功！", Toast.LENGTH_SHORT).show();
                            break;
                        case FriendshipActivity.MyMessages.Disconnect:
                            showMsg = "\n[Disconnect]\n";
                            break;
                        case FriendshipActivity.MyMessages.Error:
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

    private static class FriendListHandler extends Handler {
        private final WeakReference<FriendshipActivity> mActivity;    //弱引用

        private FriendListHandler(FriendshipActivity activity) {
            mActivity = new WeakReference<FriendshipActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            FriendshipActivity activity = mActivity.get();    //獲取弱引用的對象
            if (activity != null) {
                try {
                    switch (msg.what) {
                        case FriendshipActivity.MyMessages.Connecting:
                            showMsg = "\n[Connecting]>>>";
                            break;
                        case FriendshipActivity.MyMessages.Progressing:
                            showMsg = "[Progressing]>>>";
                            Bundle bundle = msg.getData();
                            JSONArray jsonArray = new JSONArray(bundle.getString("get_jsonArrayString"));
                            JSONObject jsonObject = jsonArray.getJSONObject(jsonArray.length() - 1);    //最後一個為responseCode
                            showMsg +=
                                    "\n[ResponseCode]：" + String.valueOf(jsonObject.getInt("responseCode")) + "\n";
                            if (jsonArray.length() == 1) {//只有回應碼
                                showMsg += "沒有好友";
                            } else {
                                for (int i = 0; i < jsonArray.length() - 1; i++) {   //取出資料，注意位置(最後一個是responseCode)，注意null
                                    jsonObject = jsonArray.getJSONObject(i);
                                    String id = jsonObject.getString("id");
                                    String nickname = jsonObject.getString("nickname");
                                    String member = jsonObject.getString("member");
                                    String friend = jsonObject.getString("friend");
                                    String renew_time = jsonObject.getString("renew_time");
                                    showMsg += "{id:" + id + ", nickname:" + nickname + ", member:" + member
                                            + ", friend:" + friend + ", renew_time:" + renew_time + "},\n";
                                }
                            }
                            Toast.makeText(activity, "讀取成功！", Toast.LENGTH_SHORT).show();
                            break;
                        case FriendshipActivity.MyMessages.Disconnect:
                            showMsg = "\n[Disconnect]\n";
                            break;
                        case FriendshipActivity.MyMessages.Error:
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

    private static class SearchFriendHandler extends Handler {
        private final WeakReference<FriendshipActivity> mActivity;    //弱引用

        private SearchFriendHandler(FriendshipActivity activity) {
            mActivity = new WeakReference<FriendshipActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            FriendshipActivity activity = mActivity.get();    //獲取弱引用的對象
            if (activity != null) {
                try {
                    switch (msg.what) {
                        case FriendshipActivity.MyMessages.Connecting:
                            showMsg = "\n[Connecting]>>>";
                            break;
                        case FriendshipActivity.MyMessages.Progressing:
                            showMsg = "[Progressing]>>>";
                            Bundle bundle = msg.getData();
                            JSONArray jsonArray = new JSONArray(bundle.getString("get_jsonArrayString"));
                            JSONObject jsonObject = jsonArray.getJSONObject(jsonArray.length() - 1);    //最後一個為responseCode
                            showMsg +=
                                    "\n[ResponseCode]：" + String.valueOf(jsonObject.getInt("responseCode")) + "\n";
                            if (jsonArray.length() == 1) {//只有回應碼
                                showMsg += "查無此人";
                            } else {
                                for (int i = 0; i < jsonArray.length() - 1; i++) {  //取出資料，注意位置(最後一個是responseCode)，注意null
                                    jsonObject = jsonArray.getJSONObject(i);
                                    String id = jsonObject.getString("id");
                                    String name = jsonObject.getString("name");
                                    String localpicture = jsonObject.getString("localpicture");
                                    String dbpicture = jsonObject.getString("dbpicture");
                                    showMsg += "{id:" + id + ", name:" + name +
                                            ", localpicture:" + localpicture + ", dbpicture:" + dbpicture + "},\n";
                                }
                                activity.btnAddFriend.setEnabled(true);
                                activity.txvFriendName.setText(jsonObject.getString("name"));
                            }
                            Toast.makeText(activity, "查詢成功！", Toast.LENGTH_SHORT).show();
                            break;
                        case FriendshipActivity.MyMessages.Disconnect:
                            showMsg = "\n[Disconnect]\n";
                            break;
                        case FriendshipActivity.MyMessages.Error:
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

    private static class AddFriendHandler extends Handler {
        private final WeakReference<FriendshipActivity> mActivity;    //弱引用

        private AddFriendHandler(FriendshipActivity activity) {
            mActivity = new WeakReference<FriendshipActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            FriendshipActivity activity = mActivity.get();    //獲取弱引用的對象
            if (activity != null) {
                try {
                    switch (msg.what) {
                        case FriendshipActivity.MyMessages.Connecting:
                            showMsg = "\n[Connecting]>>>";
                            break;
                        case FriendshipActivity.MyMessages.Progressing:
                            Bundle bundle = msg.getData();
                            JSONObject jsonObject = new JSONObject(bundle.getString("patch_jsonString"));
                            int responseCode = jsonObject.getInt("responseCode");
                            String strMsg;
                            if (responseCode == 204) {
                                strMsg = "更新成功！\n" + jsonObject.toString();


                            } else {
                                strMsg = "更新失敗！\n" + jsonObject.toString();
                            }

                            Toast.makeText(activity, strMsg, Toast.LENGTH_LONG).show();
                            break;
                        case FriendshipActivity.MyMessages.Disconnect:
                            showMsg = "\n[Disconnect]\n";
                            break;
                        case FriendshipActivity.MyMessages.Error:
                            bundle = msg.getData();
                            String errorMsg = bundle.getString("errorMsg", "");
                            Toast.makeText(activity, errorMsg, Toast.LENGTH_LONG).show();
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
        setContentView(R.layout.activity_friendship);
        txvRecord = findViewById(R.id.txvRecord);
        txvFriendName = findViewById(R.id.txvFriendName);
        txtToid = findViewById(R.id.txtToid);
        btnReturn = findViewById(R.id.btnReturn);
        btnGet = findViewById(R.id.btnGet);
        btnFriendList = findViewById(R.id.btnFriendList);
        btnSearchFriend = findViewById(R.id.btnSearchFriend);
        btnAddFriend = findViewById(R.id.btnAddFriend);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);   //進入含有EditText的Activity時，不自動彈出虛擬鍵盤

        //返回
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //取得特定資料
        btnGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            getHandler.sendEmptyMessage(FriendshipActivity.MyMessages.Connecting);

                            SharedPreferences myPref = getSharedPreferences("jwt_token", MODE_PRIVATE);
                            String token = myPref.getString("token", "");//讀取已儲存的Token
                            Message message = new Message();
                            Bundle bundle = new Bundle();
                            if (token.equals("")) {
                                message.what = FriendshipActivity.MyMessages.Error;
                                bundle.putString("errorMsg", "尚未登入，請先登入！");
                                message.setData(bundle);
                                getHandler.sendMessage(message);
                            } else {
                                message.what = FriendshipActivity.MyMessages.Progressing;
                                JSONArray jsonArray = HttpUtils.Get(MainActivity.Path.member, token);
                                bundle.putString("get_jsonArrayString", jsonArray.toString());    //轉成String
                                message.setData(bundle);
                                getHandler.sendMessage(message);
                            }

                            getHandler.sendEmptyMessage(FriendshipActivity.MyMessages.Disconnect);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        //好友列表
        btnFriendList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            friendListHandler.sendEmptyMessage(FriendshipActivity.MyMessages.Connecting);

                            SharedPreferences myPref = getSharedPreferences("jwt_token", MODE_PRIVATE);
                            String token = myPref.getString("token", "");//讀取已儲存的Token
                            Message message = new Message();
                            Bundle bundle = new Bundle();
                            if (token.equals("")) {
                                message.what = FriendshipActivity.MyMessages.Error;
                                bundle.putString("errorMsg", "尚未登入，請先登入！");
                                message.setData(bundle);
                                friendListHandler.sendMessage(message);
                            } else {
                                message.what = FriendshipActivity.MyMessages.Progressing;
                                //從SharedPreferences裡面找自己的user_id帶入查詢
                                String strPayload = myPref.getString("PAYLOAD", "");
                                JSONObject jsonPayload = MainActivity.StringToJSON(strPayload);  //轉成JSON
                                String path = MainActivity.Path.friendShip + "?member_id=" + String.valueOf(jsonPayload.getInt("user_id"));
                                JSONArray jsonArray = HttpUtils.Get(path, token);
                                bundle.putString("get_jsonArrayString", jsonArray.toString());    //轉成String
                                message.setData(bundle);
                                friendListHandler.sendMessage(message);
                            }

                            friendListHandler.sendEmptyMessage(FriendshipActivity.MyMessages.Disconnect);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        //搜尋好友
        btnSearchFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            searchFriendHandler.sendEmptyMessage(FriendshipActivity.MyMessages.Connecting);

                            SharedPreferences myPref = getSharedPreferences("jwt_token", MODE_PRIVATE);
                            String token = myPref.getString("token", "");//讀取已儲存的Token
                            Message message = new Message();
                            Bundle bundle = new Bundle();
                            if (token.equals("")) {
                                message.what = FriendshipActivity.MyMessages.Error;
                                bundle.putString("errorMsg", "尚未登入，請先登入！");
                                message.setData(bundle);
                                searchFriendHandler.sendMessage(message);
                            } else {
                                message.what = FriendshipActivity.MyMessages.Progressing;
                                //帶入查詢參數
                                String toid = txtToid.getText().toString().toUpperCase();   //大寫
                                String path = MainActivity.Path.member + "?toid=" + toid;
                                JSONArray jsonArray = HttpUtils.Get(path, token);
                                bundle.putString("get_jsonArrayString", jsonArray.toString());    //轉成String
                                message.setData(bundle);
                                searchFriendHandler.sendMessage(message);
                                //供全域使用
                                jsonObjectSearchFriend = jsonArray.getJSONObject(0); //0:資料,1:responseCode
                            }

                            searchFriendHandler.sendEmptyMessage(FriendshipActivity.MyMessages.Disconnect);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        //加好友
        btnAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //顯示"訊問是否加入"視窗
                    new AlertDialog.Builder(FriendshipActivity.this)
                            .setTitle("確認加入好友")
                            .setMessage(jsonObjectSearchFriend.getString("name"))
                            .setPositiveButton("是", FriendshipActivity.this)
                            .setNegativeButton("否", FriendshipActivity.this)
                            .show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        try {
            DH = new DBHelper(FriendshipActivity.this);
            db = DH.getReadableDatabase();
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    //取得自己的id
                    SharedPreferences myPref = getSharedPreferences("jwt_token", MODE_PRIVATE);
                    String strPayload = myPref.getString("PAYLOAD", "");
                    JSONObject jsonPayload = MainActivity.StringToJSON(strPayload);  //轉成JSON
                    final String user_id = String.valueOf(jsonPayload.getInt("user_id"));
                    //好友id
                    final String friend_id = jsonObjectSearchFriend.getString("id");


                    //寫入資料庫

                    //寫入本機會員資料表
                    String sqlCmd = String.format(//注意空格
                            "INSERT INTO mbr_member (id, name, localpicture, dbpicture) " +
                                    "VALUES (%s, \"%s\", \"%s\", \"%s\")",
                            friend_id,
                            jsonObjectSearchFriend.getString("name"),
                            jsonObjectSearchFriend.getString("localpicture"),
                            jsonObjectSearchFriend.getString("dbpicture"));
                    db.execSQL(sqlCmd);

                    //寫入MySQL好友表
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            addFriendHandler.sendEmptyMessage(MyMessages.Connecting);
                            Message message = new Message();
                            Bundle bundle = new Bundle();
                            
                            message.what = FriendshipActivity.MyMessages.Progressing;
                            SharedPreferences tokenPref = getSharedPreferences("jwt_token", MODE_PRIVATE);
                            String token = tokenPref.getString("token", "");//讀取已儲存的Token
                            Map<String, String> params = new HashMap<>();
                            params.put("member_id", user_id);
                            params.put("friend_id", friend_id);
                            params.put("nickname",null);

                            JSONObject jsonObject = HttpUtils.Post(MainActivity.Path.friendShip,token,params);
                            bundle.putString("post_jsonString", jsonObject.toString());    //轉成String
                            message.setData(bundle);
                            addFriendHandler.sendMessage(message);

                            addFriendHandler.sendEmptyMessage(MyMessages.Disconnect);
                        }
                    }).start();

                    //寫入本機好友表
                    sqlCmd = String.format(//注意空格
                            "INSERT INTO mbr_friendship (member_id, friend_id, nickname, renew_time) VALUES " +
                                    "(%s, \"%s\", \"%s\", \"%s\")," +
                                    "(%s, \"%s\", \"%s\", \"%s\")",
                            user_id,    //自己
                            friend_id, //對方
                            null,
                            datetime.getTime().toString(),

                            friend_id, //對方
                            user_id,    //自己
                            null,
                            datetime.getTime().toString());
                    db.execSQL(sqlCmd);

                    //讀取
                    cursor = db.rawQuery("SELECT member_id,friend_id,nickname FROM mbr_friendship", null);
                    String msg = "";
                    int rowsCount = cursor.getCount();
                    if (rowsCount != 0) {
                        cursor.moveToFirst();
                        for (int i = 0; i < rowsCount; i++) {
                            msg += cursor.getString(0) + " ";
                            msg += cursor.getString(1) + " ";
                            msg += cursor.getString(2) + " ";
                            msg += "\n";

                            cursor.moveToNext();
                        }
                    }
                    txvRecord.append("\n好友列表(本機)：\n" + msg);
                    db.close();
                    Toast.makeText(FriendshipActivity.this, "加入成功", Toast.LENGTH_LONG).show();

                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    Toast.makeText(FriendshipActivity.this, "取消", Toast.LENGTH_LONG).show();
                    txvFriendName.setText("(好友姓名)");
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        getHandler.removeCallbacksAndMessages(null);    //清除Handler動作
        super.onDestroy();
    }
}
