package com.atasoft.utils;


import android.content.res.AssetManager;
import android.util.Log;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDFieldTreeNode;

import java.io.*;
import java.util.Iterator;
import java.util.List;

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

    public static void setField(PDAcroForm acroForm, String name, String value) throws IOException {
        PDFieldTreeNode field = acroForm.getField( name );
        if( field != null ) {
            field.setValue(value);
        }
        else {
            System.err.println( "No field found with name:" + name );
        }
    }

    public static void savePDF(PDDocument pd, String dir, String fileName){
        File saveFile = new File(dir, fileName);
        if(saveFile.exists()) saveFile.delete();
        try{
            FileOutputStream outStr = new FileOutputStream(saveFile);
            pd.save(outStr);
            outStr.flush();
            outStr.close();

        } catch (IOException ex) {
            ex.printStackTrace();
            Log.e("pdf stuff", ex.toString());
        }
    }

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
    }
}
