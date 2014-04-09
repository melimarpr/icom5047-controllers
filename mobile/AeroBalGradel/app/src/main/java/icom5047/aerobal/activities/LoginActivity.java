package icom5047.aerobal.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {



    //Intance Fields
    private ProgressDialog progressDialog;
    private Button login;
    private EditText username;
    private EditText password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //ActionBar
        this.getActionBar().hide();

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

    //TODO: Implement when Integration
    public void doHttpLogin(String user, String pass){

        progressDialog.dismiss();
        startActivity(new Intent(this, MainActivity.class));



    }

    public void doHttpForgot(String email){
        progressDialog.dismiss();
    }

}
