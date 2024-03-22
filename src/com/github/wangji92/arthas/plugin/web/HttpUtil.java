package com.github.wangji92.arthas.plugin.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtil {
    public static String get(String urlStr) {
        HttpURLConnection conn = null;
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;

        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(1000);
            conn.setReadTimeout(1000);

            conn.connect();

            inputStream = conn.getInputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

            //拼接返回参数
            StringBuilder sb = new StringBuilder();
            String responseMsg = null;
            while ((responseMsg = bufferedReader.readLine()) != null){
                sb.append(responseMsg);
            }

            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(bufferedReader != null){
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(conn != null){
                conn.disconnect();
            }
        }
        return null;
    }
}
