package me.yeon.hangmanandroid.comm;

/**
 * Created by yeon on 2018-03-07 007.
 */

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * DataComm은 HTTP 통신과 데이터 관리를 한다.
 */
public class DataComm {
    private static final DataComm ourInstance = new DataComm();

    public static DataComm getInstance() {
        return ourInstance;
    }

    final String BASEURL = "http:///";
    final String PATH_LOGIN = "";
    final String PATH_QUESTION = "";
    final String PATH_FINISH = "";
    final String PATH_SHOWREPORT = "";

    private Gson gson;
    private JsonParser parser;

    private DataComm() {
        gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd@HH:mm:ss.SSSZ")
                .create();
        parser = new JsonParser();
    }

    /**
     * URL String으로 가져오기
     * @param urlString
     * @return
     * @throws Exception
     */
    private static String readUrl(String urlString) throws Exception {
        BufferedReader reader = null;
        try{
            URL url = new URL(urlString);

            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuffer buffer = new StringBuffer();
            int read;
            char[] chars = new char[1024];
            while((read = reader.read(chars)) != -1){
                buffer.append(chars, 0, read);
            }
            return buffer.toString();
        }finally{
            if(reader != null)
                reader.close();
        }
    }

    /**
     * URL String으로 가져오기
     * @param urlString
     * @return
     * @throws Exception
     */
    private static String readUrl(String urlString, String postContent) throws Exception {
        BufferedReader reader = null;
        try{
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);

            OutputStream out = connection.getOutputStream();
            out.write(postContent.getBytes(StandardCharsets.UTF_8));
            out.close();

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuffer buffer = new StringBuffer();
            int read;
            char[] chars = new char[1024];
            while((read = reader.read(chars)) != -1){
                buffer.append(chars, 0, read);
            }
            return buffer.toString();
        }finally{
            if(reader != null)
                reader.close();
        }
    }

    public void Login(String name){
        String result = null;
        try {
            result = readUrl(BASEURL + PATH_LOGIN);
        }catch(Exception e){
            e.printStackTrace();
            Log.d("Login","error!!");
        }

        if(result == null || result.isEmpty()){
            Log.d("Login","no result from server!");
        }

        JsonElement rootObejct = parser.parse(result)
                .getAsJsonObject().get("result")
                .getAsJsonObject().get("rec");

    }
}
