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
import android.widget.ListView;
import android.widget.Toast;

import com.atasoft.fragments.*;
import com.atasoft.utils.PDFManager;

//TODO: email intent, read existing file


public class MainActivity extends ActionBarActivity implements ActionBar.OnNavigationListener {

    public static final int SUPER_LAUNCH = PDFManager.SUPER_LAUNCH;
    public static final int APPRENTICE_LAUNCH = PDFManager.APPRENTICE_LAUNCH;
    public static final int STEWARD_LAUNCH = PDFManager.STEWARD_LAUNCH;

    private Fragment superFrag;
    private Fragment apprenticeFrag;
    private Fragment stewardFrag;
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

    private void initActionBar(int launchMode){
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(actionBar.getThemedContext(),
                android.R.layout.simple_list_item_1, android.R.id.text1,new String[]{"Supervisor Report", "Apprentice Report", "Job Steward Report"});
        actionBar.setListNavigationCallbacks(adapter, this);
        actionBar.setSelectedNavigationItem(launchMode);
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

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        swapFrag(itemPosition);
        return true;
    }

    public void openFileDialog(){
        final AlertDialog.Builder dBuilder = new AlertDialog.Builder(this);
        dBuilder.setTitle("Open Report:");
        String[][] fileStrings = PDFManager.getSavedFiles(getAssets());
        Log.w("MainActivity", fileStrings.length + ",  " + fileStrings[0].length);
        String[] itemStrings = new String[fileStrings[0].length];
        for(int i=0; i<itemStrings.length; i++){
            itemStrings[i] = fileStrings[0][i] + " - " + fileStrings[1][i];
        }
        if (itemStrings.length > 0) {
            dBuilder.setItems(itemStrings, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        } else {
            dBuilder.setMessage("No previous PDF report files found in:\n" + PDFManager.outputPath);
        }

        dBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), "You clicked ok bitch", Toast.LENGTH_SHORT).show();
            }
        });
        dBuilder.create().show();
    }
}

