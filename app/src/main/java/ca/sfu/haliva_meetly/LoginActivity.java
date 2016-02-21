package ca.sfu.haliva_meetly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


public class LoginActivity extends ActionBarActivity {
    public String username;
    public String password;
    public int tokenNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    private String getUsername(){
        EditText username = (EditText) findViewById(R.id.usernameLogin);
        return username.getText().toString();
    }

    private String getPassword(){
        EditText password = (EditText) findViewById(R.id.passwordLogin);
        return password.getText().toString();
    }

    private void keepUsername(){
        username = getUsername();
    }

    private void keepPassword(){
        password = getPassword();
    }

    public void setupLoginBtn(View v){
        keepUsername();
        keepPassword();

        Thread.UncaughtExceptionHandler exc = new Thread.UncaughtExceptionHandler(){
            public void uncaughtException(Thread th, Throwable ex){
                new Thread(){
                    @Override
                    public void run(){
                        Looper.prepare();
                        Toast.makeText(getApplicationContext(), "Password incorrect, please try again.", Toast.LENGTH_SHORT);
                        Looper.loop();
                    }
                }.start();
            }
        };
        Thread t = new Thread(){
            public void run(){
                Looper.prepare();
                login();
                Looper.loop();
            }
        };
        t.setUncaughtExceptionHandler(exc);
        t.start();
    }

    public void login(){
        MeetlyServerImpl server = new MeetlyServerImpl();

        int userToken = -1;
        boolean passed = false;
        try{
            userToken = server.login(username, password);
            passed = true;
        } catch(MeetlyServer.FailedLoginException e){
            Toast toast = Toast.makeText(getApplicationContext(), "Password incorrect, please try again.", Toast.LENGTH_SHORT);
            toast.show();
        }
        if(passed){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("Username", username);
            saveLoginInfo(username, password, userToken);
            startActivity(intent);
            finish();
        }
    }

    public void setupCancelBtn(View v){
        Intent newPage = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(newPage);
        finish();
    }

    public void saveLoginInfo(String username, String password, int tokenNumber){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("tokenNumber_key", tokenNumber);
        editor.putString("username_key", username);
        editor.putString("password_key", password);
        editor.putBoolean("loggedIn_key", true);
        editor.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
