package me.yeon.hangmanandroid;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.concurrent.ThreadLocalRandom;

import me.yeon.hangmanandroid.comm.DataComm;

public class MainActivity extends AppCompatActivity {

    final String[] DEFNAMES = new String[]{ "카네포라", "아라비카", "리베리카", "샤리에", "아라부스타" };
    DataComm comm = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Hangman Server Login");
        ImageButton btnGo = findViewById(R.id.btnGo);
        final EditText edtName = findViewById(R.id.tbxName);

        SharedPreferences sharedPref = MainActivity.this.getPreferences(Context.MODE_PRIVATE);
        if(sharedPref.contains("lastName")) {
            String lastname = null;
            lastname = sharedPref.getString("lastName", null);
            edtName.setText(lastname);
        }
        
        btnGo.setOnClickListener(new ImageButton.OnClickListener(){
            @Override
            public void onClick(View v) {
                EditText edtName = findViewById(R.id.tbxName);
                String name = String.valueOf(edtName.getText());
                SharedPreferences sharedPref = MainActivity.this.getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                if(name == null || name.isEmpty()){
                    editor.remove("lastName");
                    Toast.makeText(MainActivity.this, "입력한 이름이 없으면 무작위 이름으로 진행됩니다.", Toast.LENGTH_SHORT).show();
                    name = String.valueOf(edtName.getHint());
                } else {
                    //직접 입력한 값은 자동으로 저장한다.
                    editor.putString("lastName", name);
                }
                editor.commit();

                if(comm == null)
                    comm = new DataComm(name, MainActivity.this, null);
                else {
                    if(comm.getMyid() == null) { //생성되었으나 아이디를 받지 못하면, 다시 로그인해야 한다.
                        comm.login(name);
                    }else { //로그인 이후 과정 진행
                        comm.loginPost(null);
                    }
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        int iname = ThreadLocalRandom.current().nextInt(0, DEFNAMES.length);
        EditText edtName = findViewById(R.id.tbxName);
        edtName.setHint(String.valueOf(DEFNAMES[iname]));
    }
}