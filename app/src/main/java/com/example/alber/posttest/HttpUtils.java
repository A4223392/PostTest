package com.example.alber.posttest;

import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;


public class HttpUtils {
    /**
     * 发送POST请求到服务器并返回服务器信息
     *
     * @param path  POST的路徑
     * @param params 请求体内容
     * @param encode 编码格式
     * @return 服务器返回信息
     */
    public static String SubmitPostData(String path, Map<String, String> params, String encode) throws MalformedURLException {

        byte[] data = GetRequestData(params, encode).toString().getBytes();
        URL url = new URL(path);    //路徑
        HttpURLConnection httpURLConnection = null;
        try {
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setConnectTimeout(3000);  // 设置连接超时时间
            httpURLConnection.setDoInput(true);         // 打开输入流，以便从服务器获取数据
            httpURLConnection.setDoOutput(true);        // 打开输出流，以便向服务器提交数据
            httpURLConnection.setRequestMethod("POST"); // 设置以POST方式提交数据
            httpURLConnection.setUseCaches(false);      // 使用POST方式不能使用缓存
            // 设置请求体的类型是文本类型
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            // 设置请求体的长度
            httpURLConnection.setRequestProperty("Content-Length", String.valueOf(data.length));
            // 获得输入流，向服务器写入数据
            OutputStream outputStream = new BufferedOutputStream(httpURLConnection.getOutputStream());
            outputStream.write(data);
            outputStream.flush();                       // 重要！flush()之后才会写入

            int response = httpURLConnection.getResponseCode();     // 获得服务器响应码
            if (response == HttpURLConnection.HTTP_OK) {            //HTTP Status Code=200
                InputStream inputStream = httpURLConnection.getInputStream();
                String post_result=DealResponseResult(inputStream); // 处理服务器响应结果
                Log.i("POST_RESULT", post_result);              // 輸出回傳狀態
                return post_result;
            } else if (response == HttpURLConnection.HTTP_ACCEPTED){//201
                Log.i("POST_RESULT", String.valueOf(response));
                return String.valueOf(response);
            } else{
                Log.i("POST_RESULT", String.valueOf(response));
                return String.valueOf(response);
            }
            /*
            * 200 OK 用於請求成功 。GET 檔案成功，PUT， PATCH 更新成功
            * 201 Created 用於請求 POST 成功建立資料。
            * 204 No Content 用於請求 DELETE 成功。
            * 400 Bad Request 用於請求 API 參數不正確的情況，例如傳入的 JSON 格式錯誤。
            * 401 Unauthorized 用於表示請求的 API 缺少身份驗證資訊。
            * 403 Forbidden 用於表示該資源不允許特定用戶訪問。
            * 404 Not Found 用於表示請求一個不存在的資源。
            * */
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            httpURLConnection.disconnect();
        }

        return "";  //只有程式錯誤時才會執行到這行
    }

    /**
     * 封装请求体信息
     *
     * @param params 请求体内容
     * @param encode 编码格式
     * @return 请求体信息
     */
    public static StringBuffer GetRequestData(Map<String, String> params, String encode) {
        StringBuffer stringBuffer = new StringBuffer();            //存储封装好的请求体信息
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                stringBuffer.append(entry.getKey())
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), encode))
                        .append("&");
            }
            stringBuffer.deleteCharAt(stringBuffer.length() - 1);   // 删除最后一个"&"
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuffer;
    }

    /**
     * 处理服务器的响应结果（将输入流转换成字符串)
     *
     * @param inputStream 服务器的响应输入流
     * @return 服务器响应结果字符串
     */
    public static String DealResponseResult(InputStream inputStream) {
        String resultData = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int len = 0;
        try {
            while ((len = inputStream.read(data)) != -1) {
                byteArrayOutputStream.write(data, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        resultData = new String(byteArrayOutputStream.toByteArray());
        return resultData;
    }


    /**
     * GET取得伺服器返回的JSON訊息
     *
     * @param path    路徑
     * @param colName 欄位名稱
     * @return 字串陣列
     */
    public static String[] SendGetRequest(String path, String[] colName) {
        ArrayList<String> arrayList = new ArrayList<>();
        HttpGet httpGet = new HttpGet(path);                           //创建一个GET方式的HttpRequest对象
        //DefaultHttpClient httpClient = new DefaultHttpClient();        //创建一个默认的HTTP客户端
        HttpClient httpClient = new DefaultHttpClient();
        try {
            HttpResponse httpResponse = httpClient.execute(httpGet);               //执行GET方式的HTTP请求
            int responseCode = httpResponse.getStatusLine().getStatusCode();        //获得服务器的响应码
            if (responseCode == HttpStatus.SC_OK) {
                String jsonStr = EntityUtils.toString(httpResponse.getEntity(), "UTF-8"); //注意編碼
                for (String elem : colName) {
                    arrayList.add(new JSONObject(jsonStr).getString(elem));
                }
                Log.i("GET_RESULT", String.valueOf(responseCode));     //輸出回傳狀態
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return arrayList.toArray(new String[arrayList.size()]);//ArrayList轉成String[]
    }
}
