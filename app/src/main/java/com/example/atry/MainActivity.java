package com.example.atry;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.atry.Alarm.AlarmReceiver;
import com.example.atry.Alarm.EditAlarmActivity;
import com.example.atry.Alarm.Plan;
import com.example.atry.Alarm.PlanAdapter;
import com.example.atry.Alarm.PlanDatabase;

import java.sql.ResultSet;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static android.view.View.GONE;

public class MainActivity extends BaseActivity implements OnItemClickListener, OnItemLongClickListener {

    private NoteDatabase dbHelper;
    private PlanDatabase planDbHelper;

    private FloatingActionButton fab;
    private FloatingActionButton fab_alarm;
    private ListView lv;
    private ListView lv_plan;
    private LinearLayout lv_layout;
    private LinearLayout lv_plan_layout;

    private Context context = this;
    private NoteAdapter adapter;
    private PlanAdapter planAdapter;
    private List<Note> noteList = new ArrayList<Note>();
    private List<Plan> planList = new ArrayList<Plan>();
    private TextView mEmptyView;

    private Toolbar myToolbar;

    private PopupWindow popupWindow; // 左侧弹出菜单
    private PopupWindow popupCover; // 菜单蒙版
    private LayoutInflater layoutInflater;
    private RelativeLayout main;
    private ViewGroup customView;
    private ViewGroup coverView;
    private WindowManager wm;
    private DisplayMetrics metrics;
    private TagAdapter tagAdapter;

    private TextView setting_text;
    private ImageView setting_image;
    private ListView lv_tag;
    private TextView add_tag;

    private BroadcastReceiver myReceiver;
    private Achievement achievement;

    private SharedPreferences sharedPreferences;
    private Switch content_switch;

    private AlarmManager alarmManager;

