package com.example.vaultyapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class NewPasswordActivity extends AppCompatActivity {

    private EditText edtApp, edtUser, edtPassword, edtContent;
    private TextView tvSave, tvCancle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_password);

        edtApp = findViewById(R.id.edtpen);
        edtUser = findViewById(R.id.edtUser);
        edtPassword = findViewById(R.id.edtPassword);
        edtContent = findViewById(R.id.edtContent);
        tvSave = findViewById(R.id.tvSave);
        tvCancle = findViewById(R.id.tvCancle);

        tvSave.setOnClickListener(v -> saveAccount());
        tvCancle.setOnClickListener(v -> finish());
        findViewById(R.id.back).setOnClickListener(v -> finish());
    }

    private void saveAccount() {
        String appname = edtApp.getText().toString().trim();
        String username = edtUser.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String content = edtContent.getText().toString().trim();

        if (TextUtils.isEmpty(appname) || TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Điền đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> account = new HashMap<>();
        account.put("appname", appname);
        account.put("username", username);
        account.put("password", password);
        account.put("content", content);

        FirebaseFirestore.getInstance()
                .collection("userData")
                .document(user.getUid())
                .update("account", FieldValue.arrayUnion(account))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã thêm!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(NewPasswordActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Nếu chưa có document hoặc field account (lần đầu), thì tạo mới
                    Map<String, Object> newUserData = new HashMap<>();
                    newUserData.put("account", java.util.Arrays.asList(account));
                    FirebaseFirestore.getInstance()
                            .collection("userData")
                            .document(user.getUid())
                            .set(newUserData)
                            .addOnSuccessListener(aVoid2 -> {
                                Toast.makeText(NewPasswordActivity.this, "Đã thêm!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(NewPasswordActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e2 -> Toast.makeText(this, "Lỗi: " + e2.getMessage(), Toast.LENGTH_SHORT).show());
                });
    }
}