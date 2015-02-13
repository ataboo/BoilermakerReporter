package com.atasoft.boilermakerreporter;

import android.content.Intent;
import android.os.*;
import android.support.v4.app.*;
import android.view.*;

import com.atasoft.fragments.*;


public class MainActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        swapFrag();
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

    private void openAbout(){
        Intent intent = new Intent(this, ReportForm.class);
        startActivity(intent);
    }

    private void swapFrag(){
        getSupportFragmentManager().beginTransaction().replace(R.id.container, new ReportForm()).commit();
    }
}

