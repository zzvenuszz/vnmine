# 🏔️ VNMine — Plugin Tu Tiên cho Minecraft Paper 1.21+

**Phiên bản:** 2.1.0  
**Tác giả:** VNMine Team  
**Yêu cầu:** Paper 1.21+ (API 1.21.4)

---

## 📖 TỔNG QUAN (Overview)

**VNMine** là plugin đa năng dành cho máy chủ Minecraft Paper 1.21+, mang phong cách **Tu Tiên Huyền Huyễn** (lấy cảm hứng từ Võ Lâm Truyền Kỳ, Đấu Phá Thương Khung) vào thế giới Minecraft. Plugin tích hợp đầy đủ các hệ thống từ tu luyện, kỹ năng, luyện đan, chế tạo pháp bảo, NPC shop cho đến quản trị server như phân quyền, world generation, block drop, v.v.

### ✨ Tính năng nổi bật

#### 🧘 Hệ thống Tu Luyện (Cultivation)
- **12 Cảnh Giới** — Từ Khí Động đến Luyện Khí, mỗi cảnh giới có 9 tầng (nhất trọng → cửu trọng)
- **Level 1–100** — EXP cần để lên cấp tăng theo công thức `level × 100`
- **EXP Sources** — Giết quái (+10), quái tinh anh (+50), Boss (+200), đập quặng (+5), câu cá (+15)
- **Lôi Kiếp (Tribulation)** — 3 đợt kiếp (3 giây/đợt, nghỉ 5 giây giữa các đợt), sét đánh người chơi, broadcast khi thành công/thất bại, tụt level khi thất bại

#### 💎 Linh Lực (Mana)
- **Thanh năng lượng BossBar** — Hiển thị trên màn hình người chơi
- **Hồi phục tự động** — Hồi 2 mana mỗi 2 giây (sau 5 giây không combat)
- **Tiêu hao** — Dùng để thi triển kỹ năng và cưỡi tọa kỵ (3 mana/giây)

#### 📚 Công Pháp & Kỹ Năng (Skills)
- **8 Kỹ Năng ACTIVE** — Trúc Cơ Liệu Thương (hồi máu), Khí Thuẫn (khiên), Hỏa Cầu, Phong Nhận, Lôi Kích, Thuấn Địa, Thuấn Di, Thiên Thạch
- **Skill Book System** — Sách công pháp phẩm cấp Thiên/Địa/Huyền/Hoàng × Thượng/Trung/Hạ, tỉ lệ học thành công 20%–90%
- **Skill Bar** — 9 slot kỹ năng, gán qua GUI, thi triển bằng phím Q

#### 🔬 Luyện Đan (Alchemy)
- **7 Loại Đan Dược** — Hồi Linh Đan, Đại Hồi Linh Đan, Thanh Tâm Đan, Tốc Thánh Đan, Cương Thể Đan, Tu Luyện Đan, Thiên Hồi Đan, Phi Thăng Đan, Bách Độc Đan, Phệ Ma Đan, Trường Thọ Đan
- **12 Phẩm cấp** — Hoàng/Huyền/Địa/Thiên × Hạ/Trung/Thượng, tỉ lệ thành công và số lượng đan khác nhau
- **GUI Luyện Đan** — Đặt 6 nguyên liệu, chọn công thức, chế tạo mất 60 giây

#### ⚔️ Pháp Bảo (Artifacts)
- **7 Pháp Bảo** với khả năng đặc biệt:
  - **Kiếm Phi Hành** — Ngự kiếm bay lượn tự do, tiêu hao mana
  - **Linh Chung** — Làm choáng quái trong bán kính
  - **Bát Quái Kính** — Giảm 30% sát thương khi cầm trên tay
  - **Hồn Ngọc** — Tự động hồi 50% máu khi HP < 20% (CD 5 phút)
  - **Thiên Linh Thuẫn** — Bất tử 5 giây khi bị đánh (CD 3 phút)
  - **Lôi Ấn** — Click vào quái gọi sét đánh, tiêu hao mana
  - **Phượng Hoàng Lệnh** — Tự động hồi sinh 1 lần sau khi chết (CD 1 ngày)
