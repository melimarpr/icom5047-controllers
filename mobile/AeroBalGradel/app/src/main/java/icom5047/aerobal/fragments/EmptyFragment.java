package icom5047.aerobal.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import icom5047.aerobal.activities.R;

public class EmptyFragment extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View empty_view = inflater.inflate(R.layout.fragment_experiment_empty, container, false);
        return empty_view;
    }


}