    String[] list_String = {"before one month", "before three months", "before six months", "before one year"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        achievement = new Achievement(context);
        initView();

        if (super.isNightMode())
            myToolbar.setNavigationIcon(getDrawable(R.drawable.ic_menu_white_24dp));
        else myToolbar.setNavigationIcon(getDrawable(R.drawable.ic_menu_black_24dp)); // 三道杠

        myToolbar.setNavigationOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopUpWindow();
            }
        });

    }

    private void showPopUpWindow() {
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        popupCover = new PopupWindow(coverView, width, height, false);
        popupWindow = new PopupWindow(customView, (int) (width * 0.7), (height), true);
        if (isNightMode()) popupWindow.setBackgroundDrawable(new ColorDrawable(Color.LTGRAY));
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        popupWindow.setAnimationStyle(R.style.AnimationFade);
        popupCover.setAnimationStyle(R.style.AnimationCover);


        //display the popup window
        findViewById(R.id.main_layout).post(new Runnable() {//等待main_layout加载完，再show popupwindow
            @Override
            public void run() {
                popupCover.showAtLocation(main, Gravity.NO_GRAVITY, 0, 0);
                popupWindow.showAtLocation(main, Gravity.NO_GRAVITY, 0, 0);

                setting_text = customView.findViewById(R.id.setting_settings_text);
                setting_image = customView.findViewById(R.id.setting_settings_image);
                lv_tag = customView.findViewById(R.id.lv_tag);
                add_tag = customView.findViewById(R.id.add_tag);

                add_tag.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (sharedPreferences.getString("tagListString","").split("_").length < 8) {
                            final EditText et = new EditText(context);
                            new AlertDialog.Builder(MainActivity.this)
                                    .setMessage("Enter the name of tag")
                                    .setView(et)
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            List<String> tagList = Arrays.asList(sharedPreferences.getString("tagListString", null).split("_")); //获取tags

                                            String name = et.getText().toString();
                                            if (!tagList.contains(name)) {
                                                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                                                String oldTagListString = sharedPreferences.getString("tagListString", null);
                                                String newTagListString = oldTagListString + "_" + name;
                                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                                editor.putString("tagListString", newTagListString);
                                                editor.commit();
                                                refreshTagList();
                                            }
                                            else Toast.makeText(context, "Repeated tag!", Toast.LENGTH_SHORT).show();
                                        }
                                    }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).create().show();
                        }
                        else{
                            Toast.makeText(context, "自定义的标签够多了！", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                //final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                List<String> tagList = Arrays.asList(sharedPreferences.getString("tagListString", null).split("_")); //获取tags
                tagAdapter = new TagAdapter(context, tagList, numOfTagNotes(tagList));
                lv_tag.setAdapter(tagAdapter);

                lv_tag.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        List<String> tagList = Arrays.asList(sharedPreferences.getString("tagListString", null).split("_")); //获取tags
                        int tag = position + 1;
                        List<Note> temp = new ArrayList<>();
                        for (int i = 0; i < noteList.size(); i++) {
                            if (noteList.get(i).getTag() == tag) {
                                Note note = noteList.get(i);
                                temp.add(note);
                            }
                        }
                        NoteAdapter tempAdapter = new NoteAdapter(context, temp);
                        lv.setAdapter(tempAdapter);
                        myToolbar.setTitle(tagList.get(position));
                        popupWindow.dismiss();
                        Log.d(TAG, position + "");
                    }
                });

                lv_tag.setOnItemLongClickListener(new OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                        if (position > 4) {
                            resetTagsX(parent);
                            float length = getResources().getDimensionPixelSize(R.dimen.distance);
                            TextView blank = view.findViewById(R.id.blank_tag);
                            blank.animate().translationX(length).setDuration(300).start();
                            TextView text = view.findViewById(R.id.text_tag);
                            text.animate().translationX(length).setDuration(300).start();
                            ImageView del = view.findViewById(R.id.delete_tag);
                            del.setVisibility(View.VISIBLE);
                            del.animate().translationX(length).setDuration(300).start();

                            del.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    new AlertDialog.Builder(MainActivity.this)
                                            .setMessage("All related notes will be tagged as \"no tag\" !")
                                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    int tag = position + 1;
                                                    for (int i = 0; i < noteList.size(); i++) {
                                                        //被删除tag的对应notes tag = 1
                                                        Note temp = noteList.get(i);
                                                        if (temp.getTag() == tag) {
                                                            temp.setTag(1);
                                                            CRUD op = new CRUD(context);
                                                            op.open();
                                                            op.updateNote(temp);
                                                            op.close();
                                                        }
                                                    }
                                                    List<String> tagList = Arrays.asList(sharedPreferences.getString("tagListString", null).split("_")); //获取tags
                                                    if(tag + 1 < tagList.size()) {
                                                        for (int j = tag + 1; j < tagList.size() + 1; j++) {
                                                            //大于被删除的tag的所有tag减一
                                                            for (int i = 0; i < noteList.size(); i++) {
                                                                Note temp = noteList.get(i);
                                                                if (temp.getTag() == j) {
                                                                    temp.setTag(j - 1);
                                                                    CRUD op = new CRUD(context);
                                                                    op.open();
                                                                    op.updateNote(temp);
                                                                    op.close();
                                                                }
                                                            }
                                                        }
                                                    }

                                                    //edit the preference
                                                    List<String> newTagList = new ArrayList<>();
                                                    newTagList.addAll(tagList);
                                                    newTagList.remove(position);
                                                    String newTagListString = TextUtils.join("_", newTagList);
                                                    Log.d(TAG, "onClick: " + newTagListString);
                                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                                    editor.putString("tagListString", newTagListString);
                                                    editor.commit();

                                                    refreshTagList();
                                                }
                                            }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    }).create().show();
                                }
                            });

                            return true;
                        }
                        return false;
                    }
                });


                setting_text.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(MainActivity.this, UserSettingsActivity.class));
                        overridePendingTransition(R.anim.in_lefttoright, R.anim.no);

                    }
                });
                setting_image.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(MainActivity.this, UserSettingsActivity.class));
                        overridePendingTransition(R.anim.in_lefttoright, R.anim.no);

                    }
                });


                coverView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        popupWindow.dismiss();
                        return true;
                    }
                });

                popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        popupCover.dismiss();
                    }
                });
            }
        });

    }

    private void refreshTagList() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        List<String> tagList = Arrays.asList(sharedPreferences.getString("tagListString", null).split("_")); //获取tags
        tagAdapter = new TagAdapter(context, tagList, numOfTagNotes(tagList));
        lv_tag.setAdapter(tagAdapter);
        tagAdapter.notifyDataSetChanged();
    }

    private void resetTagsX(AdapterView<?> parent) {
        for (int i = 5; i < parent.getCount(); i++) {
            View view = parent.getChildAt(i);
            if (view.findViewById(R.id.delete_tag).getVisibility() == View.VISIBLE) {
                float length = 0;
                TextView blank = view.findViewById(R.id.blank_tag);
                blank.animate().translationX(length).setDuration(300).start();
                TextView text = view.findViewById(R.id.text_tag);
                text.animate().translationX(length).setDuration(300).start();
                ImageView del = view.findViewById(R.id.delete_tag);
                del.setVisibility(GONE);
                del.animate().translationX(length).setDuration(300).start();
            }
        }
    }

    @Override
    protected void needRefresh() {
        setNightMode();
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("opMode", 10);
        startActivity(intent);
        overridePendingTransition(R.anim.night_switch, R.anim.night_switch_over);
        if (popupWindow.isShowing()) popupWindow.dismiss();
        finish();
    }

    public void initView() {

        initPrefs();

        fab = findViewById(R.id.fab);
        fab_alarm = findViewById(R.id.fab_alarm);
        lv = findViewById(R.id.lv);
        lv_plan = findViewById(R.id.lv_plan);
        lv_layout = findViewById(R.id.lv_layout);
        lv_plan_layout = findViewById(R.id.lv_plan_layout);
        content_switch = findViewById(R.id.content_switch);
        myToolbar = findViewById(R.id.my_toolbar);
        refreshLvVisibility();

        mEmptyView = findViewById(R.id.emptyView); // search page

        adapter = new NoteAdapter(getApplicationContext(), noteList);
        planAdapter = new PlanAdapter(getApplicationContext(), planList);

        refreshListView();
        lv.setAdapter(adapter);
        lv.setEmptyView(mEmptyView); // connect empty textview with listview
        lv_plan.setAdapter(planAdapter);

        boolean temp = sharedPreferences.getBoolean("content_switch", false);
        content_switch.setChecked(temp);//判断是看note还是plan
        content_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("content_switch" ,isChecked);
                editor.commit();
                refreshLvVisibility();
            }
        });

        fab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                intent.putExtra("mode", 4);     // MODE of 'new note'
                startActivityForResult(intent, 1);      //collect data from edit
                overridePendingTransition(R.anim.in_righttoleft, R.anim.out_righttoleft);

            }
        });
        fab_alarm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditAlarmActivity.class);
                intent.putExtra("mode", 2); // MODE of 'new plan'
                startActivityForResult(intent, 1);
                overridePendingTransition(R.anim.in_righttoleft, R.anim.no);
            }
        });

        lv.setOnItemClickListener(this);
        lv_plan.setOnItemClickListener(this);

        lv.setOnItemLongClickListener(this);
        lv_plan.setOnItemLongClickListener(this);


        setSupportActionBar(myToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //设置toolbar取代actionbar
        initPopupView();
    }

    private void refreshLvVisibility() {
        //决定应该现实notes还是plans
        boolean temp = sharedPreferences.getBoolean("content_switch", false);
        if(temp){
            lv_layout.setVisibility(GONE);
            lv_plan_layout.setVisibility(View.VISIBLE);
        }
        else{
            lv_layout.setVisibility(View.VISIBLE);
            lv_plan_layout.setVisibility(GONE);


        }
        if(temp) myToolbar.setTitle("All Plans");
        else myToolbar.setTitle("All Notes");
    }

    public void initPopupView() {
        //instantiate the popup.xml layout file
        layoutInflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        customView = (ViewGroup) layoutInflater.inflate(R.layout.setting_layout, null);
        coverView = (ViewGroup) layoutInflater.inflate(R.layout.setting_cover, null);

        main = findViewById(R.id.main_layout);
        //instantiate popup window
        wm = getWindowManager();
        metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);

    }

    private void initPrefs() {
        //initialize all useful SharedPreferences for the first time the app runs

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (!sharedPreferences.contains("nightMode")) {
            editor.putBoolean("nightMode", false);
            editor.commit();
        }
        if (!sharedPreferences.contains("reverseSort")) {
            editor.putBoolean("reverseSort", false);
            editor.commit();
        }
        if (!sharedPreferences.contains("fabColor")) {
            editor.putInt("fabColor", -500041);
            editor.commit();
        }
        if (!sharedPreferences.contains("tagListString")) {
            String s = "no tag_life_study_work_play";
            editor.putString("tagListString", s);
            editor.commit();
        }
        if(!sharedPreferences.contains("content_switch")) {
            editor.putBoolean("content_switch", false);
            editor.commit();
        }
        if(!sharedPreferences.contains("fabPlanColor")){
            editor.putInt("fabPlanColor", -500041);
            editor.commit();
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        //search setting
        MenuItem mSearch = menu.findItem(R.id.action_search);
        SearchView mSearchView = (SearchView) mSearch.getActionView();

        mSearchView.setQueryHint("Search");
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(content_switch.isChecked()) planAdapter.getFilter().filter(newText);
                else adapter.getFilter().filter(newText);
                return false;
            }
        });
        final int mode = (content_switch.isChecked()? 2 : 1);
        final String itemName = (mode == 1 ? "notes" : "plans");
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                final View view = findViewById(R.id.menu_clear);

                if (view != null) {
                    view.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle("Delete all "+ itemName);
                            builder.setIcon(R.drawable.ic_error_outline_black_24dp);
                            builder.setItems(list_String, new DialogInterface.OnClickListener() {//列表对话框；
                                @Override
                                public void onClick(DialogInterface dialog, final int which) {//根据这里which值，即可以指定是点击哪一个Item；
                                    new AlertDialog.Builder(MainActivity.this)
                                            .setMessage("Do you want to delete all " + itemName + " " + list_String[which] + "? ")
                                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int a) {
                                                    Log.d(TAG, "onClick: " + which);
                                                    removeSelectItems(which, mode);
                                                    refreshListView();
                                                }

                                                //根据模式与时长删除对顶的计划s/笔记s
                                                private void removeSelectItems(int which, int mode) {
                                                    int monthNum = 0;
                                                    switch (which){
                                                        case 0:
                                                            monthNum = 1;
                                                            break;
                                                        case 1:
                                                            monthNum = 3;
                                                            break;
                                                        case 2:
                                                            monthNum = 6;
                                                            break;
                                                        case 3:
                                                            monthNum = 12;
                                                            break;
                                                    }
                                                    Calendar rightNow = Calendar.getInstance();
                                                    rightNow.add(Calendar.MONTH,-monthNum);//日期加3个月
                                                    Date selectDate = rightNow.getTime();
                                                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                    String selectDateStr = simpleDateFormat.format(selectDate);
                                                    Log.d(TAG, "removeSelectItems: " + selectDateStr);
                                                    switch(mode){
                                                        case 1: //notes
                                                            dbHelper = new NoteDatabase(context);
                                                            SQLiteDatabase db = dbHelper.getWritableDatabase();
                                                            Cursor cursor = db.rawQuery("select * from notes" ,null);
                                                            while(cursor.moveToNext()){
                                                                if (cursor.getString(cursor.getColumnIndex(NoteDatabase.TIME)).compareTo(selectDateStr) < 0){
                                                                    db.delete("notes", NoteDatabase.ID + "=?", new String[]{Long.toString(cursor.getLong(cursor.getColumnIndex(NoteDatabase.ID)))});
                                                                }
                                                            }
                                                            db.execSQL("update sqlite_sequence set seq=0 where name='notes'"); //reset id to 1
                                                            refreshListView();
                                                            break;
                                                        case 2: //plans
                                                            planDbHelper = new PlanDatabase(context);
                                                            SQLiteDatabase pdb = planDbHelper.getWritableDatabase();
                                                            Cursor pcursor = pdb.rawQuery("select * from plans" ,null);
                                                            while(pcursor.moveToNext()){
                                                                if (pcursor.getString(pcursor.getColumnIndex(PlanDatabase.TIME)).compareTo(selectDateStr) < 0){
                                                                    pdb.delete("plans", PlanDatabase.ID + "=?", new String[]{Long.toString(pcursor.getLong(pcursor.getColumnIndex(PlanDatabase.ID)))});
                                                                }
                                                            }
                                                            pdb.execSQL("update sqlite_sequence set seq=0 where name='plans'");
                                                            refreshListView();
                                                            break;
                                                    }
                                                }
                                            }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    }).create().show();
                                }
                            });

                            AlertDialog dialog = builder.create();
                            dialog.show();
                            return true;
                        }
                    });
                }
            }
        });


        return super.onCreateOptionsMenu(menu);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_clear:
                if(!content_switch.isChecked()) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage("Delete All Notes ?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dbHelper = new NoteDatabase(context);
                                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                                    db.delete("notes", null, null);//delete data in table NOTES
                                    db.execSQL("update sqlite_sequence set seq=0 where name='notes'"); //reset id to 1
                                    refreshListView();
                                }
                            }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();
                }
                else{
                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage("Delete All Plans ?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    planDbHelper = new PlanDatabase(context);
                                    SQLiteDatabase db = planDbHelper.getWritableDatabase();
                                    db.delete("plans", null, null);//delete data in table NOTES
                                    db.execSQL("update sqlite_sequence set seq=0 where name='plans'"); //reset id to 1
                                    refreshListView();
                                }
                            }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();
                }

                break;
            case R.id.refresh:
                myToolbar.setTitle("All Notes");
                lv.setAdapter(adapter);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //刷新listview
    public void refreshListView() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int fabColor = sharedPreferences.getInt("fabColor", -500041);
        chooseFabColor(fabColor);
        int fabPlanColor = sharedPreferences.getInt("fabPlanColor", -500041);
        chooseFabPlanColor(fabPlanColor);
        //initialize CRUD
        CRUD op = new CRUD(context);
        op.open();

        // set adapter
        if (noteList.size() > 0) noteList.clear();
        noteList.addAll(op.getAllNotes());
        if (sharedPreferences.getBoolean("reverseSort", false)) sortNotes(noteList, 2);
        else sortNotes(noteList, 1);
        op.close();
        adapter.notifyDataSetChanged();

        com.example.atry.Alarm.CRUD op1 = new com.example.atry.Alarm.CRUD(context);
        op1.open();
        if(planList.size() > 0) {
            cancelAlarms(planList);//删除所有闹钟
            planList.clear();
        }
        planList.addAll(op1.getAllPlans());
        startAlarms(planList);//添加所有新闹钟
        if (sharedPreferences.getBoolean("reverseSort", false)) sortPlans(planList, 2);
        else sortPlans(planList, 1);
        op1.close();
        planAdapter.notifyDataSetChanged();

        achievement.listen();

    }

    //根据 preference.xml中的fabColor值调整fab颜色
    private void chooseFabColor(int fabColor) {

        switch (fabColor) {
            case -500072:
                fab.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.q)));
                break;
            case -500081:
                fab.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.w)));
                break;
            case -500061:
                fab.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.e)));
                break;
            case -500074:
                fab.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.r)));
                break;
            case -500078:
                fab.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.t)));
                break;
            case -500083:
                fab.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.y)));
                break;
            case -500079:
                fab.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.u)));
                break;
            case -500063:
                fab.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.i)));
                break;
            case -500066:
                fab.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.o)));
                break;
            case -500069:
                fab.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.p)));
                break;
            default:
                fab.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.fabColor1)));
        }
    }

    //根据 preference.xml中的fab_alarmColor值调整fab_alarm颜色
    private void chooseFabPlanColor(int fabColor) {

        switch (fabColor) {
            case -500072:
                fab_alarm.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.q)));
                break;
            case -500081:
                fab_alarm.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.w)));
                break;
            case -500061:
                fab_alarm.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.e)));
                break;
            case -500074:
                fab_alarm.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.r)));
                break;
            case -500078:
                fab_alarm.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.t)));
                break;
            case -500083:
                fab_alarm.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.y)));
                break;
            case -500079:
                fab_alarm.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.u)));
                break;
            case -500063:
                fab_alarm.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.i)));
                break;
            case -500066:
                fab_alarm.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.o)));
                break;
            case -500069:
                fab_alarm.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.p)));
                break;
            default:
                fab_alarm.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.fabColor1)));
        }
    }

    //click item in listView
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.lv:
                Note curNote = (Note) parent.getItemAtPosition(position);
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                intent.putExtra("content", curNote.getContent());
                intent.putExtra("id", curNote.getId());
                intent.putExtra("time", curNote.getTime());
                intent.putExtra("mode", 3);     // MODE of 'click to edit'
                intent.putExtra("tag", curNote.getTag());
                startActivityForResult(intent, 1);      //collect data from edit
                overridePendingTransition(R.anim.in_righttoleft, R.anim.out_righttoleft);
                break;
            case R.id.lv_plan:
                Plan curPlan = (Plan) parent.getItemAtPosition(position);
                Intent intent1 = new Intent(MainActivity.this, EditAlarmActivity.class);
                intent1.putExtra("title", curPlan.getTitle());
                intent1.putExtra("content", curPlan.getContent());
                intent1.putExtra("time", curPlan.getTime());
                intent1.putExtra("mode", 1);
                intent1.putExtra("id", curPlan.getId());
                startActivityForResult(intent1, 1);
                break;
        }
    }

    // react to startActivityForResult and collect data
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        int returnMode;
        long note_Id;
        returnMode = data.getExtras().getInt("mode", -1);
        note_Id = data.getExtras().getLong("id", 0);
        if (returnMode == 1) {  //update current note

            String content = data.getExtras().getString("content");
            String time = data.getExtras().getString("time");
            int tag = data.getExtras().getInt("tag", 1);
            Note newNote = new Note(content, time, tag);
            newNote.setId(note_Id);
            CRUD op = new CRUD(context);
            op.open();
            op.updateNote(newNote);
            achievement.editNote(op.getNote(note_Id).getContent(), content);
            op.close();

        } else if (returnMode == 2) {  //delete current note
            Note curNote = new Note();
            curNote.setId(note_Id);
            CRUD op = new CRUD(context);
            op.open();
            op.removeNote(curNote);
            op.close();
            achievement.deleteNote();
        } else if (returnMode == 0) {  // create new note
            String content = data.getExtras().getString("content");
            String time = data.getExtras().getString("time");
            int tag = data.getExtras().getInt("tag", 1);
            Note newNote = new Note(content, time, tag);
            CRUD op = new CRUD(context);
            op.open();
            op.addNote(newNote);
            op.close();
            achievement.addNote(content);
        } else if (returnMode == 11){//edit plan
            String title = data.getExtras().getString("title", null);
            String content = data.getExtras().getString("content", null);
            String time = data.getExtras().getString("time", null);
            Log.d(TAG, time);
            Plan plan = new Plan(title, content, time);
            plan.setId(note_Id);
            com.example.atry.Alarm.CRUD op = new com.example.atry.Alarm.CRUD(context);
            op.open();
            op.updatePlan(plan);
            op.close();
        }else if (returnMode == 12){//delete existing plan
            Plan plan = new Plan();
            plan.setId(note_Id);
            com.example.atry.Alarm.CRUD op = new com.example.atry.Alarm.CRUD(context);
            op.open();
            op.removePlan(plan);
            op.close();
        }else if (returnMode == 10){//create new plan
            String title = data.getExtras().getString("title", null);
            String content = data.getExtras().getString("content", null);
            String time = data.getExtras().getString("time", null);
            Plan newPlan = new Plan(title, content, time);
            com.example.atry.Alarm.CRUD op = new com.example.atry.Alarm.CRUD(context);
            op.open();
            op.addPlan(newPlan);
            Log.d(TAG, "onActivityResult: "+ time);
            op.close();
        }else{}
        refreshListView();
    }

    //longclick item in listView
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()){
            case R.id.lv:
                final Note note = noteList.get(position);
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("Do you want to delete this note ?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                CRUD op = new CRUD(context);
                                op.open();
                                op.removeNote(note);
                                op.close();
                                refreshListView();
                            }
                        }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
                break;
            case R.id.lv_plan:
                final Plan plan = planList.get(position);
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("Do you want to delete this plan ?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                com.example.atry.Alarm.CRUD op = new com.example.atry.Alarm.CRUD(context);
                                op.open();
                                op.removePlan(plan);
                                op.close();
                                refreshListView();
                            }
                        }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
                break;
        }
        return true;
    }

    //按模式时间排序笔记
    public void sortNotes(List<Note> noteList, final int mode) {
        Collections.sort(noteList, new Comparator<Note>() {
            @Override
            public int compare(Note o1, Note o2) {
                try {
                    if (mode == 1) {
                        Log.d(TAG, "sortnotes 1");
                        return npLong(dateStrToSec(o2.getTime()) - dateStrToSec(o1.getTime()));
                    }
                    else if (mode == 2) {//reverseSort
                        Log.d(TAG, "sortnotes 2");
                        return npLong(dateStrToSec(o1.getTime()) - dateStrToSec(o2.getTime()));
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return 1;
            }
        });
    }

    //按模式时间排序计划
    public void sortPlans(List<Plan> planList, final int mode){
        Collections.sort(planList, new Comparator<Plan>() {
            @Override
            public int compare(Plan o1, Plan o2) {
                try {
                    if (mode == 1)
                        return npLong(calStrToSec(o1.getTime()) - calStrToSec(o2.getTime()));
                    else if (mode == 2) //reverseSort
                        return npLong(calStrToSec(o2.getTime()) - calStrToSec(o1.getTime()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return 1;
            }
        });
    }

    //格式转换 string -> milliseconds
    public long dateStrToSec(String date) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long secTime = format.parse(date).getTime();
        return secTime;
    }

    //统计不同标签的笔记数
    public List<Integer> numOfTagNotes(List<String> noteStringList){
        Integer[] numbers = new Integer[noteStringList.size()];
        for(int i = 0; i < numbers.length; i++) numbers[i] = 0;
        for(int i = 0; i < noteList.size(); i++){
            numbers[noteList.get(i).getTag() - 1] ++;
        }
        return Arrays.asList(numbers);
    }

    //turn long into 1, 0, -1
    public int npLong(Long l) {
        if (l > 0) return 1;
        else if (l < 0) return -1;
        else return 0;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    //设置提醒
    private void startAlarm(Plan p) {
        Calendar c = p.getPlanTime();
        if(!c.before(Calendar.getInstance())) {
            Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
            intent.putExtra("title", p.getTitle());
            intent.putExtra("content", p.getContent());
            intent.putExtra("id", (int)p.getId());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (int) p.getId(), intent, 0);

            alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
        }
    }

    //设置很多提醒
    private void startAlarms(List<Plan> plans){
        for(int i = 0; i < plans.size(); i++) startAlarm(plans.get(i));
    }

    //取消提醒
    private void cancelAlarm(Plan p) {
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (int)p.getId(), intent, 0);
        alarmManager.cancel(pendingIntent);
    }

    //取消很多提醒
    private void cancelAlarms(List<Plan> plans){
        for(int i = 0; i < plans.size(); i++) cancelAlarm(plans.get(i));
    }

    @Override
    public void onResume(){
        super.onResume();
        Intent intent = getIntent();
        if(intent!=null && intent.getIntExtra("mode", 0) == 1){
            content_switch.setChecked(true);
            refreshLvVisibility();
        }
    }

    //achievement system
    public class Achievement {
        private SharedPreferences sharedPreferences;
        private boolean noteNumberState = false;
        private boolean wordNumberState = false;
        private boolean remainNumberState = false;

        private int noteNumber;
        private int wordNumber;
        private int remainNumber;
        private int maxRemainNumber;

        public Achievement(Context context) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            initPref();
            getPref();
        }

        private void getPref() {
            noteNumber = sharedPreferences.getInt("noteNumber", 0);
            wordNumber = sharedPreferences.getInt("wordNumber", 0);
            remainNumber = sharedPreferences.getInt("remainNumber", 0);
            maxRemainNumber = sharedPreferences.getInt("maxRemainNumber", 0);
        }

        private void initPref() {

            if (!sharedPreferences.contains("noteNumber")) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("noteNumber", 0);
                editor.commit();
                if (!sharedPreferences.contains("wordNumber")) {
                    editor.putInt("wordNumber", 0);
                    editor.commit();
                    if (!sharedPreferences.contains("remainNumber")) {
                        editor.putInt("remainNumber", 0);
                        editor.commit();
                        if (!sharedPreferences.contains("maxRemainNumber")) {
                            editor.putInt("maxRemainNumber", 0);
                            editor.commit();
                        }
                    }
                }
            }
        }

        //加入已写好的笔记
        private void addCurrent(List<Note> list) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("noteNumber", list.size());
            editor.putInt("remainNumber", list.size());
            int wordCount = 0;
            for (int i = 0; i < list.size(); i++) {
                wordCount += list.get(i).getContent().length();
            }
            editor.putInt("wordNumber", wordCount);
            editor.commit();
        }

        //添加笔记
        public void addNote(String content) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            noteNumber++;
            editor.putInt("noteNumber", noteNumber);
            noteNumberState = false;

            wordNumber += content.length();
            editor.putInt("wordNumber", wordNumber);
            wordNumberState = false;

            remainNumber++;
            editor.putInt("remainNumber", remainNumber);
            if (maxRemainNumber < remainNumber) {
                editor.putInt("maxRemainNumber", remainNumber);
                remainNumberState = false;
            }

            editor.commit();
        }

        //删除笔记
        public void deleteNote() {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            remainNumber--;
            editor.putInt("remainNumber", remainNumber);
            editor.commit();
        }

        //编辑笔记，修改字数
        public void editNote(String oldContent, String newContent) {
            if (newContent.length() > oldContent.length()) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                wordNumber += (newContent.length() - oldContent.length());
                editor.putInt("wordNumber", wordNumber);
                wordNumberState = false;
                editor.commit();
            }
        }

        //笔记数成就
        public void noteNumberAchievement(int num) {
            if (!noteNumberState) {
                switch (num) {
                    case 1:
                        announcement("This is your first step!", 1, num);
                        break;
                    case 10:
                        announcement("Keep going, and don't give up", 1, num);
                        break;
                    case 100:
                        announcement("This has been a long way...", 1, num);
                        break;
                    case 1000:
                        announcement("Final achievement! Well Done!", 1, num);
                        break;
                }
            }
        }

        //字数成就
        public void wordNumberAchievement(int num) {
            if (!wordNumberState) {
                if (num > 20000) announcement("Final Achievement! Congrats!", 2, 20000);
                else if (num > 5000)
                    announcement("A long story...", 2, 5000);
                else if (num > 1000)
                    announcement("Double essays!", 2, 1000);
                else if (num > 500)
                    announcement("You have written an essay!", 2, 500);
                else if (num > 100)
                    announcement("Take it slow to create more possibilities!", 2, 100);
            }
        }

        //剩余篇数成就
        public void remainNumberAchievement(int num) {
            if (!remainNumberState) {
                if (num > 800)
                    announcement("Damn, whatever...", 3, 800);
                else if (num > 300)
                    announcement("FBI warning! Clear your notes!", 3, 300);
                else if (num > 100)
                    announcement("Clear your notes in time!", 3, 100);
                else if (num > 50)
                    announcement("Remember to clear notes often!", 3, 50);
            }
        }

        //对话框
        public void announcement(String message, int mode, int num) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(annoucementTitle(mode, num))
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
            setState(mode, true);
        }

        //对话框标题
        public String annoucementTitle(int mode, int num) {
            switch (mode) {
                case 1:
                    return "You have written " + num + " notes! ";
                case 2:
                    return "You have written " + num + " words! ";
                case 3:
                    return "You have " + num + " notes remaining visible!";
            }
            return null;
        }

        public void setState(int mode, boolean state) {
            //set corresponding state to true in case repetition of annoucement
            switch (mode) {
                case 1:
                    noteNumberState = state;
                    break;
                case 2:
                    wordNumberState = state;
                    break;
                case 3:
                    remainNumberState = state;
                    break;
            }
        }

        //监听
        public void listen() {
            noteNumberAchievement(noteNumber);
            wordNumberAchievement(wordNumber);
            remainNumberAchievement(remainNumber);
        }

        //重置成就
        public void resetAll() {
            //reset all prefs and state
            setState(1, false);
            setState(2, false);
            setState(3, false);
            noteNumber = 0;
            wordNumber = 0;
            remainNumber = 0;
            maxRemainNumber = 0;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("noteNumber", noteNumber);
            editor.putInt("wordNumber", wordNumber);
            editor.putInt("remainNumber", remainNumber);
            editor.putInt("maxRemainNumber", maxRemainNumber);
            editor.commit();
        }

    }

}
