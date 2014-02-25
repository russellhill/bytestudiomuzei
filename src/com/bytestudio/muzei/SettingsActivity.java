package com.bytestudio.muzei;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit.ErrorHandler;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.Callback;
import retrofit.client.Response;

import com.bytestudio.muzei.BytestudioPxService.Category;
import com.google.android.apps.muzei.api.RemoteMuzeiArtSource.RetryException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

public class SettingsActivity extends Activity {
    private static final String TAG = "Bytestudio";

	private Spinner categoryList;
	private Spinner frequencyList;
	private PreferenceManager preferenceManager;
	
	private List<Category> retrievedCategories = null;
	
	private CategoryAdapter categoryAdapter = null;
	
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
		
		// fetch the categories from the API
		fetchCategories();		
	}
	
	private void setupFrequencyList() {
		// create a frequencyArray to use for listing the refresh intervals
		ArrayList<String> frequencyArray = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.frequency_array)));
		String storedSelection = preferenceManager.getFrequency();
		
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<String> adapter = new ArrayAdapter<String> (this, android.R.layout.simple_spinner_item, frequencyArray);

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

	private void setupCategoryList() {
		String storedSelection = preferenceManager.getCategory();

		if (categoryAdapter == null) {
			categoryAdapter = new CategoryAdapter(retrievedCategories);
			categoryList.setAdapter(categoryAdapter);
		} else {
			categoryAdapter.setData(retrievedCategories);
			categoryAdapter.notifyDataSetChanged();
		}
		
		// if we had a previous category stored, find it in the array and reselect it
		if (storedSelection != null) {
			int id = findCategory(storedSelection);
			
			// returns -1 if not found
			if (id != -1) {
				categoryList.setSelection(id);
			}
		}

		// set selected listener
		categoryList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				Category selected = (Category)parent.getItemAtPosition(pos);
				String category = selected.name;
				
				// set the chosen category name into the local settings using preferenceManager
				preferenceManager.setCategory(category);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}			
		});
	}
	
	private int findCategory(String categoryToFind) {
		int foundPos = -1;
		int pos = 0;
		
		if (retrievedCategories != null) {
			for (Category category : retrievedCategories) {
				String lCaseName = category.name.toLowerCase();
				if (lCaseName.equals(categoryToFind.toLowerCase())) {
					foundPos = pos;
					break;
				}
				pos++;
			}
		}
		
		return foundPos;
	}

	private void fetchCategories() {
		// create a new restadapter
        RestAdapter restAdapter = new RestAdapter.Builder()
//        .setEndpoint("http://www.bytestudiophotography.com")
        .setEndpoint("http://192.168.1.118:9090")
        .build();

        // call the getCategories API
		BytestudioPxService service = restAdapter.create(BytestudioPxService.class);
		service.getCategories(new Callback<List<Category>>() {
            @Override
            public void success(List<Category> categories, Response response) {
            	if (!categories.isEmpty()) {
            		Log.i(TAG, "categories:" + categories);

            		if (!(categories == null)) {
            			if (categories.size() == 0) {
            			    Log.w(TAG, "No categories returned from API.");
            			} else {
            				retrievedCategories = categories;
            				setupCategoryList();
            			}
            		}
            	} else {
            		Log.i(TAG, "categories is empty");            		
            	}
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.i(TAG, "retrofit error: " + retrofitError.getLocalizedMessage());
            }
        });
	}
	
	private class CategoryAdapter extends BaseAdapter implements SpinnerAdapter {
		private List<Category> mData;
		
		public CategoryAdapter(List<Category> newData) {
			this.mData = newData;
		}
		
	    public void setData(List<Category> newData) {
	    	this.mData = newData;
	    }
		
		@Override
		public int getCount() {
			return mData.size();
		}

		@Override
		public Object getItem(int position) {
			return mData.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View recycle, ViewGroup parent) {
			TextView text;
			
			if (recycle != null){
				// Re-use the recycled view here!
			    text = (TextView) recycle;
			} else {
			    // No recycled view, inflate the "original" from the platform:
			    text = (TextView) getLayoutInflater().inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
			}
			
			Category current = (Category)mData.get(position);
			text.setText(current.name);
			
			return text;		
		}
	}
}