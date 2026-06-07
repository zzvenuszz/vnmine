# VNMine Plugin - Hệ Thống Tu Tiên Huyền Huyễn 🏔️✨

**Phiên bản:** 2.1.0 (Big Update - Tu Tiên)  
**Tác giả:** VNMine Team  
**API:** 1.21+ (Paper)

---

## 📋 MỤC LỤC
1. [Giới Thiệu](#-giới-thiệu)
2. [Hệ Thống Màu Sắc - ColorUtils](#-hệ-thống-màu-sắc---colorutils)
3. [Hệ Thống Tu Luyện (Cultivation)](#-hệ-thống-tu-luyện-cultivation)
4. [Lôi Kiếp (Tribulation)](#-lôi-kiếp-tribulation)
5. [Linh Lực (Mana System)](#-linh-lực-mana-system)
6. [Hệ Thống Công Pháp & Kỹ Năng](#-hệ-thống-công-pháp--kỹ-năng)
7. [Hệ Thống Luyện Đan (Alchemy)](#-hệ-thống-luyện-đan-alchemy)
8. [Hệ Thống Pháp Bảo (Artifacts)](#-hệ-thống-pháp-bảo-artifacts)
9. [Hệ Thống Linh Thảo & Linh Điền](#-hệ-thống-linh-thảo--linh-điền)
10. [Hệ Thống Quái Tinh Anh & Boss](#-hệ-thống-quái-tinh-anh--boss)
11. [Menu Inventory Chính](#-menu-inventory-chính)
12. [Hệ Thống NPC](#-hệ-thống-npc)
13. [Hệ Thống Linh Thạch (Currency)](#-hệ-thống-linh-thạch-currency)
14. [Hệ Thống Tọa Kỵ (Mount)](#-hệ-thống-tọa-kỵ-mount)
15. [Danh Sách Lệnh & Ví Dụ](#-danh-sách-lệnh--ví-dụ)
16. [Danh Sách Item ID (Give Command)](#-danh-sách-item-id-give-command)
17. [Permissions (Quyền Hạn)](#-permissions-quyền-hạn)
18. [Công Thức Chế Tạo - Luyện Đan](#-công-thức-chế-tạo---luyện-đan)
19. [Công Thức Luyện Chế Pháp Bảo](#-công-thức-luyện-chế-pháp-bảo)
20. [Bảng Cảnh Giới](#-bảng-cảnh-giới)
21. [Tính Năng Mở Rộng (Gợi Ý)](#-tính-năng-mở-rộng-gợi-ý)

---

## 🎯 Giới Thiệu

**VNMine** là plugin đa năng cho Minecraft Paper 1.21+, kết hợp phong cách **Tu Tiên Huyền Huyễn** (giống Võ Lâm Truyền Kỳ, Đấu Phá Thương Khung) vào thế giới Minecraft.

Plugin bao gồm các hệ thống chính:
- ✅ **Hệ thống màu sắc** - Dùng `&` thay vì `§`, hỗ trợ tên màu (`&red`, `&blue`, `&gold`...)
- ✅ **Hệ thống tu luyện** - Level, cảnh giới, exp, lôi kiếp
- ✅ **Hệ thống linh lực** - Thanh mana hiển thị ActionBar
- ✅ **Hệ thống công pháp/kỹ năng** - Học và thi triển kỹ năng tu tiên
- ✅ **Hệ thống luyện đan** - Chế tạo đan dược từ linh thảo trong GUI
- ✅ **Hệ thống pháp bảo** - Chế tạo và sử dụng pháp bảo
- ✅ **Hệ thống linh thảo & linh điền** - Trồng trọt, thu hoạch linh thảo
- ✅ **Hệ thống quái tinh anh & boss** - Elite mob với kỹ năng đặc biệt
- ✅ **Menu GUI trực quan** - Quản lý tất cả chức năng trong inventory
- ✅ **Hệ thống NPC** - NPC shop với giao diện mua bán
- ✅ **Hệ thống Linh Thạch** - Tiền tệ tu tiên
- ✅ **Hệ thống Tọa Kỵ** - Cưỡi rồng, phượng hoàng, bạch hổ bay

---

## 🎨 Hệ Thống Màu Sắc - ColorUtils

### Cách sử dụng
Thay vì dùng `§` (Section Sign), bạn có thể dùng `&` trong mọi config:

```yaml
# Trong config.yml
prefix: "&c[Admin]"
name: "&b&l◆ Kiếm Phi Hành ◆"
lore:
  - "&7Phẩm cấp: &aTiên Thiên"
  - "&c&l✦ Pháp bảo thượng phẩm ✦"
```

### Mã màu hỗ trợ

| Mã | Kết quả | Mã tên | Kết quả |
|----|---------|--------|---------|
| `&0` | §0 Đen | `&black` | §0 Đen |
| `&1` | §1 Xanh đậm | `&dark_blue` | §1 Xanh đậm |
| `&2` | §2 Xanh lá | `&dark_green` | §2 Xanh lá |
| `&3` | §3 Xanh aqua | `&dark_aqua` | §3 Xanh aqua |
| `&4` | §4 Đỏ đậm | `&dark_red` | §4 Đỏ đậm |
| `&5` | §5 Tím | `&dark_purple` | §5 Tím |
| `&6` | §6 Vàng | `&gold` | §6 Vàng |
| `&7` | §7 Xám | `&gray` | §7 Xám |
| `&8` | §8 Xám đậm | `&dark_gray` | §8 Xám đậm |
| `&9` | §9 Xanh dương | `&blue` | §9 Xanh dương |
| `&a` | §a Xanh lá | `&green` | §a Xanh lá |
| `&b` | §b Xanh aqua | `&aqua` | §b Xanh aqua |
| `&c` | §c Đỏ | `&red` | §c Đỏ |
| `&d` | §d Hồng | `&light_purple` | §d Hồng |
| `&e` | §e Vàng | `&yellow` | §e Vàng |
| `&f` | §f Trắng | `&white` | §f Trắng |

### Định dạng
| Mã | Tên | Mã tên |
|----|-----|--------|
| `&l` | Bold (đậm) | `&bold` |
| `&o` | Italic (nghiêng) | `&italic` |
| `&n` | Underline (gạch chân) | `&underline` |
| `&m` | Strikethrough (gạch ngang) | `&strikethrough` |
| `&k` | Magic (random) | `&magic` |
| `&r` | Reset | `&reset` |

### Hex Color (nếu server hỗ trợ)
```yaml
name: "&{#FF0000}Chữ đỏ tươi&r"
```

---

## 🧘 Hệ Thống Tu Luyện (Cultivation)

### Cơ chế
- Mỗi người chơi có **cấp độ (level)** từ 1 đến 100
- Cứ 10 cấp là 1 **đại cảnh giới**
- Trong 10 cấp đó: cấp 1-9 là **nhất trọng → cửu trọng**, cấp 10 là **Đại Viên Mãn**
- Prefix tự động hiển thị: `[Luyện Khí ngũ trọng] PlayerName`

### Cách tăng EXP

| Hành động | EXP nhận được |
|-----------|---------------|
| Giết quái thường | 10 EXP |
| Giết quái Tinh Anh | 50 EXP |
| Giết Boss | 200 EXP |
| Đào quặng | 5 EXP |
| Câu cá | 15 EXP |
| Luyện đan thành công | 20-240 EXP |
| Chế tạo pháp bảo | 60-360 EXP |

### Công thức EXP
```
EXP cần cho level tiếp theo = Level hiện tại × 100
```
Ví dụ: Level 1 → 2 cần 100 EXP, Level 50 → 51 cần 5000 EXP

### Lệnh
- `/vnmine cultivate info` - Xem thông tin tu vi
- `/vnmine cultivate toggle` - Bật/tắt hệ thống

---

## ⚡ Lôi Kiếp (Tribulation)

### Cơ chế
Khi đạt **cấp chẵn (10, 20, 30...)**, người chơi sẽ bị sét đánh:
- **Cấp 10:** 1 tia sét nhẹ (5 sát thương)
- **Cấp 20:** 2 tia sét mạnh hơn (7.5 + 11.25 sát thương)
- **Cấp 30:** 3 tia sét (tăng dần)
- ... cứ thế nhân lên

### Kết quả
- **Thành công:** Được thưởng EXP, broadcast toàn server, title hoành tráng
- **Thất bại (chết):** Mất 50% EXP hiện tại, broadcast thất bại

### Lệnh cấu hình (config.yml)
```yaml
tribulation:
  enabled: true
  damage:
    base: 5.0
    per-strike-multiplier: 1.5
  countdown-seconds: 5
  strike-interval-ticks: 20
  immunity-duration-seconds: 60
```

---

## 💎 Linh Lực (Mana System)

### Giới thiệu
Linh lực là năng lượng để thi triển kỹ năng và sử dụng pháp bảo.

### Thông số
- **Max linh lực:** 100 + (Cấp × 10)
- **Hồi phục:** +2 linh lực mỗi 2 giây (khi không combat)
- **Hiển thị:** ActionBar (`❤ Linh Lực ████████░░ 150/200`)

### Cách hồi linh lực
1. Tự động hồi (2/giây, cần 5 giây không combat)
2. Dùng **Hồi Linh Đan** (+30 ngay lập tức)
3. Dùng **Đại Hồi Linh Đan** (+100 + 20% hồi phục 30s)
4. Lên cấp (hồi đầy + tăng max)

---

## 📚 Hệ Thống Công Pháp & Kỹ Năng

### Danh sách kỹ năng

| Kỹ năng | Yêu cầu | Linh lực | EXP học | Mô tả |
|---------|---------|----------|---------|-------|
| 🌿 **Trúc Cơ Liệu Thương** | Cấp 5 | 15 | 50 | Hồi 10 HP |
| 🛡️ **Linh Khí Hộ Thể** | Cấp 10 | 30 | 100 | Khiên 20 sát thương |
| 🔥 **Hỏa Cầu Thuật** | Cấp 15 | 25 | 200 | Bắn cầu lửa |
| 🌪️ **Phong Nhẫn** | Cấp 20 | 20 | 300 | Lưỡi gió xuyên thấu |
| ⚡ **Thiên Lôi Dẫn** | Cấp 25 | 40 | 500 | Gọi sét đánh |
| 💨 **Phi Vân Bộ** | Cấp 8 | 10 | 80 | Tăng 40% tốc độ 15s |
| 🌌 **Thuấn Di** | Cấp 35 | 35 | 1500 | Dịch chuyển tức thời |
| ☄️ **Tinh Thần Bạo** | Cấp 50 | 120 | 5000 | Mưa sao băng hủy diệt |

### Cách học
1. Mở menu kỹ năng: `/vnskill` hoặc click "Công Pháp" trong menu chính
2. Click vào skill để học (tiêu hao EXP)
3. Click skill đã học để thi triển (ACTIVE)
4. Shift+Click để bật/tắt skill thụ động (PASSIVE)

### Lệnh
- `/vnskill` - Mở menu kỹ năng
- `/vnskill my` - Xem skill đã học
- `/vnskill toggle` - Bật/tắt hệ thống
- `/vnskill reload` - Reload config

---

## 🔬 Hệ Thống Luyện Đan (Alchemy)

### Giới thiệu
Mở GUI luyện đan, đặt nguyên liệu vào ô, bấm nút để luyện.  
Yêu cầu: **Kỹ năng Khống Hỏa Thuật** (cấp 3+)

### Cách luyện đan
1. Mở menu luyện đan: `/vnalchemy` hoặc click "Luyện Đan" trong menu chính
2. Đặt nguyên liệu đúng công thức vào 6 ô bên trái
3. Bấm nút **🔥 Luyện Đan**
4. Chờ thời gian luyện (10-120 giây tùy loại đan)
5. Lấy thành phẩm ở ô kết quả

### Công thức luyện đan

| Đan dược | Nguyên liệu | Yêu cầu | Thời gian | Tỉ lệ | Tác dụng |
|----------|-------------|---------|-----------|-------|----------|
| 🌿 **Hồi Linh Đan** | 3 Linh Thảo + 1 Nước | Cấp 3 | 10s | 80% | Hồi 30 linh lực |
| 💎 **Đại Hồi Linh Đan** | 2 Hồi Linh Đan + 2 Huyết LT + 5 LT | Cấp 10 | 30s | 60% | Hồi 100 linh lực + 20% hồi phục 30s |
| 💪 **Cương Thể Đan** | 3 Huyết LT + 5 LT + 1 Blaze | Cấp 15 | 20s | 55% | +20% sát thương 60s |
| 🧪 **Thanh Tâm Đan** | 5 LT + 1 Nước | Cấp 5 | 15s | 85% | Giải trừ trạng thái xấu |
| 🏃 **Tốc Thánh Đan** | 3 LT + 2 Đường + 1 Lông | Cấp 8 | 15s | 70% | +50% tốc độ 30s |
| ⭐ **Tu Luyện Đan** | 10 LT + 5 Huyết LT + 2 Long Huyết + 1 Vàng | Cấp 20 | 45s | 40% | +50 EXP |
| 👑 **Phi Thăng Đan** | 3 Tu Luyện Đan + 10 Long Huyết + 1 Hơi Rồng + 2 Netherite | Cấp 50 | 120s | 15% | +500 EXP (1 lần/đại cảnh giới) |

> **Lưu ý:** Linh Thảo (Green Dye), Huyết Linh Thảo (Red Dye), Long Huyết Thảo (Orange Dye)

### Kết quả luyện đan
- **Thành công:** Nhận đan dược + EXP luyện đan
- **Thất bại nhẹ (60%):** Thu được "Phế Liệu" (có thể làm phân bón)
- **Thất bại nặng (30%):** Mất hết nguyên liệu
- **Thảm họa (10%):** Nổ lò, mất nguyên liệu + sát thương

---

## ⚔️ Hệ Thống Pháp Bảo (Artifacts)

### Giới thiệu
Mở GUI luyện chế pháp bảo, đặt vật liệu vào ô, bấm nút để chế tạo.  
Yêu cầu: **Kỹ năng Luyện Khí Thuật** (cấp 2+)

### Công thức chế tạo

| Pháp bảo | Nguyên liệu | Yêu cầu | Thời gian | Tỉ lệ | Tác dụng |
|----------|-------------|---------|-----------|-------|----------|
| 🗡️ **Kiếm Phi Hành** | 1 Kiếm DC + 8 DC + 4 Lông | Cấp 15 | 30s | 50% | Click phải: Bay trên kiếm, tốn 5 mana/giây |
| 🔔 **Linh Chung** | 1 Chuông + 4 Vàng + 2 DC | Cấp 10 | 20s | 60% | Click phải: Choáng quái AOE |
| 🪞 **Bát Quái Kính** | 1 Khiên + 4 Obsidian + 4 Ngọc | Cấp 20 | 40s | 45% | Cầm tay: Giảm 30% sát thương |
| 💚 **Hồn Ngọc** | 1 Ngọc + 4 Vàng + 2 Mắt End | Cấp 25 | 45s | 40% | Tự động: Hồi 50% máu khi HP<20%, CD 5 phút |
| 🛡️ **Thiên Linh Thuẫn** | 1 Ngực Netherite + 8 Mắt End | Cấp 40 | 60s | 25% | Kích hoạt: Bất tử 5 giây, CD 3 phút |
| ⚡ **Lôi Ấn** | 1 Đinh ba + 4 DC + 2 Nước Rồng | Cấp 30 | 35s | 35% | Click quái: Gọi sét, tốn mana |
| 🐦 **Phượng Hoàng Lệnh** | 1 Lông + 8 Vàng Khối + 4 Netherite + 1 Trứng Rồng | Cấp 60 | 120s | 10% | Tự động: Hồi sinh 1 lần, CD 1 ngày |

> DC = Diamond (Kim Cương)

---

## 🌱 Hệ Thống Linh Thảo & Linh Điền

### Cách lấy Linh Thảo
Khi phá các khối thực vật (cỏ, hoa, lá, đất...), có tỉ lệ rơi:

| Loại thực vật | Linh Thảo | Tỉ lệ | Hạt giống |
|--------------|-----------|-------|-----------|
| 🌿 Cỏ, dương xỉ | Linh Thảo | 15% | 5% |
| 🌸 Hoa | Linh Thảo | 25% | 10% |
| 🌳 Lá cây | Linh Thảo | 10% | 8% |
| 🩸 Quái Zombie/Skeleton | Huyết Linh Thảo | 15% | - |
| ❄️ Tuyết/Băng | Hàn Băng Thảo | 20% | - |
| 🐉 Đá sâu (Deepslate) | Long Huyết Thảo | 0.5% | - |

### Hệ thống Linh Điền

**Linh Điền** là đất trồng đặc biệt chỉ dùng để trồng linh thảo.

**Cách tạo Linh Điền:**
1. Chế tạo **"Linh Khí Ngọc Phù"** (công thức: 2 DC + 3 Vàng + 1 Mắt End)
2. Click phải lên **Farmland** (đất trồng trọt) với phù chú trong tay
3. Vùng 7x7 xung quanh biến thành Linh Điền (hiệu ứng hạt xanh)

**Cách trồng:**
1. Farm linh thảo → nhận hạt giống
2. Click phải hạt giống lên Linh Điền
3. Linh thảo phát triển theo thời gian thực

**Phẩm chất linh thảo theo thời gian:**

| Thời gian | Phẩm chất | Sức mạnh luyện đan |
|-----------|-----------|-------------------|
| 0-29 phút | Hạ phẩm | 1 |
| 30-59 phút | Trung phẩm | 2 |
| 1-2 giờ | Thượng phẩm | 3 |
| 2-4 giờ | Cực phẩm | 5 |
| 4+ giờ | Tiên phẩm | 10 |

> **Mẹo:** Linh thảo phẩm chất càng cao, luyện đan tỉ lệ thành công càng cao và tác dụng đan dược càng mạnh!

---

## 👹 Hệ Thống Quái Tinh Anh & Boss

### Cơ chế
Khi người chơi giết đủ số lượng quái trong một khu vực:
1. **Tích lũy số kills** cho từng loại quái
2. Khi đạt ngưỡng → sinh ra **quái Tinh Anh** cùng loại
3. **10%** tinh anh hóa thành **Boss**

### Đặc điểm Tinh Anh
- ❤️ HP × 5, ⚔️ DMG × 3, 🛡️ Giáp × 2
- 📏 Kích thước × 1.5, phát sáng (màu xanh)
- 💠 Có kỹ năng đặc biệt: Độc, choáng, triệu hồi, hút máu
- 💎 Rơi item và EXP gấp nhiều lần

### Đặc điểm Boss
- ❤️ HP × 20, ⚔️ DMG × 8, 🛡️ Giáp × 5
- 📏 Kích thước × 2.5, phát sáng đỏ
- 💠 Nhiều kỹ năng: Động đất, mưa sao băng, hồi máu, dịch chuyển
- 👑 Rơi pháp bảo, linh thạch quý, item siêu hiếm

### Thanh máu BossBar
- Tinh anh: Thanh VÀNG, hiển thị tên + HP
- Boss: Thanh ĐỎ, hiển thị tên + HP lớn

### Thông báo toàn server
- **Tinh Anh xuất hiện:** `⚠ Player đã kích hoạt Thi Khôi Tinh Anh tại (x, y, z)!`
- **Boss xuất hiện:** `♛ THÔNG BÁO THIÊN HẠ ♛ - Thi Khôi Vương đã xuất hiện!`
- **Boss bị hạ:** `✦ Player đã hạ sát Thi Khôi Vương! Danh chấn tu tiên giới!`

---

## 🖥️ Menu Inventory Chính

Mở bằng lệnh `/vn` hoặc `/vnmine menu`

```
┌──────────────────────────────────────────────┐
│          ✦ VNMine - Tu Tiên Giới ✦           │
├──────┬──────┬──────┬──────┬──────┬──────┬─────┤
│  TP  │ Công │Luyện │ Pháp │      │      │     │
│  Vi  │ Pháp │ Đan  │ Bảo  │      │      │     │
├──────┼──────┼──────┼──────┼──────┼──────┼─────┤
│Linh  │ Linh │ Đan  │Hướng │      │      │     │
│Điền  │ Thảo │ Dược │ Dẫn  │      │      │     │
└──────┴──────┴──────┴──────┴──────┴──────┴─────┘
```

### Các nút chức năng
- **👤 Tu Vi Của Bạn** - Xem thông tin tu luyện chi tiết
- **📖 Công Pháp & Kỹ Năng** - Mở menu học và thi triển kỹ năng
- **⚗️ Luyện Đan** - Mở lò luyện đan
- **🔨 Luyện Chế Pháp Bảo** - Mở lò luyện chế pháp bảo
- **🌾 Linh Điền** - Quản lý linh điền (đang phát triển)
- **🌿 Linh Thảo** - Xem kho linh thảo (đang phát triển)
- **🧪 Đan Dược** - Xem đan dược đã luyện (đang phát triển)
- **📜 Hướng Dẫn Tu Tiên** - Hướng dẫn chi tiết cách chơi

---

## 🤖 Hệ Thống NPC

### Giới thiệu
Hệ thống NPC cho phép tạo các NPC shop trong game, người chơi có thể tương tác để mua bán vật phẩm.

### Danh sách NPC mặc định (config.yml)

| NPC ID | Tên | Chức năng |
|--------|-----|-----------|
| `skill_master` | Sư Tôn Công Pháp | Dạy kỹ năng và công pháp |
| `artifact_master` | Luyện Khí Sư | Chế tạo và nâng cấp pháp bảo |
| `pill_master` | Luyện Đan Sư | Mua bán nguyên liệu và đan dược |
| `currency_master` | Linh Thạch Thương Nhân | Đổi linh thạch, mua vật phẩm đặc biệt |

### Lệnh quản lý NPC

```bash
/vnnpc create <id>     # Tạo NPC tại vị trí đang đứng (cần config trước)
/vnnpc remove <id>     # Xóa NPC
/vnnpc list            # Danh sách NPC đã tạo
/vnnpc tp <id>         # Dịch chuyển đến NPC
/vnnpc reload          # Reload config NPC
```

### Ví dụ
```bash
# Tạo NPC skill_master tại vị trí hiện tại
/vnnpc create skill_master

# Xem danh sách NPC
/vnnpc list

# Xóa NPC
/vnnpc remove skill_master
```

### Yêu cầu
- Cần định nghĩa NPC ID trong `config.yml` trước khi tạo
- NPC sẽ tự động tương tác khi người chơi click phải

---

## 💰 Hệ Thống Linh Thạch (Currency)

### Giới thiệu
Linh Thạch là đơn vị tiền tệ trong VNMine, dùng để mua bán với NPC, giao dịch giữa người chơi.

### Cách kiếm Linh Thạch
| Cách kiếm | Số lượng |
|-----------|----------|
| Giết quái thường | 1-3 Linh Thạch |
| Giết quái Tinh Anh | 10-20 Linh Thạch |
| Giết Boss | 50-100 Linh Thạch |
| Đào quặng quý | 2-5 Linh Thạch |
| Bán vật phẩm cho NPC | Tùy loại |
| Người chơi khác chuyển | Tùy ý |

### Lệnh

```bash
/vnbalance            # Xem số dư Linh Thạch
/vnpay <player> <amount>  # Chuyển Linh Thạch cho người khác
```

### Ví dụ
```bash
# Xem số dư
/vnbalance
# Output: ◆ Số dư Linh Thạch: 150 ◆

# Chuyển tiền
/vnpay Steve 50
# Output: Bạn đã chuyển 50 Linh Thạch cho Steve
```

---

## 🐉 Hệ Thống Tọa Kỵ (Mount)

### Giới thiệu
Tọa Kỵ cho phép người chơi cưỡi các sinh vật thần thoại bay lượn, tiêu hao linh lực khi sử dụng.

### Danh sách Tọa Kỵ

| ID | Tên | Yêu cầu cấp | Yêu cầu mở khóa |
|----|-----|-------------|-----------------|
| `PHUONG_HOANG` | 🦅 Phượng Hoàng Lửa | Cấp 30 | `/vngive <player> mount PHUONG_HOANG` |
| `BACH_HO` | 🐅 Bạch Hổ | Cấp 40 | `/vngive <player> mount BACH_HO` |
| `THANH_LONG` | 🐉 Thanh Long | Cấp 50 | `/vngive <player> mount THANH_LONG` |

**Linh lực tiêu hao:** 3 linh lực/giây khi bay

### Lệnh

```bash
/mount summon <id>    # Triệu hồi tọa kỵ (VD: /mount summon PHUONG_HOANG)
/mount dismiss        # Hủy tọa kỵ
/mount list           # Xem danh sách tọa kỵ đã mở
```

### Ví dụ
```bash
# Xem danh sách tọa kỵ
/mount list
# Output:
# • Phượng Hoàng Lửa (Cấp 30) ✓ Đã mở
# • Bạch Hổ (Cấp 40) ✗ Chưa mở

# Triệu hồi
/mount summon PHUONG_HOANG

# Hủy
/mount dismiss
```

---

## ⌨️ Danh Sách Lệnh & Ví Dụ

### Lệnh chính
| Lệnh | Mô tả | Quyền | Ví dụ |
|------|-------|-------|-------|
| `/vn` | Mở menu chính | `vnmine.command.vnmine` | `/vn` |
| `/vnmine menu` | Mở menu chính | `vnmine.command.vnmine` | `/vnmine menu` |
| `/vn start` | Bắt đầu hành trình tu tiên | `vnmine.command.vnmine` | `/vn start` |
| `/vnmine reload` | Reload toàn bộ config | `vnmine.command.reload` | `/vnmine reload` |

### Hệ thống thời gian
| Lệnh | Mô tả | Quyền | Ví dụ |
|------|-------|-------|-------|
| `/vnmine time on` | Bật custom time cycle | `vnmine.time.toggle` | `/vnmine time on` |
| `/vnmine time off` | Tắt custom time cycle | `vnmine.time.toggle` | `/vnmine time off` |
| `/vnmine time set day <phút>` | Set thời gian ban ngày | `vnmine.time.set` | `/vnmine time set day 15` |
| `/vnmine time set night <phút>` | Set thời gian ban đêm | `vnmine.time.set` | `/vnmine time set night 5` |
| `/vnmine time status` | Xem trạng thái | `vnmine.time.status` | `/vnmine time status` |

### Hệ thống tu luyện
| Lệnh | Mô tả | Quyền | Ví dụ |
|------|-------|-------|-------|
| `/vnmine cultivate info` | Xem thông tin tu vi | `vnmine.command.vnmine` | `/vnmine cultivate info` |
| `/vnmine cultivate toggle` | Bật/tắt hệ thống | `vnmine.command.vnmine` | `/vnmine cultivate toggle` |
| `/vnmine elite toggle` | Bật/tắt elite mob | `vnmine.command.vnmine` | `/vnmine elite toggle` |
| `/vnmine elite info` | Xem trạng thái | `vnmine.command.vnmine` | `/vnmine elite info` |

### Hệ thống công pháp
| Lệnh | Mô tả | Quyền | Ví dụ |
|------|-------|-------|-------|
| `/vnskill` | Mở menu kỹ năng | `vnmine.command.vnmine` | `/vnskill` |
| `/vnskill my` | Xem skill đã học | `vnmine.command.vnmine` | `/vnskill my` |
| `/vnskill toggle` | Bật/tắt hệ thống | `vnmine.command.vnmine` | `/vnskill toggle` |
| `/vnskill reload` | Reload config | `vnmine.command.vnmine` | `/vnskill reload` |

### Hệ thống luyện đan & pháp bảo
| Lệnh | Mô tả | Quyền | Ví dụ |
|------|-------|-------|-------|
| `/vnalchemy` | Mở lò luyện đan | `vnmine.command.vnmine` | `/vnalchemy` |
| `/vnitem toggle` | Bật/tắt hệ thống item | `vnmine.command.vnmine` | `/vnitem toggle` |
| `/vnitem reload` | Reload config | `vnmine.command.vnmine` | `/vnitem reload` |
| `/vnitem list` | Xem danh sách item | `vnmine.command.vnmine` | `/vnitem list` |

### Hệ thống NPC
| Lệnh | Mô tả | Quyền | Ví dụ |
|------|-------|-------|-------|
| `/vnnpc create <id>` | Tạo NPC tại vị trí đứng | `vnmine.command.npc` | `/vnnpc create skill_master` |
| `/vnnpc remove <id>` | Xóa NPC | `vnmine.command.npc` | `/vnnpc remove skill_master` |
| `/vnnpc list` | Danh sách NPC | `vnmine.command.npc` | `/vnnpc list` |
| `/vnnpc tp <id>` | Dịch chuyển đến NPC | `vnmine.command.npc` | `/vnnpc tp skill_master` |
| `/vnnpc reload` | Reload config NPC | `vnmine.command.npc` | `/vnnpc reload` |

### Hệ thống Linh Thạch
| Lệnh | Mô tả | Quyền | Ví dụ |
|------|-------|-------|-------|
| `/vnbalance` | Xem số dư linh thạch | `vnmine.command.vnmine` | `/vnbalance` |
| `/vnpay <player> <amount>` | Chuyển linh thạch | `vnmine.command.vnmine` | `/vnpay Steve 50` |

### Hệ thống Tọa Kỵ
| Lệnh | Mô tả | Quyền | Ví dụ |
|------|-------|-------|-------|
| `/mount summon <id>` | Triệu hồi tọa kỵ | `vnmine.command.mount` | `/mount summon PHUONG_HOANG` |
| `/mount dismiss` | Hủy tọa kỵ | `vnmine.command.mount` | `/mount dismiss` |
| `/mount list` | Danh sách tọa kỵ | `vnmine.command.mount` | `/mount list` |

### Hệ thống Give (Admin)
| Lệnh | Mô tả | Quyền | Ví dụ |
|------|-------|-------|-------|
| `/vngive <player> skill <id>` | Cho người chơi học skill | `vnmine.command.give` | `/vngive Steve skill FIRE_BALL` |
| `/vngive <player> pill <id> [amount]` | Give đan dược | `vnmine.command.give` | `/vngive Steve pill HOI_LINH_DAN 5` |
| `/vngive <player> artifact <id>` | Give pháp bảo | `vnmine.command.give` | `/vngive Steve artifact FLYING_SWORD` |
| `/vngive <player> currency <amount>` | Give linh thạch | `vnmine.command.give` | `/vngive Steve currency 1000` |
| `/vngive <player> exp <amount>` | Give EXP tu luyện | `vnmine.command.give` | `/vngive Steve exp 5000` |
| `/vngive <player> level <level>` | Set cấp độ | `vnmine.command.give` | `/vngive Steve level 50` |
| `/vngive <player> mount <id>` | Mở khóa tọa kỵ | `vnmine.command.give` | `/vngive Steve mount PHUONG_HOANG` |

### Hệ thống quản trị khác
| Lệnh | Mô tả | Quyền | Ví dụ |
|------|-------|-------|-------|
| `/tps` | Xem TPS server | `vnmine.command.tps` | `/tps` |
| `/save-all` | Lưu toàn bộ dữ liệu | `vnmine.command.saveall` | `/save-all` |
| `/vnmine perm ...` | Quản lý phân quyền | `vnmine.perm.admin` | `/vnmine perm group add admin` |
| `/vnmine world gen <name>` | Tạo world mới | `vnmine.world.gen` | `/vnmine world gen tu_tien` |
| `/vnmine world toggle` | Bật/tắt world gen | `vnmine.world.toggle` | `/vnmine world toggle` |
| `/vnmine drop toggle` | Bật/tắt block drop | `vnmine.drop.toggle` | `/vnmine drop toggle` |
| `/vnmine drop status` | Xem trạng thái drop | `vnmine.drop.status` | `/vnmine drop status` |

---

## 📦 Danh Sách Item ID (Give Command)

### Skill ID

| ID | Kỹ năng | Mô tả |
|----|---------|-------|
| `BASIC_HEAL` | 🌿 Trúc Cơ Liệu Thương | Hồi 10 HP |
| `QI_SHIELD` | 🛡️ Linh Khí Hộ Thể | Khiên 20 sát thương |
| `FIRE_BALL` | 🔥 Hỏa Cầu Thuật | Bắn cầu lửa |
| `WIND_BLADE` | 🌪️ Phong Nhẫn | Lưỡi gió xuyên thấu |
| `LIGHTNING_STRIKE` | ⚡ Thiên Lôi Dẫn | Gọi sét đánh |
| `SPEED_STEP` | 💨 Phi Vân Bộ | Tăng 40% tốc độ 15s |
| `TELEPORT` | 🌌 Thuấn Di | Dịch chuyển tức thời |
| `METEOR_STORM` | ☄️ Tinh Thần Bạo | Mưa sao băng hủy diệt |

**Ví dụ:**
```bash
/vngive Steve skill FIRE_BALL
/vngive Alex skill LIGHTNING_STRIKE
```

### Pill ID (Đan Dược)

| ID | Đan dược | Tác dụng | Material |
|----|----------|----------|----------|
| `HOI_LINH_DAN` | 🌿 Hồi Linh Đan | Hồi 30 linh lực | Glowstone Dust |
| `DAI_HOI_LINH_DAN` | 💎 Đại Hồi Linh Đan | Hồi 100 linh lực + 20% hồi phục 30s | Glowstone |
| `CUONG_THE_DAN` | 💪 Cương Thể Đan | +20% sát thương 60s | Redstone Block |
| `THANH_TAM_DAN` | 🧪 Thanh Tâm Đan | Giải trừ trạng thái xấu | Sugar |
| `TOC_THANH_DAN` | 🏃 Tốc Thánh Đan | +50% tốc độ 30s | Feather |
| `TU_LUYEN_DAN` | ⭐ Tu Luyện Đan | +50 EXP | Purple Dye |
| `PHI_THANG_DAN` | 👑 Phi Thăng Đan | +500 EXP (1 lần/đại cảnh giới) | Nether Star |

**Ví dụ:**
```bash
/vngive Steve pill HOI_LINH_DAN 10
/vngive Alex pill PHI_THANG_DAN 1
```

### Artifact ID (Pháp Bảo)

| ID | Pháp bảo | Tác dụng | Material |
|----|----------|----------|----------|
| `FLYING_SWORD` | 🗡️ Kiếm Phi Hành | Bay trên kiếm | Diamond Sword |
| `SPIRIT_BELL` | 🔔 Linh Chung | Choáng quái AOE | Bell |
| `BAGUA_MIRROR` | 🪞 Bát Quái Kính | Giảm 30% sát thương | Shield |
| `SOUL_JADE` | 💚 Hồn Ngọc | Hồi 50% máu khi HP<20% | Emerald |
| `HEAVEN_SHIELD` | 🛡️ Thiên Linh Thuẫn | Bất tử 5 giây | Netherite Chestplate |
| `THUNDER_SEAL` | ⚡ Lôi Ấn | Gọi sét đánh | Trident |
| `PHOENIX_REBIRTH` | 🐦 Phượng Hoàng Lệnh | Hồi sinh 1 lần | Feather |

**Ví dụ:**
```bash
/vngive Steve artifact FLYING_SWORD
/vngive Alex artifact HEAVEN_SHIELD
```

### Mount ID (Tọa Kỵ)

| ID | Tọa kỵ | Yêu cầu cấp |
|----|--------|-------------|
| `PHUONG_HOANG` | 🦅 Phượng Hoàng Lửa | Cấp 30 |
| `BACH_HO` | 🐅 Bạch Hổ | Cấp 40 |
| `THANH_LONG` | 🐉 Thanh Long | Cấp 50 |

**Ví dụ:**
```bash
/vngive Steve mount PHUONG_HOANG
/vngive Alex mount THANH_LONG
```

---

## 🔐 Permissions (Quyền Hạn)

### Permission Nodes

| Permission | Mô tả | Mặc định |
|------------|-------|----------|
| `vnmine.*` | Tất cả quyền của plugin | OP |
| `vnmine.command.*` | Tất cả quyền command | OP |
| `vnmine.command.vnmine` | Dùng `/vnmine`, `/vn` và hầu hết lệnh | ✅ **TRUE** (mọi người) |
| `vnmine.command.tps` | Xem TPS server | OP |
| `vnmine.command.saveall` | Lưu toàn bộ world | OP |
| `vnmine.command.reload` | Reload config plugin | OP |
| `vnmine.command.npc` | Quản lý NPC (create/remove/list/tp/reload) | OP |
| `vnmine.command.give` | Give item/skill/pill cho người chơi | OP |
| `vnmine.command.mount` | Sử dụng tọa kỵ (summon/dismiss/list) | ✅ **TRUE** (mọi người) |
| `vnmine.time.*` | Tất cả quyền time command | OP |
| `vnmine.time.set` | Set thời gian ngày/đêm | OP |
| `vnmine.time.toggle` | Bật/tắt custom time cycle | OP |
| `vnmine.time.status` | Xem trạng thái thời gian | ✅ **TRUE** (mọi người) |
| `vnmine.perm.*` | Tất cả quyền quản lý permission | OP |
| `vnmine.perm.admin` | Quản lý permission groups và players | OP |
| `vnmine.world.*` | Tất cả quyền world generation | OP |
| `vnmine.world.gen` | Tạo world mới | OP |
| `vnmine.world.toggle` | Bật/tắt world generation | OP |
| `vnmine.drop.*` | Tất cả quyền quản lý block drop | OP |
| `vnmine.drop.toggle` | Bật/tắt block drop features | OP |
| `vnmine.drop.status` | Xem trạng thái block drop | OP |

### Cách cấp quyền

```bash
# Cấp toàn bộ quyền (OP)
/op <player>

# Hoặc dùng permission plugin (LuckPerms, PermissionsEx...)
/lp user <player> permission set vnmine.command.mount true
/lp user <player> permission set vnmine.command.vnmine true
```

---

## 📦 Công Thức Chế Tạo - Luyện Đan

| Đan dược | Công thức | Tác dụng |
|----------|-----------|----------|
| **Hồi Linh Đan** | 🟢 3 Linh Thảo + 💧 1 Nước | Hồi 30 linh lực |
| **Đại Hồi Linh Đan** | ✨ 2 Hồi Linh Đan + 🔴 2 Huyết LT + 🟢 5 LT | Hồi 100 linh lực + buff hồi phục |
| **Cương Thể Đan** | 🔴 3 Huyết LT + 🟢 5 LT + 🔥 1 Blaze | +20% DMG 60s |
| **Thanh Tâm Đan** | 🟢 5 LT + 💧 1 Nước | Giải trừ mọi trạng thái xấu |
| **Tốc Thánh Đan** | 🟢 3 LT + 🍚 2 Đường + 🪶 1 Lông | +50% Speed 30s |
| **Tu Luyện Đan** | 🟢 10 LT + 🔴 5 Huyết LT + 🟠 2 Long Huyết + 🥇 1 Vàng | +50 EXP |
| **Phi Thăng Đan** | ⭐ 3 Tu Luyện Đan + 🟠 10 Long Huyết + 🐉 1 Hơi Rồng + 🪙 2 Netherite | +500 EXP |

**Chú thích:** LT = Linh Thảo (Green Dye) | Huyết LT = Huyết Linh Thảo (Red Dye) | Long Huyết = Long Huyết Thảo (Orange Dye)

---

## 🔧 Công Thức Luyện Chế Pháp Bảo

| Pháp bảo | Công thức | Tác dụng |
|----------|-----------|----------|
| 🗡️ **Kiếm Phi Hành** | 1 🗡️ Kiếm DC + 8 💎 DC + 4 🪶 Lông | Bay trên kiếm |
| 🔔 **Linh Chung** | 1 🔔 Chuông + 4 🥇 Vàng + 2 💎 DC | Choáng quái AOE |
| 🪞 **Bát Quái Kính** | 1 🛡️ Khiên + 4 🪨 Obsidian + 4 💚 Ngọc | Giảm 30% sát thương |
| 💚 **Hồn Ngọc** | 1 💚 Ngọc + 4 🥇 Vàng + 2 👁️ Mắt End | Hồi máu tự động |
| 🛡️ **Thiên Linh Thuẫn** | 1 👕 Giáp Netherite + 8 👁️ Mắt End | Bất tử 5s |
| ⚡ **Lôi Ấn** | 1 🔱 Đinh ba + 4 💎 DC + 2 🐉 Nước Rồng | Gọi sét |
| 🐦 **Phượng Hoàng Lệnh** | 1 🪶 Lông + 8 🧱 Vàng Khối + 4 🪙 Netherite + 1 🥚 Trứng Rồng | Hồi sinh 1 lần |

---

## 📊 Bảng Cảnh Giới

| Cấp độ | Cảnh giới | Tiểu cảnh giới | Màu |
|--------|-----------|---------------|------|
| 1-9 | **Khí Động** | nhất trọng → cửu trọng | `&7` Xám |
| 10 | Khí Động | Đại Viên Mãn | `&7` Xám |
| 11-19 | **Luyện Khí** | nhất trọng → cửu trọng | `&a` Xanh lá |
| 20 | Luyện Khí | Đại Viên Mãn | `&a` Xanh lá |
| 21-29 | **Trúc Cơ** | nhất trọng → cửu trọng | `&b` Xanh aqua |
| 30 | Trúc Cơ | Đại Viên Mãn | `&b` Xanh aqua |
| 31-39 | **Kim Đan** | nhất trọng → cửu trọng | `&e` Vàng |
| 40 | Kim Đan | Đại Viên Mãn | `&e` Vàng |
| 41-49 | **Nguyên Anh** | nhất trọng → cửu trọng | `&6` Vàng đậm |
| 50 | Nguyên Anh | Đại Viên Mãn | `&6` Vàng đậm |
| 51-59 | **Hóa Thần** | nhất trọng → cửu trọng | `&c` Đỏ |
| 60 | Hóa Thần | Đại Viên Mãn | `&c` Đỏ |
| 61-69 | **Hợp Thể** | nhất trọng → cửu trọng | `&5` Tím |
| 70 | Hợp Thể | Đại Viên Mãn | `&5` Tím |
| 71-79 | **Độ Kiếp** | nhất trọng → cửu trọng | `&4` Đỏ đậm |
| 80 | Độ Kiếp | Đại Viên Mãn | `&4` Đỏ đậm |
| 81-89 | **Đại Thừa** | nhất trọng → cửu trọng | `&d` Hồng |
| 90 | Đại Thừa | Đại Viên Mãn | `&d` Hồng |
| 91-99 | **Phi Thăng** | nhất trọng → cửu trọng | `&6&l` Vàng Bold |
| 100 | Phi Thăng | Đại Viên Mãn | `&6&l` Vàng Bold |

---

## 💡 Tính Năng Mở Rộng (Gợi Ý)

### Ý tưởng 1: Linh Thạch - Tiền tệ tu tiên
- Custom item "Linh Thạch" rơi từ quái elite+, quặng đặc biệt
- Dùng để cường hóa trang bị, mua vật phẩm từ NPC

### Ý tưởng 2: Tọa Kỵ Phi Hành
- Đạt level 30+ (Trúc Cơ) có thể cưỡi rồng/phượng bay
- Lệnh `/mount` triệu hồi tọa kỵ

### Ý tưởng 3: Bí Cảnh (Dungeon/Trials)
- Khu vực đặc biệt trên map, chỉ vào được khi đủ level
- Trong bí cảnh có sóng quái elite tăng dần
- Phần thưởng cuối: skill đặc biệt hoặc item truyền thuyết

### Ý tưởng 4: Cường Hóa Trang Bị
```
/cunghoa <slot>   # Mở giao diện cường hóa
Cấp 1→5: 80% tỉ lệ   # +1, +2, +3, +4, +5
Cấp 6→10: 50% tỉ lệ  # +6, +7, +8, +9, +10
Cấp 11+: 20% tỉ lệ   # Vỡ item nếu thất bại!
```

### Ý tưởng 5: Ngũ Hành Tương Sinh
- Mỗi người chơi chọn 1 trong 5 nguyên tố: Kim, Mộc, Thủy, Hỏa, Thổ
- Tương sinh: +20% sát thương
- Tương khắc: -20% sát thương

### Ý tưởng 6: Tông Môn (Clan/Guild)
- Người chơi có thể lập tông môn từ level 30
- Xây dựng căn cứ tông môn
- Boss tông môn hàng tuần
- Chiến tranh tông môn

---

## ⚙️ Cấu Hình (config.yml)

Tất cả các hệ thống đều có thể cấu hình trong `config.yml`:

```yaml
# Bật/tắt từng hệ thống
cultivation:
  enabled: true

skills:
  enabled: true

elite-mob-settings:
  enabled: true

items:
  enabled: true

spirit-farming:
  enabled: true
```

---

## 🔄 Reload

Sau khi thay đổi config, dùng lệnh:
```
/vnmine reload
```

Hoặc reload từng hệ thống:
```
/vnskill reload    # Reload công pháp
/vnitem reload     # Reload item/pháp bảo
/vnnpc reload      # Reload NPC
```

---

## 📝 License

Plugin VNMine - All Rights Reserved  
Phát triển bởi VNMine Team  
Version 2.1.0 - Big Update Tu Tiên 🏔️✨