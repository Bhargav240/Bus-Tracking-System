package com.example.busmobileapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class StudentLoginActivity extends AppCompatActivity {

    Button forgot, reset, loginstu;
    private EditText email, password;
    ProgressBar progressBar;
    TextView forgott;
    EditText forgotemail;
    private FirebaseAuth auth;
    private static final String TAG = "Login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_login);

        forgot = findViewById(R.id.buttonForgot);
        forgotemail = findViewById(R.id.emailforgot);
        reset = findViewById(R.id.buttonReset);
        forgott = findViewById(R.id.textforgot);
        loginstu = findViewById(R.id.buttonLoginstu);
        email = findViewById(R.id.emailstuin);
        password = findViewById(R.id.passwordin);
        progressBar = findViewById(R.id.progressBarl);

        forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                forgot.setVisibility(View.GONE);
                forgotemail.setVisibility(View.VISIBLE);
                reset.setVisibility(View.VISIBLE);
                forgott.setVisibility(View.VISIBLE);
            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nemail = forgotemail.getText().toString();

                if(TextUtils.isEmpty(nemail)){
                    Toast.makeText(StudentLoginActivity.this,"Please Enter your Email",Toast.LENGTH_LONG).show();
                    email.setError("Email is Required");
                    email.requestFocus();
                }
                else if(!Patterns.EMAIL_ADDRESS.matcher(nemail).matches()){
                    Toast.makeText(StudentLoginActivity.this,"Please Re-Enter your Email",Toast.LENGTH_LONG).show();
                    email.setError("Vaild Email is Required");
                    email.requestFocus();
                }
                else{
                    progressBar.setVisibility(View.VISIBLE);
                    resetPassword(nemail);
                }
                resetPassword(nemail);

            }
        });

        auth = FirebaseAuth.getInstance();

        loginstu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String femail = email.getText().toString();
                String pass = password.getText().toString();
                if (TextUtils.isEmpty(femail)) {
                    Toast.makeText(StudentLoginActivity.this, "Please Enter your Email", Toast.LENGTH_LONG).show();
                    email.setError("Email is Required");
                    email.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(femail).matches()) {
                    Toast.makeText(StudentLoginActivity.this, "Please Re-Enter your Email", Toast.LENGTH_LONG).show();
                    email.setError("Valid Email is Required");
                    email.requestFocus();
                } else if (TextUtils.isEmpty(pass)) {
                    Toast.makeText(StudentLoginActivity.this, "Please Enter the Password", Toast.LENGTH_LONG).show();
                    password.setError("Password is Required");
                    password.requestFocus();
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    loginUser(femail, pass);
                }
            }
        });
    }

    private void loginUser(String mail, String pwd) {
        auth.signInWithEmailAndPassword(mail, pwd).addOnCompleteListener(StudentLoginActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Intent intent = new Intent(StudentLoginActivity.this, StudentMainActivity.class);
                    startActivity(intent);
                    finish();
                    finishAffinity();
                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    if (firebaseUser != null) {
                        if (firebaseUser.isEmailVerified()) {
                            Toast.makeText(StudentLoginActivity.this, "You Successfully Logged in!!", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(StudentLoginActivity.this, "Please verify your Email and login again!!", Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthInvalidUserException e) {
                        email.setError("User does not exist or is no longer valid. Please register again");
                        email.requestFocus();
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        password.setError("Invalid Credentials. Enter Correct Email and Password");
                        password.requestFocus();
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(StudentLoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }
    private void resetPassword( String nemail) {
        auth = FirebaseAuth.getInstance();
        auth.sendPasswordResetEmail(nemail).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(StudentLoginActivity.this,"Please check your inbox for password reset link!!",Toast.LENGTH_LONG).show();
                    forgotemail.setVisibility(View.GONE);
                    reset.setVisibility(View.GONE);
                    forgott.setVisibility(View.GONE);
                    forgot.setVisibility(View.VISIBLE);
                }
                else{
                    try{
                        throw task.getException();
                    }catch (FirebaseAuthInvalidUserException e){
                        email.setError("User does not exist or no longer valid. Please register again");
                    }catch(Exception e){
                        Log.e(TAG,e.getMessage());
                        Toast.makeText(StudentLoginActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
                    }

                }
                progressBar.setVisibility(View.GONE);

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (auth.getCurrentUser() != null) {
            Intent intent = new Intent(StudentLoginActivity.this, StudentMainActivity.class);
            startActivity(intent);
            finish();
            finishAffinity();
        } else {
            Toast.makeText(StudentLoginActivity.this, "Please Login", Toast.LENGTH_LONG).show();
        }
    }
}
