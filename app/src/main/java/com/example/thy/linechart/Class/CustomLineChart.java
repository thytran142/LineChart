package com.example.thy.linechart.Class;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.thy.linechart.CustomMarkerView;
import com.example.thy.linechart.Model.Nav;
import com.example.thy.linechart.Model.Portfolio;
import com.example.thy.linechart.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by thy on 18/5/17.
 */

public abstract class CustomLineChart extends AppCompatActivity implements OnChartGestureListener, OnChartValueSelectedListener, AdapterView.OnItemSelectedListener {
    protected LineChart mChart;
    protected ArrayList<Integer> colorArrayList;
    protected Spinner spinner;
    private int spinnerPosition = 1;//Default is Daily
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());
        this.mChart = setmChart();
        mChart.setOnChartValueSelectedListener(this);
        mChart.setDrawGridBackground(false);
        mChart.setDescription("Portfolio");
        mChart.setTouchEnabled(true);
        mChart.setDrawMarkerViews(true);
        //Add an empty data object
        mChart.setData(new LineData());
        mChart.invalidate();
        Legend l = mChart.getLegend();
        l.setForm(Legend.LegendForm.LINE);


        //Set Color List
        this.colorArrayList = setColorList();
        //Set spinner
        this.spinner = getSpinner();
        spinner.setOnItemSelectedListener(this);
        ArrayList<String> categories = new ArrayList<>();
        categories.add("Daily");
        categories.add("Monthly");
        categories.add("Quarterly");
        //Create adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,categories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);

    }
    protected abstract Spinner getSpinner();
    protected abstract LineChart setmChart();
    protected abstract int getLayoutResourceId();
    protected abstract ArrayList<Portfolio> getPortfolioArrayList();
    private ArrayList<Integer> setColorList(){
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.BLACK);
        colors.add(Color.GREEN);
        colors.add(Color.BLUE);
        colors.add(Color.RED);
        colors.add(Color.MAGENTA);
        return colors;
    }
    public void addDailyEntry(ArrayList<Portfolio> portfolioArrayList){
        Log.d("Size Received: ",portfolioArrayList.size()+"");
        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        ArrayList<String> xVals = new ArrayList<String>();
        ArrayList<Entry> sumEntry = new ArrayList<>();
        int xvalcount = 366;
        for (int i = 1; i <= xvalcount; i++) {
            xVals.add("" + i);
        }


        for(int i=0 ; i< portfolioArrayList.size(); i++){
            ArrayList<Entry> yVals = setYAxisValuesForDaily(portfolioArrayList.get(i));
            for(int j=0;j<yVals.size();j++){
                sumEntry.add(yVals.get(j));
            }
            LineDataSet set1;
            set1 = new LineDataSet(yVals,"Data "+(i+1));
            formatSet(set1,this.colorArrayList.get(i));
            dataSets.add(set1); // add the datasets
        }//end loop of portfolios

        LineData new_data = new LineData(xVals,dataSets);
        mChart.setData(new_data);
        CustomMarkerView customMarkerView = new CustomMarkerView(this.getApplicationContext(), R.layout.custom_marker_view_layout,xVals,sumEntry);
        mChart.setMarkerView(customMarkerView);
        mChart.getData().notifyDataChanged();
        mChart.notifyDataSetChanged();
        mChart.invalidate();
    }
    public ArrayList<Entry> setYAxisValuesForDaily(Portfolio portfolio){

        ArrayList<Entry> yVals = new ArrayList<Entry>();
        ArrayList<Nav> navList = portfolio.getNavPoints();
        for (int i = 0; i < navList.size(); i++) {
            int x_label = navList.get(i).convertDate();

            float amount = navList.get(i).getAmount();
            yVals.add(new Entry(amount, x_label));

        }

        return yVals;
    }
    public void addMonthlyEntry(ArrayList<Portfolio> portfolioArrayList){
        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        ArrayList<String> xVals = new ArrayList<String>();
        ArrayList<Entry> sumEntry = new ArrayList<>();
        int xvalcount = 12;
        for (int i = 1; i <= xvalcount; i++) {
            xVals.add(i+"");
        }

        for(int i=0 ; i< portfolioArrayList.size(); i++){
            ArrayList<Entry> yVals = setYAxisValuesForMonthly(portfolioArrayList.get(i));
            for(int j=0;j<yVals.size();j++){
                sumEntry.add(yVals.get(j));
            }
            LineDataSet set1;
            set1 = new LineDataSet(yVals,"Data "+(i+1));
            formatSet(set1,colorArrayList.get(i));
            dataSets.add(set1); // add the datasets

        }//end loop of portfolios

        LineData new_data = new LineData(xVals,dataSets);
        CustomMarkerView customMarkerView = new CustomMarkerView(this.getApplicationContext(),R.layout.custom_marker_view_layout,xVals,sumEntry);
        mChart.setMarkerView(customMarkerView);
        mChart.setData(new_data);
        mChart.getData().notifyDataChanged();
        mChart.notifyDataSetChanged();
        mChart.invalidate();

    }
    public ArrayList<Entry> setYAxisValuesForMonthly(Portfolio p){
        ArrayList<Entry> yVals = new ArrayList<Entry>();
        ArrayList<Nav> navList = p.getNavPoints();
        for(int month = 1; month<=12;month++){
            int index = maximumOfMonth(month,navList);
            float amount = 0;
            if(index>0) {
                amount = navList.get(index).getAmount();
            }
            yVals.add(new Entry(amount,month-1));
        }
        return yVals;
    }
    public void addQuarterlyEntry(ArrayList<Portfolio> portfolioArrayList){
        Log.d("Data received",portfolioArrayList.size()+"");
        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        ArrayList<String> xVals = new ArrayList<String>();
        ArrayList<Entry> sumEntry = new ArrayList<>();
        int xvalcount = 12;
        for (int i = 0; i < xvalcount; i=i+3) {
            xVals.add(i+3+"");//4 value
        }

        for(int i=0 ; i< portfolioArrayList.size(); i++){
            ArrayList<Entry> yVals = setYAxisValuesForQuarterly(portfolioArrayList.get(i));
            for(int j=0;j<yVals.size();j++){
                sumEntry.add(yVals.get(j));
            }
            LineDataSet set1;
            set1 = new LineDataSet(yVals,"Data "+(i+1));
            formatSet(set1,colorArrayList.get(i));
            dataSets.add(set1); // add the datasets
        }//end loop of portfolios

        LineData new_data = new LineData(xVals,dataSets);
        CustomMarkerView customMarkerView = new CustomMarkerView(this.getApplicationContext(),R.layout.custom_marker_view_layout,xVals,sumEntry);
        mChart.setMarkerView(customMarkerView);
        mChart.setData(new_data);
        mChart.getData().notifyDataChanged();
        mChart.notifyDataSetChanged();
        mChart.invalidate();

    }
    public ArrayList<Entry> setYAxisValuesForQuarterly(Portfolio p){
        ArrayList<Entry> yVals = new ArrayList<Entry>();
        ArrayList<Nav> navList = p.getNavPoints();
        int xValue = 0;
        for(int month = 0; month<12;month = month+3){
            int index = maximumOfMonth(month+3,navList);//3,6,9,12
            float amount = 0;
            if(index>0) {
                amount = navList.get(index).getAmount();
            }
            yVals.add(new Entry(amount,xValue));
            xValue++;
        }
        return yVals;
    }

    private LineDataSet formatSet(LineDataSet set1,int color){
        set1.setColor(color);
        set1.setCircleColor(color);
        set1.setFillAlpha(110);
        set1.setLineWidth(1f);
        set1.setCircleRadius(3f);
        set1.setDrawCircleHole(false);
        set1.setValueTextSize(9f);
        return set1;
    }
    public int maximumOfMonth(int month, ArrayList<Nav> navList){

            ArrayList<String> daysList = new ArrayList<>();
            for(int i=0;i<navList.size();i++){
                daysList.add(navList.get(i).getDate());
            }
            ArrayList<Integer> arrayDays = new ArrayList<>();
            for(int i=0; i<daysList.size();i++){
                if(getMonth(daysList.get(i)) == month) {
                    Log.d("Analyze Month",month+"");
                    Log.d("Get Month is ",daysList.get(i));
                    int day = convertDate(daysList.get(i));
                    arrayDays.add(day);
                }
                if(arrayDays.size()>32){
                    break;
                }
            }
            if(arrayDays.size()>0){
                int max = arrayDays.get(0);
                for(int i=0; i<arrayDays.size();i++){
                    if(arrayDays.get(i)>max){
                        max = arrayDays.get(i);
                    }
                }
                Log.d("Maximum is ",max+"");
                String finalDay = getDate(max+1+"");
                Log.d("Final Day",finalDay);
                int index = daysList.indexOf(finalDay);
                return index;
            }
            return -1;
        }
    private int convertDate(String dateString){
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
    public int getMonth(String dayString){
        String str[] = dayString.split("-");
        int month = Integer.parseInt(str[1]);
        return month;
    }
    public String getDate(String xValue){
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.DAY_OF_YEAR,Integer.parseInt(xValue)-1);
        Date date = new Date(calendar.getTimeInMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(date);
    }
    @Override
    public void onNothingSelected(){
        Log.i("Nothing selected","Nothing selected");
    }
    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h){
        Log.i("Entry selected: ",e.toString());
        Log.i("LOWHIGH","low: "+mChart.getLowestVisibleXIndex()
                +", high: "+mChart.getHighestVisibleXIndex());

        Log.i("MIN MAX","xmin: "+mChart.getXChartMin()
                + ", xmax: "+mChart.getXChartMax()
                +", ymin: "+mChart.getYChartMin()
                +", ymax:" +mChart.getYChartMax());

    }
    @Override
    public void onChartSingleTapped(MotionEvent me){
        Log.i("Single Tap","Chart single-tapped");

    }
    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2,float velocityX, float velocityY){
        Log.i("Fling","Chart flinged. VeloX: "+velocityX + ", VeloY: "+velocityY);
    }
    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY){
        Log.i("Scale / Zoon","Scale X: "+scaleX+", Scale Y: "+scaleY);
    }
    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY){
        Log.i("Translate / Move", "dX: "+dX+", dY: "+dY);
    }
    @Override
    public void onChartLongPressed(MotionEvent me){
        Log.i("Long Press ", "Chart Longpressed");
    }
    @Override
    public void onChartDoubleTapped(MotionEvent me){
        Log.i("Double Tap","Chart double-tapped");
    }
    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture){
        Log.i("Gesture","START,x" + me.getX() + ",y: "+me.getY());
    }
    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture){
        Log.i("Gesture","END, lastGesture: "+lastPerformedGesture);
        if(lastPerformedGesture != ChartTouchListener.ChartGesture.SINGLE_TAP)
            mChart.highlightValue(null);
    }
    @Override
    public void onItemSelected(AdapterView parent, View view, int position, long id){
        String item = parent.getItemAtPosition(position).toString();
        this.spinnerPosition = position+1;
        if(this.spinnerPosition == 1){
                addDailyEntry(getPortfolioArrayList());
                Toast.makeText(parent.getContext(),"Selected: "+item,Toast.LENGTH_LONG).show();
            }
            else if(this.spinnerPosition == 2){
                addMonthlyEntry(getPortfolioArrayList());

                Toast.makeText(parent.getContext(),"Selected: "+item,Toast.LENGTH_LONG).show();
            }else{
                addQuarterlyEntry(getPortfolioArrayList());
                Toast.makeText(parent.getContext(),"Selected: "+item,Toast.LENGTH_LONG).show();
            }
        }



}