- **21 Cấp Phẩm** — Phàm/Linh/Vương/Thánh/Đế/Tiên/Thần/Chí Tôn × các cấp con
- **Click phải kích hoạt** — Có niệm khẩu quyết và cooldown

#### 👹 Quái Tinh Anh & Boss
- **Elite Mob** — HP ×5, DMG ×3, thanh máu cam, rơi nguyên liệu linh thảo
- **Boss (10% từ Elite)** — HP ×20, DMG ×8, kỹ năng đặc biệt, thanh máu đỏ, rơi nhiều linh thạch/EXP

#### 🤖 NPC Shop
- **4 Loại NPC** — Đại Sư Công Pháp, Luyện Khí Đại Sư, Luyện Đan Sư, Linh Thạch Thương
- **Mua bán trực tiếp** — Mua sách kỹ năng, pháp bảo, đan dược, linh thạch từ NPC
- **Quản lý đầy đủ** — Tạo/xóa/dịch chuyển/đổi tên/đổi skin NPC
- **ProtocolLib** — Tùy biến skin NPC theo player bất kỳ

#### 💰 Linh Thạch (Currency)
- **3 Cấp Tiền Tệ** — Hạ Phẩm (giá trị 1), Trung Phẩm (giá trị 100), Thượng Phẩm (giá trị 10,000)
- **Drop tự động** — Rơi từ quái (10% cơ hội), quái tinh anh (100%), Boss (100%), đập quặng (5%)
- **Giao dịch P2P** — `/vnpay` chuyển tiền, `/vnexchange` đổi cấp tiền tệ

#### 🐉 Tọa Kỵ (Mount)
- **3 Loại Tọa Kỵ** — Phượng Hoàng (cấp 30), Bạch Hổ (cấp 40), Thanh Long (cấp 50)
- **Bay lượn** — Chế độ bay tự do, tốc độ 0.8–1.0 tùy loại
- **Tiêu hao mana** — 3 mana/giây, tự hạ cánh khi hết mana

#### 🔐 Hệ Thống Phân Quyền (Permission)
- **LuckPerms-like** — Group hierarchy, inheritance, wildcard permissions
- **Quản lý Group** — Tạo/xóa nhóm, thêm quyền, thêm nhóm cha, đặt prefix/suffix
- **Quản lý Player** — Set group, thêm quyền riêng, kiểm tra quyền chi tiết

#### 🌍 World Generation
- **Tạo world tuỳ chỉnh** — Seed, type (NORMAL/FLAT/AMPLIFIED), environment
- **Tỉ lệ tuỳ chỉnh** — ore-rates (tỉ lệ quặng), structure-rates (cấu trúc), mob-rates (quái)
- **Tự động tạo** — Generate world khi chưa tồn tại

#### ⛏️ Block Drop Advanced
- **Tool-break** — Cơ chế đập vỡ công cụ khi dùng sai loại tool
- **Exploaded** — Block phát nổ khi bị đập
- **Drop-replace** — Thay thế drop mặc định bằng block khác với tỉ lệ chance

#### 🖥️ Menu GUI Trực Quan
- **Menu Chính** — Xem thống kê tu vi, mở các hệ thống con
- **Admin Menu** — Test item (đan, pháp bảo, nguyên liệu) chỉ 1 click
- **Phím tắt F** — Mở menu nhanh (Swap Hand) thay vì gõ `/vn`

#### 🎨 Tiện Ích (Utilities)
- **ColorUtils** — Hỗ trợ `&` color code, tên màu (`&red`, `&blue`, `&gold`), Hex Color
- **NameTag Manager** — Hiển thị prefix cảnh giới trên tab list, chat, đỉnh đầu
- **ItemBuilder** — Tạo item đặc biệt với NBT tag, lore, glow effect, skull owner
- **Quick Menu (F key)** — Mở menu nhanh bằng Swap Hand

---

## 📋 THÔNG TIN KỸ THUẬT (Technical Specifications)

