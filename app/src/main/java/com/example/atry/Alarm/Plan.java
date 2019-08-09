package com.example.atry.Alarm;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Plan {
    private long id;
    private String title;
    private String content;
    private Calendar planTime;


    public Plan(String title, String content, String planTime) {
        this.title = title;
        this.content = content;
        setTime(planTime);
    }

    public Plan(){
        this.planTime = Calendar.getInstance();
    }

    public int getYear(){
        return planTime.get(Calendar.YEAR);
    }

    public int getMonth(){
        return planTime.get(Calendar.MONTH);
    }

    public int getDay() {
        return planTime.get(Calendar.DAY_OF_MONTH);
    }

    public int getHour() {
        return planTime.get(Calendar.HOUR_OF_DAY);
    }

    public int getMinute() {
        return planTime.get(Calendar.MINUTE);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Calendar getPlanTime() {
        return planTime;
    }

    public String getTime(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return simpleDateFormat.format(planTime.getTime());
    }
    public void setTime(String format){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            Date temp = simpleDateFormat.parse(format);
            Log.d("shit", ""+temp);
            planTime = Calendar.getInstance();
            planTime.setTime(temp);
        } catch (ParseException e) {

        }
    }

}
