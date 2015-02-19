package com.atasoft.utils;

//functions used to edit PDFs for BoilermakerReporter

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Layout;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.atasoft.fragments.ApprenticeReportFrag;
import com.atasoft.fragments.SuperReportFrag;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckbox;
import org.apache.pdfbox.pdmodel.interactive.form.PDFieldTreeNode;

import java.io.*;
import java.util.ArrayList;
public class PDFManager {
    public static final String outputFolder = "/Downloads/BMReporter";
    public static final String root = Environment.getExternalStorageDirectory().toString();
    public static final String outputPath = root + outputFolder;
    public static final String saveTagField = "projType1";

    public static final int NOT_REPORT_PDF = -1;
    public static final int SUPER_LAUNCH = 0;
    public static final int APPRENTICE_LAUNCH = 1;
    public static final int STEWARD_LAUNCH = 2;

    public static PDDocument loadPDFFile(String filePath, AssetManager assetMan){
        try{
            InputStream input = assetMan.open(filePath);
            PDDocument pd = PDDocument.load(input);
            input.close();
            return pd;
        } catch(IOException ie){
            ie.printStackTrace();
            Log.w("PDFManager", ie.toString());
            return null;
        }
    }

    public static PDDocument loadPDFFile(File pdfFile){
        if(!pdfFile.exists()) return null;
        FileInputStream fStream = null;
        try {
            fStream = new FileInputStream(pdfFile);
        } catch (FileNotFoundException fnfe){
            fnfe.printStackTrace();
        }
        try{
            PDDocument pd = PDDocument.load(fStream);
            fStream.close();
            return pd;
        } catch(IOException ie){
            ie.printStackTrace();
            Log.w("PDFManager", "Error loading PDDocument from " + pdfFile.getName());
            return null;
        }
    }

    public static String savePDFFile(PDDocument pd, String fileName){
        String fileNameChanged = fileName;
        File outDir = new File(outputPath);
        outDir.mkdirs();
        File saveFile = new File(outputPath, fileNameChanged + ".pdf");
        if(saveFile.exists()) {
            int nameIncrement = 1;
            while (nameIncrement < 1000) {
                fileNameChanged = fileNameChanged + "_" + Integer.toString(nameIncrement);
                saveFile = new File(outputPath, fileNameChanged + ".pdf");
                if(!saveFile.exists()) break;
                fileNameChanged = fileNameChanged.split("_")[0];
                nameIncrement++;
            }
        }
        //Tags white text box for comparison when loading files that have been edited.
        try {
            setTextField(pd.getDocumentCatalog().getAcroForm(), "typeTag", fileName);
        } catch (IOException ie){
            Log.e("PDFManager", "IOException when trying to tag typeTag");
            ie.printStackTrace();
        }

        fileNameChanged = fileNameChanged+".pdf";
        try{
            FileOutputStream outStr = new FileOutputStream(saveFile);
            pd.save(outStr);
            outStr.flush();
            outStr.close();
            pd.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            Log.e("PDFManager", ex.toString());
        }
        return outputPath+"/"+fileNameChanged;
    }

    public static String getTextFieldValue(PDAcroForm acroForm, String fieldName){
        try{
            PDFieldTreeNode field = acroForm.getField(fieldName);
            if(field == null){
                Log.w("PDFManager", "Couldn't find field: " + fieldName);
                return "";
            }
            return field.getValue().toString();
        } catch (IOException ie){
            ie.printStackTrace();
            Log.e("PDFManager", "IOException getting text field: " + fieldName);
        }
        return "";
    }

    public static View getViewByName(String name, View fragView){
        int resId = fragView.getResources().getIdentifier(name, "id",
                fragView.getContext().getPackageName());
        View retView =  fragView.findViewById(resId);
        if (retView == null){
            Log.w("ReportForm.java", "Can't find View " + name + ". Returned null.");
        }
        return retView;}