| Mục | Chi tiết |
|-----|----------|
| **Phiên bản Minecraft** | 1.21+ (API 1.21.4-R0.1-SNAPSHOT) |
| **Nền tảng tương thích** | Paper, Purpur, Pufferfish (các fork tương thích Paper API) |
| **Ngôn ngữ phát triển** | Java 21 |
| **Công cụ build** | Maven |
| **Dependencies (bắt buộc)** | Không — plugin hoạt động **standalone**, chỉ cần Paper |
| **Soft-Dependencies** | **ProtocolLib** — plugin này sử dụng ProtocolLib để hỗ trợ tính năng **thay đổi skin cho NPC**. Nếu không có ProtocolLib, plugin vẫn hoạt động bình thường nhưng tính năng tùy biến skin cho NPC sẽ bị hạn chế (NPC sẽ sử dụng skin mặc định của loại mob đó). |
| **Lưu trữ dữ liệu** | YAML (config.yml, permissions.yml, cultivation.yml) |
| **File cấu hình chính** | `config.yml` (tất cả hệ thống), `permissions.yml` (phân quyền), `cultivation.yml` (cảnh giới) |

### 📥 Hướng dẫn cài đặt nhanh

1. **Tải file `.jar`** — Bản mới nhất: `VNMine-2.1.0.jar`
2. **Đặt vào thư mục plugins** — Sao chép file `.jar` vào thư mục `plugins/` của server
3. **(Khuyến khích) Cài ProtocolLib** — Tải `ProtocolLib.jar` và đặt vào cùng thư mục `plugins/` để có đầy đủ tính năng skin NPC
4. **Khởi động lại server** — Dùng lệnh `restart` hoặc `stop` rồi `start` lại
   > ⚠️ **Khuyến cáo:** Luôn **restart** server sau khi cài plugin mới, không dùng `/reload` vì có thể gây lỗi không mong muốn.
5. **Phân quyền** — Mặc định người chơi có thể dùng `/vn` và `/vnbalance`. Admin/OP có toàn quyền.

---

## ⌨️ CÁC LỆNH, QUYỀN HẠN & VÍ DỤ CỤ THỂ (Commands, Permissions & Examples)

### 🔹 Lệnh chính & Menu

| Lệnh | Quyền hạn | Mô tả | Ví dụ |
|------|-----------|-------|-------|
| `/vn` | `vnmine.command.vnmine` | Mở menu chính VNMine (phím tắt **F**) | `/vn` |
| `/vnmine menu` | `vnmine.command.vnmine` | Mở menu chính | `/vnmine menu` |
| `/vn start` | `vnmine.command.vnmine` | Bắt đầu hành trình tu tiên | `/vn start` |
| `/vnmine reload` | `vnmine.command.reload` | Reload toàn bộ cấu hình plugin | `/vnmine reload` |
| `/tps` | `vnmine.command.tps` | Xem TPS server | `/tps` |
| `/save-all` (hoặc `/save`) | `vnmine.command.saveall` | Lưu toàn bộ world | `/save-all` |

### 🔹 Hệ thống Thời Gian

| Lệnh | Quyền hạn | Mô tả | Ví dụ |
|------|-----------|-------|-------|
| `/vnmine time on` | `vnmine.time.toggle` | Bật chu kỳ thời gian tuỳ chỉnh | `/vnmine time on` |
| `/vnmine time off` | `vnmine.time.toggle` | Tắt chu kỳ thời gian tuỳ chỉnh | `/vnmine time off` |
| `/vnmine time set day 15` | `vnmine.time.set` | Đặt thời gian ban ngày 15 phút | `/vnmine time set day 15` |
| `/vnmine time set night 5` | `vnmine.time.set` | Đặt thời gian ban đêm 5 phút | `/vnmine time set night 5` |
| `/vnmine time status` | `vnmine.time.status` | Xem trạng thái thời gian hiện tại | `/vnmine time status` |

### 🔹 Hệ thống Tu Luyện (Cultivation)

