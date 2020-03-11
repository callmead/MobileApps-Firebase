package edu.utep.cs.cs4330.firebaseproject;

import android.content.Intent;
import android.support.annotation.NonNull;
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
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class Signup extends AppCompatActivity  implements View.OnClickListener {
    ProgressBar progressBar1;
    EditText txtEmail1, txtPassword1;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        txtEmail1 = (EditText) findViewById(R.id.txtEmail1);
        txtPassword1= (EditText) findViewById(R.id.txtPassword1);
        progressBar1 = (ProgressBar) findViewById(R.id.login_progress1);

        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.btnSignup).setOnClickListener(this);
        findViewById(R.id.lblCancel).setOnClickListener(this);
    }

    private void registerUser() {
        String email = txtEmail1.getText().toString().trim();
        String password = txtPassword1.getText().toString().trim();

        if (email.isEmpty()) {
            txtEmail1.setError("Email is required");
            txtEmail1.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            txtEmail1.setError("Please enter a valid email");
            txtEmail1.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            txtPassword1.setError("Password is required");
            txtPassword1.requestFocus();
            return;
        }

        if (password.length() < 6) {
            txtPassword1.setError("Minimum lenght of password should be 6");
            txtPassword1.requestFocus();
            return;
        }

        progressBar1.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                //progressBar1.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    progressBar1.setVisibility(View.GONE);
                    finish();
                    Toast.makeText(getApplicationContext(), "User created, signing in...", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(Signup.this, edu.utep.cs.cs4330.firebaseproject.MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//keeping track of activity stack
                    startActivity(i);
                } else {
                    progressBar1.setVisibility(View.GONE);
                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                        Toast.makeText(getApplicationContext(), "User email already registered", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSignup:
                registerUser();
                break;

            case R.id.lblCancel:
                finish();
                Intent i = new Intent(Signup.this, edu.utep.cs.cs4330.firebaseproject.LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//keeping track of activity stack
                startActivity(i);
                break;
        }
    }
    @Override
    public void onBackPressed() {
        //moveTaskToBack(true);
        finish();
        Intent i = new Intent(Signup.this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//keeping track of activity stack
        startActivity(i);
    }
}
