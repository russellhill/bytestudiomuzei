package com.bytestudio.muzei;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {

	private SharedPreferences pref;
	private SharedPreferences.Editor editor;
	private Context _context;
	
	// Constructor
	public PreferenceManager(Context context){
		this._context = context;
		pref = _context.getSharedPreferences(Constants.kPreferencesName, Context.MODE_PRIVATE);
		editor = pref.edit();
	}
	
	public void setCategory(String category){
		editor.putString(Constants.kCategory, category);
		editor.commit();
	}
	
	public String getCategory(){
		return pref.getString(Constants.kCategory, null); 
	}
	
	public void setFrequency(String frequency){
		editor.putString(Constants.kRefreshFrequency, frequency);
		editor.commit();
	}
	
	public String getFrequency(){
		return pref.getString(Constants.kRefreshFrequency, null); 
	}
	
}
