package com.bytestudio.muzei;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
	private FrequencyAdapter frequencyAdapter = null;
	
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

        // create a new instance of the preference manager
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
		String frequencyJSON = getFrequencyJSON();
		JSONObject parsedJSON = null;
		JSONArray frequencyArray = null;

		try {
			parsedJSON = new JSONObject(frequencyJSON);
			
			frequencyArray = parsedJSON.getJSONArray("categories");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		frequencyAdapter = new FrequencyAdapter(frequencyArray);
		
		int storedSelection = preferenceManager.getFrequency();
		
		// Apply the adapter to the spinner
		frequencyList.setAdapter(frequencyAdapter);		
		
		// if we had a previous frequency stored, find it in the array and reselect it
		int id = findFrequency(frequencyArray, storedSelection);
		frequencyList.setSelection(id);
				
		// set selected listener
		frequencyList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				JSONObject selected = (JSONObject)parent.getItemAtPosition(pos);
				preferenceManager.setFrequency(selected.optInt("interval"));
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
	
	private int findFrequency(JSONArray frequencyList, int itemToFind) {
		int foundPos = -1;
		
		if (frequencyList != null) {
			for (int pos = 0; pos < frequencyList.length(); pos++) {
				JSONObject frequency = frequencyList.optJSONObject(pos);
				if (frequency.optInt("interval") == itemToFind) {
					foundPos = pos;
					break;
				}
			}
		}
		
		return foundPos;
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
        .setEndpoint("http://www.bytestudiophotography.com")
        .build();

        // call the getCategories API
		BytestudioPxService service = restAdapter.create(BytestudioPxService.class);
		service.getCategories(new Callback<List<Category>>() {
            @Override
            public void success(List<Category> categories, Response response) {
            	if (!categories.isEmpty()) {
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

	private class FrequencyAdapter extends BaseAdapter implements SpinnerAdapter {
		private JSONArray mData;
		
		public FrequencyAdapter(JSONArray newData) {
			this.mData = newData;
		}
		
	    public void setData(JSONArray newData) {
	    	this.mData = newData;
	    }
		
		@Override
		public int getCount() {
			return mData.length();
		}

		@Override
		public Object getItem(int position) {
			return mData.optJSONObject(position);
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
			
			JSONObject current = (JSONObject)mData.optJSONObject(position);
			text.setText(current.optString("name"));
			
			return text;		
		}
	}
	
    private String getFrequencyJSON() {
    	Boolean success = false;
        String JSONString = "";
    	
		try {
			InputStream inputStream = this.getAssets().open("frequency.json");
			
			if (inputStream != null) {
			    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			    String receiveString = "";
			    StringBuilder stringBuilder = new StringBuilder();
			
				while ( (receiveString = bufferedReader.readLine()) != null ) {
					stringBuilder.append(receiveString);
				}
				
				inputStream.close();
				JSONString = stringBuilder.toString();

				success = true;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }   
        
		if (success == false) {
			JSONString = null;
		}
		
    	return JSONString;
    }

}