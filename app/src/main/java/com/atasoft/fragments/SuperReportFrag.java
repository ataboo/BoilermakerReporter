package com.atasoft.fragments;

import com.atasoft.boilermakerreporter.*;
import com.atasoft.utils.*;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.*;
import android.preference.PreferenceManager;
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
    private SharedPreferences prefs;
    private MainActivity parentActivity;
    private ProgressDialog pBar;
    private LinearLayout towLay;
    private LinearLayout dutyLay;
    private Spinner ratingSpinner;
    private Spinner jobTypeSpinner;
    public int typeCode = PDFManager.SUPER_LAUNCH;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.super_report, container, false);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews();
        //should be null for first run
    }

    @Override
    public void onResume() {
        super.onResume();
        setViewsFromPrefs();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        setPrefsFromViews();
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
        if(v.getId() == R.id.clearBootan){
            clearViews();
        }
    }

    public void setupViews(){
        this.thisFrag = getView();
        this.context = thisFrag.getContext();
        this.parentActivity = (MainActivity) getActivity();
        this.prefs = PreferenceManager.getDefaultSharedPreferences(context);

        Button goButton = (Button) thisFrag.findViewById(R.id.pushBootan);
        goButton.setOnClickListener(this);
        Button clearButton = (Button) thisFrag.findViewById(R.id.clearBootan);
        clearButton.setOnClickListener(this);

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
        PDFManager.populateLinWithEdits(thisFrag, towLay, towEdits);
        this.dutyLay = (LinearLayout) thisFrag.findViewById(R.id.dutyCheckLay);
        PDFManager.populateCheckViewsFromFormLin(thisFrag, dutyLay, dutyChecks);

        //If this wasn't they active frag when loadPDFFromFile was called, MainActivity added the
        //PDDoc to a queue.
        PDDocument pdRead = parentActivity.getPDFLoadQueued(typeCode);
        if(pdRead != null) loadPDFtoViews(pdRead);

    }

    private void setViewsFromPrefs(){
        String prefix = Integer.toString(typeCode);
        RelativeLayout relLay = (RelativeLayout) thisFrag.findViewById(R.id.masterRelLay);
        PDFManager.powerLoad(prefs, relLay, prefix);
    }

    private void setPrefsFromViews(){
        String prefix = Integer.toString(typeCode);
        RelativeLayout relLay = (RelativeLayout) thisFrag.findViewById(R.id.masterRelLay);
        SharedPreferences.Editor editor = prefs.edit();
        PDFManager.powerSave(editor, relLay, prefix);
        editor.commit();
    }

    private void clearViews(){
        prefs.edit().clear().commit();
        setViewsFromPrefs();
    }

    //Called by subclass StewardReportFrag
    public void setToSteward(boolean setSteward){
        TextView headerTitle = (TextView) thisFrag.findViewById(R.id.reportHeaderLabel);
        if(headerTitle == null){
            Log.e("SuperReportFrag", "headerTitle was null when trying to setSteward.");
            return;
        }

        int headerID = (setSteward) ? R.string.report_header_steward : R.string.super_header_label;
        headerTitle.setText(getResources().getString(headerID));
        this.outputFileName = setSteward ? stewardOutputFileName : superOutputFileName;
        this.inputPath = setSteward ? stewardInputPath : superInputPath;
        this.typeCode = setSteward ? PDFManager.STEWARD_LAUNCH : PDFManager.SUPER_LAUNCH;
    }

    //if loadPDFtoViews is called when this isn't the active Fragment this will return false
    //and the pdf will be added to a queue.  setupViews() checks for queued documents when done
    // and it will check the queue then.
    public boolean loadPDFtoViews(PDDocument pdRead){
        if(getView() == null) return false;

        PDAcroForm acroForm = pdRead.getDocumentCatalog().getAcroForm();
        PDFManager.setEditsFromFormLin(acroForm, towLay);
        PDFManager.setCheckViewsFromFormLin(acroForm, dutyLay);
        for(String[] fieldArr : fieldNameEdits){
            PDFManager.setEditView((EditText) PDFManager.getViewByName(fieldArr[0], thisFrag),
                    PDFManager.getTextFieldValue(acroForm, fieldArr[1]));
        }
        for(String[] fieldArr : ratingCommentFieldNames){
            PDFManager.setEditView((EditText) PDFManager.getViewByName(fieldArr[0], thisFrag),
                    PDFManager.getTextFieldValue(acroForm, fieldArr[1]));
        }

        PDFManager.setSpinnerCheckFromFile(acroForm, jobTypeSpinnerOptions[0], thisFrag);

        for(String[] fieldArr: attendanceSpinners){
            PDFManager.setSpinnerCheckFromFile(acroForm, fieldArr, thisFrag);
        }

        for(String[] fieldArr: ratingSpinners){
            PDFManager.setSpinnerTextFromFile(acroForm, fieldArr, thisFrag);
        }
        try{
            pdRead.close();
        } catch(IOException ie){
            ie.printStackTrace();
        }
        return true;
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
            PDFManager.setFormCheckBoxes(acroForm, fieldArr);
        }

        FormFieldHolder holder = fieldMap.get(jobTypeSpinnerOptions[0][1]);
        holder.setOutputFromSpinner(jobTypeSpinner);
        PDFManager.setFormCheckBoxes(acroForm, holder.getFieldArray());

        PDFManager.setFormFromEditsLin(towLay, acroForm);
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
                PDDocument pd = PDFManager.loadPDFFile(inputPath, assetMan);
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
                    outFileFull = PDFManager.savePDFFile(pd, outputFileName);
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

    //make these with ctrl-alt-T
    //<editor-fold desc="Static fieldStrings">
    //CheckBox Toggle Fields [Form Checkbox Name, Display String]
    public static final String[][] towEdits = {
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





