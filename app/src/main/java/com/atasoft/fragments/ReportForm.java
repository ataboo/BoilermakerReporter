package com.atasoft.fragments;

import com.atasoft.boilermakerreporter.*;
import com.atasoft.utils.*;

import android.content.Context;
import android.content.DialogInterface;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

//TODO: Super name not working.  Comments placeholder.  Job Type Checkboxes. Rating Checkboxes.


public class ReportForm extends Fragment implements OnClickListener {

    private HashMap<String, FormFieldHolder> fieldMap;

    private View thisFrag;
    private Context context;
    private AssetManager assetMan;
    private ProgressDialog pBar;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

       View v = inflater.inflate(R.layout.fragment_main, container, false);
        thisFrag = v;
        this.context = v.getContext();
        this.assetMan = context.getAssets();
        setupViews();
        return v;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.pushBootan){
            goPush();
        }

    }

    LinearLayout towLay;
    LinearLayout dutyLay;
    private void setupViews(){
        Button goButton = (Button) thisFrag.findViewById(R.id.pushBootan);
        goButton.setOnClickListener(this);

        if(fieldMap == null) {
            fieldMap = new HashMap<String, FormFieldHolder>();
            for(String[] fieldName: FormFieldHolder.fieldNameEdits) {
                fieldMap.put(fieldName[0],
                        new FormFieldHolder(fieldName[0], fieldName[1]));
            }
            for(String[] rating: FormFieldHolder.ratingSpinners){
                fieldMap.put(rating[0],
                        new FormFieldHolder(rating[1], rating[0], 5));
            }
            for(String[] rating: FormFieldHolder.attendanceSpinners){
                fieldMap.put(rating[0],
                        new FormFieldHolder(rating[1], rating[0], 3));
            }
        }
        pBar = new ProgressDialog(context);

        this.towLay = (LinearLayout) thisFrag.findViewById(R.id.towCheckLay);
        this.dutyLay = (LinearLayout) thisFrag.findViewById(R.id.dutyCheckLay);
        populateChecks(thisFrag, towLay, dutyLay, FormFieldHolder.checkNames);

    }

    private static void populateChecks(View thisFrag, LinearLayout towLay, LinearLayout dutyLay, String[][] names){
        if(towLay == null || dutyLay == null){
            Log.e("ReportForm Fragment", "One of the LinearLayouts was null.  Aborted populating with checks.");
            return;
        }
        for(int i=0; i<names.length; i++){
            LinearLayout.LayoutParams checkWrap = new LinearLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            CheckBox check = new CheckBox(thisFrag.getContext());
            check.setLayoutParams(checkWrap);
            check.setTag(names[i][0]);
            check.setText(names[i][1]);
            if(i<14){
                towLay.addView(check);
            } else {
                dutyLay.addView(check);
            }
        }
    }

    private void goEdits(PDAcroForm acroForm) throws IOException{
        for(String[] fieldName : FormFieldHolder.fieldNameEdits){
            FormFieldHolder holder = fieldMap.get(fieldName[0]);
            holder.setOutputFromEdit((EditText) getViewByName(holder.viewName));
            PDFManager.setField(acroForm, fieldName[0], holder.outputString);
        }

        for(String[] fieldName : FormFieldHolder.ratingSpinners){
            FormFieldHolder holder = fieldMap.get(fieldName[0]);
            holder.setOutputFromSpinner((Spinner) getViewByName(holder.viewName));
            String[][] fieldArr = holder.getFieldArray();
            PDFManager.setFields(acroForm, fieldArr);
        }

        for(String[] fieldName : FormFieldHolder.attendanceSpinners){
            FormFieldHolder holder = fieldMap.get(fieldName[0]);
            holder.setOutputFromSpinner((Spinner) getViewByName(holder.viewName));
            String[][] fieldArr = holder.getFieldArray();
            PDFManager.setFields(acroForm, fieldArr);
        }



        setFieldsFromChildren(towLay, acroForm);
        setFieldsFromChildren(dutyLay, acroForm);
    }

    private void setFieldsFromChildren(LinearLayout parentLay, PDAcroForm acroForm) throws IOException{
        //0 is the title, 1 is the pagebreak
        for(int i=0; i<parentLay.getChildCount(); i++){
            View child = parentLay.getChildAt(i);
            if(child instanceof CheckBox){
                PDFManager.setCheckBox(acroForm, parentLay.getChildAt(i).getTag().toString(),
                        ((CheckBox) child).isChecked());
            }
        }
    }

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

    public static final int PUSHED_BUTTON = 0;
    public static final int STARTED_EDITS = 1;
    public static final int DONE_EDITS = 2;
    public static final int DONE_SAVE = 3;
    private String outFileFull = "";
    private void goPush() {
        Log.w("pdf stuff", "Go Push");
        pHandler.sendMessage(Message.obtain(pHandler, PUSHED_BUTTON));

        final Thread pdfThread = new Thread(){

            @Override
            public void run() {
                String root = Environment.getExternalStorageDirectory().toString();
                String inputPath = "Files/ApprenticeReport.pdf";
                String outputPath = root + "/Documents";
                File outDir = new File(outputPath);
                outDir.mkdirs();
                String outputFile = "Report";
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
                    goEdits(acroForm);
                    Log.w("pdf stuff", "DoneEdits");
                    pHandler.sendMessage(Message.obtain(pHandler, DONE_EDITS));
                    //Log.w("pdf stuff", "Fields after:");
                   //PDFManager.printFields(pd);
                    outFileFull = PDFManager.savePDF(pd, outputPath, outputFile);
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

    private void toaster(String text){
        Toast.makeText(context, text, Toast.LENGTH_SHORT);
    }

    private View getViewByName(String name){
        int resId = getResources().getIdentifier(name, "id", context.getPackageName());
        return thisFrag.findViewById(resId);
    }
}





