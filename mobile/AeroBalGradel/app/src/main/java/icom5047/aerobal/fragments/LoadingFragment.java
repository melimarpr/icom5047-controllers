package icom5047.aerobal.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import icom5047.aerobal.activities.R;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Use the {@link LoadingFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class LoadingFragment extends Fragment {

    private String message;

    public static LoadingFragment newInstance(String message) {
        LoadingFragment fragment = new LoadingFragment();
        fragment.setMessage(message);
        return fragment;
    }

    private void setMessage(String message){
        this.message = message;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_loading, container, false);
        ((TextView) view.findViewById(R.id.fragLoadingMessage)).setText(message);

        return view;
    }


}
