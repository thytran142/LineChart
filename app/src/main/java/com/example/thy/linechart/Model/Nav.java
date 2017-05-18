package com.example.thy.linechart.Model;

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
}
