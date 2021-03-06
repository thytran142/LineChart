package com.example.thy.linechart;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by thy on 17/5/17.
 */

public class CustomMarkerView extends MarkerView {

    private TextView tvContent;
    ArrayList<String> mXLabels;
    ArrayList<Entry> sumEntries;
    public CustomMarkerView (Context context, int layoutResource, ArrayList<String> xLabels, ArrayList<Entry> sumEntries) {
        super(context, layoutResource);
        // this markerview only displays a textview
        tvContent = (TextView) findViewById(R.id.tvContent);
        mXLabels = xLabels;
        this.sumEntries = sumEntries;
    }

    // callbacks everytime the MarkerView is redrawn, can be used to update the
// content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        String xVal = mXLabels.get(e.getXIndex());
        float total = 0;
        if(mXLabels.size()>12){
            xVal = getDate(xVal);
        }
        for(int i=0;i<sumEntries.size();i++){
            if(sumEntries.get(i).getXIndex()==e.getXIndex()){
                total += sumEntries.get(i).getVal();
            }
        }

        tvContent.setText(""+xVal+": " +e.getVal()+",total: "+total); // set the entry-value as the display text

    }
    public String getDate(String xValue){
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.DAY_OF_YEAR,Integer.parseInt(xValue)-1);
        Date date = new Date(calendar.getTimeInMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(date);
    }

    @Override
    public int getXOffset(float xpos) {
        // this will center the marker-view horizontally
        return -(getWidth() / 2);
    }

    @Override
    public int getYOffset(float ypos) {
        // this will cause the marker-view to be above the selected value
        return -getHeight();
    } }