    public static File[] getSavedFiles(){
        File folder = new File(outputPath);
        if(!folder.exists()){
            folder.mkdirs();
        }

        ArrayList<File> filesArr = new ArrayList<File>();
        for(File fileEntry :  folder.listFiles()) {
            if (fileEntry.isFile()) {
                String[] fileSplit = fileEntry.getName().split("\\.");
                if(fileSplit.length > 1 && fileSplit[fileSplit.length -1].matches("pdf"))
                    filesArr.add(fileEntry);
            }
        }
        File[] outArr = new File[filesArr.size()];
        outArr = filesArr.toArray(outArr);
        return outArr;
    }

    public static int getReportType(File pdfFile){
        PDDocument pd = loadPDFFile(pdfFile);
        if(pd == null) return NOT_REPORT_PDF;

        return getReportType(pd, pdfFile.getName());
    }

    //Filename only needed for logging
    public static int getReportType(PDDocument pdDoc, String fileName){
        String tagString = "fail";
        try{
            PDFieldTreeNode typeField = pdDoc.getDocumentCatalog().getAcroForm().getField("typeTag");
            if(typeField == null){
                Log.e("PDFManager", "got null from typeTag field from: " + fileName);
                pdDoc.close();
                return NOT_REPORT_PDF;
            }
            tagString = typeField.getValue().toString();
            pdDoc.close();
        } catch (IOException ie){
            Log.w("PDFManager", "typeTag IOError.");
            return  NOT_REPORT_PDF;
        }
        return tagString.matches(SuperReportFrag.stewardOutputFileName) ? STEWARD_LAUNCH :
                tagString.matches(SuperReportFrag.superOutputFileName) ? SUPER_LAUNCH :
                        tagString.matches(ApprenticeReportFrag.outputFileName) ? APPRENTICE_LAUNCH :
                                NOT_REPORT_PDF;
    }

    public static String getStringFromFileCode(int code){
        switch(code){
            case SUPER_LAUNCH:
                return "Supervisor";
            case APPRENTICE_LAUNCH:
                return "Apprentice";
            case STEWARD_LAUNCH:
                return "Steward";
        }
        return "Not Valid";
    }

    public static boolean getCheckboxFieldState(PDAcroForm acroForm, String checkName){
        try{
            PDCheckbox checkbox = (PDCheckbox) acroForm.getField(checkName);
            return checkbox.isChecked();
        } catch (IOException ie){
            ie.printStackTrace();
        }
        return false;
    }

    //Generate Labeled EditTexts in a linearlayout from strings
    public static void populateLinWithEdits(View parentFrag, LinearLayout linLay, String[][] names) {
        if(linLay == null){
            Log.e("PDFManager", "One of the LinearLayouts was null. Aborted populateEdits.");
            return;
        }
        Context context = parentFrag.getContext();
        int labelWidth = dipToPixel(context, 120);
        int editWidth = dipToPixel(context, 60);
        int editMaxLength = 5;
        for(int i=0; i<names.length; i++){
            LinearLayout.LayoutParams wrapConParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            LinearLayout.LayoutParams hrsEditParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            LinearLayout holderLay = new LinearLayout(context);
            holderLay.setLayoutParams(wrapConParams);
            holderLay.setOrientation(LinearLayout.HORIZONTAL);

            TextView hoursEditLabel = new TextView(context);
            hoursEditLabel.setLayoutParams(wrapConParams);
            hoursEditLabel.setWidth(labelWidth);
            hoursEditLabel.setText(names[i][1]);
            hoursEditLabel.setTag(names[i][0]);
            holderLay.addView(hoursEditLabel);

            EditText hourEdit = new EditText(context);
            hourEdit.setLayoutParams(hrsEditParams);
            hourEdit.setWidth(editWidth);
            hourEdit.setInputType(InputType.TYPE_CLASS_NUMBER);
            hourEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(editMaxLength)});
            hourEdit.setText("0");
            holderLay.addView(hourEdit);

