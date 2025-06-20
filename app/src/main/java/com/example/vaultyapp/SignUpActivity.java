package com.example.vaultyapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {

    private EditText edtUser, edtPhone, edtPassword, edtPasswordAgain;
    private Button buttonSign;
    private ImageView back;
    private FirebaseAuth mAuth;

    // Phone Auth
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private ImageView imgTogglePassword, imgTogglePasswordAgain;
    private boolean isPasswordVisible = false;
    private boolean isPasswordAgainVisible = false;
    private TextView tvPasswordMatch;
    private TextView tvAccountExists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_page);

        edtUser = findViewById(R.id.edtUser);
        edtPhone = findViewById(R.id.edtPhone);
        edtPassword = findViewById(R.id.edtPassword);
        edtPasswordAgain = findViewById(R.id.edtPasswordAgain);
        buttonSign = findViewById(R.id.buttonSign);
        back = findViewById(R.id.back);

        mAuth = FirebaseAuth.getInstance();

        back.setOnClickListener(v -> finish());
        buttonSign.setOnClickListener(view -> signUpUser());

        edtPassword.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2; // drawableEnd
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (edtPassword.getRight() - edtPassword.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    isPasswordVisible = !isPasswordVisible;
                    if (isPasswordVisible) {
                        edtPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        edtPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.icon_eyeopen, 0);
                    } else {
                        edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        edtPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.ic_eyeclosed, 0);
                    }
                    edtPassword.setSelection(edtPassword.getText().length());
                    return true;
                }
            }
            return false;
        });

        // Xử lý hiện/ẩn mật khẩu trường nhập lại
        edtPasswordAgain.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (edtPasswordAgain.getRight() - edtPasswordAgain.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    isPasswordAgainVisible = !isPasswordAgainVisible;
                    if (isPasswordAgainVisible) {
                        edtPasswordAgain.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        edtPasswordAgain.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.icon_eyeopen, 0);
                    } else {
                        edtPasswordAgain.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        edtPasswordAgain.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.ic_eyeclosed, 0);
                    }
                    edtPasswordAgain.setSelection(edtPasswordAgain.getText().length());
                    return true;
                }
            }
            return false;
        });
    }



    private void signUpUser() {
        String username = edtUser.getText().toString().trim();
        String emailOrPhone = edtPhone.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String passwordAgain = edtPasswordAgain.getText().toString().trim();

        // Validate
        if (TextUtils.isEmpty(username)) {
            edtUser.setError("Vui lòng nhập tên người dùng");
            return;
        }
        if (TextUtils.isEmpty(emailOrPhone)) {
            edtPhone.setError("Vui lòng nhập email hoặc số điện thoại");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            edtPassword.setError("Vui lòng nhập mật khẩu");
            return;
        }
        if (!password.equals(passwordAgain)) {
            edtPasswordAgain.setError("Mật khẩu nhập lại không khớp");
            return;
        }
        if (password.length() < 6) {
            edtPassword.setError("Mật khẩu phải từ 6 ký tự trở lên");
            return;
        }

        // Phân biệt email hay số điện thoại
        if (Patterns.EMAIL_ADDRESS.matcher(emailOrPhone).matches()) {
            // Đăng ký bằng email
            registerWithEmail(emailOrPhone, password);
        } else {
            // Chuẩn hóa số điện thoại (người dùng không thấy)
            String phone = standardizeVietnamPhone(emailOrPhone);
            if (isValidPhoneNumber(phone)) {
                registerWithPhone(phone);
            } else {
                edtPhone.setError("Vui lòng nhập đúng email hoặc số điện thoại!");
            }
        }
    }

    // Chuẩn hóa số điện thoại Việt Nam về dạng +84xxxxxxxxx (người dùng không thấy, chỉ dùng trong code)
    private String standardizeVietnamPhone(String input) {
        String phone = input.trim();
        if (phone.startsWith("+")) {
            return phone;
        }
        // Nếu bắt đầu bằng "0" và đủ 10-11 số, chuyển thành +84
        if (phone.startsWith("0") && phone.length() >= 10) {
            return "+84" + phone.substring(1);
        }
        // Nếu chỉ nhập số, tự động thêm +84
        if (Pattern.matches("^[1-9][0-9]{8,}$", phone)) {
            return "+84" + phone;
        }
        return phone;
    }

    // Kiểm tra SĐT đúng định dạng +84xxxxxxxxx
    private boolean isValidPhoneNumber(String phone) {
        Pattern phonePattern = Pattern.compile("^\\+?[0-9]{9,15}$");
        return phonePattern.matcher(phone).matches();
    }

    // Đăng ký bằng Email
    private void registerWithEmail(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Đăng ký thành công, gửi xác minh email
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            user.sendEmailVerification()
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            showCheckEmailDialog();
                                        } else {
                                            Toast.makeText(SignUpActivity.this, "Lỗi gửi email xác minh: " + task1.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(SignUpActivity.this, "Đăng ký thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Đăng ký bằng SĐT (gửi mã xác minh)
    private void registerWithPhone(String phone) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phone) // Đã chuẩn hóa sẵn
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                                // Tự động xác minh (auto fill SMS OTP)
                                signInWithPhoneAuthCredential(credential);
                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                Toast.makeText(SignUpActivity.this, "Gửi mã xác minh thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onCodeSent(@NonNull String verificationId,
                                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                                super.onCodeSent(verificationId, token);
                                mVerificationId = verificationId;
                                mResendToken = token;

                                // Hiển thị dialog nhập mã OTP cho người dùng
                                showOTPDialog();
                            }
                        })
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    // Hàm xác thực OTP
    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    // Đăng nhập hoặc đăng ký với credential
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(SignUpActivity.this, "Xác minh số điện thoại thành công!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Xóa stack về login
                        startActivity(intent);
                    } else {
                        Toast.makeText(SignUpActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    // Hiển thị dialog nhập mã OTP
    private void showOTPDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_otp, null);

        EditText edtOtp = view.findViewById(R.id.edtOtp);
        Button btnConfirm = view.findViewById(R.id.btnOtpConfirm);
        Button btnCancel = view.findViewById(R.id.btnOtpCancel);

        builder.setView(view);
        android.app.AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        btnConfirm.setOnClickListener(v -> {
            String code = edtOtp.getText().toString().trim();
            if (TextUtils.isEmpty(code)) {
                edtOtp.setError("Vui lòng nhập mã OTP");
            } else {
                verifyPhoneNumberWithCode(mVerificationId, code);
                dialog.dismiss();

            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showCheckEmailDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_check_email, null);
        Button btnOk = view.findViewById(R.id.btnOk);

        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .create();

        btnOk.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        dialog.show();
    }
}