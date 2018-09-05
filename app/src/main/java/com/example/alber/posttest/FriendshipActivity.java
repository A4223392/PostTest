package com.example.alber.posttest;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
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

public class FriendshipActivity extends AppCompatActivity {

    private Button btnReturn,btnGet,btnFriendList,btnSearchFriend;
    private static String showMsg = "\n";
    private TextView txvRecord;
    private EditText txtToid;
    private final GetHandler getHandler = new GetHandler(FriendshipActivity.this);
    private final FriendListHandler friendListHandler = new FriendListHandler(FriendshipActivity.this);
    private final SearchFriendHandler searchFriendHandler = new SearchFriendHandler(FriendshipActivity.this);

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
                            if(jsonArray.length()==1){//只有回應碼
                                showMsg += "沒有好友";
                            }else {
                                for (int i = 0; i < jsonArray.length()-1; i++) {   //取出資料，注意位置(最後一個是responseCode)，注意null
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
                            if(jsonArray.length()==1){//只有回應碼
                                showMsg += "查無此人";
                            }else {
                                for (int i = 0; i < jsonArray.length()-1; i++) {  //取出資料，注意位置(最後一個是responseCode)，注意null
                                    jsonObject = jsonArray.getJSONObject(i);
                                    String id = jsonObject.getString("id");
                                    String name = jsonObject.getString("name");
                                    String localpicture = jsonObject.getString("localpicture");
                                    String dbpicture = jsonObject.getString("dbpicture");
                                    showMsg += "{id:" + id + ", name:" + name +
                                            ", localpicture:" + localpicture + ", dbpicture:" + dbpicture + "},\n";
                                }
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friendship);
        txvRecord = findViewById(R.id.txvRecord);
        txtToid = findViewById(R.id.txtToid);
        btnReturn = findViewById(R.id.btnReturn);
        btnGet = findViewById(R.id.btnGet);
        btnFriendList = findViewById(R.id.btnFriendList);
        btnSearchFriend = findViewById(R.id.btnSearchFriend);
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
                                String path = MainActivity.Path.member + "?toid=" + toid ;
                                JSONArray jsonArray = HttpUtils.Get(path, token);
                                bundle.putString("get_jsonArrayString", jsonArray.toString());    //轉成String
                                message.setData(bundle);
                                searchFriendHandler.sendMessage(message);
                            }

                            searchFriendHandler.sendEmptyMessage(FriendshipActivity.MyMessages.Disconnect);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
    }

    @Override
    protected void onDestroy() {    //清除所有Handler動作
        getHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
