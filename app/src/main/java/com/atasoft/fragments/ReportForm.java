package com.atasoft.fragments;

import com.atasoft.boilermakerreporter.*;
import com.atasoft.utils.*;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.*;
import android.support.v4.app.Fragment;
import android.app.*;
import android.util.Log;
import android.view.*;
import android.widget.*;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.interactive.form.*;

import java.io.*;


public class ReportForm extends Fragment {
    private View thisFrag;
    private Context context;
    private AssetManager assetMan;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

       View v = inflater.inflate(R.layout.fragment_main, container, false);
        thisFrag = v;
        this.context = v.getContext();
        this.assetMan = context.getAssets();
        setupViews();

        pdfStuff();
        return v;
    }

    private void setupViews(){

    }

    private void pdfStuff() {
        String root = Environment.getExternalStorageDirectory().toString();
        String inputPath = "Files/TestForm.pdf";
        String outputPath = root + "/Documents";
        String outputFile = "Report.pdf";

        PDDocument pd;


        /*pd = PDFManager.loadPDF(inputPath, assetMan);

        try{
            Log.w("pdf stuff", "Fields before:");
            PDFManager.printFields(pd);

            PDAcroForm acroForm = pd.getDocumentCatalog().getAcroForm();
            PDFManager.setField(acroForm, "Text Box 1", "Bananananananananana");


            Log.w("pdf stuff", "Fields after:");
            PDFManager.printFields(pd);
            PDFManager.savePDF(pd, outputPath, outputFile);
            pd.close();
        } catch (IOException ie) {
            ie.printStackTrace();
            Log.w("pdf stuff", ie.toString());
        }*/

    }
}





