package com.example.vaultyapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;

public class LoginActivity extends AppCompatActivity {

    private EditText edtPhone, edtPassword;
    private Button buttonLog;
    private TextView tvAccountError, tvPasswordError;
    private TextView tvSign;
    private ImageView imgTogglePassword;
    private boolean isPasswordVisible = false;
    private LinearLayout fbBtn, googleBtn, appleBtn;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    private static final int RC_SIGN_IN = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        mAuth = FirebaseAuth.getInstance();

        edtPhone = findViewById(R.id.edtPhone);
        edtPassword = findViewById(R.id.edtPassword);
        buttonLog = findViewById(R.id.buttonLog);
        tvSign = findViewById(R.id.tvSign);

        // Thêm TextView báo lỗi dưới các trường
        tvAccountError = findViewById(R.id.tvAccountError);
        tvPasswordError = findViewById(R.id.tvPasswordError);

        // Set error màu đỏ
        tvAccountError.setTextColor(Color.parseColor("#D32F2F"));
        tvPasswordError.setTextColor(Color.parseColor("#D32F2F"));

        // Ẩn/hiện mật khẩu
        edtPassword.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2; // drawableEnd
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (edtPassword.getRight() - edtPassword.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    isPasswordVisible = !isPasswordVisible;
                    if (isPasswordVisible) {
                        edtPassword.setTransformationMethod(null);
                        edtPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        edtPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.icon_eyeopen, 0);
                    } else {
                        edtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        edtPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.ic_eyeclosed, 0);
                    }
                    edtPassword.setSelection(edtPassword.getText().length());
                    return true;
                }
            }
            return false;
        });

        // Xử lý Login
        buttonLog.setOnClickListener(v -> loginUser());

        // Đăng ký
        tvSign.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        // Google Sign In setup
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Lấy từ google-services.json
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Social buttons
        fbBtn = findViewById(R.id.btnFacebook);
        googleBtn = findViewById(R.id.btnGoogle);
        appleBtn = findViewById(R.id.btnApple);

        googleBtn.setOnClickListener(v -> signInWithGoogle());
        fbBtn.setOnClickListener(v -> Toast.makeText(this, "Đang phát triển!", Toast.LENGTH_SHORT).show());
        appleBtn.setOnClickListener(v -> Toast.makeText(this, "Đang phát triển!", Toast.LENGTH_SHORT).show());
    }

    private void loginUser() {
        String account = edtPhone.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        tvAccountError.setText("");
        tvPasswordError.setText("");
        tvAccountError.setVisibility(View.GONE);
        tvPasswordError.setVisibility(View.GONE);

        if (TextUtils.isEmpty(account)) {
            edtPhone.setError("Vui lòng nhập email hoặc số điện thoại");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            edtPassword.setError("Vui lòng nhập mật khẩu");
            return;
        }

        if (Patterns.EMAIL_ADDRESS.matcher(account).matches()) {
            // Đăng nhập bằng email
            mAuth.signInWithEmailAndPassword(account, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Đăng nhập thành công
                            startMain();
                        } else {
                            Exception e = task.getException();
                            if (e instanceof FirebaseAuthInvalidUserException) {
                                edtPhone.setError("Tài khoản không tồn tại");
                            } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                                edtPassword.setError("Sai mật khẩu");
                            } else {
                                Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            // Đăng nhập bằng SĐT
            String phone = standardizeVietnamPhone(account);
            mAuth.fetchSignInMethodsForEmail(phone + "@vaulty.fake") // Trick: tạo mapping phone->email nếu bạn dùng phone làm email
                    .addOnCompleteListener(methodTask -> {
                        if (methodTask.isSuccessful() && !methodTask.getResult().getSignInMethods().isEmpty()) {
                            // Có tài khoản với SĐT này (email mapping)
                            mAuth.signInWithEmailAndPassword(phone + "@vaulty.fake", password)
                                    .addOnCompleteListener(this, task -> {
                                        if (task.isSuccessful()) {
                                            startMain();
                                        } else {
                                            Exception e = task.getException();
                                            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                                                edtPassword.setError("Sai mật khẩu");
                                            } else {
                                                Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            edtPhone.setError("Tài khoản không tồn tại");
                        }
                    });
        }
    }

    private String standardizeVietnamPhone(String input) {
        String phone = input.trim();
        if (phone.startsWith("+")) {
            return phone;
        }
        if (phone.startsWith("0") && phone.length() >= 10) {
            return "+84" + phone.substring(1);
        }
        if (phone.matches("^[1-9][0-9]{8,}$")) {
            return "+84" + phone;
        }
        return phone;
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Đăng nhập Google thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        startMain();
                    } else {
                        Toast.makeText(this, "Đăng nhập Google thất bại!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void startMain() {
        // Chuyển sang MainActivity hoặc trang chính của bạn
        Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
        // startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}