| Lệnh | Quyền hạn | Mô tả | Ví dụ |
|------|-----------|-------|-------|
| `/vnmine cultivate info` | `vnmine.command.vnmine` | Xem thông tin tu vi (cấp, cảnh giới, exp, linh lực) | `/vnmine cultivate info` |
| `/vnmine cultivate toggle` | `vnmine.command.vnmine` | Bật/tắt hệ thống tu luyện | `/vnmine cultivate toggle` |
| `/vnmine elite toggle` | `vnmine.command.vnmine` | Bật/tắt quái tinh anh & boss | `/vnmine elite toggle` |
| `/vnmine elite info` | `vnmine.command.vnmine` | Xem trạng thái hệ thống elite mob | `/vnmine elite info` |

### 🔹 Hệ thống Công Pháp & Kỹ Năng

| Lệnh | Quyền hạn | Mô tả | Ví dụ |
|------|-----------|-------|-------|
| `/vnskill` | `vnmine.command.vnmine` | Mở menu học kỹ năng | `/vnskill` |
| `/vnskill my` | `vnmine.command.vnmine` | Mở menu quản lý Skill Bar cá nhân | `/vnskill my` |
| `/vnskill bar` | `vnmine.command.vnmine` | Mở menu quản lý Skill Bar | `/vnskill bar` |
| `/vnskill toggle` | `vnmine.command.vnmine` | Bật/tắt hệ thống công pháp | `/vnskill toggle` |
| `/vnskill reload` | `vnmine.command.vnmine` | Reload cấu hình hệ thống công pháp | `/vnskill reload` |
| `/vnskill book hoanbh FIRE_BALL THIEN THUONG` | `vnmine.command.give` | (Admin) Give sách kỹ năng cho hoanbh — hoanbh sẽ nhận được sách công pháp Hỏa Cầu Thuật phẩm cấp Thiên Thượng | `/vnskill book hoanbh FIRE_BALL THIEN THUONG` |

### 🔹 Hệ thống Luyện Đan & Pháp Bảo

| Lệnh | Quyền hạn | Mô tả | Ví dụ |
|------|-----------|-------|-------|
| `/vnalchemy` | `vnmine.command.vnmine` | Mở lò luyện đan | `/vnalchemy` |
| `/vnitem toggle` | `vnmine.command.vnmine` | Bật/tắt hệ thống item & pháp bảo | `/vnitem toggle` |
| `/vnitem list` | `vnmine.command.vnmine` | Xem danh sách pháp bảo có trong hệ thống | `/vnitem list` |
| `/vnitem reload` | `vnmine.command.vnmine` | Reload cấu hình item & pháp bảo | `/vnitem reload` |

### 🔹 Hệ thống Linh Điền

| Lệnh | Quyền hạn | Mô tả | Ví dụ |
|------|-----------|-------|-------|
| `/vnfarm` | `vnmine.command.vnmine` | Mở menu quản lý linh điền | `/vnfarm` |

### 🔹 Hệ thống NPC Shop

| Lệnh | Quyền hạn | Mô tả | Ví dụ |
|------|-----------|-------|-------|
| `/vnnpc create skill_master` | `vnmine.command.npc` | Tạo NPC tại vị trí hiện tại với ID `skill_master` | `/vnnpc create skill_master` |
| `/vnnpc remove skill_master` | `vnmine.command.npc` | Xóa NPC có ID `skill_master` | `/vnnpc remove skill_master` |
| `/vnnpc removeall` | `vnmine.command.npc` | Xóa tất cả NPC trên server | `/vnnpc removeall` |
| `/vnnpc list` | `vnmine.command.npc` | Xem danh sách tất cả NPC đã tạo | `/vnnpc list` |
| `/vnnpc tp skill_master` | `vnmine.command.npc` | Dịch chuyển đến NPC có ID `skill_master` | `/vnnpc tp skill_master` |
| `/vnnpc movehere skill_master` | `vnmine.command.npc` | Di chuyển NPC `skill_master` đến vị trí hiện tại | `/vnnpc movehere skill_master` |
| `/vnnpc rename skill_master "&bSư Phụ Mới"` | `vnmine.command.npc` | Đổi tên NPC `skill_master` thành "Sư Phụ Mới" (màu xanh aqua) | `/vnnpc rename skill_master "&bSư Phụ Mới"` |
| `/vnnpc skin skill_master hoanbh` | `vnmine.command.npc` | Đổi skin của NPC `skill_master` thành skin của người chơi **hoanbh** (cần ProtocolLib) | `/vnnpc skin skill_master hoanbh` |
| `/vnnpc reload` | `vnmine.command.npc` | Reload cấu hình NPC | `/vnnpc reload` |

