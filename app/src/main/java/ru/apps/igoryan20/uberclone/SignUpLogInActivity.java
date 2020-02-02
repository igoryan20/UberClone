package ru.apps.igoryan20.uberclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

public class SignUpLogInActivity extends AppCompatActivity implements View.OnClickListener {

    enum State{
        SIGNUP, LOGIN
    }

    private Button btnSignUpLogin, btnOneTimeLogin;
    private RadioButton driverRadioButton, passengerRadioButton;
    private EditText edtUserName, edtPassword, edtDriverOrPassenger;

    private State state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actvity_sign_up);

        ParseInstallation.getCurrentInstallation().saveInBackground();
        if(ParseUser.getCurrentUser() != null){
           transitionToPassangerActivity();
           transitionToDriverRequestListActivity();
        }

        btnSignUpLogin = findViewById(R.id.btnSignUp);
        btnOneTimeLogin = findViewById(R.id.btnOneTimeLogin);
        driverRadioButton = findViewById(R.id.rbDriver);
        passengerRadioButton = findViewById(R.id.rbPassanger);
        edtUserName = findViewById(R.id.edtUserName);
        edtPassword = findViewById(R.id.edtPassword);
        edtDriverOrPassenger = findViewById(R.id.edtDriverOrPassanger);

        state = State.SIGNUP;

        btnSignUpLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(state == State.SIGNUP){
                    if(driverRadioButton.isChecked() == false && passengerRadioButton.isChecked() == false){
                        Toast.makeText(SignUpLogInActivity.this, "Are you driver or passenger?", Toast.LENGTH_LONG).show();
                        return;
                    }
                    ParseUser appUser = new ParseUser();
                    appUser.setUsername(edtUserName.getText().toString());
                    appUser.setPassword(edtPassword.getText().toString());
                    if(driverRadioButton.isChecked()){
                        appUser.put("as", "Driver");
                    } else if(passengerRadioButton.isChecked()){
                        appUser.put("as", "Passenger");
                    }
                    appUser.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e == null){
                                Toast.makeText(SignUpLogInActivity.this, "Signed Up", Toast.LENGTH_SHORT).show();
                                transitionToPassangerActivity();
                                transitionToDriverRequestListActivity();
                            }
                        }
                    });
                } else if(state == State.LOGIN){
                    ParseUser.logInInBackground(edtUserName.getText().toString(), edtPassword.getText().toString(), new LogInCallback() {
                        @Override
                        public void done(ParseUser user, ParseException e) {
                            if(user != null && e == null){
                                Toast.makeText(SignUpLogInActivity.this, "Loged", Toast.LENGTH_SHORT).show();
                                transitionToPassangerActivity();
                                transitionToDriverRequestListActivity();
                            }
                        }
                    });
                }
            }
        });
        btnOneTimeLogin.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sign_up_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.loginItem:
                if(state == State.SIGNUP){
                    state = State.LOGIN;
                    item.setTitle("Sign Up");
                    btnSignUpLogin.setText("Log In");
                } else if(state == State.LOGIN){
                    state = State.SIGNUP;
                    item.setTitle("Log In");
                    btnSignUpLogin.setText("Sign Up");
                }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if(edtDriverOrPassenger.getText().toString().equals("Driver") ||
            edtDriverOrPassenger.getText().toString().equals("Passenger")){
            if(ParseUser.getCurrentUser() == null){
                ParseAnonymousUtils.logIn(new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        if(user != null && e == null){
                            Toast.makeText(SignUpLogInActivity.this, "We have anonymus access", Toast.LENGTH_LONG).show();
                            user.put("as", edtDriverOrPassenger.getText().toString());
                            user.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    transitionToPassangerActivity();
                                    transitionToDriverRequestListActivity();
                                }
                            });
                        }
                    }
                });
            } else {
                Toast.makeText(this, "Lol", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Are you driver or passanger?", Toast.LENGTH_SHORT).show();
        }
    }

    private void transitionToPassangerActivity(){
        if(ParseUser.getCurrentUser() != null){
            if(ParseUser.getCurrentUser().get("as").equals("Passenger")){
                Intent intent = new Intent(SignUpLogInActivity.this, PassengerActivity.class);
                startActivity(intent);
            }
        }
    }

    private void transitionToDriverRequestListActivity(){

        if(ParseUser.getCurrentUser() != null){
            if(ParseUser.getCurrentUser().get("as").equals("Driver")){
                Intent intent = new Intent(SignUpLogInActivity.this, DriverRequestActivity.class);
                startActivity(intent);
            }
        }

    }
}
