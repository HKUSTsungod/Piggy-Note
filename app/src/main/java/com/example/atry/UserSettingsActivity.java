package com.example.atry;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.example.atry.R;
import com.example.atry.Setting.FabColorActivity;

import org.greenrobot.eventbus.EventBus;

public class UserSettingsActivity extends BaseActivity {

    private Switch nightMode;
    private Switch reverseSort;
    private LinearLayout fabColor;
    private LinearLayout fabPlanColor;
    private SharedPreferences sharedPreferences;

    private static boolean night_change;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.preference_layout);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        Intent intent = getIntent();
        if(intent.getExtras() != null) night_change = intent.getBooleanExtra("night_change", false);
        else night_change = false;

        initView();

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(isNightMode()) myToolbar.setNavigationIcon(getDrawable(R.drawable.ic_settings_white_24dp));
        else myToolbar.setNavigationIcon(getDrawable(R.drawable.ic_settings_black_24dp));
    }

    @Override
    protected void needRefresh() {
        //因为自身的刷新与其他activity不同步，所以此处留白
    }

    private void initView(){
        nightMode = findViewById(R.id.nightMode);
        reverseSort = findViewById(R.id.reverseSort);
        fabColor = findViewById(R.id.fabColor);
        fabPlanColor = findViewById(R.id.fabPlanColor);



        nightMode.setChecked(sharedPreferences.getBoolean("nightMode", false));
        reverseSort.setChecked(sharedPreferences.getBoolean("reverseSort", false));

        nightMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setNightModePref(isChecked);
                setSelfNightMode();
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                boolean temp = false;
            }
        });

        reverseSort.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences sharedPreferences1 = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                SharedPreferences.Editor editor = sharedPreferences1.edit();
                editor.putBoolean("reverseSort", isChecked);
                editor.commit();
            }
        });


        fabColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserSettingsActivity.this, FabColorActivity.class);
                intent.putExtra("mode", 1); // add note button
                startActivityForResult(intent, 1);
                overridePendingTransition(R.anim.in_righttoleft, R.anim.no);
            }
        });

        fabPlanColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserSettingsActivity.this, FabColorActivity.class);
                intent.putExtra("mode", 2); // add plan button
                startActivityForResult(intent, 1);
                overridePendingTransition(R.anim.in_righttoleft, R.anim.no);
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        int opMode = data.getExtras().getInt("opMode", -1);
        if(opMode == 1){
            int imgId = data.getExtras().getInt("id");
            SharedPreferences.Editor editor = sharedPreferences.edit(); // 开始编辑该文件
            editor.putInt("fabColor", imgId);
            editor.commit();
        }
        else if(opMode == 2){
            int imgId = data.getExtras().getInt("id");
            SharedPreferences.Editor editor = sharedPreferences.edit(); // 开始编辑该文件
            editor.putInt("fabPlanColor", imgId);
            editor.commit();
        }
    }

    private void setSelfNightMode(){
        //重新赋值并重启本activity

        super.setNightMode();
        Intent intent = new Intent(this, UserSettingsActivity.class);
        intent.putExtra("night_change", !night_change); //重启一次，正负颠倒。最终为正值时重启MainActivity。

        startActivity(new Intent(this, UserSettingsActivity.class));
        overridePendingTransition(R.anim.night_switch, R.anim.night_switch_over);
        finish();
    }

    private void setNightModePref(boolean night){
        //通过nightMode switch修改pref中的nightMode
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("nightMode", night);
        editor.commit();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
            Intent intent = new Intent();
            intent.setAction("NIGHT_SWITCH");
            sendBroadcast(intent);
            finish();
            overridePendingTransition(R.anim.in_lefttoright, R.anim.out_lefttoright);
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }


}
