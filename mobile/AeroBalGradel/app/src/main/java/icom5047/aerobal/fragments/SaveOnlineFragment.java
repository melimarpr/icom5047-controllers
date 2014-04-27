package icom5047.aerobal.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.aerobal.data.dto.SessionDto;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import icom5047.aerobal.activities.R;
import icom5047.aerobal.activities.SaveActivity;
import icom5047.aerobal.controllers.UserController;
import icom5047.aerobal.http.HttpRequest;
import icom5047.aerobal.http.Server;

/**
 * Created by enrique on 4/27/14.
 */
public class SaveOnlineFragment extends Fragment {

    private ProgressBar progressBar;
    private ListView listView;
    private SaveActivity saveActivity;
    private TextView emptySession;
    private UserController userController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_open_local, container, false);

        //Get Views
        listView = (ListView) view.findViewById(R.id.fragOpenLocalListView);
        progressBar = (ProgressBar) view.findViewById(R.id.fragOpenLocalProgress);
        emptySession = (TextView) view.findViewById(R.id.fragOpenLocalEmptyFolder);

        //Set View Nitpicks
        Typeface face = Typeface.createFromAsset(this.getActivity().getAssets(), "fonts/Roboto-Thin.ttf");
        emptySession.setTypeface(face);
        emptySession.setText(R.string.fragment_open_online_no_session);
        //Instance Fields
        saveActivity = (SaveActivity) this.getActivity();
        userController = new UserController(saveActivity);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(userController.isUserLogIn()) {
            doHttpSessions();
        }
        else{
            Toast.makeText(saveActivity, R.string.toast_invalid_access, Toast.LENGTH_SHORT).show();
            saveActivity.finish();
        }

    }

    private void doHttpSessions() {
        //URL
        Uri.Builder urlB = Uri.parse(Server.Session.GET_USER_SESSION).buildUpon();
        //Params
        final Bundle params = new Bundle();
        params.putString("method", "GET");
        params.putString("url", urlB.toString().trim());
        //Headers
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(Server.Headers.TOKEN, userController.getToken());

        HttpRequest request = new HttpRequest(params, headers, new HttpRequest.HttpCallback() {
            @Override
            public void onSucess(JSONObject json) {

                if(json.has("payload")) {
                    try {
                        JSONArray array = json.getJSONArray("payload");
                        SessionDto[] sessions = new SessionDto[array.length()];
                        for (int i = 0; i < array.length(); i++) {
                            Gson gson = new Gson();
                            sessions[i] = gson.fromJson(array.getJSONObject(i).toString(), SessionDto.class);
                        }

                        if (sessions.length == 0) {
                            progressBar.setVisibility(View.GONE);
                            emptySession.setVisibility(View.VISIBLE);
                        } else {
                            progressBar.setVisibility(View.GONE);
                            listView.setAdapter(new ArrayAdapter<SessionDto>(getActivity(), android.R.layout.simple_list_item_1, sessions) {
                                @Override
                                public View getView(int position, View convertView, ViewGroup parent) {
                                    SessionDto session = this.getItem(position);
                                    LayoutInflater inflater = ((Activity) this.getContext()).getLayoutInflater();
                                    View rowLayout = inflater.inflate(R.layout.row_open_session, parent, false);

                                    TextView textView = (TextView) rowLayout.findViewById(R.id.rowOpenSessionTitle);
                                    Typeface face = Typeface.createFromAsset(saveActivity.getAssets(), "fonts/Roboto-Thin.ttf");
                                    textView.setTypeface(face);
                                    textView.setText(session.name());

                                    ((TextView) rowLayout.findViewById(R.id.rowOpenSessionDesc)).setText(session.description());

                                    return rowLayout;
                                }
                            });
                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    SessionDto sessionDto = (SessionDto) parent.getItemAtPosition(position);
                                    saveActivity.saveExperiment(sessionDto.id());
                                }
                            });
                            listView.setVisibility(View.VISIBLE);
                        }

                    } catch (JSONException e) {
                        Toast.makeText(getActivity(), R.string.toast_error_json, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailed(JSONObject jsonObject) {
                Toast.makeText(getActivity(), R.string.toast_net_error, Toast.LENGTH_SHORT).show();
            }


        });
        request.execute();

    }
}
