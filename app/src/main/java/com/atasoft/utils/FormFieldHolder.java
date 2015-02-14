package com.atasoft.utils;

import android.util.Log;
import android.widget.EditText;
import android.widget.Spinner;

public class FormFieldHolder {
    public String outputString = " ";


    private String fieldName;
    public String viewName;
    private int fieldCount = 0;
    private int selectedIndex = 0;
    public FormFieldHolder(String fieldName, String viewName){
        this.fieldName = fieldName;
        this.viewName = viewName;
    }
    //For spinner that sets PDF fieldName1, fieldName2,...
    public FormFieldHolder(String fieldName, String viewName, int fieldCount){
        this.fieldName = fieldName;
        this.viewName = viewName;
        this.fieldCount = fieldCount;
    }

    public void setOutputFromEdit(EditText eText){
        if(eText == null){
            Log.e("FormFieldHolder", this.fieldName + " holder received null View in " +
                    "setOutputFromEdit. Failed.");
            return;
        }
        if(!(eText instanceof EditText)){
            Log.e("FormFieldHolder", this.fieldName + " holder expected EditText in " +
                    "setOutputFromEdit. Failed.");
            return;
        }
        String outStr = eText.getText().toString();
        if (outStr.matches("")) outStr = " ";

        //sinText 1, 2 , 3 are split into 3 views from 9 digit sin.
        if(fieldName.contains("sinText")) {
            outStr = outStr.replace(" ", "");
            if(outStr.length() != 9){
                Log.w("FormFieldHolder", "Sin number was not 9 digits.");
                return;
            }
            int sinInt = Integer.parseInt(fieldName.substring(7));
            switch(sinInt){
                case 1:
                    outStr = outStr.substring(0,3);
                    break;
                case 2:
                    outStr = outStr.substring(3,6);
                    break;
                default:
                    outStr = outStr.substring(6);
                    break;
            }
        }
        this.outputString = outStr;
    }

    public void setOutputFromSpinner(Spinner spinner){
        if(spinner == null){
            Log.e("FormFieldHolder", this.fieldName + " holder received null View in " +
                    "setOutputFromSpinner. Failed.");
            return;
        }
        if(!(spinner instanceof Spinner)){
            Log.e("FormFieldHolder", this.fieldName + " holder expected Spinner in " +
                    "setOutputFromEdit. Failed.");
            return;
        }
        this.selectedIndex = spinner.getSelectedItemPosition();
    }

    //creates array [field name sequential][value]
    public String[][] getFieldArray(){
        String[][] outArr = new String[fieldCount][2];
        for(int i=0; i< fieldCount; i++){
            outArr[i][0] = fieldName + (i+1);
            outArr[i][1] = (i == selectedIndex) ? "X" : " ";
        }
        return outArr;
    }

    //<editor-fold desc="Field Name Arrays">
    //Edit Text fields.  [EditText name, PDF form name]
    public static final String[][] fieldNameEdits = {
            {"appNameEdit", "aprNameText"},
            {"sinNumberEdit", "sinText1"},
            {"sinNumberEdit", "sinText2"},
            {"sinNumberEdit", "sinText3"},
            {"empNameEdit", "empNameText"},
            {"jobLocationEdit", "jobLocText"},
            {"jobStewardEdit", "jobStewardText"},
            {"currentDateEdit", "curDateText"},
            {"jobStartEdit", "jobStartText"},
            {"jobEndEdit", "jobEndText"},
            {"absentEdit", "absNumberBox"},
            {"lateEdit", "lateNumberBox"},
            {"superNameEdit", "superName"},
            {"commentsEdit", "commentsText"}};

    //CheckBox Spinner Fields.  [Spinner View name, PDF box1, displayname 1, PDF box2...]
    public static final String[][] jobTypeSpinnerOptions = {
            {"projTypeSpinner", "projType"},
            {"Construction","Maintenance","Demolition", "Shop"}};

    //CheckBox/Spinner Fields.  [Spinner View, PDF box1 name, display name 1, PDF box2 name, ...]
    public static final String[][] ratingSpinnerOptions = {
            {"ratingSpinner", "rating"},
            {"As good as a Journeyman", "Needs more experience", "Below average"}};

    //Rating Excellent, Above Average, Average, Below Average, Unsatisfactory [Spinner View name, textfield name 1-5]
    public static final String[][] ratingSpinners = {
            {"safetySpinner", "safetyBox"},
            {"workerSpinner", "workersBox"},
            {"jobSpinner", "jobBox"},
            {"initSpinner", "initBox"},
            {"capSpinner", "capBox"}};
    //Rating 1-3
    public static final String[][] attendanceSpinners = {
            {"absentSpinner", "absBox"},
            {"lateSpinner", "lateBox"}};

    //CheckBox Toggle Fields [Form Checkbox Name, Display String]
    public static final String[][] towChecks = {
            //Type of Work
            {"towAtomic", "Atomic Rad Work"},
            {"towBins", "Bins & Hoppers"},
            {"towBoilers", "Boilers"},
            {"towConds", "Condensers/Evaporators"},
            {"towFurnaces", "Furnaces"},
            {"towExchangers", "Heat Exchangers"},
            {"towPenstock", "Penstock"},
            {"towPrecips", "Precipitators"},
            {"towVessel", "Pressure Vessel"},
            {"towScroll", "Scroll Casings"},
            {"towScrubbers", "Scrubbers"},
            {"towStacks", "Stacks & Breeching"},
            {"towTanks", "Tanks"},
            {"towTowers", "Towers"}};

    public static final String[][] dutyChecks = {
            {"dutyOHS", "Adhere to OH & S"},
            {"dutyBurning", "Burning"},
            {"dutyWatch", "Confined Space Watch"},
            {"dutyExpanding", "Expanding"},
            {"dutyGlass", "Fibreglass"},
            {"dutyFitting", "Fitting"},
            {"dutyGrinding", "Grinding"},
            {"dutyLayout", "Layout"},
            {"dutyMetalizing", "Metalizing"},
            {"dutyReading", "Reading Drawings"},
            {"dutyRigging", "Rigging"},
            {"dutySpark", "Spark Watch"},
            {"dutyTack", "Tack Welding"},
            {"dutyTray", "Tray Work"}};
    //</editor-fold
}