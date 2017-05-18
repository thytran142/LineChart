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
import android.widget.Toast;

import com.example.thy.linechart.Class.CustomLineChart;
import com.example.thy.linechart.Http.HttpHandler;
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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class MainActivity extends CustomLineChart {



    ArrayList<Portfolio> portfolioArrayList = new ArrayList<>();
    private static String url = "https://api.myjson.com/bins/js8ul";
    private ProgressDialog pDialog;

     @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //Read JSON OFFLINE
        //portfolioArrayList = loadJSONFromAsset();
        //READ JSON ONLINE
         new getPortfolio().execute();

    }
    @Override
    public Spinner getSpinner(){
        return (Spinner) findViewById(R.id.spinner);
    }
    @Override
    public int getLayoutResourceId(){
        return R.layout.activity_main;
    }
    @Override
    public LineChart setmChart(){
        return  (LineChart) findViewById(R.id.linechart);

    }
    @Override
    public ArrayList<Portfolio> getPortfolioArrayList(){
        return this.portfolioArrayList;
    }


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

            addDailyEntry(portfolioArrayList);
        }

    }
    private int searchDate(ArrayList<Nav> arrayList, Nav point){

        ArrayList<String> dateArray = new ArrayList<>();
        for(int i=0;i<arrayList.size();i++){
            dateArray.add(arrayList.get(i).getDate());
        }
       return dateArray.indexOf(point.getDate());
    }
    @Override
    public void onNothingSelected(AdapterView parent){

    }

}
