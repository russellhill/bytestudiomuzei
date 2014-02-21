package com.bytestudio.muzei;

import java.util.ArrayList;
import java.util.Arrays;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class SettingsActivity extends Activity {
	private Spinner categoryList;
	private Spinner frequencyList;
	private PreferenceManager preferenceManager;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_layout);

        // Make sure we're running on Honeycomb or higher to use ActionBar APIs
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }	

        preferenceManager = new PreferenceManager(this);
        
		// setup the UI etc
		setup();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home: {
				// go to previous screen when app icon in action bar is clicked
				finish();
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed() {
	    super.onBackPressed();
	    finish();
	}

	private void setup() {
		categoryList = (Spinner) findViewById(R.id.categoryList);
		frequencyList = (Spinner) findViewById(R.id.frequencyList);
		
		// frequency list comes from a local array...configure it
		setupFrequencyList();
	}
	
	private void setupFrequencyList() {
		ArrayList<String> frequencyArray = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.frequency_array)));
		String storedSelection = preferenceManager.getFrequency();
		
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.frequency_array, android.R.layout.simple_spinner_item);

		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		// Apply the adapter to the spinner
		frequencyList.setAdapter(adapter);		
		
		// if we had a previous frequency stored, find it in the array and reselect it
		if (storedSelection != null) {
			int id = frequencyArray.indexOf(storedSelection);
			frequencyList.setSelection(id);
		}
				
		// set selected listener
		frequencyList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				String selected = (String)parent.getItemAtPosition(pos);
				preferenceManager.setFrequency(selected);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}			
		});
	}

}
