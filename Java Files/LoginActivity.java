package edu.utep.cs.cs4330.firebaseproject;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{
    FirebaseAuth mAuth;
    EditText txtEmail, txtPassword;
    ProgressBar progressBar;
    public boolean connectionStatus = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Check internet connection and open settings menu...
        if(!networkConnected(LoginActivity.this)){
            AlertDialog.Builder bd = new AlertDialog.Builder(LoginActivity.this);
            bd.setTitle("Internet Connectivity");
            bd.setMessage("Active internet connection is required to work on this app and your phone is not connected. " +
                    "\n\nEnable Wifi?");
            //adding options to the button
            bd.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface di, int i){
                    Intent x = new Intent(Intent.ACTION_MAIN, null);
                    x.addCategory(Intent.CATEGORY_LAUNCHER);
                    ComponentName c = new ComponentName("com.android.settings", "com.android.settings.wifi.WifiSettings");
                    x.setComponent(c);
                    x.setFlags(x.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(x);
                }
            });
            bd.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                    System.exit(0);
                }
            });
            bd.create().show();
        }//End Check internet connection and open settings menu...

        mAuth = FirebaseAuth.getInstance();

        txtEmail = (EditText) findViewById(R.id.txtEmail);
        txtPassword = (EditText) findViewById(R.id.txtPassword);
        progressBar = (ProgressBar) findViewById(R.id.login_progress);

        findViewById(R.id.lblSignup).setOnClickListener(this);
        findViewById(R.id.lblReset).setOnClickListener(this);
        findViewById(R.id.btnLogin).setOnClickListener(this);

        //signout if the app was not active and created again
        FirebaseAuth.getInstance().signOut();

    }

    private void userLogin() {
        String email = txtEmail.getText().toString().trim();
        String password = txtPassword.getText().toString().trim();

        if (email.isEmpty()) {
            txtEmail.setError("Email is required");
            txtEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            txtEmail.setError("Please enter a valid email");
            txtEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            txtPassword.setError("Password is required");
            txtPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            txtPassword.setError("At least 6 characters");
            txtPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Login Sucessful", Toast.LENGTH_SHORT).show();
                    finish();
                    Intent i = new Intent(LoginActivity.this, edu.utep.cs.cs4330.firebaseproject.MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//keeping track of activity stack
                    startActivity(i);
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mAuth.getCurrentUser() != null) {
            finish();
            Intent i = new Intent(LoginActivity.this, edu.utep.cs.cs4330.firebaseproject.MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//keeping track of activity stack
            startActivity(i);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.lblSignup:
                finish();
                Intent i = new Intent(LoginActivity.this, edu.utep.cs.cs4330.firebaseproject.Signup.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//keeping track of activity stack
                startActivity(i);
                break;

            case R.id.btnLogin:
                userLogin();
                break;

            case R.id.lblReset:
                //password reset email.
                String email = txtEmail.getText().toString().trim();
                //check
                if (txtEmail.getText().toString().equals("")) {
                    Toast.makeText(getApplication(), "Enter your registered email id", Toast.LENGTH_SHORT).show();
                    txtEmail.requestFocus();
                    return;
                }
                //proceed
                progressBar.setVisibility(View.VISIBLE);
                mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), "Password reset email sent!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Failed to send reset email!", Toast.LENGTH_SHORT).show();
                                }
                                progressBar.setVisibility(View.GONE);
                            }
                        });
                break;
        }
    }
    //checking network status...
    public boolean networkConnected(Context c){
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(c.CONNECTIVITY_SERVICE);
        NetworkInfo n = cm.getActiveNetworkInfo();

        if(n!=null && n.isConnectedOrConnecting()){
            android.net.NetworkInfo WiFi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            android.net.NetworkInfo Mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            //Either wifi or mobile network, one of them should be available.
            if(WiFi!=null && WiFi.isConnectedOrConnecting() || (Mobile!=null && Mobile.isConnectedOrConnecting())){//isConnectedOrConnecting()
                connectionStatus = true;
                Toast.makeText(getApplicationContext(), "App connected to the internet", Toast.LENGTH_SHORT).show();
                return true;
            }else{
                return false;
            }
        }
        return false;
    }
}