### 🔹 Hệ thống Linh Thạch & Tọa Kỵ

| Lệnh | Quyền hạn | Mô tả | Ví dụ |
|------|-----------|-------|-------|
| `/vnbalance` | `vnmine.command.vnmine` | Xem số dư linh thạch hiện tại | `/vnbalance` |
| `/vnpay hoanbh 100` | `vnmine.command.vnmine` | Chuyển 100 linh thạch cho **hoanbh** — sau khi thực hiện, hoanbh sẽ nhận được 100 linh thạch vào tài khoản | `/vnpay hoanbh 100` |
| `/mount summon PHUONG_HOANG` | `vnmine.command.mount` | Triệu hồi tọa kỵ Phượng Hoàng | `/mount summon PHUONG_HOANG` |
| `/mount dismiss` | `vnmine.command.mount` | Hủy tọa kỵ hiện tại | `/mount dismiss` |
| `/mount list` | `vnmine.command.mount` | Xem danh sách tọa kỵ đã mở khóa | `/mount list` |

### 🔹 Hệ thống Give (Admin)

| Lệnh | Quyền hạn | Mô tả | Ví dụ |
|------|-----------|-------|-------|
| `/vngive hoanbh skill FIRE_BALL` | `vnmine.command.give` | Give kỹ năng Hỏa Cầu Thuật cho **hoanbh** — hoanbh sẽ học được kỹ năng này ngay lập tức | `/vngive hoanbh skill FIRE_BALL` |
| `/vngive hoanbh pill HOI_LINH_DAN 10` | `vnmine.command.give` | Give 10 viên Hồi Linh Đan cho **hoanbh** — hoanbh nhận được 10 đan dược vào túi đồ | `/vngive hoanbh pill HOI_LINH_DAN 10` |
| `/vngive hoanbh artifact FLYING_SWORD` | `vnmine.command.give` | Give pháp bảo Kiếm Phi Hành cho **hoanbh** — hoanbh nhận 1 Kiếm Phi Hành vào túi đồ | `/vngive hoanbh artifact FLYING_SWORD` |
| `/vngive hoanbh currency 1000` | `vnmine.command.give` | Give 1000 linh thạch cho **hoanbh** — số dư của hoanbh tăng thêm 1000 | `/vngive hoanbh currency 1000` |
| `/vngive hoanbh exp 5000` | `vnmine.command.give` | Give 5000 EXP tu luyện cho **hoanbh** — hoanbh được cộng 5000 EXP | `/vngive hoanbh exp 5000` |
| `/vngive hoanbh level 50` | `vnmine.command.give` | Set cấp độ của **hoanbh** lên 50 — hoanbh sẽ đạt cấp 50 và mở khóa cảnh giới tương ứng | `/vngive hoanbh level 50` |
| `/vngive hoanbh mount PHUONG_HOANG` | `vnmine.command.give` | Mở khóa tọa kỵ Phượng Hoàng cho **hoanbh** — hoanbh có thể triệu hồi Phượng Hoàng | `/vngive hoanbh mount PHUONG_HOANG` |

### 🔹 Hệ thống Phân Quyền (Permission)

