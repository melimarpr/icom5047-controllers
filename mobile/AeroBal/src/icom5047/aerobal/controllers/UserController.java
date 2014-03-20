package icom5047.aerobal.controllers;

import icom5047.aerobal.activities.LoginActivity;
import icom5047.aerobal.resources.Keys;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.aerobal.data.objects.User;

public class UserController {
	
	
	private boolean logIn;
	private Context context;
	private String[] baseStringArray = new String[]{ "New", "Open", "Bluetooth Setup"};
	private User user;
	
	
	public UserController(Context context, User user){
		this.context = context;
		this.user = user;
		init();
	}
	
	private void init(){
		
		SharedPreferences pref = this.context.getSharedPreferences(Keys.SharedPref.UserSharedPreferences, Context.MODE_PRIVATE);
		logIn = pref.getBoolean(Keys.SharedPref.LoginStatus, false);
		
		
	}
	
	//Returns User o.w. Null
	public User getCurrentUser(){
		return user;
	}
	
	
	public boolean isUserLogIn(){
		return logIn;
	};
	
	
	public void refresh(){
		SharedPreferences pref = this.context.getSharedPreferences(Keys.SharedPref.UserSharedPreferences, Context.MODE_PRIVATE);
		logIn = pref.getBoolean(Keys.SharedPref.LoginStatus, false);
	};
	
	public List<String> getDrawerList(){
		List<String> returnList = new LinkedList<String>(Arrays.asList(baseStringArray));
		if(logIn){
			returnList.add("Log Out");
		}
		else{
			returnList.add("Log In");
		}
		return returnList;
	}
	
	
	public void login(){
		Intent intent = new Intent( context, LoginActivity.class);
		context.startActivity(intent);
	}
	
	public void logout(){
		
		logIn = false;
		user = null;
		
		Intent intent = new Intent( context, LoginActivity.class);
		context.startActivity(intent);
		
		
	}
	
	
	
	

}
