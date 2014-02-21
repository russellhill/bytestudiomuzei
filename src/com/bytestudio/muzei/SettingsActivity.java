package com.bytestudio.muzei;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class SettingsActivity extends FragmentActivity {
	private Spinner categoryList;
	private Spinner frequencyList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_layout);

		// setup the UI etc
		setup();
	}

	private void setup() {
		categoryList = (Spinner) findViewById(R.id.categoryList);
		frequencyList = (Spinner) findViewById(R.id.frequencyList);
		
		// frequency list comes from a local array...configure it
		setupFrequencyList();
	}
	
	private void setupFrequencyList() {
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.frequency_array, android.R.layout.simple_spinner_item);
		
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		// Apply the adapter to the spinner
		frequencyList.setAdapter(adapter);		
		
		// set selected listener
		frequencyList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}			
		});
	}

}
