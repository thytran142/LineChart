package com.example.thy.linechart;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.thy.linechart.Model.HttpHandler;
import com.example.thy.linechart.Model.Nav;
import com.example.thy.linechart.Model.Portfolio;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static android.graphics.Color.BLACK;

public class MainActivity extends AppCompatActivity implements OnChartGestureListener, OnChartValueSelectedListener, AdapterView.OnItemSelectedListener {
    private LineChart mChart;
    private int spinnerPosition = 1;//Default is Daily
    ArrayList<Portfolio> portfolioArrayList = new ArrayList<>();
    private static String url = "https://api.myjson.com/bins/js8ul";
    private ProgressDialog pDialog;

     @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);


        mChart = (LineChart) findViewById(R.id.linechart);
        mChart.setOnChartValueSelectedListener(this);
        mChart.setDrawGridBackground(false);
        mChart.animateX(5000);
        mChart.setDescription("Portfolio");
        mChart.setTouchEnabled(true);
        mChart.setDrawMarkerViews(true);


        //Add an empty data object
        mChart.setData(new LineData());
        mChart.invalidate();


        //Set Spinner
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);
        ArrayList<String> categories = new ArrayList<>();
        categories.add("Daily");
        categories.add("Monthly");
        categories.add("Quarterly");
        //Create adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,categories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);

        //Read JSON OFFLINE
        //portfolioArrayList = loadJSONFromAsset();
        //READ JSON ONLINE
         new getPortfolio().execute();


        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // modify the legend ...
        // l.setPosition(LegendPosition.LEFT_OF_CHART);
        l.setForm(Legend.LegendForm.LINE);
    }
    //INITIALIZE DATA FUNCTIONS 
    private ArrayList<Portfolio> loadJSONFromAsset(){
        ArrayList<Portfolio> portfolioList = new ArrayList<>();

        String json = null;
        try{
            InputStream is = getAssets().open("data_json.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");


        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        try{

            JSONArray m_jArry = new JSONArray(json);

            for (int i = 0; i < m_jArry.length(); i++){
                JSONObject jo_inside = m_jArry.getJSONObject(i);
                Portfolio portfolio = new Portfolio();
                portfolio.setPortfolioId(jo_inside.getString("portfolioId"));
                JSONArray nav_Array = jo_inside.getJSONArray("navs");
                ArrayList<Nav> navList = new ArrayList<>();
                for(int j = 0; j< nav_Array.length(); j++){
                    JSONObject nav_inside = nav_Array.getJSONObject(j);
                    Nav nav = new Nav();
                    nav.setDate(nav_inside.getString("date"));
                    nav.setAmount((float) nav_inside.getDouble("amount"));
                    navList.add(nav);

                }

                portfolio.setNavPoints(navList);
                portfolioList.add(portfolio);

            }
        }catch(JSONException e){
            e.printStackTrace();
            return null;
        }
        return portfolioList;
    }

    private class getPortfolio extends AsyncTask<Void, Void,Void> {

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }
        @Override
        protected Void doInBackground(Void... arg0){
            HttpHandler sh = new HttpHandler();
            String jsonString = sh.makeServiceCall(url);
            Log.d("JSON","Response from url: "+jsonString);
            if(jsonString!=null){
                try{

                    JSONArray m_jArry = new JSONArray(jsonString);

                    for (int i = 0; i < m_jArry.length(); i++){
                        JSONObject jo_inside = m_jArry.getJSONObject(i);
                        Portfolio portfolio = new Portfolio();
                        portfolio.setPortfolioId(jo_inside.getString("portfolioId"));
                        JSONArray nav_Array = jo_inside.getJSONArray("navs");
                        ArrayList<Nav> navList = new ArrayList<>();
                        for(int j = 0; j< nav_Array.length(); j++){
                            JSONObject nav_inside = nav_Array.getJSONObject(j);
                            Nav nav = new Nav();
                            nav.setDate(nav_inside.getString("date"));
                            nav.setAmount((float) nav_inside.getDouble("amount"));
                            navList.add(nav);

                        }

                        portfolio.setNavPoints(navList);
                        portfolioArrayList.add(portfolio);
                        Log.d("Size is ",portfolioArrayList.size()+"");
                    }
                }catch(final JSONException e){
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            }else{
                Log.e("JSON", "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result){
            super.onPostExecute(result);
            if(pDialog.isShowing()){
                pDialog.dismiss();
            }
            Log.d("Correct value",portfolioArrayList.get(0).getNavPoints().get(100).getAmount()+"");
            addDailyEntry();

        }

    }

    private ArrayList<Integer> colorArrayList(){
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.BLACK);
        colors.add(Color.GREEN);
        colors.add(Color.BLUE);
        colors.add(Color.RED);
        colors.add(Color.MAGENTA);
        return colors;
    }
//ADD DATA TO DATASET FUNCTIONS 
    private void addDailyEntry(){

        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        ArrayList<String> xVals = new ArrayList<String>();
        ArrayList<Entry> sumEntry = new ArrayList<>();
        int xvalcount = 366;
        for (int i = 1; i <= xvalcount; i++) {
            xVals.add("" + i);
        }

       ArrayList<Integer> colorList = colorArrayList();
        for(int i=0 ; i< portfolioArrayList.size(); i++){
            ArrayList<Entry> yVals = setYAxisValuesForDaily(portfolioArrayList.get(i));
            for(int j=0;j<yVals.size();j++){
                sumEntry.add(yVals.get(j));
            }
            LineDataSet set1;
            set1 = new LineDataSet(yVals,"Data "+(i+1));
            formatSet(set1,colorList.get(i));
            dataSets.add(set1); // add the datasets
        }//end loop of portfolios

        LineData new_data = new LineData(xVals,dataSets);
        mChart.setData(new_data);
        CustomMarkerView customMarkerView = new CustomMarkerView(this.getApplicationContext(),R.layout.custom_marker_view_layout,xVals,sumEntry);
        mChart.setMarkerView(customMarkerView);
        mChart.getData().notifyDataChanged();
        mChart.notifyDataSetChanged();
        mChart.invalidate();
    }
    public void addMonthlyEntry(){
        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        ArrayList<String> xVals = new ArrayList<String>();
        ArrayList<Entry> sumEntry = new ArrayList<>();
        int xvalcount = 12;
        for (int i = 1; i <= xvalcount; i++) {
            xVals.add(i+"");
        }
        ArrayList<Integer> colorList = colorArrayList();
        for(int i=0 ; i< portfolioArrayList.size(); i++){
            ArrayList<Entry> yVals = setYAxisValuesForMonthly(portfolioArrayList.get(i));
            for(int j=0;j<yVals.size();j++){
                sumEntry.add(yVals.get(j));
            }
            LineDataSet set1;
            set1 = new LineDataSet(yVals,"Data "+(i+1));
            formatSet(set1,colorList.get(i));
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
    public void addQuarterlyEntry(){
        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        ArrayList<String> xVals = new ArrayList<String>();
        ArrayList<Entry> sumEntry = new ArrayList<>();
        int xvalcount = 12;
        for (int i = 0; i < xvalcount; i=i+3) {
            xVals.add(i+3+"");//4 value
        }
        ArrayList<Integer> colorList = colorArrayList();
        for(int i=0 ; i< portfolioArrayList.size(); i++){
            ArrayList<Entry> yVals = setYAxisValuesForQuarterly(portfolioArrayList.get(i));
            for(int j=0;j<yVals.size();j++){
                sumEntry.add(yVals.get(j));
            }
            LineDataSet set1;
            set1 = new LineDataSet(yVals,"Data "+(i+1));
            formatSet(set1,colorList.get(i));
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

//MINOR FUNCTIONS 
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

    private int searchDate(ArrayList<Nav> arrayList, Nav point){

        ArrayList<String> dateArray = new ArrayList<>();
        for(int i=0;i<arrayList.size();i++){
            dateArray.add(arrayList.get(i).getDate());
        }
       return dateArray.indexOf(point.getDate());
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
    private Portfolio getSum(ArrayList<Portfolio> arrayList){
        Portfolio sum = new Portfolio();
        sum.setPortfolioId("SUM");
        ArrayList<Nav> navList = new ArrayList<>();

        for(int i=0 ; i<arrayList.size(); i++){//0,1,2
            ArrayList<Nav> currentList = arrayList.get(i).getNavPoints();
            for( int j=0; j< currentList.size(); j++){//0 to 365
                Nav point = currentList.get(j);
                int index = searchDate(navList,point);//search current sum navPoint List

                if(index>0){
                    //If yes, has the date already, we need to add up amount for that date only
                    float current_amount = navList.get(index).getAmount();

                    navList.get(index).setAmount(current_amount+point.getAmount());

                }else{
                    //If no means the sum does not have this date, we add this date.
                    navList.add(point);
                }
            }
        }//end i

        sum.setNavPoints(navList);

        return sum;
    }
    private ArrayList<Entry> setYAxisValuesForDaily(Portfolio portfolio){

        ArrayList<Entry> yVals = new ArrayList<Entry>();
        ArrayList<Nav> navList = portfolio.getNavPoints();



            for (int i = 0; i < navList.size(); i++) {
                int x_label = convertDate(navList.get(i).getDate());
                float amount = navList.get(i).getAmount();
                yVals.add(new Entry(amount, x_label));

            }

        return yVals;
    }


//INTERACTIVE FUNCTIONS 

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
    public void onChartLongPressed(MotionEvent me){
        Log.i("Long Press ", "Chart Longpressed");
    }
    @Override
    public void onChartDoubleTapped(MotionEvent me){
        Log.i("Double Tap","Chart double-tapped");
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
    public void onNothingSelected(){
        Log.i("Nothing selected","Nothing selected");
    }
    @Override
    public void onNothingSelected(AdapterView parent){

    }
    @Override
    public void onItemSelected(AdapterView parent, View view, int position, long id){
        String item = parent.getItemAtPosition(position).toString();
        this.spinnerPosition = position+1;
        ArrayList<Portfolio> tempList = this.portfolioArrayList;
        if(this.portfolioArrayList.size()>0){
            if(this.spinnerPosition == 1){
                addDailyEntry();
                Toast.makeText(parent.getContext(),"Selected: "+item,Toast.LENGTH_LONG).show();
            }
            else if(this.spinnerPosition == 2){
                addMonthlyEntry();
                Toast.makeText(parent.getContext(),"Selected: "+item,Toast.LENGTH_LONG).show();
            }else{
                addQuarterlyEntry();
                Toast.makeText(parent.getContext(),"Selected: "+item,Toast.LENGTH_LONG).show();
            }
        }else{

        }

    }
}
