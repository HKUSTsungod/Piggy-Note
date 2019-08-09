package com.example.atry;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.view.View.OnClickListener;
import android.widget.Spinner;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EditActivity extends BaseActivity{

    private NoteDatabase dbHelper;
    private Context context = this;

    private EditText et;

    private String old_content = "";
    private String old_time = "";
    private int old_Tag = 1;
    private long id = 0;
    private int openMode = 0;
    private int tag = 1;
    private boolean tagChange = false;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_layout);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Spinner mySpinner = (Spinner)findViewById(R.id.spinner);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        List<String> tagList = Arrays.asList(sharedPreferences.getString("tagListString", null).split("_")); //获取tags
        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, tagList);
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mySpinner.setAdapter(myAdapter);

        mySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tag = (int)id + 1;
                tagChange = true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        if(isNightMode()) myToolbar.setNavigationIcon(getDrawable(R.drawable.ic_keyboard_arrow_left_white_24dp));
        else myToolbar.setNavigationIcon(getDrawable(R.drawable.ic_keyboard_arrow_left_black_24dp));

        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                if(openMode == 4){
                    if(et.getText().toString().length() == 0){
                        intent.putExtra("mode", -1); //nothing new happens.
                    }
                    else{
                        intent.putExtra("mode", 0); // new one note;
                        intent.putExtra("content", et.getText().toString());
                        intent.putExtra("time", dateToStr());
                        intent.putExtra("tag", tag);
                    }
                }
                else {
                    if (et.getText().toString().equals(old_content) && !tagChange)
                        intent.putExtra("mode", -1); // edit nothing
                    else {
                        intent.putExtra("mode", 1); //edit the content
                        intent.putExtra("content", et.getText().toString());
                        intent.putExtra("time", dateToStr());
                        intent.putExtra("id", id);
                        intent.putExtra("tag", tag);
                    }
                }
                setResult(RESULT_OK, intent);
                finish();//返回
                overridePendingTransition(R.anim.in_lefttoright, R.anim.out_lefttoright);
            }
        });

        et = (EditText)findViewById(R.id.et);

        Intent getIntent = getIntent();

        openMode = getIntent.getIntExtra("mode", 0);
        if (openMode == 3) {//打开已存在的note
            id = getIntent.getLongExtra("id", 0);
            old_content = getIntent.getStringExtra("content");
            old_time = getIntent.getStringExtra("time");
            old_Tag = getIntent.getIntExtra("tag", 1);
            et.setText(old_content);
            et.setSelection(old_content.length());
            mySpinner.setSelection(old_Tag - 1);
        }
    }

    @Override
    protected void needRefresh() {
        setNightMode();
        startActivity(new Intent(this, EditActivity.class));
        overridePendingTransition(R.anim.night_switch, R.anim.night_switch_over);
        finish();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if( keyCode== KeyEvent.KEYCODE_HOME){
            return true;
        } else if( keyCode== KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
            Intent intent = new Intent();
            if(openMode == 4){
                if(et.getText().toString().length() == 0){
                    intent.putExtra("mode", -1); //nothing new happens.
                }
                else{
                    intent.putExtra("mode", 0); // new one note;
                    intent.putExtra("content", et.getText().toString());
                    intent.putExtra("time", dateToStr());
                    intent.putExtra("tag", tag);
                }
            }
            else {
                if (et.getText().toString().equals(old_content)&&!tagChange)
                    intent.putExtra("mode", -1); // edit nothing
                else {
                    intent.putExtra("mode", 1); //edit the content
                    intent.putExtra("content", et.getText().toString());
                    intent.putExtra("time", dateToStr());
                    intent.putExtra("id", id);
                    intent.putExtra("tag", tag);
                }
            }
            setResult(RESULT_OK, intent);
            finish();
            overridePendingTransition(R.anim.in_lefttoright, R.anim.out_lefttoright);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final Intent intent = new Intent();
        switch (item.getItemId()){
            case R.id.delete:
                new AlertDialog.Builder(EditActivity.this)
                        .setMessage("Delete this Note ?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(openMode == 4){
                                    intent.putExtra("mode", -1); // delete the note
                                    setResult(RESULT_OK, intent);
                                }
                                else {
                                    intent.putExtra("mode", 2); // delete the note
                                    intent.putExtra("id", id);
                                    setResult(RESULT_OK, intent);
                                }
                                finish();
                            }
                        }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public String dateToStr(){
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(date);
    }

}
