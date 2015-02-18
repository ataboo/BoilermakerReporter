package com.atasoft.fragments;

import com.atasoft.boilermakerreporter.*;
import com.atasoft.utils.*;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.*;
import android.support.annotation.Nullable;
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

//Superclass for StewardReportFrag as well
public class SuperReportFrag extends Fragment implements OnClickListener {

    private static final String superInputPath = "Files/ApprenticeReportSuper.pdf";
    public static final String superOutputFileName = "ApprReportSuper";
    private static final String stewardInputPath = "Files/ApprenticeReportSteward.pdf";
    public static final String stewardOutputFileName = "ApprReportSteward";

    private String inputPath = superInputPath;
    private String outputFileName = superOutputFileName;


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

       View v = inflater.inflate(R.layout.super_report, container, false);
        //thisFrag = getView();
        //this.context = v.getContext();
        //setupViews();
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.thisFrag = getView();
        this.context = thisFrag.getContext();
        setupViews();
        super.onViewCreated(view, savedInstanceState);
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

    public void setupViews(){
        Button goButton = (Button) thisFrag.findViewById(R.id.pushBootan);
        goButton.setOnClickListener(this);

        pBar = new ProgressDialog(context);

        //fieldMap stores the non-checkbox fields as <(String) fieldName from pdf, (FormFieldHolder)>
        if(fieldMap == null) {
            fieldMap = new HashMap<String, FormFieldHolder>();
            for(String[] fieldName: fieldNameEdits) {
                fieldMap.put(fieldName[1],
                        new FormFieldHolder(fieldName[1], fieldName[0]));
            }
            for(String[] fieldName: ratingCommentFieldNames){
                fieldMap.put(fieldName[1],
                        new FormFieldHolder(fieldName[1], fieldName[0]));
            }

            for(String[] rating: ratingSpinners){
                fieldMap.put(rating[0],
                        new FormFieldHolder(rating[1], rating[0], 5));
            }
            for(String[] rating: attendanceSpinners){
                fieldMap.put(rating[0],
                        new FormFieldHolder(rating[1], rating[0], 3));
            }

            this.jobTypeSpinner = (Spinner) thisFrag.findViewById(R.id.jobTypeSpinner);

            fieldMap.put(jobTypeSpinnerOptions[0][1],
                    new FormFieldHolder(jobTypeSpinnerOptions[0][1],
                            jobTypeSpinnerOptions[0][0],
                            jobTypeSpinnerOptions[1].length));
        }

        this.towLay = (LinearLayout) thisFrag.findViewById(R.id.towCheckLay);
        PDFManager.populateWithEdits(thisFrag, towLay, towChecks);
        this.dutyLay = (LinearLayout) thisFrag.findViewById(R.id.dutyCheckLay);
        PDFManager.populateChecks(thisFrag, dutyLay, dutyChecks);
    }

    public void setToSteward(boolean setSteward){
        TextView headerTitle = (TextView) thisFrag.findViewById(R.id.reportHeaderLabel);
        if(headerTitle == null){
            Log.e("SuperReportFrag", "headerTitle was null when trying to setSteward.");
            return;
        }

        int headerID = (setSteward) ? R.string.report_header_steward : R.string.super_header_label;
        headerTitle.setText(getResources().getString(headerID));
        outputFileName = setSteward ? stewardOutputFileName : superOutputFileName;
        inputPath = setSteward ? stewardInputPath : superInputPath;
    }

    //Lot of dirty laundry here but PDFManager is getting busy
    public void loadPDFtoViews(PDDocument pdRead){
        PDAcroForm acroForm = pdRead.getDocumentCatalog().getAcroForm();
        PDFManager.setEditsLinLayFromFile(acroForm, towLay);
        PDFManager.setCheckViewsFromFile(acroForm, dutyLay);
        for(String[] fieldArr : fieldNameEdits){
            PDFManager.setEditViewFromFile(PDFManager.getTextFieldValue(acroForm, fieldArr[1]),
                    (EditText) PDFManager.getViewByName(fieldArr[0], thisFrag));
        }
        for(String[] fieldArr : ratingCommentFieldNames){
            PDFManager.setEditViewFromFile(PDFManager.getTextFieldValue(acroForm, fieldArr[1]),
                    (EditText) PDFManager.getViewByName(fieldArr[0],thisFrag));
        }

        PDFManager.setSpinnerCheckFromFile(acroForm, jobTypeSpinnerOptions[0], thisFrag);

        for(String[] fieldArr: attendanceSpinners){
            PDFManager.setSpinnerCheckFromFile(acroForm, fieldArr, thisFrag);
        }

        for(String[] fieldArr: ratingSpinners){
            PDFManager.setSpinnerTextFromFile(acroForm, fieldArr, thisFrag);
        }
    }



