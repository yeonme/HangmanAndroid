package me.yeon.hangmanandroid.comm;

/**
 * Created by yeon on 2018-03-07 007.
 */

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.CalendarView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

import me.yeon.hangmanandroid.MainActivity;
import me.yeon.hangmanandroid.PlayActivity;
import me.yeon.hangmanandroid.vo.AsyncResult;
import me.yeon.hangmanandroid.vo.Result;
import me.yeon.hangmanandroid.vo.User;

/**
 * DataComm은 HTTP 통신과 데이터 관리를 한다.
 */
public class DataComm {
    private static DataComm ourInstance = null;

    public static DataComm getInstance() {
        return ourInstance;
    }

    final String BASEURL = "http://10.10.15.168:8888/hangman/";
    final String PATH_LOGIN = "login?username=%s&key=%s";
    final String PATH_TIMECOMPARE = "timecompare?fromDate=%s&key=%s";
    final String PATH_QUESTION = "test?key=%s";
    final String PATH_FINISH = "";
    final String PATH_SHOWREPORT = "";

    final String APIKEY = "ae2f41f8e65344f196cb3d4cdbfd42bd"; //부정한 로그인을 방지한다.

    private Gson gson;
    private JsonParser parser;
    private String myid;
    public int timeAdv;

    public MainActivity ma;
    public PlayActivity pa;

    HashMap<String, AsyncResult> httpResults = new HashMap<>();

    public String getMyid() {
        return myid;
    }

    public DataComm(MainActivity ma, PlayActivity pa) {
        gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd@HH:mm:ss.SSSZ")
                .create();
        parser = new JsonParser();

        if(ma != null)
            this.ma = ma;
        if(pa != null)
            this.pa = pa;

        ourInstance = this;
    }

    public DataComm(String name, MainActivity ma, PlayActivity pa) {
        this(ma,pa);
        if (name != null) {
            login(name);
        }
    }

    public void setMa(MainActivity ma) {
        this.ma = ma;
    }

    public void setPa(PlayActivity pa) {
        this.pa = pa;
    }

    public void login(String name) {
        try {
            new HttpWorker().execute(this, "login", BASEURL + String.format(PATH_LOGIN, URLEncoder.encode(name, StandardCharsets.UTF_8.name()), APIKEY));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void loginPost(String result){
        if (result == null || result.isEmpty()) {
            Log.d("Login", "no result from server!");
            if(myid == null) {
                Toast.makeText(ma, "서버에 로그인할 수 없습니다.", Toast.LENGTH_LONG).show();
                return;
            }
        }


        if(result != null) {
            Log.d("Login", result);

            Result resultClass = gson.fromJson(result, Result.class);
            if (resultClass != null) {
                if ("ok".equals(resultClass.getStatus())) {
                    Log.d("Login", "Success: " + resultClass.getData());
                    myid = resultClass.getData().toString();
                } else {
                    Log.d("Login", resultClass.getStatus());
                }
            }
        }

        Intent intent = new Intent(ma.getBaseContext(), PlayActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        ma.startActivity(intent);

        time();
    }

    public void time(){
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd@HH:mm:ss.SSSZ");
        String time = sdf.format(c.getTime());
        try {
            time = URLEncoder.encode(time, StandardCharsets.UTF_8.name());
        }catch(Exception e){
            e.printStackTrace();
        }

        new HttpWorker().execute(this, "time", String.format(BASEURL + PATH_TIMECOMPARE, time, APIKEY));
    }

    public void timePost(String result){
        if(result == null || result.isEmpty()){
            Log.d("time", "no result from server!");
            return;
        }

        if(result != null) {
            Log.d("time", result);

            try{
                timeAdv = Integer.parseInt(result);
            }catch(Exception e){
                Log.d("time","숫자가 아니어서 실패");
            }
        }
    }

    public void question(){
        new HttpWorker().execute(this, "question", String.format(BASEURL + PATH_QUESTION, APIKEY));
    }

    public void questionPost(String result){
        if (result == null || result.isEmpty()) {
            Log.d("Question", "no result from server!");
            Toast.makeText(ma, "서버에 연결하기 어렵습니다.", Toast.LENGTH_LONG).show();
            return;
        }

        if(result != null) {
            Log.d("Question", result);

            Result resultClass = gson.fromJson(result, Result.class);
            if (resultClass != null) {
                if ("ok".equals(resultClass.getStatus())) {
                    Log.d("Question", "Success: " + resultClass.getData());
                    myid = resultClass.getData().toString();
                } else {
                    Log.d("Question", resultClass.getStatus());
                }
            }
        }

        Intent intent = new Intent(ma.getBaseContext(), PlayActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        ma.startActivity(intent);
    }

}

/**
 * AsyncTask는 Http 비동기 요청을 처리한다.
 * 작업 완료 후 Post 작업에서 받은 레퍼런스의 public 함수를 호출한다.
 */
class HttpWorker extends AsyncTask<Object, Void, AsyncResult> {
    public boolean isDone = false;
    public boolean isError = false;
    public String result;

    private DataComm datacomm = null;

    /**
     * URL String으로 가져오기
     *
     * @param urlString
     * @return
     * @throws Exception
     */
    private static String readUrl(String urlString) throws Exception {
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);

            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuffer buffer = new StringBuffer();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1) {
                buffer.append(chars, 0, read);
            }
            return buffer.toString();
        } finally {
            if (reader != null)
                reader.close();
        }
    }

    /**
     * URL String으로 가져오기
     *
     * @param urlString
     * @return
     * @throws Exception
     */
    private static String readUrl(String urlString, String postContent) throws Exception {
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
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
            while ((read = reader.read(chars)) != -1) {
                buffer.append(chars, 0, read);
            }
            return buffer.toString();
        } finally {
            if (reader != null)
                reader.close();
        }
    }


    @Override
    protected AsyncResult doInBackground(Object... objects) {
        try {
            isError = false;
            if(objects.length == 4) {
                return new AsyncResult((DataComm)objects[0], objects[1].toString(), readUrl(objects[2].toString(), objects[3].toString()));
            } else {
                return new AsyncResult((DataComm) objects[0], objects[1].toString(), readUrl(objects[2].toString()));
            }
        } catch(Exception e){
            e.printStackTrace();
            isError = true;
        }
        return null;
    }

    @Override
    protected void onPostExecute(AsyncResult result) {
        if(result == null){
            return;
        }

        this.result = result.getResult();
        isDone = true;
        isError = false;

        DataComm d = result.getDataComm();
        switch(result.getRequestUrl()){
            case "login":
                d.loginPost(result.getResult());
                break;
            case "time":
                d.timePost(result.getResult());
                break;
            case "question":
                d.questionPost(result.getResult());
                break;
        }
    }

    @Override
    protected void onCancelled() {
        isDone = true;
        isError = true;
        super.onCancelled();
    }
}
