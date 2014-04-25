package icom5047.aerobal.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import icom5047.aerobal.http.HttpRequest;
import icom5047.aerobal.http.Server;
import icom5047.aerobal.resources.Keys;

public class LoginActivity extends FragmentActivity {



    //Intance Fields
    private ProgressDialog progressDialog;
    private EditText username;
    private EditText password;
    private Activity myself;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //ActionBar
        this.getActionBar().hide();

        //Set Activity
        myself = this;

        //Set Up Progress Dialog

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.progress_login));

        //Get EditText Variables
        username = (EditText) findViewById(R.id.loginUsername);
        password = (EditText) findViewById(R.id.loginPassword);

        //Get Objects

        findViewById(R.id.loginButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();

                String user = username.getText().toString().trim();
                String pass = password.getText().toString();
                if(!user.isEmpty()  && !pass.isEmpty())
                    doHttpLogin(user , pass);
                else{
                    progressDialog.dismiss();
                    Toast.makeText(getBaseContext(), R.string.toast_invalid_user_pass, Toast.LENGTH_SHORT).show();
                }


            }
        });

        findViewById(R.id.loginButtonForgot).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showForgotDialog();
            }
        });


        findViewById(R.id.loginButtonRegister).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.website_register)));
                startActivity(i);

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    private void showForgotDialog(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.title_dialog_forgot_password);


        View view = this.getLayoutInflater().inflate(R.layout.dialog_edit_text, null);

        final EditText et = (EditText) view.findViewById(R.id.dialogEditText);
        et.setHint(R.string.dialog_login_forgot_password);

        builder.setView(view);


        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //NoOp
            }
        });

        builder.setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                progressDialog.show();
                String email = et.getText().toString().trim();
                if(!email.isEmpty()){
                    doHttpForgot(email);
                }
                else{
                    progressDialog.dismiss();
                    Toast.makeText(getBaseContext(), R.string.toast_invalid_email, Toast.LENGTH_SHORT).show();
                }

            }
        });

        builder.create().show();
    }

    public void doHttpLogin(String user, String pass){

        Bundle params = new Bundle();
        params.putString("method", "POST");
        params.putString("url", Server.User.POST_LOGIN);

        String payload = "user="+user.trim()+"&"+"password="+pass.trim();

        HttpRequest request = new HttpRequest(params, payload, HttpRequest.CONTENT_TYPE_X_FORM_URL_ENCODED, new HttpRequest.HttpCallback() {
            @Override
            public void onSucess(JSONObject json) {

                Log.d("JSON:", json.toString());
                if(!json.has("error")){

                    try{
                        String token = json.getString("token");
                        doResult(token);

                    } catch (JSONException e){
                        Toast.makeText(getBaseContext(), R.string.toast_net_login_fail, Toast.LENGTH_SHORT).show();
                    }

                }else{
                    Toast.makeText(getBaseContext(), R.string.toast_net_login_fail, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailed() {
                Toast.makeText(getBaseContext(), R.string.toast_net_error, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDone() {
                progressDialog.dismiss();
            }
        });
        request.execute();



    }

    private void doResult(String token) {
        Intent mainIntent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString(Keys.BundleKeys.UserToken, token);
        mainIntent.putExtras(bundle);
        setResult(Activity.RESULT_OK, mainIntent);
        finish();
    }

    public void doHttpForgot(String email){

        Uri.Builder urlB = Uri.parse(Server.User.GET_FORGOT_PASSWORD).buildUpon();
        urlB.appendQueryParameter("email", email);

        Bundle params = new Bundle();
        params.putString("method", "GET");
        params.putString("url", urlB.toString().trim());

        HttpRequest request = new HttpRequest(params, new HttpRequest.HttpCallback() {
            @Override
            public void onSucess(JSONObject json) {
                Toast.makeText(getBaseContext(), R.string.toast_net_forgot_success, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed() {
                Toast.makeText(getBaseContext(), R.string.toast_net_error, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDone() {
                progressDialog.dismiss();
            }
        });
        request.execute();
    }

}
