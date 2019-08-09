package com.example.atry.Setting;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.atry.R;

public class FabColorActivity extends AppCompatActivity implements View.OnClickListener {

    private Toolbar myToolbar;
    private int openMode;
    private SharedPreferences sharedPreferences;
    private ImageView q,w,e,r,t,y,u,i,o,p, curFab, defFab;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_fabcolor);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        openMode = getIntent().getIntExtra("mode", 1);

        myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //设置toolbar取代actionbar
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("opMode", 0);//无事发生
                setResult(RESULT_OK, intent);
                overridePendingTransition(R.anim.in_lefttoright, R.anim.out_lefttoright);
                finish();
            }
        });

        initImageView();

    }

    private void initImageView(){
        q = findViewById(R.id.q);
        w = findViewById(R.id.w);
        e = findViewById(R.id.e);
        r = findViewById(R.id.r);
        t = findViewById(R.id.t);
        y = findViewById(R.id.y);
        u = findViewById(R.id.u);
        i = findViewById(R.id.i);
        o = findViewById(R.id.o);
        p = findViewById(R.id.p);
        curFab = findViewById(R.id.curFab);
        if(openMode == 1) chooseCurFabColor(sharedPreferences.getInt("fabColor", -500041));
        else chooseCurFabColor(sharedPreferences.getInt("fabPlanColor", -500041));
        defFab = findViewById(R.id.defFab);
        setClick();
    }

    private void chooseCurFabColor(int fabColor){
        //根据 preference.xml中的fabColor值调整curFab颜色，从MainActivity抄过来的
        switch (fabColor){
            case -500072:
                curFab.setBackgroundResource(R.color.q);
                break;
            case -500081:
                curFab.setBackgroundResource(R.color.w);
                break;
            case -500061:
                curFab.setBackgroundResource(R.color.e);
                break;
            case -500074:
                curFab.setBackgroundResource(R.color.r);
                break;
            case -500078:
                curFab.setBackgroundResource(R.color.t);
                break;
            case -500083:
                curFab.setBackgroundResource(R.color.y);
                break;
            case -500079:
                curFab.setBackgroundResource(R.color.u);
                break;
            case -500063:
                curFab.setBackgroundResource(R.color.i);
                break;
            case -500066:
                curFab.setBackgroundResource(R.color.o);
                break;
            case -500069:
                curFab.setBackgroundResource(R.color.p);
                break;
            default:
                curFab.setBackgroundResource(R.color.fabColor1);
        }
    }


    private void setClick(){
        q.setOnClickListener(this);
        w.setOnClickListener(this);
        e.setOnClickListener(this);
        r.setOnClickListener(this);
        t.setOnClickListener(this);
        y.setOnClickListener(this);
        u.setOnClickListener(this);
        i.setOnClickListener(this);
        o.setOnClickListener(this);
        p.setOnClickListener(this);
        curFab.setOnClickListener(this);
        defFab.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.curFab){
            Toast.makeText(this, "Current color cannot be selected!", Toast.LENGTH_SHORT).show();
        }else {

            Intent intent = new Intent();
            intent.putExtra("opMode", openMode);//decide which button according to input intent
            switch (v.getId()) {
                case R.id.q:
                    intent.putExtra("id", -500072);
                    break;
                case R.id.w:
                    intent.putExtra("id", -500081);
                    break;
                case R.id.e:
                    intent.putExtra("id", -500061);
                    break;
                case R.id.r:
                    intent.putExtra("id", -500074);
                    break;
                case R.id.t:
                    intent.putExtra("id", -500078);
                    break;
                case R.id.y:
                    intent.putExtra("id", -500083);
                    break;
                case R.id.u:
                    intent.putExtra("id", -500079);
                    break;
                case R.id.i:
                    intent.putExtra("id", -500063);
                    break;
                case R.id.o:
                    intent.putExtra("id", -500066);
                    break;
                case R.id.p:
                    intent.putExtra("id", -500069);
                    break;
                case R.id.defFab:
                    intent.putExtra("id", -500041);
                    break;

            }
            setResult(RESULT_OK, intent);
            overridePendingTransition(R.anim.in_lefttoright, R.anim.out_lefttoright);
            finish();
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if( keyCode== KeyEvent.KEYCODE_HOME){
            return true;
        } else if( keyCode== KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
            Intent intent = new Intent();
            intent.putExtra("opMode", 0);
            setResult(RESULT_OK, intent);
            finish();
            overridePendingTransition(R.anim.in_lefttoright, R.anim.out_lefttoright);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
