package me.yeon.hangmanandroid;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.time.Duration;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.ThreadLocalRandom;

import me.yeon.hangmanandroid.comm.DataComm;
import me.yeon.hangmanandroid.vo.Question;
import me.yeon.hangmanandroid.vo.Result;

/**
 * Created by yeon on 2018-03-08 008.
 */

public class PlayActivity extends AppCompatActivity {

    private int mInterval = 25; // 5 seconds by default, can be changed later
    private Calendar dueTime;
    private Handler mHandler;

    //private
    private Calendar currentTime(){
        Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        now.add(Calendar.MILLISECOND, DataComm.getInstance().timeAdv);
        return now;
    }
    private Question currentWord;
    private String myAnswer;
    private int life = 5;

    private ProgressBar pbTime, pbLife, pbInd;
    private TextView tvTime, tvLife, tvStatus, tvHeader, tvAnswer, tvDesc;
    private ImageView ivFace;
    private TableLayout layout;
    private WebView webView;

    final private String[] challenge = new String[] {
            "남들보다 빠르게 풀어보세요!",
            "이 정도는 쉬운 단어죠.",
            "속도! 속도만이 살 길이에요.",
            "대화명 자랑하고 싶지 않으세요?",
            "이런 거 잘하시죠?",
            "1등한테는 좋은 일이 일어날거에요."
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        setTitle("Hangman Game");

        DataComm.getInstance().pa = this;

        layout = findViewById(R.id.layoutPlay);
        int all = layout.getChildCount();
        for(int i = 0; i < all; i++){
            View v = layout.getChildAt(i);
            if(v instanceof TableRow) {
                int row = ((TableRow)v).getChildCount();
                for(int j = 0; j < row; j++) {
                    View bv = ((TableRow)v).getChildAt(j);
                    if (bv instanceof Button) {
                        //버튼만 모두 가져오기
                        bv.setVisibility(View.INVISIBLE);
                        bv.setEnabled(false);

                        Resources r = getResources();
                        int px = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, r.getDisplayMetrics()));
                        bv.setPadding(px,px,px,px);
                        bv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View av) { //로마자 버튼 클릭시 반응
                                if (av.getTag() instanceof String) {
                                    String b = (String) av.getTag();
                                    charButtonClicked(b,(Button)av);
                                }
                            }
                        });
                    }
                }
            }
        }

        pbTime = findViewById(R.id.pbTime);
        pbTime.setMax(20000);
        tvTime = findViewById(R.id.tvTime);
        tvAnswer = findViewById(R.id.tvAnswer);
        tvHeader = findViewById(R.id.tvHeader);
        tvStatus = findViewById(R.id.tvStatus);
        ivFace = findViewById(R.id.ivFace);
        pbLife = findViewById(R.id.pbLife);
        pbInd = findViewById(R.id.pbInd);
        tvLife = findViewById(R.id.tvLife);
        tvDesc = findViewById(R.id.tvDesc);
        webView = findViewById(R.id.wvDetail);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
        webView.getSettings().setSupportMultipleWindows(false);
        pbLife.setMax(5);
        tvLife.setText("LIFE");
        dueTime = Calendar.getInstance();
        dueTime.add(Calendar.SECOND,20);
        mHandler = new Handler();
    }

    @Override
    protected void onStart() {
        Log.d("PlayActivity","onStart()");
        super.onStart();

        //여기서 문제 유효성 검증 및 요청
        requestView();

        mStatusChecker.run();
    }

    private int status = -1; //현재 상태 (0,2: 문제 풀이, 1: 성적 확인)
    private int attempt = 0; //통신 재시도 횟수
    private int qnum = 0; //문제 번호
    public int commStatus = 0; //서버의 특수 응답 기호를 저장
    private void requestView() {
        if(commStatus == 1){
            //이상상황 1: 성적표 보기 상태에서 진입
            tvStatus.setText("다른 사람들을 기다립니다...");
            tvStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        }
        if(currentWord == null || currentWord.getReportDate().before(currentTime())){
            status = 0;
            //문제 푼 적이 없거나, 만료된 경우
            if(DataComm.getInstance().getStatus() != AsyncTask.Status.RUNNING) {
                Log.d("requestView","신규 요청");
                DataComm.getInstance().question();
                pbInd.setVisibility(View.VISIBLE);
                attempt++;
                if(commStatus == 0) {
                    tvStatus.setText(String.format(attempt > 1 ? "%d번째 통신 재시도 중..." : "서버와 통신 중...", attempt));
                    tvStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                }
            }
        } else if(currentWord.getDueDate().before(currentTime())){
            status = 1;
            dueTime = currentWord.getReportDate();
            Log.d("requestView","성적");
            tvStatus.setText("정답자 순위입니다.");
            //Toast.makeText(this, "성적표 타임", Toast.LENGTH_SHORT).show();

            tvStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        } else { //매순간 발생
            status = 2;
            //Log.d("requestView","갱신");
            readyQuestion(null);
        }
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            boolean again = false;
            try {
                again = timerJob(); //this function can change value of mInterval.
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                if(again) mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };

    private int lastTick = 0;
    private int lastMax = 0;
    private boolean timerJob() {
        int remaining = 1;
        if(status >= 0) { //문제가 시작된 다음에만 다룸
            remaining = (int) (dueTime.getTimeInMillis() - Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis() - DataComm.getInstance().timeAdv);
            if (lastMax < remaining) {
                lastMax = remaining;
                pbTime.setMax(remaining);
            }
            remaining = Math.max(0, Math.min(pbTime.getMax(), remaining));

            pbTime.setProgress(pbTime.getMax() - remaining);
            tvTime.setText(String.format("%1.3f", remaining / 1000.0));

            int ilife = Math.max(0,Math.min(pbLife.getMax(), life));
            pbLife.setProgress(pbLife.getMax()-ilife);
            tvLife.setText(String.format("%d/5", ilife));
        }
        if(lastTick == 0 || lastTick - remaining > 500 || remaining == 0){
            lastTick = remaining;
            requestView();
        }
        return true;
    }

    boolean updateQuestion = false;
    public void readyQuestion(Question q){
        if(q == null){
            q = currentWord;
        } else {
            //q에 정보를 담아서 호출하는 것은 DataComm이 유일하다.
            //이 블럭은 문제당 한 번만 실행되어야 하나, 통신이 밀리면 여러 번 실행될 수 있음
            updateQuestion = true;
            attempt = 0;
            life = 5;
            lastMax = 0;
            tvHeader.setText(String.format("%d번째 문제", qnum));

            tvDesc.setVisibility(View.INVISIBLE);
            layout.setVisibility(View.VISIBLE);
            tvAnswer.setGravity(Gravity.CENTER);
            webView.setVisibility(View.INVISIBLE);
        }
        Calendar now = currentTime();
        if(q != null){
            int len = q.getLength();
            String fullAns = q.getWord();
            if(updateQuestion) {
                if(myAnswer == null || !myAnswer.toLowerCase().equals(fullAns)) {
                    myAnswer = q.getWord().toUpperCase();
                }
                StringBuilder sb = new StringBuilder(len);
                for (int i = 0; i < len; i++) {
                    //PROtESt
                    //protest
                    if (fullAns.charAt(i) != myAnswer.charAt(i)) {
                        sb.append("_");
                    } else {
                        sb.append(String.valueOf(fullAns.charAt(i)));
                    }
                }
                tvAnswer.setText(sb.toString());
            }

            if(myAnswer.equals(fullAns)){
                //문제를 다 풀면 대문자가 전부 소문자가 된다.
                if(layout.getVisibility() == View.VISIBLE) {
                    endQuestion();
                }
            }

            if(q.equals(currentWord)){
                //문제가 바뀌지 않았다면 탈출
                return;
            }
            if(q.getBeginDate().after(now)){
                //문제 시작 전!
                return;
            }
            //여기서부터 문제당 한 번만 처음 실행된다.
            qnum++;

            dueTime = q.getDueDate();
            //dueTime.add(Calendar.MILLISECOND, DataComm.getInstance().timeAdv);
            showButtons(q.getQuestions());
            pbInd.setVisibility(View.INVISIBLE);

            tvStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            tvStatus.setText(challenge[ThreadLocalRandom.current().nextInt(0, challenge.length)]);

            currentWord = q;
            updateQuestion = false;
        }
    }

    public void endQuestion() {
        endQuestion(false);
    }

    public void endQuestion(boolean fail){
        layout.setVisibility(View.GONE);
        tvAnswer.setGravity(Gravity.CENTER | Gravity.CENTER_VERTICAL);
        tvStatus.setText("다른 사람들이 문제 푸는 중...");
        webView.loadUrl("http://m.endic.naver.com/search.nhn?query="+currentWord.getWord()+"&searchOption=");
        tvDesc.setText(Html.fromHtml(fail?"정답은 "+currentWord.getWord()+"입니다.":""));

        tvDesc.setVisibility(View.VISIBLE);
        webView.setVisibility(View.VISIBLE);
        webView.setWebChromeClient(new WebChromeClient() );
    }

    public void showReport(){

    }

    public void charButtonClicked(String b, Button btn){
        if(status == 0 || status == 2) {
            //문제 진행중에만 버튼이 눌릴 수 있다.
            String before = myAnswer;
            myAnswer = myAnswer.replace(b.toUpperCase(), b);
            if(myAnswer.equals(before)){
                //WRONG
                life--;
                if(life == 0){
                    endQuestion(true); //문제풀이 실패!
                }
            }else {
                //updateQuestion = true;
                int len = myAnswer.length();
                String fullAns = currentWord.getWord();
                StringBuilder sb = new StringBuilder(len);
                for (int i = 0; i < len; i++) {
                    //PROtESt
                    //protest
                    if (fullAns.charAt(i) != myAnswer.charAt(i)) {
                        sb.append("_");
                    } else {
                        sb.append(String.valueOf(fullAns.charAt(i)));
                    }
                }
                tvAnswer.setText(sb.toString());
            }
            btn.setEnabled(false);
        }
    }

    private void showButtons(String... strings){
        int all = layout.getChildCount();
        int required = strings.length;
        int btnidx = 0;
        for(int i = 0; i < all; i++){
            View v = layout.getChildAt(i);
            if(v instanceof TableRow) {
                int row = ((TableRow) v).getChildCount();
                for (int j = 0; j < row; j++) {
                    View bv = ((TableRow) v).getChildAt(j);
                    if (bv instanceof Button) {
                        //버튼만 모두 가져오기
                        if (btnidx < required) {
                            bv.setVisibility(View.VISIBLE);
                            ((Button) bv).setText(strings[btnidx].toUpperCase());
                            bv.setTag(strings[btnidx]);
                            bv.setEnabled(true);
                            btnidx++;
                        } else {
                            bv.setVisibility(View.INVISIBLE);
                            bv.setEnabled(false);
                        }
                    }
                }
            }
        }
    }

}