| Lệnh | Quyền hạn | Mô tả | Ví dụ |
|------|-----------|-------|-------|
| `/vnmine perm group list` | `vnmine.perm.admin` | Xem danh sách tất cả nhóm quyền trong hệ thống | `/vnmine perm group list` |
| `/vnmine perm group info admin` | `vnmine.perm.admin` | Xem thông tin chi tiết nhóm `admin` (thành viên, quyền, nhóm cha) | `/vnmine perm group info admin` |
| `/vnmine perm group create mod` | `vnmine.perm.admin` | Tạo nhóm quyền mới tên `mod` | `/vnmine perm group create mod` |
| `/vnmine perm group addperm admin vnmine.command.reload` | `vnmine.perm.admin` | Thêm quyền `vnmine.command.reload` cho nhóm `admin` | `/vnmine perm group addperm admin vnmine.command.reload` |
| `/vnmine perm group addparent admin member` | `vnmine.perm.admin` | Thêm nhóm `member` làm nhóm cha của `admin` — admin sẽ kế thừa tất cả quyền của member | `/vnmine perm group addparent admin member` |
| `/vnmine perm group setprefix admin "&c[Admin]"` | `vnmine.perm.admin` | Đặt prefix cho nhóm `admin` hiển thị màu đỏ `[Admin]` | `/vnmine perm group setprefix admin "&c[Admin]"` |
| `/vnmine perm player info hoanbh` | `vnmine.perm.admin` | Xem thông tin quyền của **hoanbh** — hiển thị nhóm, quyền riêng, ngày hết hạn | `/vnmine perm player info hoanbh` |
| `/vnmine perm player setgroup hoanbh admin` | `vnmine.perm.admin` | Gán **hoanbh** vào nhóm `admin` — hoanbh sẽ có tất cả quyền của nhóm admin | `/vnmine perm player setgroup hoanbh admin` |
| `/vnmine perm player addperm hoanbh vnmine.command.admin` | `vnmine.perm.admin` | Thêm quyền `vnmine.command.admin` riêng cho **hoanbh** (không qua nhóm) | `/vnmine perm player addperm hoanbh vnmine.command.admin` |
| `/vnmine perm check hoanbh vnmine.command.admin` | `vnmine.perm.admin` | Kiểm tra **hoanbh** có quyền `vnmine.command.admin` không — trả về có/không kèm nguồn quyền | `/vnmine perm check hoanbh vnmine.command.admin` |
| `/vnmine perm reload` | `vnmine.perm.admin` | Reload lại hệ thống phân quyền từ file permissions.yml | `/vnmine perm reload` |

### 🔹 Hệ thống World Generation & Block Drop

| Lệnh | Quyền hạn | Mô tả | Ví dụ |
|------|-----------|-------|-------|
| `/vnmine world gen tu_tien` | `vnmine.world.gen` | Tạo world mới tên `tu_tien` với cấu hình tỉ lệ quặng/cấu trúc/quái tuỳ chỉnh | `/vnmine world gen tu_tien` |
| `/vnmine world toggle` | `vnmine.world.toggle` | Bật/tắt hệ thống world generation | `/vnmine world toggle` |
| `/vnmine drop toggle` | `vnmine.drop.toggle` | Bật/tắt hệ thống block drop đặc biệt (nổ block, vỡ công cụ, thay thế drop) | `/vnmine drop toggle` |
| `/vnmine drop status` | `vnmine.drop.status` | Xem trạng thái hiện tại của hệ thống block drop | `/vnmine drop status` |

### 🔹 Admin Menu

| Lệnh | Quyền hạn | Mô tả | Ví dụ |
|------|-----------|-------|-------|
| `/vnadmin` | `vnmine.command.admin` | Mở menu admin GUI — lấy item test (đan dược, pháp bảo, nguyên liệu) chỉ với 1 click | `/vnadmin` — mở giao diện admin, click vào đan dược để nhận 64 cái, click vào pháp bảo để nhận 1 cái |

---

## 🔐 TỔNG HỢP QUYỀN HẠN (All Permissions Summary)

### 👤 Quyền dành cho Người chơi (Player Permissions)

*Mặc định: `true` — người chơi có quyền sử dụng ngay mà không cần cấp thêm*

