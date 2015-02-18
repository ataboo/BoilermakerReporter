package com.atasoft.utils;

//functions used to edit PDFs for BoilermakerReporter

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Environment;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.atasoft.boilermakerreporter.MainActivity;
import com.atasoft.boilermakerreporter.R;
import com.atasoft.fragments.ApprenticeReportFrag;
import com.atasoft.fragments.SuperReportFrag;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckbox;
import org.apache.pdfbox.pdmodel.interactive.form.PDFieldTreeNode;

import java.io.*;
import java.util.ArrayList;
public class PDFManager {
    public static final String outputFolder = "/Documents/BMReporter";
    public static final String root = Environment.getExternalStorageDirectory().toString();
    public static final String outputPath = root + outputFolder;
    public static final String saveTagField = "projType1";

    public static final int NOT_REPORT_PDF = -1;
    public static final int SUPER_LAUNCH = 0;
    public static final int APPRENTICE_LAUNCH = 1;
    public static final int STEWARD_LAUNCH = 2;
    public static PDDocument loadPDF(String filePath, AssetManager assetMan){
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

    public static String savePDF(PDDocument pd, String fileName){
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

    public static void setCheckBoxes(PDAcroForm acroForm, String[][] fieldNames) throws IOException{
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

    public static void setEditsInLayout(LinearLayout parentLay, PDAcroForm acroForm) throws IOException{
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

    //Generate Labeled EditTexts in a linearlayout from strings
    public static void populateWithEdits(View parentFrag, LinearLayout linLay, String[][] names) {
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
    public static void populateChecks(View parentFrag, LinearLayout linLay, String[][] names){
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

    public static View getViewByName(String name, View fragView){
        int resId = fragView.getResources().getIdentifier(name, "id",
                fragView.getContext().getPackageName());
        View retView =  fragView.findViewById(resId);
        if (retView == null){
            Log.w("ReportForm.java", "Can't fine View " + name + ". Returned null.");
        }
        return retView;}

    public static int dipToPixel(Context context, int dips){
        context.getResources();
        int pixels = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dips,
                context.getResources().getDisplayMetrics());
        return pixels;
    }

    public static String[][] getSavedFiles(AssetManager assetManager){
        File folder = new File(outputPath);
        ArrayList<String> fileCandidates = new ArrayList<String>();
        ArrayList<String> fileTypes = new ArrayList<String>();
        for(File fileEntry :  folder.listFiles()) {
            if (fileEntry.isFile()) {
                int reportType = getReportType(fileEntry, assetManager);
                if (reportType != NOT_REPORT_PDF) {
                    fileCandidates.add(fileEntry.getName());
                    fileTypes.add(getStringFromFileCode(reportType));
                }
            }
        }

        String[][] outArr = new String[2][fileCandidates.size()];
        outArr[0] = fileCandidates.toArray(outArr[0]);
        outArr[1] = fileTypes.toArray(outArr[1]);
        return outArr;
    }


    public static int getReportType(File pdfFile, AssetManager assetManager){
        String tagString = "fail";
        PDDocument pd = openPDFFromResources(pdfFile);
        if(pd == null) return NOT_REPORT_PDF;

        try{
            PDFieldTreeNode typeField = pd.getDocumentCatalog().getAcroForm().getField("typeTag");
            if(typeField == null){
                Log.w("PDFManager", "got null from typeTag field on: " + pdfFile.getName());
                pd.close();
                return NOT_REPORT_PDF;
            }
            tagString = typeField.getValue().toString();
            Log.w("PDFManager", "got: " + tagString + " from: " + pdfFile.getName());
            pd.close();
        } catch (IOException ie){
            Log.w("PDFManager", "typeTag IOError.");
            return  NOT_REPORT_PDF;
        }
        return tagString.matches(SuperReportFrag.stewardOutputFileName) ? STEWARD_LAUNCH :
                tagString.matches(SuperReportFrag.superOutputFileName) ? SUPER_LAUNCH :
                        tagString.matches(ApprenticeReportFrag.outputFileName) ? APPRENTICE_LAUNCH :
                                NOT_REPORT_PDF;
    }

    public static PDDocument openPDFFromResources(File pdfFile){
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

    public static String getStringFromFileCode(int code){
        switch(code){
            case SUPER_LAUNCH:
                return "Supervisor";
            case APPRENTICE_LAUNCH:
                return "Apprentice";
            case STEWARD_LAUNCH:
                return "Steward";
        }
        return "Not Valid File";
    }
}
