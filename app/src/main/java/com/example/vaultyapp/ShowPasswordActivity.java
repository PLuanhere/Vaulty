package com.example.vaultyapp;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShowPasswordActivity extends AppCompatActivity {

    private boolean isEditing = false;
    private boolean isPasswordVisible = false;

    private String originalAppname, originalUsername, originalPassword, originalContent;

    // View cho từng trường
    private TextView titleShowPass, tvFix, tvDelete;
    private TextView tvUserShow, tvPassShow;
    private EditText edtUserShow, edtPassShow, edtContent;
    private ImageView iconEye;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_password);

        // Nhận dữ liệu
        originalAppname = getIntent().getStringExtra("appname");
        originalUsername = getIntent().getStringExtra("username");
        originalPassword = getIntent().getStringExtra("password");
        originalContent = getIntent().getStringExtra("content");

        // Ánh xạ view
        titleShowPass = findViewById(R.id.titleShowPass);
        tvFix = findViewById(R.id.tvFix);
        tvDelete = findViewById(R.id.tvDelete);

        tvUserShow = findViewById(R.id.tvUserShow);
        edtUserShow = findViewById(R.id.edtUserShow);
        tvPassShow = findViewById(R.id.tvPassShow);
        edtPassShow = findViewById(R.id.edtPassShow);
        edtContent = findViewById(R.id.edtContent);
        iconEye = findViewById(R.id.iconEye);

        // Hiển thị dữ liệu ban đầu
        titleShowPass.setText(originalAppname);
        tvUserShow.setText(originalUsername);
        edtUserShow.setText(originalUsername);
        tvPassShow.setText(maskPassword(originalPassword));
        edtPassShow.setText(originalPassword);
        edtContent.setText(originalContent);

        // Chế độ xem mặc định
        setViewMode();

        // Gắn chức năng back
        ImageView btnBack = findViewById(R.id.back);
        btnBack.setOnClickListener(v -> finish());

        // Gắn chức năng copy user
        findViewById(R.id.iconCopy).setOnClickListener(v -> {
            String usernameToCopy = isEditing ? edtUserShow.getText().toString() : tvUserShow.getText().toString();
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("username", usernameToCopy);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Đã copy tên người dùng", Toast.LENGTH_SHORT).show();
        });

        // Gắn chức năng copy mật khẩu
        iconEye.setOnClickListener(v -> {
            if (!isEditing) {
                // Ẩn/hiện mật khẩu ở chế độ xem
                isPasswordVisible = !isPasswordVisible;
                if (isPasswordVisible) {
                    tvPassShow.setText(originalPassword);
                    iconEye.setImageResource(R.drawable.icon_eyeopen); // Đảm bảo bạn có icon này
                } else {
                    tvPassShow.setText(maskPassword(originalPassword));
                    iconEye.setImageResource(R.drawable.ic_eyeclosed); // Đảm bảo bạn có icon này
                }
            } else {
                // Ẩn/hiện mật khẩu ở chế độ sửa
                isPasswordVisible = !isPasswordVisible;
                if (isPasswordVisible) {
                    edtPassShow.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    edtPassShow.setSelection(edtPassShow.getText().length());
                    iconEye.setImageResource(R.drawable.icon_eyeopen);
                } else {
                    edtPassShow.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    edtPassShow.setSelection(edtPassShow.getText().length());
                    iconEye.setImageResource(R.drawable.ic_eyeclosed);
                }
            }
        });

        // Ấn giữ iconEye để copy mật khẩu
        iconEye.setOnLongClickListener(v -> {
            String passwordToCopy = isEditing ? edtPassShow.getText().toString() : originalPassword;
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("password", passwordToCopy);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Đã copy mật khẩu", Toast.LENGTH_SHORT).show();
            return true;
        });

        // Xử lý nút xóa
        tvDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Xóa tài khoản")
                    .setMessage("Bạn có chắc muốn xóa tài khoản này?")
                    .setPositiveButton("Có", (dialog, which) -> {
                        deleteAccount(originalAppname, originalUsername, originalPassword, originalContent);
                    })
                    .setNegativeButton("Không", null)
                    .show();
        });

        // Xử lý nút Sửa/Lưu
        tvFix.setOnClickListener(v -> {
            if (!isEditing) {
                setEditMode();
            } else {
                saveChanges();
            }
        });
    }

    // Chuyển về chế độ xem: hiện TextView, ẩn EditText
    private void setViewMode() {
        isEditing = false;
        tvFix.setText("Sửa");

        tvUserShow.setVisibility(View.VISIBLE);
        edtUserShow.setVisibility(View.GONE);

        tvPassShow.setVisibility(View.VISIBLE);
        edtPassShow.setVisibility(View.GONE);

        edtContent.setEnabled(false);

        // Đặt lại trạng thái hiển thị mật khẩu
        isPasswordVisible = false;
        tvPassShow.setText(maskPassword(originalPassword));
        iconEye.setImageResource(R.drawable.ic_eyeclosed);
    }

    // Chuyển sang chế độ sửa: ẩn TextView, hiện EditText, copy giá trị hiện tại
    private void setEditMode() {
        isEditing = true;
        tvFix.setText("Lưu");

        tvUserShow.setVisibility(View.GONE);
        edtUserShow.setVisibility(View.VISIBLE);
        edtUserShow.setText(tvUserShow.getText());

        tvPassShow.setVisibility(View.GONE);
        edtPassShow.setVisibility(View.VISIBLE);
        edtPassShow.setText(originalPassword);

        edtContent.setEnabled(true);
        edtContent.requestFocus();

        // Đặt lại trạng thái hiển thị mật khẩu
        isPasswordVisible = false;
        edtPassShow.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        edtPassShow.setSelection(edtPassShow.getText().length());
        iconEye.setImageResource(R.drawable.ic_eyeclosed);
    }

    // Lưu thay đổi lên Firestore và chuyển lại về chế độ xem
    private void saveChanges() {
        String newAppname = originalAppname;
        String newUsername = edtUserShow.getText().toString();
        String newPassword = edtPassShow.getText().toString();
        String newContent = edtContent.getText().toString();

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance()
                .collection("userData")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<Map<String, Object>> accounts = (List<Map<String, Object>>) documentSnapshot.get("account");
                    if (accounts != null) {
                        // Xóa phần tử cũ
                        for (int i = 0; i < accounts.size(); i++) {
                            Map<String, Object> acc = accounts.get(i);
                            boolean matchApp = originalAppname.equals(acc.get("appname"));
                            boolean matchUser = originalUsername.equals(acc.get("username"));
                            boolean matchPass = originalPassword.equals(acc.get("password"));
                            boolean matchContent = (originalContent == null && acc.get("content") == null)
                                    || (originalContent != null && originalContent.equals(acc.get("content")));
                            if (matchApp && matchUser && matchPass && matchContent) {
                                accounts.remove(i);
                                break;
                            }
                        }

                        // Thêm phần tử mới (đã sửa)
                        Map<String, Object> newAccount = new HashMap<>();
                        newAccount.put("appname", newAppname);
                        newAccount.put("username", newUsername);
                        newAccount.put("password", newPassword);
                        newAccount.put("content", newContent);
                        accounts.add(newAccount);

                        Map<String, Object> update = new HashMap<>();
                        update.put("account", accounts);
                        FirebaseFirestore.getInstance()
                                .collection("userData")
                                .document(uid)
                                .update(update)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                                    // Cập nhật lại hiển thị và gốc cho lần sửa tiếp theo
                                    tvUserShow.setText(newUsername);
                                    edtUserShow.setText(newUsername);
                                    originalUsername = newUsername;

                                    tvPassShow.setText(maskPassword(newPassword));
                                    edtPassShow.setText(newPassword);
                                    originalPassword = newPassword;

                                    edtContent.setText(newContent);
                                    originalContent = newContent;

                                    setViewMode();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Lỗi khi cập nhật!", Toast.LENGTH_SHORT).show();
                                });
                    }
                });
    }

    // Hàm xóa phần tử khỏi Firestore (không đổi so với bạn)
    private void deleteAccount(String appname, String username, String password, String content) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance()
                .collection("userData")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<Map<String, Object>> accounts = (List<Map<String, Object>>) documentSnapshot.get("account");
                    if (accounts != null) {
                        for (int i = 0; i < accounts.size(); i++) {
                            Map<String, Object> acc = accounts.get(i);
                            boolean matchApp = appname.equals(acc.get("appname"));
                            boolean matchUser = username.equals(acc.get("username"));
                            boolean matchPass = password.equals(acc.get("password"));
                            boolean matchContent = (content == null && acc.get("content") == null) ||
                                    (content != null && content.equals(acc.get("content")));
                            if (matchApp && matchUser && matchPass && matchContent) {
                                accounts.remove(i);
                                break;
                            }
                        }
                        Map<String, Object> update = new HashMap<>();
                        update.put("account", accounts);
                        FirebaseFirestore.getInstance()
                                .collection("userData")
                                .document(uid)
                                .update(update)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(this, "Đã xóa tài khoản!", Toast.LENGTH_SHORT).show();
                                    // Quay về Main
                                    Intent intent = new Intent(this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Lỗi khi xóa tài khoản!", Toast.LENGTH_SHORT).show();
                                });
                    }
                });
    }

    // Hàm để ẩn mật khẩu dạng ••••
    private String maskPassword(String password) {
        if (password == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < password.length(); i++) sb.append("•");
        return sb.toString();
    }
}