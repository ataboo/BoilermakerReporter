package com.atasoft.utils;

//functions used to edit PDFs for BoilermakerReporter

import android.content.Context;
import android.content.res.AssetManager;
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

import com.atasoft.boilermakerreporter.R;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckbox;
import org.apache.pdfbox.pdmodel.interactive.form.PDFieldTreeNode;

import java.io.*;

public class PDFManager {
    public static PDDocument loadPDF(String filePath, AssetManager assetMan){
        try{
            InputStream input = assetMan.open(filePath);
            PDDocument pd = PDDocument.load(input);
            input.close();
            return pd;
        } catch(IOException ie){
            ie.printStackTrace();
            Log.w("pdf stuff", ie.toString());
            return null;
        }
    }

    public static String savePDF(PDDocument pd, String dir, String fileName){
        File saveFile = new File(dir, fileName + ".pdf");
        if(saveFile.exists()) {
            int nameIncrement = 1;
            while (nameIncrement < 1000) {
                fileName = fileName + "_" + Integer.toString(nameIncrement);
                saveFile = new File(dir, fileName + ".pdf");
                if(!saveFile.exists()) break;
                fileName = fileName.split("_")[0];
                nameIncrement++;
            }

        }
        fileName = fileName+".pdf";
        try{
            FileOutputStream outStr = new FileOutputStream(saveFile);
            pd.save(outStr);
            outStr.flush();
            outStr.close();

        } catch (IOException ex) {
            ex.printStackTrace();
            Log.e("pdf stuff", ex.toString());
        }
        return dir+"/"+fileName;
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
                            Log.w("PDFManager", "set editTextValue to:" + editTextValue);
                        } else {
                            if(childView.getTag() != null) {
                                labelTag = ((TextView) childView).getTag().toString();
                                Log.w("PDFManager", "set labelTag to: " + labelTag);
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
        Log.w("PDFManager", "Converted " + dips + " to " + pixels);
        return pixels;
    }

    //make these with ctrl-alt-T
    //<editor-fold desc="Old Functions">
/*
    public static void printFields(PDDocument pd) throws IOException {
        PDDocumentCatalog docCatalog = pd.getDocumentCatalog();
        PDAcroForm acroForm = docCatalog.getAcroForm();
        List fields = acroForm.getFields();
        Iterator fieldsIter = fields.iterator();

        Log.w("pdf stuff", new Integer(fields.size()).toString() + " top-level fields were found on the form");

        while( fieldsIter.hasNext()) {
            PDField field = (PDField)fieldsIter.next();
            processField(field, "|--", field.getPartialName());
        }
    }
    private static void processField(PDField field, String sLevel, String sParent) throws IOException
    {
        List kids = field.getKids();
        if(kids != null) {
            Iterator kidsIter = kids.iterator();
            if(!sParent.equals(field.getPartialName())) {
                sParent = sParent + "." + field.getPartialName();
            }

            Log.w("pdf stuff", sLevel + sParent);

            while(kidsIter.hasNext()) {
                Object pdfObj = kidsIter.next();
                if(pdfObj instanceof PDField) {
                    PDField kid = (PDField)pdfObj;
                    processField(kid, "|  " + sLevel, sParent);
                }
            }
        }
        else {
            String outputString = sLevel + sParent + "." + field.getPartialName() + ",  type=" + field.getClass().getName() + ", value= " + field.getValue();
            Log.w("pdf stuff", outputString);
        }
    }*/
    //</editor-fold>

}
