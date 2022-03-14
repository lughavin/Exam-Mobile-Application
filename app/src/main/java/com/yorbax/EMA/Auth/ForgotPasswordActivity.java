package com.yorbax.EMA.Auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.yorbax.EMA.R;

import static com.yorbax.EMA.Constants.Constants.mAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    MaterialButton btnSignInBack,btnSubmitEmail;
    TextInputEditText edEmail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        init();
        listeners();
    }

    private void listeners() {
        btnSignInBack.setOnClickListener(view -> finish());

        btnSubmitEmail.setOnClickListener(view -> {
            if(edEmail.getText().toString().equals("")){
                edEmail.setError("Required!");
            }else
                sendEmail();
        });
    }

    private void sendEmail() {
        mAuth.sendPasswordResetEmail(edEmail.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ForgotPasswordActivity.this, "We have sent you instructions to reset your password!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(ForgotPasswordActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void init() {
        btnSignInBack = findViewById(R.id.btn_signin_back);
        btnSubmitEmail = findViewById(R.id.btn_submit);
        edEmail = findViewById(R.id.txt_email);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.nothing, R.anim.slide_down);
    }
}