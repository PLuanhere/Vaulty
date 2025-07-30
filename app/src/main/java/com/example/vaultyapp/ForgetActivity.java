package com.example.vaultyapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class ForgetActivity extends AppCompatActivity {

    private EditText edtEmail;
    private Button btnSend;
    private TextView tvBackLogin;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget);

        edtEmail = findViewById(R.id.edtEmail);
        btnSend = findViewById(R.id.btnSend);
        tvBackLogin = findViewById(R.id.tvBackLogin);

        mAuth = FirebaseAuth.getInstance();

        btnSend.setOnClickListener(v -> sendResetPassword());

        tvBackLogin.setOnClickListener(v -> {
            Intent intent = new Intent(ForgetActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void sendResetPassword() {
        String email = edtEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Vui lòng nhập email");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Email không hợp lệ");
            return;
        }

        btnSend.setEnabled(false);
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    btnSend.setEnabled(true);
                    if (task.isSuccessful()) {
                        Toast.makeText(ForgetActivity.this, "Đã gửi email đặt lại mật khẩu. Vui lòng kiểm tra hộp thư!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(ForgetActivity.this, "Gửi email thất bại: " + (task.getException() == null ? "" : task.getException().getMessage()), Toast.LENGTH_LONG).show();
                    }
                });
    }
}