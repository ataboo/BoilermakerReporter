package com.atasoft.utils;

//functions used to edit PDFs for BoilermakerReporter

import android.content.res.AssetManager;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

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

    public static void populateChecks(View thisFrag, LinearLayout linLay, String[][] names){
        if(linLay == null){
            Log.e("PDFManager", "One of the LinearLayouts was null.  Aborted populate checks.");
            return;
        }
        for(int i=0; i<names.length; i++){
            LinearLayout.LayoutParams checkWrap = new LinearLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            CheckBox check = new CheckBox(thisFrag.getContext());
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