    private void editFields(PDAcroForm acroForm) throws IOException{

        for(String[] fieldName : fieldNameEdits){
            FormFieldHolder holder = fieldMap.get(fieldName[1]);
            holder.setOutputFromEdit((EditText) PDFManager.getViewByName(holder.viewName, thisFrag));
            PDFManager.setTextField(acroForm, fieldName[1], holder.outputString);
        }
        for(String[] fieldName : ratingCommentFieldNames){
            FormFieldHolder holder = fieldMap.get(fieldName[1]);
            holder.setOutputFromEdit((EditText) PDFManager.getViewByName(holder.viewName, thisFrag));
            PDFManager.setTextField(acroForm, fieldName[1], holder.outputString);
        }

        for(String[] fieldName : ratingSpinners){
            FormFieldHolder holder = fieldMap.get(fieldName[0]);
            holder.setOutputFromSpinner((Spinner) PDFManager.getViewByName(holder.viewName, thisFrag));
            PDFManager.setTextField(acroForm, holder.fieldName, Integer.toString(5 - holder.selectedIndex));
        }

        for(String[] fieldName : attendanceSpinners){
            FormFieldHolder holder = fieldMap.get(fieldName[0]);
            holder.setOutputFromSpinner((Spinner) PDFManager.getViewByName(holder.viewName, thisFrag));
            String[][] fieldArr = holder.getFieldArray();
            PDFManager.setCheckBoxes(acroForm, fieldArr);
        }

        FormFieldHolder holder = fieldMap.get(jobTypeSpinnerOptions[0][1]);
        holder.setOutputFromSpinner(jobTypeSpinner);
        PDFManager.setCheckBoxes(acroForm, holder.getFieldArray());

        PDFManager.setEditsInLayout(towLay, acroForm);
        PDFManager.setCheckboxesInLayout(dutyLay, acroForm);
    }

    private void goSubmit() {
        Log.w("ReportForm", "goSubmit");
        pHandler.sendMessage(Message.obtain(pHandler, PUSHED_BUTTON));

        final Thread pdfThread = new Thread(){
            @Override
            public void run() {
                String outputPath = Environment.getExternalStorageDirectory().toString() + PDFManager.outputFolder;
                File outDir = new File(outputPath);
                outDir.mkdirs();
                AssetManager assetMan = context.getAssets();
                PDDocument pd = PDFManager.loadPDF(inputPath, assetMan);
                if (pd == null) {
                    Log.e("SuperReportFrag", "pd is null, returning");
                    return;
                }

                try

                {
                    PDAcroForm acroForm = pd.getDocumentCatalog().getAcroForm();
                    Log.w("SuperReportFrag", "StartEdits");
                    pHandler.sendMessage(Message.obtain(pHandler, STARTED_EDITS));
                    editFields(acroForm);
                    Log.w("SuperReportFrag", "DoneEdits");
                    pHandler.sendMessage(Message.obtain(pHandler, DONE_EDITS));
                    outFileFull = PDFManager.savePDF(pd, outputFileName);
                    pd.close();
                } catch ( IOException ie) {
                    ie.printStackTrace();
                    Log.e("SuperReportFrag", ie.toString());
                }
                Log.w("SuperReportFrag", "DoneSave");
                pHandler.sendMessage(Message.obtain(pHandler, DONE_SAVE));
            }
        };
        pdfThread.start();
    }

    private void populateSpinner(Spinner spinner, String[] strings){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity().getApplicationContext(),
                android.R.layout.simple_spinner_dropdown_item, strings);
        spinner.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    //make these with ctrl-alt-T
    //<editor-fold desc="Static fieldStrings">
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
            {"appNameEdit", "aprNameText"},
            {"empNumberEdit", "empNumText"},
            {"localNumEdit", "localText"},
            {"empNameEdit", "empNameText"},
            {"jobLocationEdit", "jobLocText"},
            {"jobStewardEdit", "jobStewardText"},
            {"currentDateEdit", "curDateText"},
            {"jobStartEdit", "jobStartText"},
            {"jobEndEdit", "jobEndText"},
            {"absentEdit", "absentText"},
            {"lateEdit", "lateText"},
            {"superNameEdit", "superNameText"},
            {"commentsEdit", "commentsText"}};
    //[EditText name, PDFField name]
    public static final String[][] ratingCommentFieldNames = {
            {"safetyComsEdit", "safeAttTextCom"},
            {"workerComsEdit", "workAttTextCom"},
            {"jobComsEdit", "jobAttTextCom"},
            {"initComsEdit","initTextCom"},
            {"capComsEdit","capTextCom"},
            {"ratingComsEdit","ratingTextCom"}};

    //CheckBox Spinner Fields.  [Spinner View name, PDF box1, displayname 1, PDF box2...]
    public static final String[][] jobTypeSpinnerOptions = {
            {"jobTypeSpinner", "projType"},
            {"Construction","Maintenance","Demolition", "Shop"}};

    //Rating Excellent, Above Average, Average, Below Average, Unsatisfactory [Spinner View name, textfield name 1-5]
    public static final String[][] ratingSpinners = {
            {"safetySpinner", "safeAttText"},
            {"workerSpinner", "workAttText"},
            {"jobSpinner", "jobAttText"},
            {"initSpinner", "initText"},
            {"capSpinner", "capText"},
            {"ratingSpinner", "ratingText"}};

    //Rating 1-3
    public static final String[][] attendanceSpinners = {
            {"absentSpinner", "absent"},
            {"lateSpinner", "late"}};
    //</editor-fold>
}





