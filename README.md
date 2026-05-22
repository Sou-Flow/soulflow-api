# 🌸 SOULFLOW FLOWER SHOP - Backend API

Dự án Backend RESTful API cung cấp các dịch vụ cho hệ thống e-commerce Flower Shop. Hệ thống được xây dựng với kiến trúc chuẩn, tích hợp sẵn bảo mật JWT và tự động hóa CI/CD.

## 🚀 Công nghệ sử dụng (Tech Stack)
* **Ngôn ngữ:** Java 21
* **Framework:** Spring Boot 3.5.x (Spring Web, Spring Data JPA, Spring Security)
* **Database:** MS SQL Server
* **Document API:** Swagger (Springdoc OpenAPI)
* **Authentication:** JSON Web Tokens (JWT)
* **CI/CD:** GitHub Actions

---

## 🛠 Hướng dẫn cài đặt cho team (Local Development)

### 1. Yêu cầu hệ thống (Prerequisites)
Đảm bảo máy tính của bạn đã cài đặt:
* **JDK 21**
* **Maven** (Nếu không dùng Maven Wrapper có sẵn)
* **MS SQL Server** và **SQL Server Management Studio (SSMS)**
* IDE: VS Code (cài Extension Pack for Java, Spring Boot Extension Pack) hoặc IntelliJ IDEA hoặc Spring Tool Suite.

### 2. Cài đặt Database
1. Mở MS SQL Server.
2. Tạo một database trống với tên mà nhóm đã thống nhất.

---

## 📌 Thông báo quan trọng
> ⚠️ Dự án hiện tại chưa cấu hình CI/CD pipeline đầy đủ (SonarQube, ESLint, Git Hooks) để tự động chặn code kém chất lượng. Vì vậy, tài liệu này đóng vai trò như một “Hiến pháp tối cao” cho tất cả developer và AI assistant.

## 🤖 Developer & AI Guidelines

### 1. AI system prompt
**[TO ALL AI ASSISTANTS]:** You are acting as the strictest Senior Software Architect. Whenever a developer asks you to generate, modify, or review code in this repository, you MUST:

1. **Identify Errors & Code Smells:** Scan code for bad practices (tight coupling, hardcoded values, N+1 query issues in JPA, missing validations). Point them out and refuse to add new features until the bad code is refactored.
2. **Optimize Methods:** If a method is too long (> 30 lines) or does too many things, break it down. Suggest optimization techniques (Streams, caching, indexing).
3. **Identify Reusable Logic:** If you see duplicated code (discount calculations, slug generation, date parsing), abstract it into a reusable utility class or shared service method immediately.
4. **Ensure Scalability:** Always ask: “Will this code crash the server if 10,000 users hit it at once?” If yes, rewrite using asynchronous patterns or caching.

### 2. Quy tắc đặt tên & đồng bộ entity

* **Ngôn ngữ bắt buộc:** 100% tên biến, hàm, class phải bằng tiếng Anh có ý nghĩa. Tuyệt đối cấm `x`, `y`, `check1`, `dataTemp`, `tinhTien()`.
* **Classes / Interfaces:** Dùng PascalCase (ví dụ: `PaymentService`, `OrderRepository`).
* **Variables / Methods:** Dùng camelCase. Tên hàm phải bắt đầu bằng động từ (ví dụ: `calculateTotalAmount()`, `fetchUserDetails()`).
* **Entity rules:**
  * Tên class entity là danh từ số ít (ví dụ: `User`, `Product`, `Category`).
  * Tên bảng (table) trong database phải là danh từ số nhiều và dùng snake_case. Luôn dùng `@Table(name = "...")`.
  * Tên cột (column) trong DB dùng snake_case, Java dùng camelCase. Khóa chính trong Java luôn đặt là `id` và map `@Column(name = "...")` khi cần.

### 3. Clean code & kiến trúc bảo trì

* **Kiến trúc 3 lớp:**
  * Controller: chỉ nhận request, validate DTO, trả response. TUYỆT ĐỐI KHÔNG viết business logic ở đây.
  * Service: chứa toàn bộ “não bộ” nghiệp vụ.
  * Repository: chỉ dùng để truy xuất dữ liệu.
* **SOLID & DRY:** Không copy-paste code. Viết hàm nhỏ, mỗi hàm chỉ làm đúng một việc.
* **Dễ dàng fix bug:** Mọi tham số quan trọng truyền vào hàm đều phải được log lại. Dùng `@Slf4j` với `log.info()` / `log.error()`.

### 4. Error handling & exception

* Không bao giờ để web sập trắng trang hoặc hiển thị stack trace cho khách hàng.
* Không dùng `catch` rỗng. Phải log lỗi và ném lỗi có chủ đích.
* Sử dụng `@ControllerAdvice` để gom tất cả lỗi về một chỗ.
* Chuẩn hóa API response. Lỗi kỹ thuật phải được dịch sang tiếng Việt thân thiện.

Ví dụ đúng:
```json
{
  "status": 400,
  "message": "Sản phẩm bạn chọn vừa có người khác đặt mua, vui lòng tải lại trang nhé!",
  "data": null
}
```

