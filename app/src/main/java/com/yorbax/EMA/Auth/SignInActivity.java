package com.yorbax.EMA.Auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.pixplicity.easyprefs.library.Prefs;
import com.yorbax.EMA.HomeActivity;
import com.yorbax.EMA.Model.UserModel;
import com.yorbax.EMA.R;
import com.yorbax.EMA.admin.activity.DashboardAdmin;
import com.yorbax.EMA.admin.model.LectureModel;
import com.yorbax.EMA.lecturer.activity.DashboardLecturer;

import io.paperdb.Paper;

import static com.yorbax.EMA.Constants.Constants.currentUser;
import static com.yorbax.EMA.Constants.Constants.lectureCurrentUser;
import static com.yorbax.EMA.Constants.Constants.mAuth;
import static com.yorbax.EMA.Constants.Constants.mDatabase;
import static com.yorbax.EMA.Utils.HelperMethods.saveUserData;
import static com.yorbax.EMA.Utils.HelperMethods.saveUserLecturerData;

public class SignInActivity extends AppCompatActivity {
    private static final String TAG = "SignIn";
    MaterialButton btnSignIn, btnSignUp;
    TextView btnForgotPass;
    TextInputEditText edEmail, edPass;
    Switch switchAvailable;
    MaterialButtonToggleGroup toggleBtns;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        init();
        listeners();

        boolean data = Prefs.getBoolean("is_login", false);
//        if (data) {
//            startActivity(new Intent(SignInActivity.this, DashboardLecturer.class));
//            finish();
//        }
    }

    private void listeners() {
        btnSignIn.setOnClickListener(view -> {
            if (edEmail.getText().toString().equals("")) {
                edEmail.setError("Required");
                return;
            }
            if (edPass.getText().toString().equals("")) {
                edPass.setError("Required");
                return;
            }
            if (getType() == 1) {
                authListener();
            } else if (getType() == 2) {
                authListenerLecturer();
            } else {
                if (edEmail.getText().toString().equals("admin") && edPass.getText().toString().equals("admin")) {
                    Intent intent = new Intent(SignInActivity.this, DashboardAdmin.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_up, R.anim.nothing);
                }

            }
        });

        btnSignUp.setOnClickListener(view -> {
            Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_up, R.anim.nothing);
        });

        btnForgotPass.setOnClickListener(view -> {
            Intent intent = new Intent(SignInActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_up, R.anim.nothing);
        });
    }

    private void authListenerLecturer() {
        Paper.init(this);
        mAuth.signInWithEmailAndPassword(edEmail.getText().toString(), edPass.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("MyApp", "signInWithEmail:success");
                            if (switchAvailable.isChecked()) {
                                Prefs.putBoolean("is_login", true);
                            }
                            FirebaseUser user = mAuth.getCurrentUser();
                            mDatabase.child("users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        Log.e(TAG, "onDataChange: Key" + dataSnapshot.getKey());
                                        LectureModel userModel = dataSnapshot.getValue(LectureModel.class);
                                        Log.e(TAG, "onDataChange: " + userModel.email);
                                        lectureCurrentUser = userModel;
                                        int type = getType();
                                        if (lectureCurrentUser.loginType == type) {
                                            //Save in Paper
                                            saveUserLecturerData(lectureCurrentUser);
                                            if (lectureCurrentUser != null) {
                                                startActivity(new Intent(SignInActivity.this, DashboardLecturer.class));
                                                finish();
                                            } else {
                                                Toast.makeText(SignInActivity.this, "An Error Occurred ", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    } else {
                                        Toast.makeText(SignInActivity.this, "An Error Occurred ", Toast.LENGTH_SHORT).show();
                                        lectureCurrentUser = null;
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    lectureCurrentUser = null;
                                    Toast.makeText(SignInActivity.this, "Error Occurred : " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });


                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("MyApp", "signInWithEmail:failure", task.getException());
                            Toast.makeText(getApplicationContext(), task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void authListener() {
        Paper.init(this);
        mAuth.signInWithEmailAndPassword(edEmail.getText().toString(), edPass.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("MyApp", "signInWithEmail:success");
                            if (switchAvailable.isChecked()) {
                                Prefs.putBoolean("is_login", true);
                            }
                            FirebaseUser user = mAuth.getCurrentUser();
                            mDatabase.child("users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        Log.e(TAG, "onDataChange: Key" + dataSnapshot.getKey());
                                        UserModel userModel = dataSnapshot.getValue(UserModel.class);
                                        Log.e(TAG, "onDataChange: " + userModel.getEmail());
                                        currentUser = userModel;
                                        int type = getType();
                                        if (currentUser.getLoginType() == type) {
                                            //Save in Paper
                                            saveUserData(currentUser);
                                            if (currentUser != null) {
                                                startActivity(new Intent(SignInActivity.this, HomeActivity.class));
                                                finish();
                                            } else {
                                                Toast.makeText(SignInActivity.this, "An Error Occurred ", Toast.LENGTH_SHORT).show();
                                            }
                                        }else {
                                            Toast.makeText(SignInActivity.this, "Wrong username or password ", Toast.LENGTH_SHORT).show();
                                            currentUser = null;
                                        }
                                    } else {
                                        Toast.makeText(SignInActivity.this, "An Error Occurred ", Toast.LENGTH_SHORT).show();
                                        currentUser = null;
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    currentUser = null;
                                    Toast.makeText(SignInActivity.this, "Error Occurred : " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
//                            startActivity(new Intent(SignInActivity.this, HomeActivity.class));
//                            finish();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("MyApp", "signInWithEmail:failure", task.getException());
                            Toast.makeText(getApplicationContext(), task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private int getType() {
        switch (toggleBtns.getCheckedButtonId()) {
            case R.id.ck_admin:
                return 3;
            case R.id.ck_lecturer:
                return 2;
            default:
                return 1;
        }
    }

    private void init() {
        btnSignIn = findViewById(R.id.btn_signin);
        btnSignUp = findViewById(R.id.btn_signup);
        toggleBtns = findViewById(R.id.toggle_btns);
        btnForgotPass = findViewById(R.id.btn_forgotPass);
        edEmail = findViewById(R.id.txt_email);
        edPass = findViewById(R.id.txt_pass);
        switchAvailable = findViewById(R.id.switch_available);
    }

}