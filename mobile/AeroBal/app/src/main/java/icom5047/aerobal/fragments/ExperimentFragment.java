package icom5047.aerobal.fragments;

import icom5047.aerobal.activities.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ExperimentFragment extends Fragment {

	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		//Inflates View
		View view = inflater.inflate(R.layout.fragment_experiment_summary, container, false);
		
		return view;
	}
}