            linLay.addView(holderLay);
        }

    }

    //Generate checkboxes in a linearlayout from strings
    public static void populateCheckViewsFromFormLin(View parentFrag, LinearLayout linLay, String[][] names){
        if(linLay == null){
            Log.e("PDFManager", "One of the LinearLayouts was null.  Aborted populate checks.");
            return;
        }
        for(int i=0; i<names.length; i++){
            LinearLayout.LayoutParams checkWrap = new LinearLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            CheckBox check = new CheckBox(parentFrag.getContext());
            check.setLayoutParams(checkWrap);
            check.setTag(names[i][0]);
            check.setText(names[i][1]);
            linLay.addView(check);
        }
    }

    public static void setTextField(PDAcroForm acroForm, String name, String value) throws IOException {
        //doesn't stick without space for some reason
        if(value.matches("")) value = " ";

        PDFieldTreeNode field = acroForm.getField( name );
        if( field != null ) {
            field.setValue(value);
            //Set as Readonly
            field.setReadonly(true);
        }
        else {
            System.err.println( "No field found with name:" + name );
        }
    }

    public static void setTextFields(PDAcroForm acroForm, String[][] fieldNames) throws IOException{
        for(String[] fieldArr: fieldNames){
            setTextField(acroForm, fieldArr[0], fieldArr[1]);
        }
    }

    public static void setCheckBox(PDAcroForm acroForm, String name, boolean isChecked) throws IOException{
        PDFieldTreeNode field = acroForm.getField(name);
        if(field instanceof PDCheckbox){
            if(isChecked){
                ((PDCheckbox) field).check();
            } else {
                ((PDCheckbox) field).unCheck();
            }
            //set as readonly
            //field.getDictionary().setInt("Ff", 1);
            field.setReadonly(true);
        }
    }

    public static void setFormCheckBoxes(PDAcroForm acroForm, String[][] fieldNames) throws IOException{
        for(String[] fieldArr: fieldNames){
            setCheckBox(acroForm, fieldArr[0], fieldArr[1].matches("X"));
        }
    }

    public static void setCheckboxesInLayout(LinearLayout parentLay, PDAcroForm acroForm) throws IOException{
        //0 is the title, 1 is the pagebreak
        for(int i=0; i<parentLay.getChildCount(); i++){
            View child = parentLay.getChildAt(i);
            if(child instanceof CheckBox){
                PDFManager.setCheckBox(acroForm, parentLay.getChildAt(i).getTag().toString(),
                        ((CheckBox) child).isChecked());
            }
        }
    }

    public static void setFormFromEditsLin(LinearLayout parentLay, PDAcroForm acroForm) throws IOException{
        for(int i=0; i<parentLay.getChildCount();i++) {
            View childLay = parentLay.getChildAt(i);
            if(childLay instanceof LinearLayout){
                String labelTag = "";
                String editTextValue = "";
                for(int j=0; j<((LinearLayout) childLay).getChildCount(); j++){
                    View childView = ((LinearLayout) childLay).getChildAt(j);
                    if(childView instanceof TextView) {
                        if (childView instanceof EditText) {
                            editTextValue = ((EditText) childView).getText().toString();
                        } else {
                            if(childView.getTag() != null) {
                                labelTag = ((TextView) childView).getTag().toString();
                            }
                        }
                    }
                }
                if(editTextValue.matches("0")) editTextValue = "";
                setTextField(acroForm, labelTag + "Hrs", editTextValue);
                setCheckBox(acroForm, labelTag, !(editTextValue.matches("")));
                //Checkboxes will be set from another function
            }
        }
    }

    public static void setEditsFromFormLin(PDAcroForm acroForm, LinearLayout linLay){
        for(int i=0; i< linLay.getChildCount(); i++){
            View child = linLay.getChildAt(i);
            if(child instanceof LinearLayout){
                EditText fieldEdit = null;
                String editLabelTag = null;
                for(int j=0; j<((LinearLayout) child).getChildCount(); j++){
                    View grandChild = ((LinearLayout) child).getChildAt(j);
                    if(grandChild instanceof TextView){
                        //EditText is a subclass of TextView
                        if(grandChild instanceof EditText){
                            fieldEdit = (EditText) grandChild;
                        } else {
                            editLabelTag = grandChild.getTag().toString();
                        }
                    }
                }
                if(fieldEdit != null && editLabelTag != null) {
                    editLabelTag = editLabelTag + "Hrs";
                    fieldEdit.setText(getTextFieldValue(acroForm, editLabelTag));
                }
            }
        }
    }

    public static void setCheckViewsFromFormLin(PDAcroForm acroForm, LinearLayout linLay){
        for(int i=0; i<linLay.getChildCount(); i++){
            View child = linLay.getChildAt(i);
            if(child instanceof CheckBox){
                String checkTag = child.getTag().toString();
                if(checkTag != null){
                    boolean isChecked = getCheckboxFieldState(acroForm, checkTag);
                    ((CheckBox) child).setChecked(isChecked);
                }
            }
        }
    }

    //Basically just a nullCheck
    public static void setEditView(EditText eText, String value){
        if(eText == null){
            Log.e("PDFManager", "Tried to set Edit from File and eText was null.");
            return;
        }
        eText.setText(value);
    }



    //========================================Bundle Stuff==========================================
    public static void setEditFromBundle(Bundle bundle, String eTextName, View parentView){
        EditText eText = (EditText) getViewByName(eTextName, parentView);
        if(eText == null) return;
        eText.setText(bundle.getString("etext_" + eTextName, ""));
        String value = bundle.getString("etext_" + eTextName, "");
        Log.w("PDFMananger", "Set " + eTextName + " to " + value);
    }

    public static void setBundleFromEdit(Bundle bundle, String eTextName, View parentView){
        EditText eText = (EditText) getViewByName(eTextName, parentView);
        if(eText == null) return;

        bundle.putString("etext_" + eTextName, eText.getText().toString());
    }

    public static void setSpinnerFromBundle(Bundle bundle, String spinnerName, View parrentView){
        Spinner spinner = (Spinner) getViewByName(spinnerName, parrentView);
        if(spinner == null) return;

        spinner.setSelection(bundle.getInt("spinner_" + spinnerName, 0));
    }

    public static void setBundleFromSpinner(Bundle bundle, String spinnerName, View parentView){
        Spinner spinner = (Spinner) getViewByName(spinnerName, parentView);
        if(spinner == null) return;

        bundle.putInt("spinner_" + spinnerName, spinner.getSelectedItemPosition());
    }


    public static void setCheckboxFromBundle(Bundle bundle, String checkName, View parentView){
        CheckBox checkBox = (CheckBox) getViewByName(checkName, parentView);
        if(checkBox == null) return;

        checkBox.setChecked(bundle.getBoolean("checkbox_" + checkName, false));
    }

    public static void setBundleFromCheckbox(Bundle bundle, String checkName, View parentView){
        CheckBox checkBox = (CheckBox) getViewByName(checkName, parentView);
        if(checkBox == null) return;

        bundle.putBoolean("checkbox_" + checkName, checkBox.isChecked());
    }

    //Enough screwing around with bundles. Brute force.
    public static void powerSave(SharedPreferences.Editor editor, RelativeLayout relLay, String prefix){
        if(relLay == null){
            Log.e("PDFManager", "Tried to powerSave but RelativeLayout Prefix: " + prefix + " was null.");
            return;
        }
        setPrefsFromChildren(editor, relLay, 0, prefix);
    }

    public static void powerLoad(SharedPreferences prefs, RelativeLayout relLay, String prefix){
        if(relLay == null){
            Log.e("PDFManager", "Tried to powerLoad but RelativeLayout Prefix: " + prefix + " was null.");
            return;
        }
        setChildrenFromPrefs(prefs, relLay, 0, prefix);
    }

    public static int setPrefsFromChildren(SharedPreferences.Editor editor, View parent, int count, String prefix) {
        if(parent instanceof LinearLayout){
            for(int i=0; i<((LinearLayout) parent).getChildCount(); i++){
                count = setPrefsFromChildren(editor, ((LinearLayout) parent).getChildAt(i), count, prefix);
            }
        }
        if(parent instanceof RelativeLayout){
            for(int i=0; i<((RelativeLayout) parent).getChildCount(); i++){
                count = setPrefsFromChildren(editor, ((RelativeLayout) parent).getChildAt(i), count, prefix);
            }
        }
        if(parent instanceof EditText) {
            editor.putString(prefix + "Edit_" + count, ((EditText) parent).getText().toString());
            //String value = ((EditText) parent).getText().toString();
            //Log.w("PDFManager", "Bundled Edit_" + count + " as " + value);
            return ++count;
        }
        if(parent instanceof Spinner){
            editor.putInt(prefix + "Spinner_" + count, ((Spinner) parent).getSelectedItemPosition());
            //int value = ((Spinner) parent).getSelectedItemPosition();
            //Log.w("PDFManager", "Bundled Spinner_" + count + " as " + value);
            return ++count;
        }
        if(parent instanceof CheckBox){
            editor.putBoolean(prefix + "Check_" + count, ((CheckBox) parent).isChecked());
            //boolean value = ((CheckBox) parent).isChecked();
            //Log.w("PDFManager", "Bundled Check_" + count + " as " + value);
            return ++count;
        }
        return count;
    }

    private static int setChildrenFromPrefs(SharedPreferences prefs, View parent, int seed, String prefix){
        int count = seed;
        if(parent instanceof LinearLayout){
            for(int i=0; i<((LinearLayout) parent).getChildCount(); i++){
                count = setChildrenFromPrefs(prefs, ((LinearLayout) parent).getChildAt(i), count, prefix);
            }
        }
        if(parent instanceof RelativeLayout){
            for(int i=0; i<((RelativeLayout) parent).getChildCount(); i++){
                count = setChildrenFromPrefs(prefs, ((RelativeLayout) parent).getChildAt(i), count, prefix);
            }
        }
        if(parent instanceof EditText) {
            ((EditText) parent).setText(prefs.getString(prefix + "Edit_" + count, ""));
            //String value = ((EditText) parent).getText().toString();
            //Log.w("PDFManager", "Set Edit_" + count + " to " + value);
            return ++count;
        }
        if(parent instanceof Spinner){
            ((Spinner) parent).setSelection(prefs.getInt(prefix + "Spinner_" + count, 0));
            //int value = ((Spinner) parent).getSelectedItemPosition();
            //Log.w("PDFManager", "Set Spinner_" + count + " to " + value);
            return ++count;
        }
        if(parent instanceof CheckBox){
            ((CheckBox) parent).setChecked(prefs.getBoolean(prefix + "Check_" + count, false));
            //boolean value = ((CheckBox) parent).isChecked();
            //Log.w("PDFManager", "Set Check_" + count + " to " + value);
            return ++count;
        }
        return count;
    }




    public static void setSpinnerCheckFromFile(PDAcroForm acroForm, String[] fieldNames, View parentFrag){
        Spinner spinner = (Spinner) getViewByName(fieldNames[0], parentFrag);
        if(spinner == null){
            Log.e("PDFManager", "Tried to set Spinner: " + fieldNames[0] + " and found null.");
            return;
        }
        int fieldCount = spinner.getAdapter().getCount();
        for(int i=0; i<fieldCount; i++){
            boolean isChecked = getCheckboxFieldState(acroForm, fieldNames[1] + Integer.toString(i+1));
            if(isChecked){
                spinner.setSelection(i);
                return;
            }
        }
        return;
    }

    public static void setSpinnerTextFromFile(PDAcroForm acroForm, String[] fieldNames, View parentFrag){
        Spinner spinner = (Spinner) getViewByName(fieldNames[0], parentFrag);
        if(spinner == null){
            Log.e("PDFManager", "Tried to set Spinner: " + fieldNames[0] + " and found null.");
            return;
        }
        String fieldVal =  getTextFieldValue(acroForm, fieldNames[1]);
        int spinIndex = 0;
        try{
            spinIndex = Integer.parseInt(fieldVal);
        } catch (NumberFormatException nfe){
            Log.e("PDFManager", "setSpinnerTextFromFile tried to parse: " + fieldNames[1] +
                    " and got an NFE.");
            // nfe.printStackTrace();
        }
        spinIndex = (spinIndex < spinner.getCount()) ? spinIndex : 0;
        spinner.setSelection(spinIndex);
    }

    public static int dipToPixel(Context context, int dips){
        context.getResources();
        int pixels = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dips,
                context.getResources().getDisplayMetrics());
        return pixels;
    }
}
