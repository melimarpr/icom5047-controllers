package icom5047.aerobal.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
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

import com.aerobal.data.dto.ExperimentDto;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import icom5047.aerobal.activities.OpenActivity;
import icom5047.aerobal.activities.R;
import icom5047.aerobal.controllers.UnitController;
import icom5047.aerobal.controllers.UserController;
import icom5047.aerobal.http.HttpRequest;
import icom5047.aerobal.http.Server;
import icom5047.aerobal.resources.GlobalConstants;
import icom5047.aerobal.resources.Keys;
import icom5047.aerobal.resources.UnitFactory;

/**
 * Created by enrique on 4/26/14.
 */
public class OpenOnlineExperimentsFragment extends Fragment{

    private ProgressBar progressBar;
    private ListView listView;
    private OpenActivity openActivity;
    private TextView emptySession;
    private UserController userController;
    private long sessionId;
    private UnitController unitController;

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
        emptySession.setText(R.string.fragment_open_online_no_exp_in_session);
        //Instance Fields
        openActivity = (OpenActivity) this.getActivity();
        userController = new UserController(openActivity);
        Bundle bundle = getArguments();
        sessionId = bundle.getLong(Keys.BundleKeys.SessionID);
        unitController = (UnitController) bundle.getSerializable(Keys.BundleKeys.UnitController);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(userController.isUserLogIn()) {
            doHttpExperiments();
        }
        else{
            Toast.makeText(openActivity, R.string.toast_invalid_access, Toast.LENGTH_SHORT).show();
            openActivity.finish();
        }

    }

    public void doHttpExperiments(){
        //URL
        Uri.Builder urlB = Uri.parse(Server.Experiments.GET_EXPERIMENTS_FOR_SESSION).buildUpon();
        urlB.appendQueryParameter(Server.Experiments.Parmas.SESSION_ID, sessionId+"");
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


                if(listView == null){
                    //Not Active
                    return;
                }
                if(json.has("payload")) {
                    try {
                        JSONArray array = json.getJSONArray("payload");
                        final ExperimentDto[] experiments = new ExperimentDto[array.length()];
                        for (int i = 0; i < array.length(); i++) {
                            Gson gson = new Gson();
                            experiments[i] = gson.fromJson(array.getJSONObject(i).toString(), ExperimentDto.class);
                        }

                        if(progressBar == null || listView ==  null){
                            return;
                        }

                        if (experiments.length == 0) {
                            progressBar.setVisibility(View.GONE);
                            emptySession.setVisibility(View.VISIBLE);
                        } else {
                            progressBar.setVisibility(View.GONE);
                            listView.setAdapter(new ArrayAdapter<ExperimentDto>(getActivity(), android.R.layout.simple_list_item_1, experiments){
                                @Override
                                public View getView(int position, View convertView, ViewGroup parent) {
                                    final ExperimentDto experiment = this.getItem(position);
                                    LayoutInflater inflater = ((Activity) this.getContext()).getLayoutInflater();
                                    View rowLayout = inflater.inflate(R.layout.row_open_experiment, parent, false);

                                    TextView textView = (TextView) rowLayout.findViewById(R.id.rowOpenExperimentTitle);
                                    Typeface face = Typeface.createFromAsset(openActivity.getAssets(), "fonts/Roboto-Thin.ttf");
                                    textView.setTypeface(face);
                                    textView.setText(experiment.name());

                                   rowLayout.findViewById(R.id.rowOpenExperimentInfo).setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
                                           createInfoDialog(experiment);

                                       }
                                   });

                                    return rowLayout;

                                }});
                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    ExperimentDto experiment = (ExperimentDto) parent.getItemAtPosition(position);
                                    openActivity.setActivityResultDTO(experiment);

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

    public void createInfoDialog(ExperimentDto experiment){

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.title_dialog_experiment_info);

        View view = View.inflate(getActivity(), R.layout.dialog_experiment_information ,null);

        TextView title = (TextView) view.findViewById(R.id.dialogExpInfoTitle);
        TextView windSpeed = (TextView) view.findViewById(R.id.dialogExpInfoWindSpeed);
        TextView windSpeedUnit = (TextView) view.findViewById(R.id.dialogExpInfoWindSpeedUnits);
        TextView samples = (TextView) view.findViewById(R.id.dialogExpInfoSample);
        TextView timeInterval = (TextView) view.findViewById(R.id.dialogExpInfoTimeIntervalValue);

        Typeface face = Typeface.createFromAsset(openActivity.getAssets(), "fonts/Roboto-Thin.ttf");
        title.setTypeface(face);
        title.setText(experiment.name());


        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(GlobalConstants.DecimalPrecision);
        nf.setMinimumFractionDigits(GlobalConstants.DecimalPrecision);

        samples.setText(""+experiment.amountOfValues());
        timeInterval.setText(""+experiment.frequency());


        double newVal = UnitFactory.Speed.convert(experiment.windSpeed(), UnitFactory.Speed.DEFAULT, unitController.getCurrentType(UnitFactory.Type.SPEED));

        windSpeed.setText(""+nf.format(newVal));
        windSpeedUnit.setText(unitController.getCurrentSpeedUnit());


        builder.setView(view);

        Dialog dialog = builder.create();
        dialog.show();

    }
}