| Node quyền | Mô tả |
|------------|-------|
| `vnmine.command.vnmine` | Cho phép dùng `/vnmine`, `/vn` và hầu hết các lệnh cơ bản |
| `vnmine.command.mount` | Cho phép sử dụng tọa kỵ (`/mount summon`, `/mount dismiss`, `/mount list`) |
| `vnmine.time.status` | Cho phép xem trạng thái thời gian (`/vnmine time status`) |

### 🛡️ Quyền dành cho Quản trị viên (Admin Permissions)

*Mặc định: `op` — chỉ dành cho người chơi có quyền OP hoặc được cấp thủ công*

| Node quyền | Mô tả |
|------------|-------|
| `vnmine.*` | **Super node** — toàn bộ quyền của plugin |
| `vnmine.command.*` | Tất cả quyền command |
| `vnmine.command.tps` | Xem TPS server |
| `vnmine.command.saveall` | Lưu toàn bộ world |
| `vnmine.command.reload` | Reload cấu hình plugin |
| `vnmine.command.npc` | Quản lý NPC (tạo, xóa, dịch chuyển, đổi skin...) |
| `vnmine.command.give` | Give item/skill/pill/currency/exp cho người chơi |
| `vnmine.command.admin` | Mở menu admin lấy item test |
| `vnmine.perm.admin` | Quản lý permission groups và players |
| `vnmine.time.*` | Tất cả quyền về thời gian |
| `vnmine.time.set` | Set thời gian ngày/đêm |
| `vnmine.time.toggle` | Bật/tắt chu kỳ thời gian tuỳ chỉnh |
| `vnmine.world.*` | Tất cả quyền về world generation |
| `vnmine.world.gen` | Tạo world mới |
| `vnmine.world.toggle` | Bật/tắt world generation |
| `vnmine.drop.*` | Tất cả quyền quản lý block drop |
| `vnmine.drop.toggle` | Bật/tắt hệ thống block drop đặc biệt |
| `vnmine.drop.status` | Xem trạng thái block drop |

### 📌 Ghi chú về quyền hạn

- Người chơi mới mặc định có `vnmine.command.vnmine` và `vnmine.time.status` (có thể dùng `/vn` và xem thời gian)
- Tất cả quyền còn lại mặc định chỉ dành cho OP
- Có thể cấp quyền bằng lệnh `/vnmine perm player setgroup <người_chơi> <nhóm>` hoặc dùng OP trực tiếp (`/op <người_chơi>`)
- Danh sách nhóm quyền và cấu hình chi tiết nằm trong file `permissions.yml`

---

## ⚙️ CẤU HÌNH (Configuration)

### 📄 File `config.yml` (mẫu — các thiết lập quan trọng)

