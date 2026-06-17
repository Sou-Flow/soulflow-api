-- Seed default roles required for registration
USE flower_shop;
GO

INSERT INTO roles (code, name_vn, name_eng, del_if) VALUES
('ADMIN', N'Quản trị viên', 'Administrator', 0),
('STAFF', N'Nhân viên', 'Staff', 0),
('CUSTOMER', N'Khách hàng', 'Customer', 0);
GO
