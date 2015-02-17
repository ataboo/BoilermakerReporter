package com.atasoft.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.atasoft.boilermakerreporter.R;
import com.atasoft.utils.FormFieldHolder;
import com.atasoft.utils.PDFManager;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class ApprenticeReportFrag extends Fragment implements OnClickListener {

    private static final String inputPath = "Files/ApprenticeReportAppr.pdf";
    private static final String outputFileName = "ApprReport_Apprentice";
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
    private Spinner jobTypeSpinner;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

       View v = inflater.inflate(R.layout.appr_report, container, false);
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
        if(v.getId() == R.id.appr_pushBootan){
            goSubmit();
        }
    }

    private void setupViews(){
        Button goButton = (Button) thisFrag.findViewById(R.id.appr_pushBootan);
        goButton.setOnClickListener(this);

        pBar = new ProgressDialog(context);

        //fieldMap stores the non-checkbox fields as <(String) fieldName from pdf, (FormFieldHolder)>
        if(fieldMap == null) {
            fieldMap = new HashMap<String, FormFieldHolder>();
            for(String[] fieldName: fieldNameEdits) {
                fieldMap.put(fieldName[1],
                        new FormFieldHolder(fieldName[1], fieldName[0]));
            }

            //already populated
            for(String[] arr : ratingSpinners){
                fieldMap.put(arr[1], new FormFieldHolder(arr[1], arr[0], 5));
            }
            for(String[] arr : attendSpinners){
                fieldMap.put(arr[1], new FormFieldHolder(arr[1], arr[0], 5));
            }
            fieldMap.put(jobSpinnerOptions[0][1],
                    new FormFieldHolder(jobSpinnerOptions[0][1], jobSpinnerOptions[0][0]));
        }
        this.jobTypeSpinner = (Spinner) thisFrag.findViewById(R.id.appr_jobTypeSpinner);

        this.towLay = (LinearLayout) thisFrag.findViewById(R.id.appr_towCheckLay);
        PDFManager.populateChecks(thisFrag, towLay, towChecks);
        this.dutyLay = (LinearLayout) thisFrag.findViewById(R.id.appr_dutyCheckLay);
        PDFManager.populateChecks(thisFrag, dutyLay, dutyChecks);
    }

    private void editFields(PDAcroForm acroForm) throws IOException{
        for(String[] fieldName : fieldNameEdits){
            FormFieldHolder holder = fieldMap.get(fieldName[1]);
            holder.setOutputFromEdit((EditText) PDFManager.getViewByName(holder.viewName, thisFrag));
            PDFManager.setTextField(acroForm, fieldName[1], holder.outputString);
        }
        for(String[] arr: ratingSpinners){
            processRatingSpinner(arr[1], (Spinner) PDFManager.getViewByName(arr[0], thisFrag), acroForm);
        }
        for(String[] arr: attendSpinners){
            processCheckSpinner(arr[1], (Spinner) PDFManager.getViewByName(arr[0], thisFrag), acroForm);
        }
        processCheckSpinner(jobSpinnerOptions[0][1], jobTypeSpinner, acroForm);

        PDFManager.setCheckboxesInLayout(towLay, acroForm);
        PDFManager.setCheckboxesInLayout(dutyLay, acroForm);
    }



    private void goSubmit() {
        Log.w("ApprenticeReportFrag", "goSubmit");
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
                    Log.e("ApprenticeReportFrag", "pd is null, returning");
                    return;
                }

                try

                {
                    //Log.w("pdf stuff", "Fields before:");
                    //PDFManager.printFields(pd);
                    PDAcroForm acroForm = pd.getDocumentCatalog().getAcroForm();
                    Log.w("ApprenticeReportFrag", "StartEdits");
                    pHandler.sendMessage(Message.obtain(pHandler, STARTED_EDITS));
                    editFields(acroForm);
                    Log.w("ApprenticeReportFrag", "DoneEdits");
                    pHandler.sendMessage(Message.obtain(pHandler, DONE_EDITS));
                    //Log.w("pdf stuff", "Fields after:");
                   //PDFManager.printFields(pd);
                    outFileFull = PDFManager.savePDF(pd, outputPath, outputFileName);
                    pd.close();
                } catch ( IOException ie) {
                    ie.printStackTrace();
                    Log.e("ApprenticeReportFrag", ie.toString());
                }
                Log.w("ApprenticeReportFrag", "DoneSave");
                pHandler.sendMessage(Message.obtain(pHandler, DONE_SAVE));
            }
        };
        pdfThread.start();
    }

    private void processCheckSpinner(String fieldName, Spinner spinner, PDAcroForm acroForm) throws IOException {
        FormFieldHolder holder = fieldMap.get(fieldName);
        holder.setOutputFromSpinner(spinner);
        PDFManager.setCheckBoxes(acroForm, holder.getFieldArray());
    }

    private void processRatingSpinner(String fieldName, Spinner spinner, PDAcroForm acroForm) throws IOException{
        FormFieldHolder holder = fieldMap.get(fieldName);
        holder.setOutputFromSpinner(spinner);
        PDFManager.setTextField(acroForm, fieldName, Integer.toString(5 - holder.selectedIndex));
    }

    //<editor-fold desc="Static String Arrays">
    //CheckBox Toggle Fields [Form Checkbox Name, Display String]
    public static final String[][] towChecks = {
            //Type of Work
            {"towAtomic", "Atomic Rad Work"},
            {"towPlate", "Plate-Work"},
            {"towBoilers", "Boilers"},
            {"towConds", "Condensers/Evaporators"},
            {"towFurnaces", "Furnaces"},
            {"towExchangers", "Heat Exchangers"},
            {"towPollution", "Pollution Control"},
            {"towHydro", "Hydroelectric"},
            {"towTanks", "Tanks"},
            {"towTowers", "Towers"}};

    public static final String[][] dutyChecks = {
            {"dutyBurning", "Burning"},
            {"dutyWatch", "Confined Space Watch"},
            {"dutyExpanding", "Expanding"},
            {"dutyGlass", "Fibreglass"},
            {"dutyGrinding", "Grinding"},
            {"dutyLayout", "Layout"},
            {"dutyMetalizing", "Metalizing"},
            {"dutyReading", "Reading Drawings"},
            {"dutyRigging", "Rigging"},
            {"dutyTack", "Tack Welding"}};

    //Edit Text fields.  [EditText name, PDF form name]
    public static final String[][] fieldNameEdits = {
            {"appr_appNameEdit", "aprNameText"},
            {"appr_appNameEdit", "aprNameText2"},
            {"appr_regNumEdit", "regNumText"},
            {"appr_hoursEdit", "hoursText"},
            {"appr_localNumEdit", "localText"},
            {"appr_empNameEdit", "empNameText"},
            {"appr_jobLocationEdit", "jobLocText"},
            {"appr_jobStewardEdit", "jobStewardText"},
            {"appr_currentDateEdit", "curDateText"},
            {"appr_currentDateEdit", "curDateText2"},
            {"appr_jobStartEdit", "jobStartText"},
            {"appr_jobEndEdit", "jobEndText"},
            {"appr_phoneEdit", "phoneText"},
            {"appr_commentsEdit", "commentsText"}};
    //[SpinnerName, fieldName(1-4 convention)]
    //pre-filled [very good, good, average, poor]
    public static final String[][] ratingSpinners = {
            {"appr_appraiseSpinner", "appraiseBox"},
            {"appr_relationSpinner", "relationBox"},
            {"appr_superRateSpinner", "superBox"},
            {"appr_journeySpinner", "journeyBox"}};
    //pre-filled [yes, no, often, rarely, never]
    public static final String[][] attendSpinners = {
            {"appr_lateSpinner", "late"},
            {"appr_absentSpinner", "absent"}};
    //CheckBox Spinner Fields.  [Spinner View name, PDF box1, displayname 1, PDF box2...]
    public static final String[][] jobSpinnerOptions = {
            {"projTypeSpinner", "projType"},
            {"Construction","Maintenance","Demolition", "Shop"}};
    //</editor-fold>

}