```yaml
# ===================================
# I. THỜI GIAN NGÀY/ĐÊM
# ===================================
day-minutes: 10          # Thời gian ban ngày (phút)
night-minutes: 10        # Thời gian ban đêm (phút)

# ===================================
# II. HỆ THỐNG PHÂN QUYỀN
# ===================================
permission-system:
  enabled: true          # Bật/tắt hệ thống phân quyền tích hợp (như LuckPerms)
  groups:
    default:             # Nhóm mặc định cho người chơi mới vào server
      weight: 0
      prefix: "&7"
      default: true
      parents: []
      permissions:
        - vnmine.time.status
        - vnmine.command.vnmine
    member:
      weight: 10
      prefix: "&7[Member]"
      parents: [default]
      permissions:
        - vnmine.command.tps
    admin:
      weight: 100
      prefix: "&c[Admin]"
      parents: [member]
      permissions:
        - vnmine.*
  players: {}            # Lưu thông tin người chơi riêng lẻ (tự động cập nhật)

# ===================================
# III. HỆ THỐNG TU LUYỆN
# ===================================
cultivation:
  enabled: true
  exp-per-level-multiplier: 100   # EXP cần để lên cấp = level hiện tại × 100
  realms:
    1:   { name: "Khí Động",  prefix: "&7[Khí Động" }
    11:  { name: "Luyện Khí", prefix: "&a[Luyện Khí" }
    21:  { name: "Trúc Cơ",   prefix: "&b[Trúc Cơ" }
    # ... (xem chi tiết đầy đủ trong file)
  exp-sources:
    kill-mob:   { enabled: true, amount: 10 }
    kill-elite: { enabled: true, amount: 50 }
    kill-boss:  { enabled: true, amount: 200 }
    break-ore:  { enabled: true, amount: 5 }
    fishing:    { enabled: true, amount: 15 }
  mana:
    base-max-mana: 100            # Linh lực tối đa cơ bản
    mana-per-level: 10            # Mỗi cấp tăng thêm 10 linh lực
    regen:
      base-amount: 2              # Hồi 2 linh lực mỗi lần
      regen-interval-ticks: 40    # 2 giây hồi 1 lần
      combat-delay-ticks: 100     # Sau 5 giây không combat mới bắt đầu hồi
  tribulation:
    enabled: true
    damage:
      base: 3.0
      per-strike-multiplier: 1.2
      damage-per-level: 0.8
      armor-reduction: 0.6
    level-drop-on-fail: 1         # Số level bị tụt khi độ kiếp thất bại

# ===================================
# IV. HỆ THỐNG CÔNG PHÁP (SKILLS)
# ===================================
skills:
  enabled: true
  skills:
    BASIC_HEAL:
      enabled: true
      name: "&aTrúc Cơ Liệu Thương"
      type: "ACTIVE"
      required-level: 5
      exp-cost: 50
      mana-cost: 15
      cooldown-seconds: 10

# ===================================
# V. HỆ THỐNG NPC SHOP
# ===================================
npc-shop:
  enabled: true
  npcs:
    skill_master:
      enabled: true
      name: "&b&l◆ Đại Sư Công Pháp ◆"
      type: "VILLAGER"
      trades:
        SKILL_BASIC_HEAL:
          type: "SKILL"
          price:
            material: "EMERALD"
            amount: 5
    artifact_master:
      enabled: true
      name: "&6&l◆ Luyện Khí Đại Sư ◆"
    pill_master:
      enabled: true
      name: "&a&l◆ Luyện Đan Sư ◆"
    currency_master:
      enabled: true
      name: "&e&l◆ Linh Thạch Thương ◆"

# ===================================
# VI. HỆ THỐNG LINH THẠCH
# ===================================
currency:
  enabled: true
  item:
    material: "PRISMARINE_SHARD"  # Vật phẩm đại diện cho linh thạch
    name: "&bLinh Thạch"
  drops:
    kill-mob:  { enabled: true, chance: 10.0, amount: "1-3" }
    kill-elite:{ enabled: true, chance: 100.0, amount: "5-10" }
    kill-boss: { enabled: true, chance: 100.0, amount: "20-50" }
    break-ore: { enabled: true, chance: 5.0, amount: 1 }

# ===================================
# VII. HỆ THỐNG TỌA KỴ
# ===================================
mount:
  enabled: true
  required-level: 30              # Cấp tối thiểu để sử dụng tọa kỵ
  mana-per-second: 3              # Linh lực tiêu hao mỗi giây khi cưỡi
  mounts:
    PHUONG_HOANG:                 # Phượng Hoàng Lửa — yêu cầu cấp 30
      speed: 0.5
      flight-speed: 0.8
    BACH_HO:                      # Bạch Hổ — yêu cầu cấp 40
      speed: 0.7
      flight-speed: 0.9
    THANH_LONG:                   # Thanh Long — yêu cầu cấp 50
      speed: 0.6
      flight-speed: 1.0
```

> 📝 Xem cấu hình **đầy đủ** (block drop, skill books, world gen, elite mob) trong file `config.yml` của plugin.

---

## 🆘 Hỗ trợ & Liên hệ

- **Báo lỗi & Đóng góp ý tưởng:** Mở issue trên GitHub
- **Wiki chi tiết:** Xem tại repository của dự án

---

## 📝 License

**VNMine Plugin** — All Rights Reserved  
Phát triển bởi VNMine Team  
Phiên bản 2.1.0 — Big Update Tu Tiên 🏔️✨