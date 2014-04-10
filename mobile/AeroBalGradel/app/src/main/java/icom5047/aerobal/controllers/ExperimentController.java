package icom5047.aerobal.controllers;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.v4.app.FragmentActivity;

import com.aerobal.data.objects.Experiment;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import icom5047.aerobal.activities.R;
import icom5047.aerobal.containers.SpinnerContainer;

/**
 * Created by enrique on 3/20/14.
 */
public class ExperimentController implements Serializable{


    //Constants
    public static int ALL_RUNS = -1;
    public static String ALL_RUNS_STRING = "All";

    private Experiment experiment;



    private SpinnerContainer activeRun;


    public ExperimentController() {
        experiment = null;
        this.activeRun = new SpinnerContainer(0, ALL_RUNS, ALL_RUNS_STRING);
    }


    public void setExperiment(Experiment experiment) {
        this.experiment = experiment;
    }

    public boolean isExperimentSet() {
        return experiment != null;
    }


    public Experiment getExperiment() {
        return experiment;
    }

    public List<SpinnerContainer> getRunsListSpinner(){
        LinkedList<SpinnerContainer> list = new LinkedList<SpinnerContainer>();

        //Add All Value
        list.add(0, new SpinnerContainer(0, ALL_RUNS, ALL_RUNS_STRING ));

        //Add Other Values
        for(int i=0; i< getExperiment().runs().size(); i++){

            list.add(i+1, new SpinnerContainer(i+1, i, "Runs"+(i+1)));

        }
        return list;
    }


    //Getter & Setter for Active Values
    public SpinnerContainer getActiveRun(){
        return this.activeRun;
    }


    public void setActiveRun(SpinnerContainer activeRun) {
        this.activeRun = activeRun;
    }


    //Run a Run
    public void generateExperimentRun(BluetoothController btController, FragmentActivity context){


        ProgressDialog progressDialog = new ProgressDialog(context);


        progressDialog.setMessage( context.getString(R.string.progress_running) );

        progressDialog.show();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        showConfirmDialog(context, progressDialog);




    }

    private void showConfirmDialog(FragmentActivity activity, final ProgressDialog progressDialog){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setTitle(R.string.title_dialog_confirm_run);

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //NoOp
            }
        });

         builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                progressDialog.dismiss();

             }
         });


        builder.create().show();

    }







}
