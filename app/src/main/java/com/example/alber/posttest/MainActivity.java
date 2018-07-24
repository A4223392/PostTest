package com.example.alber.posttest;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {


    private Button btnPost, btnGet, btnClear;
    private TextView txvShow;
    private String showMsg = "";

    public class MyMessages {
        public static final int Connecting = 0;
        public static final int Post_Progressing = 1;
        public static final int Get_Progressing = 2;
        public static final int Disconnect = 3;
    }

    public class MainHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            try {
                switch (msg.what) {
                    case MyMessages.Connecting:
                        showMsg = "[Connecting]\n";
                        break;
                    case MyMessages.Post_Progressing:
                        showMsg = "[Post_Progressing]\n--------------------\n";
                        Bundle postData = msg.getData();
                        String postReturnValue = postData.getString("PostReturnValue");
                        showMsg += postReturnValue + "\n";
                        break;
                    case MyMessages.Get_Progressing:
                        showMsg = "[Get_Progressing]\n--------------------\n";
                        Bundle getData = msg.getData();
                        String[] getReturnValue = getData.getStringArray("GetReturnValue");
                        for (String elem : getReturnValue) {
                            showMsg += elem + "\n";
                        }
                        break;
                    case MyMessages.Disconnect:
                        showMsg = "--------------------\n[Disconnect]";
                        break;
                    default:
                        break;
                }
                txvShow.append(showMsg);
                //super.handleMessage(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnPost = findViewById(R.id.btnPost);
        btnGet = findViewById(R.id.btnGet);
        btnClear = findViewById(R.id.btnClear);
        txvShow = findViewById(R.id.txvShow);
        txvShow.setText("");

        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final MainHandler mainHandler = new MainHandler();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mainHandler.sendEmptyMessage(MyMessages.Connecting);

                            String path = "http://192.168.88.246/api/member/";   //路徑
                            Map<String, String> params = new HashMap<>();
                            params.put("account", "A42233929");
                            params.put("identifier", "12345678");
                            params.put("name", "江建呈");
                            params.put("nickname", "江");
                            params.put("password", "MySQL0819");
                            params.put("localpicture", "images\\usr\\pic001.jpg");
                            params.put("dbpicture", "images\\usr\\pic020.jpg");
                            params.put("membertype_id", "1");

                            Message message = new Message();
                            message.what = MyMessages.Post_Progressing;
                            Bundle bundle = new Bundle();
                            bundle.putString("PostReturnValue", HttpUtils.SubmitPostData(path, params, "Utf-8"));
                            message.setData(bundle);
                            mainHandler.sendMessage(message);

                            mainHandler.sendEmptyMessage(MyMessages.Disconnect);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        btnGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final MainHandler mainHandler = new MainHandler();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mainHandler.sendEmptyMessage(MyMessages.Connecting);

                            String path = "http://192.168.88.246/api/member/6/"; //路徑
                            String[] colName = {
                                    "id", "account", "identifier",
                                    "name", "nickname", "password",
                                    "localpicture", "dbpicture", "renew_time", "membertype_id"};
                            Message message = new Message();
                            message.what = MyMessages.Get_Progressing;
                            Bundle bundle = new Bundle();
                            bundle.putStringArray("GetReturnValue", HttpUtils.SendGetRequest(path, colName));
                            message.setData(bundle);
                            mainHandler.sendMessage(message);

                            mainHandler.sendEmptyMessage(MyMessages.Disconnect);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txvShow.setText("");
            }
        });
    }
}
