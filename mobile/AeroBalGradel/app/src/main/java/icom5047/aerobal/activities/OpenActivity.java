package icom5047.aerobal.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.aerobal.data.objects.Experiment;

import java.util.Stack;

import icom5047.aerobal.controllers.UnitController;
import icom5047.aerobal.dialog.OpenDialog;
import icom5047.aerobal.fragments.OpenLocalFragment;
import icom5047.aerobal.fragments.OpenOnlineSessionsFragment;
import icom5047.aerobal.resources.Keys;


public class OpenActivity extends Activity {

    private Stack<Fragment> fragmentStack;
    private String type;
    private UnitController unitController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open);

        final ActionBar ab = this.getActionBar();

        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);

        fragmentStack = new Stack<Fragment>();
        //Instance Field


        Bundle bnd = getIntent().getExtras();

        if(bnd != null){
            type = bnd.getString(Keys.BundleKeys.OpenType);
        }

        FragmentManager fragmentManager = getFragmentManager();
        if(type.equalsIgnoreCase(OpenDialog.LOCAL)){
            setTitle(R.string.title_activity_open_local);
            OpenLocalFragment openLocalFragment = new OpenLocalFragment();
            Bundle bundle = new Bundle();

            //Load Base
            String extState = Environment.getExternalStorageState();
            if(!extState.equals(Environment.MEDIA_MOUNTED)){
                Toast.makeText(this, R.string.toast_file_not_create, Toast.LENGTH_SHORT).show();
                finish();
            }

            bundle.putSerializable(Keys.BundleKeys.FileRoot, Environment.getExternalStorageDirectory());
            openLocalFragment.setArguments(bundle);
            fragmentStack.push(openLocalFragment);
            fragmentManager.beginTransaction().replace(R.id.main_container, fragmentStack.peek()).commit();

        }
        else{

            setTitle(R.string.title_activity_open_online);
            OpenOnlineSessionsFragment openOnlineFragment = new OpenOnlineSessionsFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable(Keys.BundleKeys.UnitController, bnd.getSerializable(Keys.BundleKeys.UnitController));
            openOnlineFragment.setArguments(bundle);
            fragmentStack.push(openOnlineFragment);
            fragmentManager.beginTransaction().replace(R.id.main_container, fragmentStack.peek()).commit();
        }







    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
            case R.id.ab_btn_back:
                folderBack();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void folderBack(){
        if(this.fragmentStack.size() <= 1){
            Toast.makeText(this, R.string.toast_file_parent_error, Toast.LENGTH_SHORT).show();
        }
        else{
            this.fragmentStack.pop();
            //Set Main Fragment
            FragmentManager fm = this.getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.main_container, fragmentStack.peek()).commit();
        }
    }

    public void setActivityResult(Experiment experiment){
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable(Keys.BundleKeys.Experiment, experiment);
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();
    }
    public void setActivityResultDTO(com.aerobal.data.dto.Experiment experiment){

    }

    public void loadFragmentIntoActivity(Fragment fragment){
        //Set Main Fragment
        FragmentManager fm = this.getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        fragmentStack.push(fragment);
        ft.replace(R.id.main_container, fragmentStack.peek()).commit();
    }

    @Override
    public void onBackPressed() {
        //Normal Back if no other Fragment is Use
        if(this.fragmentStack.size() <= 1){
            super.onBackPressed();
        }
        else{
            this.fragmentStack.pop();
            //Set Main Fragment
            FragmentManager fm = this.getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.main_container, fragmentStack.peek()).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.open, menu);
        return true;
    }

}
