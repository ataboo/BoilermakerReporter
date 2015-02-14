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
import android.view.View.*;
import android.widget.*;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.interactive.form.*;

import java.io.*;
import java.util.HashMap;

public class ReportForm extends Fragment implements OnClickListener {

    private static final String inputPath = "Files/ApprenticeReportSuper.pdf";
    private static final String outputFileName = "ApprReport_Super";
    private static final String outputFolder = "/Documents";
    public static final int PUSHED_BUTTON = 0;
    public static final int STARTED_EDITS = 1;
    public static final int DONE_EDITS = 2;
    public static final int DONE_SAVE = 3;

    private HashMap<String, FormFieldHolder> fieldMap;
    private String outFileFull = "";
    private View thisFrag;
    private Context context;
    private ProgressDialog pBar;
    private LinearLayout towLay;
    private LinearLayout dutyLay;
    private Spinner ratingSpinner;
    private Spinner jobTypeSpinner;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

       View v = inflater.inflate(R.layout.fragment_main, container, false);
        thisFrag = v;
        this.context = v.getContext();
        setupViews();
        return v;
    }

    //<editor-fold desc="pHandler">
    final Handler pHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case PUSHED_BUTTON:
                    pBar.setMessage("Reading Template...");
                    pBar.show();
                    break;
                case STARTED_EDITS:
                    pBar.setMessage("Editing Fields...");
                    break;
                case DONE_EDITS:
                    pBar.setMessage("Saving PDF...");
                    break;
                case DONE_SAVE:
                    Toast.makeText(context, "Saved as " + outFileFull, Toast.LENGTH_LONG).show();
                    pBar.hide();
                    break;
            }
            super.handleMessage(msg);
        }
    };
    //</editor-fold>



    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.pushBootan){
            goSubmit();
        }
    }

    private void setupViews(){
        Button goButton = (Button) thisFrag.findViewById(R.id.pushBootan);
        goButton.setOnClickListener(this);

        pBar = new ProgressDialog(context);

        //fieldMap stores the non-checkbox fields as <(String) fieldName from pdf, (FormFieldHolder)>
        if(fieldMap == null) {
            fieldMap = new HashMap<String, FormFieldHolder>();
            for(String[] fieldName: FormFieldHolder.fieldNameEdits) {
                fieldMap.put(fieldName[1],
                        new FormFieldHolder(fieldName[1], fieldName[0]));
            }
            for(String[] rating: FormFieldHolder.ratingSpinners){
                fieldMap.put(rating[0],
                        new FormFieldHolder(rating[1], rating[0], 5));
            }
            for(String[] rating: FormFieldHolder.attendanceSpinners){
                fieldMap.put(rating[0],
                        new FormFieldHolder(rating[1], rating[0], 3));
            }

            this.jobTypeSpinner = (Spinner) thisFrag.findViewById(R.id.jobTypeSpinner);
            populateSpinner(jobTypeSpinner, FormFieldHolder.jobTypeSpinnerOptions[1]);

            fieldMap.put(FormFieldHolder.jobTypeSpinnerOptions[0][1],
                    new FormFieldHolder(FormFieldHolder.jobTypeSpinnerOptions[0][1],
                    FormFieldHolder.jobTypeSpinnerOptions[0][0],
                    FormFieldHolder.jobTypeSpinnerOptions[1].length));

            this.ratingSpinner = (Spinner) thisFrag.findViewById(R.id.ratingSpinner);
            populateSpinner(ratingSpinner, FormFieldHolder.ratingSpinnerOptions[1]);

            fieldMap.put(FormFieldHolder.ratingSpinnerOptions[0][1],
                    new FormFieldHolder(FormFieldHolder.ratingSpinnerOptions[0][1],
                    FormFieldHolder.ratingSpinnerOptions[0][0],
                    FormFieldHolder.ratingSpinnerOptions[1].length));

        }

        this.towLay = (LinearLayout) thisFrag.findViewById(R.id.towCheckLay);
        PDFManager.populateChecks(thisFrag, towLay, FormFieldHolder.towChecks);
        this.dutyLay = (LinearLayout) thisFrag.findViewById(R.id.dutyCheckLay);
        PDFManager.populateChecks(thisFrag, dutyLay, FormFieldHolder.dutyChecks);
    }



    private void editFields(PDAcroForm acroForm) throws IOException{
        for(String[] fieldName : FormFieldHolder.fieldNameEdits){
            FormFieldHolder holder = fieldMap.get(fieldName[1]);
            holder.setOutputFromEdit((EditText) PDFManager.getViewByName(holder.viewName, thisFrag));
            PDFManager.setTextField(acroForm, fieldName[1], holder.outputString);
        }

        for(String[] fieldName : FormFieldHolder.ratingSpinners){
            FormFieldHolder holder = fieldMap.get(fieldName[0]);
            holder.setOutputFromSpinner((Spinner) PDFManager.getViewByName(holder.viewName, thisFrag));
            String[][] fieldArr = holder.getFieldArray();
            PDFManager.setTextFields(acroForm, fieldArr);
        }

        for(String[] fieldName : FormFieldHolder.attendanceSpinners){
            FormFieldHolder holder = fieldMap.get(fieldName[0]);
            holder.setOutputFromSpinner((Spinner) PDFManager.getViewByName(holder.viewName, thisFrag));
            String[][] fieldArr = holder.getFieldArray();
            PDFManager.setTextFields(acroForm, fieldArr);
        }

        FormFieldHolder holder = fieldMap.get(FormFieldHolder.jobTypeSpinnerOptions[0][1]);
        holder.setOutputFromSpinner(jobTypeSpinner);
        PDFManager.setCheckBoxes(acroForm, holder.getFieldArray());

        holder = fieldMap.get(FormFieldHolder.ratingSpinnerOptions[0][1]);
        holder.setOutputFromSpinner(ratingSpinner);
        PDFManager.setCheckBoxes(acroForm, holder.getFieldArray());

        PDFManager.setCheckboxesInLayout(towLay, acroForm);
        PDFManager.setCheckboxesInLayout(dutyLay, acroForm);
    }

    private void goSubmit() {
        Log.w("ReportForm", "goSubmit");
        pHandler.sendMessage(Message.obtain(pHandler, PUSHED_BUTTON));

        final Thread pdfThread = new Thread(){
            @Override
            public void run() {
                String outputPath = Environment.getExternalStorageDirectory().toString() + outputFolder;
                File outDir = new File(outputPath);
                outDir.mkdirs();
                AssetManager assetMan = context.getAssets();
                PDDocument pd = PDFManager.loadPDF(inputPath, assetMan);
                if (pd == null) {
                    Log.e("pdf stuff", "pd is null, returning");
                    return;
                }

                try

                {
                    //Log.w("pdf stuff", "Fields before:");
                    //PDFManager.printFields(pd);
                    PDAcroForm acroForm = pd.getDocumentCatalog().getAcroForm();
                    Log.w("pdf stuff", "StartEdits");
                    pHandler.sendMessage(Message.obtain(pHandler, STARTED_EDITS));
                    editFields(acroForm);
                    Log.w("pdf stuff", "DoneEdits");
                    pHandler.sendMessage(Message.obtain(pHandler, DONE_EDITS));
                    //Log.w("pdf stuff", "Fields after:");
                   //PDFManager.printFields(pd);
                    outFileFull = PDFManager.savePDF(pd, outputPath, outputFileName);
                    pd.close();
                } catch ( IOException ie) {
                    ie.printStackTrace();
                    Log.e("pdf stuff", ie.toString());
                }
                Log.w("pdf stuff", "DoneSave");
                pHandler.sendMessage(Message.obtain(pHandler, DONE_SAVE));
            }
        };
        pdfThread.start();
    }

    private void populateSpinner(Spinner spinner, String[] strings){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_dropdown_item, strings);
        spinner.setAdapter(adapter);
    }


}





