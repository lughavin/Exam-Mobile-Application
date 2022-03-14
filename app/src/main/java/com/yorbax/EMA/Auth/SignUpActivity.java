package com.yorbax.EMA.Auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.yorbax.EMA.Model.UserModel;
import com.yorbax.EMA.R;

import static com.yorbax.EMA.Constants.Constants.mAuth;
import static com.yorbax.EMA.Constants.Constants.mDatabase;

public class SignUpActivity extends AppCompatActivity {

    TextInputEditText edEmail, edPass, edPass2, edUsername;
    MaterialButton btnSignUp, btnBackSignIn;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        init();
        listerners();
        databaseReference = mDatabase;
    }


    private void signUpListener() {
        mAuth.createUserWithEmailAndPassword(edEmail.getText().toString(), edPass.getText().toString())
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("MyApp", "createUserWithEmail:success");
                        Toast.makeText(SignUpActivity.this, "SignUp Successful",
                                Toast.LENGTH_SHORT).show();
                        FirebaseUser user = mAuth.getCurrentUser();
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(edUsername.getText().toString())
                                .build();
                        user.updateProfile(profileUpdates)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        UserModel userModel = new UserModel() ;
                                        userModel.setEmail(edEmail.getText().toString());
                                        userModel.setLoginType(1);
                                        userModel.setUserId(user.getUid());
                                        userModel.setUsername(edUsername.getText().toString());
                                        databaseReference.child("users").child(userModel.getUserId()).setValue(userModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful())
                                                {
                                                    Toast.makeText(SignUpActivity.this, "User Created Successfully!", Toast.LENGTH_SHORT).show();
                                                    finish();
                                                }else{
                                                    Toast.makeText(SignUpActivity.this, "Something goes wrong please try again", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    } else {
                                        Toast.makeText(this, "An Error Occurred!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.e("rss", task.getException().getMessage());
                        Toast.makeText(SignUpActivity.this, task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void listerners() {
        btnSignUp.setOnClickListener(view -> {
            if (edUsername.getText().toString().equals("")) {
                edUsername.setError("Required!");
                return;
            }
            if (edEmail.getText().toString().equals("")) {
                edEmail.setError("Required!");
                return;
            }
            if (edPass.getText().toString().equals("")) {
                edPass.setError("Required!");
                return;
            }
            if (edPass2.getText().toString().equals("")) {
                edPass2.setError("Required!");
                return;
            }

            if (edPass.getText().toString().equals(edPass2.getText().toString())) {
                signUpListener();
            } else {
                Toast.makeText(SignUpActivity.this, "Password must match!", Toast.LENGTH_SHORT).show();
            }
        });

        btnBackSignIn.setOnClickListener(view -> finish());
    }

    private void init() {
        edEmail = findViewById(R.id.txt_email);
        edUsername = findViewById(R.id.txt_username);
        edPass = findViewById(R.id.txt_pass);
        edPass2 = findViewById(R.id.txt_pass_2);
        btnSignUp = findViewById(R.id.btn_signup);
        btnBackSignIn = findViewById(R.id.btn_signin_back);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.nothing, R.anim.slide_down);
    }
}