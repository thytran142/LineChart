package com.example.thy.linechart.Model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by thy on 17/5/17.
 */

public class Nav {
    String date;
    Float amount;
    public void setDate(String date){
        this.date = date;
    }
    public void setAmount(Float amount){
        this.amount = amount;
    }
    public String getDate(){
        return this.date;
    }
    public Float getAmount(){
        return this.amount;
    }
    public int convertDate(){
        String dateString = this.getDate();
        Calendar cal = new GregorianCalendar();
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = (Date)formatter.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
        cal.setTime(date);

        return cal.get(Calendar.DAY_OF_YEAR);
    }
}
