package com.example.vaultyapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private LinearLayout linearAccounts;
    private ImageView btnAdd, btnSort, btnClear, btnControl;
    private EditText edtSearch;

    private List<Map<String, Object>> allAccounts = new ArrayList<>();
    private int currentSortMode = 0; // 0: Mới nhất, 1: Cũ nhất, 2: A-Z, 3: Z-A
    private View dimOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        linearAccounts = findViewById(R.id.linearAccounts);
        btnAdd = findViewById(R.id.btnAdd);
        btnSort = findViewById(R.id.btnSort);
        edtSearch = findViewById(R.id.edtSearch);
        btnClear = findViewById(R.id.btnClear);
        btnControl = findViewById(R.id.btnControl);
        dimOverlay = findViewById(R.id.dimOverlay);

        loadAccountData();

        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, NewPasswordActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        btnControl.setOnClickListener(v -> showLogoutPopup());

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterAccounts(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnSort.setOnClickListener(v -> showSortPopup());

        btnClear.setOnClickListener(v -> showConfirmDeleteAllPopup());
    }

    private void loadAccountData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        FirebaseFirestore.getInstance()
                .collection("userData")
                .document(user.getUid())
                .addSnapshotListener((documentSnapshot, error) -> {
                    linearAccounts.removeAllViews();
                    allAccounts.clear();
                    if (error != null || documentSnapshot == null || !documentSnapshot.exists() || documentSnapshot.get("account") == null)
                        return;

                    List<Map<String, Object>> list = (List<Map<String, Object>>) documentSnapshot.get("account");
                    if (list != null) allAccounts.addAll(list);
                    filterAccounts(edtSearch.getText().toString());
                });
    }

    private void filterAccounts(String query) {
        // Copy và sort trước khi render
        List<Map<String, Object>> filtered = new ArrayList<>();
        String lowerQuery = query.toLowerCase(Locale.ROOT).trim();

        for (Map<String, Object> map : allAccounts) {
            String appname = (String) map.get("appname");
            if (lowerQuery.isEmpty() || (appname != null && appname.toLowerCase(Locale.ROOT).contains(lowerQuery))) {
                filtered.add(map);
            }
        }

        // Sắp xếp theo chế độ sort đang chọn
        switch (currentSortMode) {
            case 0: // Mới nhất (giả sử Firestore trả về theo thứ tự mới nhất, nếu có trường "createdAt" thì sort theo nó)
                Collections.reverse(filtered); // Đảo ngược để mới nhất lên đầu
                break;
            case 1: // Cũ nhất
                // Không cần đảo, mặc định là cũ nhất lên đầu
                break;
            case 2: // A-Z (appname)
                Collections.sort(filtered, (m1, m2) -> {
                    String a1 = (String) m1.get("appname");
                    String a2 = (String) m2.get("appname");
                    if (a1 == null) a1 = "";
                    if (a2 == null) a2 = "";
                    return a1.compareToIgnoreCase(a2);
                });
                break;
            case 3: // Z-A (appname)
                Collections.sort(filtered, (m1, m2) -> {
                    String a1 = (String) m1.get("appname");
                    String a2 = (String) m2.get("appname");
                    if (a1 == null) a1 = "";
                    if (a2 == null) a2 = "";
                    return a2.compareToIgnoreCase(a1);
                });
                break;
        }

        // Hiển thị ra giao diện
        linearAccounts.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (Map<String, Object> map : filtered) {
            String appname = (String) map.get("appname");
            View itemView = inflater.inflate(R.layout.item_account, linearAccounts, false);
            TextView tvAppname = itemView.findViewById(R.id.tvAppname);
            TextView tvUsername = itemView.findViewById(R.id.tvUsername);
            tvAppname.setText(appname);
            tvUsername.setText((String) map.get("username"));
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, ShowPasswordActivity.class);
                intent.putExtra("appname", appname);
                intent.putExtra("username", (String) map.get("username"));
                intent.putExtra("password", (String) map.get("password"));
                intent.putExtra("content", (String) map.get("content"));
                startActivity(intent);
            });
            linearAccounts.addView(itemView);
        }
    }

    // Hiện popup sort
    private void showSortPopup() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View popupView = inflater.inflate(R.layout.activity_sort, null, false);

        PopupWindow popupWindow = new PopupWindow(popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true);

        // Làm tối nền
        dimOverlay.setVisibility(View.VISIBLE);
        popupWindow.setOnDismissListener(() -> dimOverlay.setVisibility(View.GONE));

        // Lấy vị trí dưới btnSort
        int[] location = new int[2];
        btnSort.getLocationOnScreen(location);
        popupWindow.showAtLocation(btnSort, Gravity.NO_GRAVITY, location[0], location[1] + btnSort.getHeight());

        // Xử lý chọn sort
        popupView.findViewById(R.id.optionNewest).setOnClickListener(v -> {
            currentSortMode = 0;
            filterAccounts(edtSearch.getText().toString());
            popupWindow.dismiss();
        });
        popupView.findViewById(R.id.optionOldest).setOnClickListener(v -> {
            currentSortMode = 1;
            filterAccounts(edtSearch.getText().toString());
            popupWindow.dismiss();
        });
        popupView.findViewById(R.id.optionAZ).setOnClickListener(v -> {
            currentSortMode = 2;
            filterAccounts(edtSearch.getText().toString());
            popupWindow.dismiss();
        });
        popupView.findViewById(R.id.optionZA).setOnClickListener(v -> {
            currentSortMode = 3;
            filterAccounts(edtSearch.getText().toString());
            popupWindow.dismiss();
        });
    }

    private void showLogoutPopup() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View popupView = inflater.inflate(R.layout.popup_logout, null, false);

        PopupWindow popupWindow = new PopupWindow(popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true);

        // Làm tối nền
        dimOverlay.setVisibility(View.VISIBLE);
        popupWindow.setOnDismissListener(() -> dimOverlay.setVisibility(View.GONE));

        // Lấy vị trí dưới btnControl
        int[] location = new int[2];
        btnControl.getLocationOnScreen(location);
        popupWindow.showAtLocation(btnControl, Gravity.NO_GRAVITY, location[0], location[1] + btnControl.getHeight());

        // Xử lý đăng xuất
        TextView tvLogout = popupView.findViewById(R.id.tvLogout);
        tvLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            popupWindow.dismiss();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void showConfirmDeleteAllPopup() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View popupView = inflater.inflate(R.layout.popup_confirm_delete_all, null, false);

        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true
        );

        // Làm tối nền
        dimOverlay.setVisibility(View.VISIBLE);
        popupWindow.setOnDismissListener(() -> dimOverlay.setVisibility(View.GONE));

        popupWindow.showAtLocation(btnClear, Gravity.CENTER, 0, 0);

        TextView btnYes = popupView.findViewById(R.id.btnYes);
        TextView btnNo = popupView.findViewById(R.id.btnNo);

        btnYes.setOnClickListener(view -> {
            deleteAllAccounts();
            popupWindow.dismiss();
        });

        btnNo.setOnClickListener(view -> popupWindow.dismiss());
    }

    private void deleteAllAccounts() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        FirebaseFirestore.getInstance()
                .collection("userData")
                .document(user.getUid())
                .update("account", new ArrayList<Map<String, Object>>())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã xóa tất cả tài khoản!", Toast.LENGTH_SHORT).show();
                    allAccounts.clear();
                    linearAccounts.removeAllViews();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}