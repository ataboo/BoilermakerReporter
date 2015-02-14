package com.atasoft.boilermakerreporter;

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

//TODO: fix logs, add about, email intent?, permanence


public class MainActivity extends ActionBarActivity implements ActionBar.OnNavigationListener {

    public static final int SUPER_LAUNCH = 0;
    public static final int APPRENTICE_LAUNCH = 1;

    private Fragment superFrag;
    private Fragment apprenticeFrag;
    private ActionBar actionBar;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        int launchMode = getIntent().getIntExtra("launch_mode", SUPER_LAUNCH);
        Log.w("MainActivity", "Launch Mode:" + launchMode);

        setContentView(R.layout.activity_main);
        //check both just for giggles
        if(superFrag == null || apprenticeFrag == null){
            superFrag = new SuperReportFrag();
            apprenticeFrag = new ApprenticeReportFrag();
            this.actionBar = getSupportActionBar();
            if(actionBar!=null) initActionBar(launchMode);
        }
        swapFrag(launchMode);

	}

    private void initActionBar(int launchMode){
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(actionBar.getThemedContext(),
                android.R.layout.simple_list_item_1, android.R.id.text1,new String[]{"Supervisor Report", "Apprentice Report"});
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

/*		Handle presses on the action bar items
        switch(item.getItemId()){
            case R.id.action_settings:
                openSettings();
                return true;
            case R.id.action_about:
                openAbout();
                return true;
        }*/
		return super.onOptionsItemSelected(item);
    }

    private void swapFrag(int launchMode) {
        Fragment activeFrag;
        switch (launchMode) {
            default:
                activeFrag = superFrag;
                break;
            case APPRENTICE_LAUNCH:
                activeFrag = apprenticeFrag;
                break;
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.container, activeFrag).commit();
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        swapFrag(itemPosition);
        return true;
    }
}

