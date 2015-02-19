package com.atasoft.boilermakerreporter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.*;
import android.support.v4.app.*;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.atasoft.fragments.*;
import com.atasoft.utils.PDFManager;

import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;

//TODO: email intent, apprentice open file, switch over to onViewCreated for view setups


public class MainActivity extends ActionBarActivity implements ActionBar.OnNavigationListener {

    public static final int SUPER_LAUNCH = PDFManager.SUPER_LAUNCH;
    public static final int APPRENTICE_LAUNCH = PDFManager.APPRENTICE_LAUNCH;
    public static final int STEWARD_LAUNCH = PDFManager.STEWARD_LAUNCH;

    private SuperReportFrag superFrag;
    private ApprenticeReportFrag apprenticeFrag;
    private StewardReportFrag stewardFrag;
    private ActionBar actionBar;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        int launchMode = getIntent().getIntExtra("launch_mode", SUPER_LAUNCH);
        Log.w("MainActivity", "Launch Mode:" + launchMode);

        setContentView(R.layout.activity_main);
        //check both just for giggles
        if(superFrag == null){
            superFrag = new SuperReportFrag();
            apprenticeFrag = new ApprenticeReportFrag();
            stewardFrag = new StewardReportFrag();
            this.actionBar = getSupportActionBar();
            if(actionBar!=null) initActionBar(launchMode);
        }
        swapFrag(launchMode);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            /*
            case R.id.action_settings:
                //openSettings();
                return true;
            */
            case R.id.action_about:
                openAbout();
                return true;
            case R.id.action_openFile:
                openFileDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        swapFrag(itemPosition);
        return true;
    }

    private void initActionBar(int launchMode){
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(actionBar.getThemedContext(),
                android.R.layout.simple_list_item_1, android.R.id.text1,new String[]{"Supervisor Report", "Apprentice Report", "Job Steward Report"});
        actionBar.setListNavigationCallbacks(adapter, this);
        actionBar.setSelectedNavigationItem(launchMode);
    }

    private void openAbout(){
        Intent intent = new Intent(this, AboutPage.class);
        startActivity(intent);
    }

    private void swapFrag(int launchMode) {
        Fragment activeFrag = superFrag;
        switch (launchMode) {
            case SUPER_LAUNCH:
                activeFrag = superFrag;
                break;
            case APPRENTICE_LAUNCH:
                activeFrag = apprenticeFrag;
                break;
            case STEWARD_LAUNCH:
                activeFrag = stewardFrag;
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.container, activeFrag).commit();
    }

    //if a frage isn't done onViewCreated() it will return false on loadPDFtoViews
    //The PD is added to queue and getPDFLoadQueued() is called by the fragment once it's done loading
    private void loadFileToFragment(File pdfFile){
        PDDocument pdDoc = PDFManager.loadPDFFile(pdfFile);
        if(pdfFile == null){
            String failToast = pdfFile.getName() + " is not valid or was not made with Reporter.";
            Toast.makeText(this, failToast, Toast.LENGTH_LONG).show();
            Log.w("MainActivity", failToast);
        }
        int reportType = PDFManager.getReportType(pdDoc, pdfFile.getName());

        switch (reportType){
            case SUPER_LAUNCH:
                swapFrag(SUPER_LAUNCH);
                if(!superFrag.loadPDFtoViews(pdDoc)) addPDToQueue(pdDoc, reportType);
                break;
            case APPRENTICE_LAUNCH:
                swapFrag(APPRENTICE_LAUNCH);
                if(!apprenticeFrag.loadPDFtoViews(pdDoc)) addPDToQueue(pdDoc, reportType);
                break;
            case STEWARD_LAUNCH:
                swapFrag(STEWARD_LAUNCH);
                if(!stewardFrag.loadPDFtoViews(pdDoc)) addPDToQueue(pdDoc, reportType);
                break;
            default:
                return;
        }
    }

    private PDDocument queuedPD = null;
    private int queuedCode = -1;
    private void addPDToQueue(PDDocument pdDoc, int reportType) {
            this.queuedCode = reportType;
            this.queuedPD = pdDoc;
    }

    public PDDocument getPDFLoadQueued(int typeCode){
        if(typeCode != queuedCode) return null;
        PDDocument returnPD = queuedPD;
        queuedPD = null;
        queuedCode = -1;
        Log.w("MainActivity", "returnPD is null: " + (returnPD==null));
        return returnPD;
    }

    public void openFileDialog(){
        final AlertDialog.Builder dBuilder = new AlertDialog.Builder(this);
        dBuilder.setTitle("Open Report:");

        final File[] fileArr = PDFManager.getSavedFiles();
        String[] fileStrings = new String[fileArr.length];

        for(int i=0; i<fileArr.length; i++){
            fileStrings[i] = fileArr[i].getName();
        }
        if (fileStrings.length > 0) {
            dBuilder.setItems(fileStrings, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    loadFileToFragment(fileArr[which]);
                    dialog.dismiss();
                }
            });
        } else {
            dBuilder.setMessage("No previous PDF report files found in:\n" + PDFManager.outputPath);
        }

        dBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dBuilder.create().show();
    }
}

