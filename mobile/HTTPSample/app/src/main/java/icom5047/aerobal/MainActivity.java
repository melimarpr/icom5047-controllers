package icom5047.aerobal;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.json.JSONObject;

import icom5047.aerobal.http.HttpRequest;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doHttpTest();
            }
        });
    }

    private void doHttpTest() {

        Uri.Builder urlB = Uri.parse("http://107.150.13.167:9000/user").buildUpon();
        urlB.appendQueryParameter("id", 1+"");

        Bundle params = new Bundle();
        params.putString("method", "GET");
        params.putString("url", urlB.toString().trim());

        HttpRequest request = new HttpRequest(params, new HttpRequest.HttpCallback() {

            @Override
            public void onSucess(JSONObject json) {

                Log.v("Success", json.toString());

                Toast.makeText(getBaseContext(), json.toString(), Toast.LENGTH_SHORT).show();



            }

            @Override
            public void onFailed() {
                Log.e("Error", "Recieving Value");
            }

            @Override
            public void onDone() {
            }
        });
        request.execute();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
