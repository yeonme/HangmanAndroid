package me.yeon.hangmanandroid.comm;

/**
 * Created by yeon on 2018-03-07 007.
 */

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
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
import com.google.gson.JsonObject;
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
import me.yeon.hangmanandroid.vo.Question;
import me.yeon.hangmanandroid.vo.Result;
import me.yeon.hangmanandroid.vo.User;

/**
 * DataComm은 HTTP 통신과 데이터 관리를 한다.
 */
public class DataComm {
    private static DataComm ourInstance = null;
    final String BASEURL = "http://test.yeon.me/HangmanServer/";

    final String PATH_LOGIN = "login?username=%s&key=%s";

    private HttpWorker lastwork;
    public HttpWorker getLastwork(){
        return lastwork;
    }

    public AsyncTask.Status getStatus(){
        return lastwork.getStatus();
    }

    public static DataComm getInstance() {
        return ourInstance;
    }
    final String PATH_TIMECOMPARE = "timecompare?fromDate=%s&key=%s&uid=%s";
    final String PATH_QUESTION = "test?key=%s&uid=%s";
    final String PATH_FINISH = "finish?partial=%d&tries=%d&wrong=%d&ms=%d&id=%s&name=%s&device=%s&key=%s";
    final String PATH_SHOWREPORT = "score?id=%s&key=%s";

    final String APIKEY = "ae2f41f8e65344f196cb3d4cdbfd42bd"; //부정한 로그인을 방지한다.

    private Gson gson;
    private JsonParser parser;
    private String myid;
    public int timeAdv;
    public String device;

    public MainActivity ma;
    public PlayActivity pa;

    HashMap<String, AsyncResult> httpResults = new HashMap<>();

    public String getMyid() {
        return myid;
    }

    public DataComm(MainActivity ma, PlayActivity pa) {
        gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'@'HH:mm:ss.SSSZ")
                .registerTypeAdapter(Calendar.class, new DateDeserializer())
                .create();

        parser = new JsonParser();

        if(ma != null)
            this.ma = ma;
        if(pa != null)
            this.pa = pa;

        device = getDeviceName();

        ourInstance = this;
    }

    public DataComm(String name, MainActivity ma, PlayActivity pa) {
        this(ma,pa);
        if (name != null) {
            login(name);
        }
    }

    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return model;
        } else {
            return manufacturer + " " + model;
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
             lastwork = (HttpWorker) new HttpWorker().execute(this, "login", BASEURL + String.format(PATH_LOGIN, URLEncoder.encode(name, StandardCharsets.UTF_8.name()), APIKEY));
        } catch (Exception e) {
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

        lastwork = (HttpWorker)new HttpWorker().execute(this, "time", String.format(BASEURL + PATH_TIMECOMPARE, time, APIKEY, myid));
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
        lastwork = (HttpWorker)new HttpWorker().execute(this, "question", String.format(BASEURL + PATH_QUESTION, APIKEY, myid));
    }

    public void questionPost(String result){
        if (result == null || result.isEmpty()) {
            Log.d("Question", "no result from server!");
            Toast.makeText(ma, "서버에 연결하기 어렵습니다.", Toast.LENGTH_LONG).show();
            return;
        }

        if(result != null) {
            Log.d("Question", result);

            //desc / status / data ->
            JsonObject jResult = parser.parse(result).getAsJsonObject();
            if ("ok".equals(jResult.get("status").getAsString())) {
                pa.commStatus = 0;
                JsonElement jData = jResult.get("data");
                Question q = gson.fromJson(jData, Question.class);
                if (q != null) {
                    Log.d("Question", q.toString());
                    pa.readyQuestion(q);
                } else {
                    Toast.makeText(pa, "서버 응답이 있었으나, 문제가 포함되어 있지 않습니다.", Toast.LENGTH_SHORT).show();
                }
            }else if("question-overdue".equals(jResult.get("desc").getAsString())) {
                pa.commStatus = 1; //입장했더니 성적표 보기 타임
            }else{
                Toast.makeText(pa, "서버 응답이 올바르지 않아서 문제가 시작되지 않았습니다. 잠시 후 재시도합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void finish(boolean partial, int tries, int wrong, int ms, String name){
        lastwork = (HttpWorker)new HttpWorker().execute(this, "finish", String.format(BASEURL + PATH_FINISH, partial, tries, wrong,
                ms, myid, device, APIKEY));
    }

    public void finishPost(String result){
        if (result == null || result.isEmpty()) {
            Log.d("Question", "no result from server!");
            Toast.makeText(ma, "서버에 연결하기 어렵습니다.", Toast.LENGTH_LONG).show();
            return;
        }

        if(result != null) {
            Log.d("Question", result);

            //desc / status / data ->
            JsonObject jResult = parser.parse(result).getAsJsonObject();
            if ("ok".equals(jResult.get("status").getAsString())) {
                pa.commStatus = 0;
                JsonElement jData = jResult.get("data");
                Question q = gson.fromJson(jData, Question.class);
                if (q != null) {
                    Log.d("Question", q.toString());
                    pa.readyQuestion(q);
                } else {
                    Toast.makeText(pa, "서버 응답이 있었으나, 문제가 포함되어 있지 않습니다.", Toast.LENGTH_SHORT).show();
                }
            }else if("question-overdue".equals(jResult.get("desc").getAsString())) {
                pa.commStatus = 1; //입장했더니 성적표 보기 타임
            }else{
                Toast.makeText(pa, "서버 응답이 올바르지 않아서 문제가 시작되지 않았습니다. 잠시 후 재시도합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void report(){
        lastwork = (HttpWorker)new HttpWorker().execute(this, "report", String.format(BASEURL + PATH_FINISH, myid, APIKEY));
    }

    public void reportPost(String result){
        if (result == null || result.isEmpty()) {
            Log.d("Report", "no result from server!");
            Toast.makeText(ma, "서버에 연결하기 어렵습니다.", Toast.LENGTH_LONG).show();
            return;
        }

        if(result != null) {
            Log.d("Question", result);

            //desc / status / data ->
            JsonObject jResult = parser.parse(result).getAsJsonObject();
            if ("ok".equals(jResult.get("status").getAsString())) {
                pa.commStatus = 0;
                JsonElement jData = jResult.get("data");
                Question q = gson.fromJson(jData, Question.class);
                if (q != null) {
                    Log.d("Question", q.toString());
                    pa.readyQuestion(q);
                } else {
                    Toast.makeText(pa, "서버 응답이 있었으나, 문제가 포함되어 있지 않습니다.", Toast.LENGTH_SHORT).show();
                }
            }else if("question-overdue".equals(jResult.get("desc").getAsString())) {
                pa.commStatus = 1; //입장했더니 성적표 보기 타임
            }else{
                Toast.makeText(pa, "서버 응답이 올바르지 않아서 문제가 시작되지 않았습니다. 잠시 후 재시도합니다.", Toast.LENGTH_SHORT).show();
            }
        }
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
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setDoInput(true);

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
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

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
