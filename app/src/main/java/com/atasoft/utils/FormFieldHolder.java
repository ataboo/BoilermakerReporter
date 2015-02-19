package com.atasoft.utils;

import android.util.Log;
import android.widget.EditText;
import android.widget.Spinner;

public class FormFieldHolder {
    public String outputString = " ";


    public String fieldName;
    public String viewName;
    private int fieldCount = 0;
    public int selectedIndex = 0;
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
        String outStr = eText.getText().toString();
        if (outStr.matches("")) outStr = " ";
        this.outputString = outStr;
    }

    public void setOutputFromSpinner(Spinner spinner){
        if(spinner == null){
            Log.e("FormFieldHolder", this.fieldName + " holder received null View in " +
                    "setOutputFromSpinner. Failed.");
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
}