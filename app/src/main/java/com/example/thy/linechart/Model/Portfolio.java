package com.example.thy.linechart.Model;

import java.util.ArrayList;

/**
 * Created by thy on 17/5/17.
 */

public class Portfolio {
    String portfolioId;
    ArrayList<Nav> navPoints;
    public void setPortfolioId(String portfolioId){
        this.portfolioId = portfolioId;
    }
    public void setNavPoints(ArrayList<Nav> navPoints){
        this.navPoints = navPoints;
    }
    public ArrayList<Nav> getNavPoints(){
        return this.navPoints;
    }
    public String getPortfolioId(){
        return this.portfolioId;
    }

}
