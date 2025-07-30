package com.example.vaultyapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.*;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private EditText edtAccount, edtPassword;
    private Button buttonLog;
    private TextView tvAccountError, tvPasswordError;
    private TextView tvSign;
    private boolean isPasswordVisible = false;
    private LinearLayout fbBtn, googleBtn, appleBtn;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private TextView tvForget;

    private static final int RC_SIGN_IN = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startMain();
            return;
        }

        setContentView(R.layout.login_page);

        mAuth = FirebaseAuth.getInstance();

        edtAccount = findViewById(R.id.edtPhone); // Chỉ dùng 1 trường cho email hoặc sdt
        edtPassword = findViewById(R.id.edtPassword);
        buttonLog = findViewById(R.id.buttonLog);
        tvSign = findViewById(R.id.tvSign);

        tvAccountError = findViewById(R.id.tvAccountError);
        tvPasswordError = findViewById(R.id.tvPasswordError);

        // Set error màu đỏ
        tvAccountError.setTextColor(Color.parseColor("#D32F2F"));
        tvPasswordError.setTextColor(Color.parseColor("#D32F2F"));

        // Ẩn/hiện mật khẩu
        edtPassword.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
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



        buttonLog.setOnClickListener(v -> loginUser());

        tvSign.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        // Google Sign In setup
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        fbBtn = findViewById(R.id.btnFacebook);
        googleBtn = findViewById(R.id.btnGoogle);
        appleBtn = findViewById(R.id.btnApple);
        tvForget = findViewById(R.id.tvForget);

        googleBtn.setOnClickListener(v -> signInWithGoogle());
        fbBtn.setOnClickListener(v -> Toast.makeText(this, "Đang phát triển!", Toast.LENGTH_SHORT).show());
        appleBtn.setOnClickListener(v -> Toast.makeText(this, "Đang phát triển!", Toast.LENGTH_SHORT).show());
        tvForget.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgetActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String account = edtAccount.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        tvAccountError.setText("");
        tvPasswordError.setText("");
        tvAccountError.setVisibility(View.GONE);
        tvPasswordError.setVisibility(View.GONE);

        if (TextUtils.isEmpty(account)) {
            edtAccount.setError("Vui lòng nhập email hoặc số điện thoại");
            return;
        }

        // Đăng nhập bằng Email
        if (Patterns.EMAIL_ADDRESS.matcher(account).matches()) {
            if (TextUtils.isEmpty(password)) {
                edtPassword.setError("Vui lòng nhập mật khẩu");
                return;
            }
            mAuth.signInWithEmailAndPassword(account, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null && !user.isEmailVerified()) {
                                mAuth.signOut();
                                Toast.makeText(this, "Bạn cần xác thực email trước khi đăng nhập!", Toast.LENGTH_LONG).show();
                                return;
                            }
                            startMain();
                        } else {
                            Exception e = task.getException();
                            if (e instanceof FirebaseAuthInvalidUserException) {
                                tvAccountError.setText("Tài khoản không tồn tại");
                                tvAccountError.setVisibility(View.VISIBLE);
                            } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                                tvPasswordError.setText("Sai mật khẩu");
                                tvPasswordError.setVisibility(View.VISIBLE);
                            } else {
                                Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
        // Đăng nhập bằng SĐT (OTP)
        else if (isValidPhoneNumber(account)) {
            String phone = standardizeVietnamPhone(account);
            startPhoneAuthFlow(phone);
        }
        // Không hợp lệ
        else {
            tvAccountError.setText("Vui lòng nhập email hoặc số điện thoại hợp lệ.");
            tvAccountError.setVisibility(View.VISIBLE);
        }
    }

    private boolean isValidPhoneNumber(String phone) {
        phone = standardizeVietnamPhone(phone);
        return phone.matches("^\\+84[0-9]{9,10}$") || phone.matches("^\\+\\d{10,15}$");
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

    // Đăng nhập bằng SĐT qua OTP Firebase Auth
    private void startPhoneAuthFlow(String phone) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phone)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                                signInWithPhoneAuthCredential(credential);
                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                tvAccountError.setText("Số điện thoại không hợp lệ hoặc đã bị chặn.");
                                tvAccountError.setVisibility(View.VISIBLE);
                                Toast.makeText(LoginActivity.this, "Xác minh thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onCodeSent(@NonNull String verificationId,
                                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                                super.onCodeSent(verificationId, token);
                                showOTPDialog(verificationId);
                            }
                        })
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    // Hiển thị dialog nhập OTP
    private void showOTPDialog(String verificationId) {
        View view = getLayoutInflater().inflate(R.layout.dialog_otp, null);
        EditText edtOtp = view.findViewById(R.id.edtOtp);
        Button btnConfirm = view.findViewById(R.id.btnOtpConfirm);
        Button btnCancel = view.findViewById(R.id.btnOtpCancel);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .create();

        btnConfirm.setOnClickListener(v -> {
            String code = edtOtp.getText().toString().trim();
            if (TextUtils.isEmpty(code)) {
                edtOtp.setError("Vui lòng nhập mã OTP");
            } else {
                verifyPhoneNumberWithCode(verificationId, code);
                dialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        startMain();
                    } else {
                        tvAccountError.setText("Đăng nhập SĐT thất bại!");
                        tvAccountError.setVisibility(View.VISIBLE);
                        Toast.makeText(LoginActivity.this, "Đăng nhập với SĐT thất bại: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode, Intent data) {
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
        Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Xoá stack login
        startActivity(intent);
        finish();
    }
}