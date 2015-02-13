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




public class ReportForm extends Fragment implements OnClickListener {
    public class FormFieldHolder {
        public String outputString;
        public String name;
        public String editTextName;
        public FormFieldHolder(String fieldName, String editTextName){
            this.name = fieldName;
            this.editTextName = editTextName;
            this.outputString = " ";
        }

        public void setOutputFromEdit(){
            int resId = getResources().getIdentifier(editTextName, "id", context.getPackageName());
            EditText eText = (EditText) thisFrag.findViewById(resId);
            if(eText == null) return;
            String outStr = eText.getText().toString();
            if (outStr.matches("")) outStr = " ";

            //sinText 1, 2 , 3 are seperated
            if(name.contains("sinText")) {
                if(outStr.length() != 9) return;
                int sinInt = Integer.parseInt(name.substring(7));
                switch(sinInt){
                    case 1:
                        outStr = outStr.substring(0,3);
                        break;
                    case 2:
                        outStr = outStr.substring(3,6);
                        break;
                    default:
                        outStr = outStr.substring(6);
                        break;
                }
            }
            this.outputString = outStr;
        }
    }

    //Edit Text fields.  [PDF form name, EditText View name]
    private static final String[][] fieldNameEdits = {
            {"aprNameText","appNameEdit"},
            {"sinText1","sinNumberEdit"},
            {"sinText2","sinNumberEdit"},
            {"sinText3","sinNumberEdit"},
            {"empNameText","empNameEdit"},
            {"jobLocText","jobLocationEdit"},
            {"jobStewardText","jobStewardEdit"},
            {"curDateText","currentDateEdit"},
            {"jobStartText","jobStartEdit"},
            {"jobEndText","jobEndEdit"}};
    //CheckBox Spinner Fields.  [Spinner View name, PDFForm option name1, PDFForm option displayname 1, ...]
    private static final String[][] fieldNameSpinners = {
            {"projTypeSpinner", "projConstBox", "Construction", "projMaintBox", "Maintenance", "projDemoBox", "Demolition", "projShopBox", "Shop"},
            {}};
    //CheckBox Toggle Fields [Form Checkbox Name, Display String]
    private static final String[][] toggleNameSpinners = {
            //Type of Work
            {"towAtomic", "Atomic Rad Work"},
            {"towBins", "Bins & Hoppers"},
            {"towBoilers", "Boilers"},
            {"towConds", "Condensers/Evaporators"},
            {"towFurnaces", "Furnaces"},
            {"towExchangers", "Heat Exchangers"},
            {"towPenstock", "Penstock"},
            {"towPrecips", "Precipitators"},
            {"towVessel", "Pressure Vessel"},
            {"towScroll", "Scroll Casings"},
            {"towScrubbers", "Scrubbers"},
            {"towStacks", "Stacks & Breeching"},
            {"towTanks", "Tanks"},
            {"towTowers", "Towers"},
            //Duties
            {"dutyOHS", "Adhere to OH & S"},
            {"dutyBurning", "Burning"},
            {"dutyWatch", "Confined Space Watch"},
            {"dutyExpanding", "Expanding"},
            {"dutyGlass", "Fibreglass"},
            {"dutyFitting", "Fitting"},
            {"dutyGrinding", "Grinding"},
            {"dutyLayout", "Layout"},
            {"dutyMetalizing", "Metalizing"},
            {"dutyReading", "Reading Drawings"},
            {"dutyRigging", "Rigging"},
            {"dutySpark", "Spark Watch"},
            {"dutyTack", "Tack Welding"},
            {"dutyTray", "Tray Work"}};

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


    private void setupViews(){
        Button goButton = (Button) thisFrag.findViewById(R.id.pushBootan);
        goButton.setOnClickListener(this);

        if(fieldMap == null) {
            fieldMap = new HashMap<String, FormFieldHolder>();
            for (int i = 0; i < fieldNameEdits.length; i++) {
                fieldMap.put(fieldNameEdits[i][0],
                        new FormFieldHolder(fieldNameEdits[i][0], fieldNameEdits[i][1]));
                //Log.w("pdf stuff", "Added " + fieldNameStrings[i][0] + " to location " + i);
            }
        }
        pBar = new ProgressDialog(context);

    }

    private void goEdits(PDAcroForm acroForm) throws IOException{
        for(int i=0; i<fieldNameEdits.length; i++){
            FormFieldHolder holder = fieldMap.get(fieldNameEdits[i][0]);
            holder.setOutputFromEdit();
            PDFManager.setField(acroForm, fieldNameEdits[i][0], holder.outputString);
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
}





