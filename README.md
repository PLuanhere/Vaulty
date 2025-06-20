# VaultyApp

**VaultyApp** là ứng dụng quản lý mật khẩu cá nhân dành cho Android, giúp bạn lưu trữ, bảo vệ và truy xuất thông tin đăng nhập các tài khoản của mình một cách an toàn, hiện đại và tiện lợi.

---

## 🌟 Tính năng chính

- **Đăng ký/Đăng nhập bằng Email**  
  Sử dụng Firebase Authentication, bảo mật tài khoản người dùng.

- **Lưu trữ đám mây**  
  Đồng bộ dữ liệu với Firestore, không lo mất mật khẩu khi đổi thiết bị.

- **Quản lý tài khoản linh hoạt**  
  - Thêm, sửa, xóa các tài khoản dịch vụ (Facebook, Gmail, Zalo,...)
  - Xem chi tiết, copy nhanh tên đăng nhập hoặc mật khẩu.
  - Tìm kiếm tài khoản theo tên.
  - Sắp xếp danh sách theo tên (A-Z, Z-A) hoặc theo thời gian tạo (mới nhất, cũ nhất).

- **Giao diện thân thiện**  
  - Thiết kế hiện đại, bo góc, dễ sử dụng.
  - Hỗ trợ chế độ ẩn/hiện mật khẩu.

- **Đăng xuất an toàn**  
  Chỉ một chạm để đăng xuất khỏi ứng dụng.

---

## 📸 Hình ảnh giao diện

<p align="center">
  <img src="https://github.com/user-attachments/assets/01b3892d-458e-4496-acbe-8187bbe57d32" width="220"/>
  <img src="https://github.com/user-attachments/assets/8b44ad4b-b0e1-4b5b-a9b1-f75623d4ad17" width="220"/>
  <img src="https://github.com/user-attachments/assets/50d12354-1918-4291-a193-fd3814a359cd" width="220"/>
</p>
---

## 🚀 Cài đặt & sử dụng

### 1. Clone Project

```bash
git clone https://github.com/PLuanhere/VaultyApp.git
cd VaultyApp
```

### 2. Thiết lập Firebase

1. Truy cập [Firebase Console](https://console.firebase.google.com/), tạo project mới.
2. Thêm ứng dụng Android với package name (ví dụ: `com.example.vaultyapp`).
3. Tải file `google-services.json` và đặt vào thư mục `app/` của project.
4. Bật Authentication (Email/Password) và Firestore Database trên Firebase console.

### 3. Build & chạy ứng dụng

- Mở project bằng Android Studio.
- Sync Gradle.
- Kết nối thiết bị/emulator, bấm **Run**.

---

## 🗂️ Cấu trúc chính

```
VaultyApp/
└── app/
    ├── src/main/java/com/example/vaultyapp/
    │   ├── MainActivity.java
    │   ├── NewPasswordActivity.java
    │   ├── ShowPasswordActivity.java
    │   └── ...
    ├── res/
    │   ├── layout/
    │   ├── drawable/
    │   └── values/
    └── google-services.json
```

---

## 💡 Đóng góp & Liên hệ

Bạn có ý tưởng hoặc phát hiện lỗi? Hãy mở [issue](https://github.com/PLuanhere/VaultyApp/issues) hoặc gửi pull request!

---

## 👨‍💻 Nhóm phát triển

- **Phan Luân** - [Github](https://github.com/PLuanhere)
- **Trung Hậu**  
- **Hồng Anh**

*Thông tin chi tiết vui lòng liên hệ qua Github cá nhân.*

---

> **VaultyApp - Quản lý mật khẩu thông minh, bảo mật tối đa!**
