package com.example.atry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class NoteDatabase extends SQLiteOpenHelper {

    public static final String TABLE_NAME = "notes";
    public static final String CONTENT = "content";
    public static final String ID = "_id";
    public static final String TIME = "time";
    public static final String MODE = "mode";


    public NoteDatabase(Context context){
        super(context, "notes", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "+ TABLE_NAME
                + "("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + CONTENT + " TEXT NOT NULL,"
                + TIME + " TEXT NOT NULL,"
                + MODE + " INTEGER DEFAULT 1)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /*for(int i = oldVersion; i < newVersion; i++) {
            switch (i) {
                case 1:
                    break;
                case 2:
                    updateMode(db);
                default:
                    break;
            }
        }*/
    }

    private void updateMode(SQLiteDatabase db){
        //version 1 -> 2, 增加 mode -- notes的分类，默认为1
        db.execSQL("alter table "+ TABLE_NAME + " add column " + MODE);
        Cursor cursor = db.rawQuery("select * from " + TABLE_NAME, null);
        while(cursor.moveToNext()){
            String content = cursor.getString(cursor.getColumnIndex(CONTENT));
            String time = cursor.getString(cursor.getColumnIndex(TIME));
            int mode = cursor.getInt(cursor.getColumnIndex(MODE));
            ContentValues values = new ContentValues();
            values.put(CONTENT, content);
            values.put(TIME, time);
            values.put(MODE, 1);//默认模式
            db.update(TABLE_NAME, values, CONTENT +"=?", new String[]{content});
        }
        Log.d("Base", "update db 1 - 2");
    }
}
