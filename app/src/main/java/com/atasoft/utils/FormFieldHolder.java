package com.atasoft.utils;

import android.widget.EditText;
import android.widget.Spinner;

public class FormFieldHolder {
    public String outputString = " ";


    public String fieldName;
    public String viewName;
    public int fieldCount = 0;
    public int selectedIndex = 0;
    public FormFieldHolder(String fieldName, String viewName){
        this.fieldName = fieldName;
        this.viewName = viewName;
    }
    public FormFieldHolder(String fieldName, String viewName, int fieldCount){
        this.fieldName = fieldName;
        this.viewName = viewName;
        this.fieldCount = fieldCount;
    }

    public void setOutputFromEdit(EditText eText){
        /*int resId = getResources().getIdentifier(viewName, "id", context.getPackageName());
        EditText eText = (EditText) thisFrag.findViewById(resId);*/
        if(eText == null || !(eText instanceof EditText)) return;
        String outStr = eText.getText().toString();
        if (outStr.matches("")) outStr = " ";

        //sinText 1, 2 , 3 are seperated
        if(fieldName.contains("sinText")) {
            if(outStr.length() != 9) return;
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
        /*int resId = getResources().getIdentifier(viewName, "id", context.getPackageName());
        Spinner spinner = (Spinner) thisFrag.findViewById(resId);*/

        if(spinner == null || !(spinner instanceof Spinner)) return;
        this.selectedIndex = spinner.getSelectedItemPosition();
    }

    public String[][] getFieldArray(){
        String[][] outArr = new String[fieldCount][2];
        for(int i=0; i< fieldCount; i++){
            outArr[i][0] = fieldName + (i+1);
            outArr[i][1] = (i == selectedIndex) ? "X" : " ";
        }
        return outArr;
    }

    //Edit Text fields.  [PDF form name, EditText View name]
    public static final String[][] fieldNameEdits = {
            {"aprNameText","appNameEdit"},
            {"sinText1","sinNumberEdit"},
            {"sinText2","sinNumberEdit"},
            {"sinText3","sinNumberEdit"},
            {"empNameText","empNameEdit"},
            {"jobLocText","jobLocationEdit"},
            {"jobStewardText","jobStewardEdit"},
            {"curDateText","currentDateEdit"},
            {"jobStartText","jobStartEdit"},
            {"jobEndText","jobEndEdit"},
            {"absNumberBox", "absentEdit"},
            {"lateNumberBox", "lateEdit"}};

    //CheckBox Spinner Fields.  [Spinner View name, PDFForm option name1, PDFForm option displayname 1, ...]
    public static final String[] jobTypeSpinner = {"projTypeSpinner", "projConstBox", "Construction",
            "projMaintBox", "Maintenance", "projDemoBox", "Demolition", "projShopBox", "Shop"};


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
    public static final String[][] checkNames = {
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
            {"towTowers", "Towers"},
            //Duties
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
}