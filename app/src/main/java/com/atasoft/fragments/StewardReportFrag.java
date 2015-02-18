package com.atasoft.fragments;

//Could have just used a constructor in SuperReportFrag but this is cooler.
public class StewardReportFrag extends SuperReportFrag {
    @Override
    public void setupViews(){
        super.setToSteward(true);
        super.setupViews();
    }
}
