# 🏔️ VNMine — Plugin Tu Tiên cho Minecraft Paper 1.21+

**Phiên bản:** 2.1.0  
**Tác giả:** VNMine Team  
**Yêu cầu:** Paper 1.21+ (API 1.21.4)

---

## 📖 Tổng Quan (Overview)

**VNMine** là plugin đa năng dành cho máy chủ Minecraft Paper 1.21+, mang phong cách **Tu Tiên Huyền Huyễn** (lấy cảm hứng từ Võ Lâm Truyền Kỳ, Đấu Phá Thương Khung) vào thế giới Minecraft. Plugin tích hợp đầy đủ các hệ thống từ tu luyện, kỹ năng, luyện đan, chế tạo pháp bảo, NPC shop cho đến quản trị server như phân quyền, world generation, block drop, v.v.

### ✨ Tính năng nổi bật

- **🧘 Hệ thống Tu Luyện** — Level, cảnh giới (12 đại cảnh giới), exp, lôi kiếp (Tribulation) với hiệu ứng sét và broadcast
- **💎 Linh Lực (Mana)** — Thanh năng lượng hiển thị ActionBar, hồi phục tự động, dùng để thi triển kỹ năng
- **📚 Công Pháp & Kỹ Năng** — 8 kỹ năng ACTIVE (hồi máu, khiên, hỏa cầu, lôi kích, thuấn di, v.v.), hệ thống thành thục (Proficiency)
- **🎮 Skill Bar** — Thanh kỹ năng nhanh 9 slot, gán kỹ năng qua GUI, thi triển bằng phím Q
- **🔬 Luyện Đan (Alchemy)** — GUI luyện 7 loại đan dược từ nguyên liệu thu thập trong game
- **⚔️ Pháp Bảo (Artifacts)** — Chế tạo 7 loại pháp bảo với hiệu ứng đặc biệt (bay trên kiếm, bất tử, hồi sinh...)
- **🌱 Linh Thảo & Linh Điền** — Trồng trọt, thu hoạch linh thảo trên vùng đất linh điền 7×7
- **👹 Quái Tinh Anh & Boss** — Elite mob (HP×5, DMG×3), 10% hóa Boss (HP×20, DMG×8, kỹ năng đặc biệt)
- **🤖 NPC Shop** — Tạo NPC mua bán vật phẩm trực tiếp trong game
- **💰 Linh Thạch (Currency)** — Tiền tệ tu tiên, rơi từ quái/quặng, giao dịch giữa người chơi
- **🐉 Tọa Kỵ (Mount)** — Cưỡi Phượng Hoàng, Bạch Hổ, Thanh Long bay lượn
- **🔐 Hệ Thống Phân Quyền** — LuckPerms-like với Group hierarchy, inheritance, prefix/suffix
- **🌍 World Generation** — Tạo world tuỳ chỉnh với tỉ lệ quặng/cấu trúc/quái
- **⛏️ Block Drop Advanced** — Cơ chế đập vỡ công cụ, phát nổ khối, thay thế drop ngẫu nhiên
- **🖥️ Menu GUI Trực Quan** — Tất cả chức năng quản lý trong inventory, phím tắt F (Swap Hand)
- **🎨 ColorUtils** — Hỗ trợ mã màu `&` và tên màu (`&red`, `&blue`, `&gold`...), Hex Color

---

## 📋 Thông Tin Kỹ Thuật (Technical Specifications)

| Mục | Chi tiết |
|-----|----------|
| **Phiên bản Minecraft** | 1.21+ (API 1.21.4-R0.1-SNAPSHOT) |
| **Nền tảng tương thích** | Paper, Purpur, Pufferfish (các fork tương thích Paper API) |
| **Ngôn ngữ phát triển** | Java 21 |
| **Công cụ build** | Maven |
| **Dependencies (bắt buộc)** | Không — plugin hoạt động standalone, chỉ cần Paper |
| **Dependencies (khuyến khích)** | Không — plugin có hệ thống phân quyền riêng tích hợp sẵn |
| **Lưu trữ dữ liệu** | YAML (config.yml, permissions.yml, cultivation.yml) |
| **File cấu hình chính** | `config.yml` (tất cả hệ thống), `permissions.yml` (phân quyền), `cultivation.yml` (cảnh giới) |

### 📥 Hướng dẫn cài đặt nhanh

1. **Tải file `.jar`** — Bản mới nhất: `VNMine-2.1.0.jar`
2. **Đặt vào thư mục plugins** — Sao chép file `.jar` vào thư mục `plugins/` của server
3. **Khởi động lại server** — Dùng lệnh `restart` hoặc `stop` rồi `start` lại
   > ⚠️ **Khuyến cáo:** Luôn **restart** server sau khi cài plugin mới, không dùng `/reload` vì có thể gây lỗi không mong muốn.
4. **Phân quyền** — Mặc định người chơi có thể dùng `/vn` và `/vnbalance`. Admin/OP có toàn quyền.

---

## ⌨️ Các Lệnh & Ví Dụ Cụ Thể (Commands & Examples)

### 🔹 Lệnh chính & Menu

| Lệnh | Mô tả | Ví dụ |
|------|-------|-------|
| `/vn` | Mở menu chính VNMine (phím tắt **F**) | `/vn` |
| `/vnmine menu` | Mở menu chính | `/vnmine menu` |
| `/vn start` | Bắt đầu hành trình tu tiên | `/vn start` |
| `/vnmine reload` | Reload toàn bộ cấu hình plugin | `/vnmine reload` |
| `/tps` | Xem TPS server | `/tps` |
| `/save-all` (hoặc `/save`) | Lưu toàn bộ world | `/save-all` |

### 🔹 Hệ thống Thời Gian

| Lệnh | Mô tả | Ví dụ |
|------|-------|-------|
| `/vnmine time on` | Bật chu kỳ thời gian tuỳ chỉnh | `/vnmine time on` |
| `/vnmine time off` | Tắt chu kỳ thời gian tuỳ chỉnh | `/vnmine time off` |
| `/vnmine time set day 15` | Đặt thời gian ban ngày 15 phút | `/vnmine time set day 15` |
| `/vnmine time set night 5` | Đặt thời gian ban đêm 5 phút | `/vnmine time set night 5` |
| `/vnmine time status` | Xem trạng thái thời gian hiện tại | `/vnmine time status` |

### 🔹 Hệ thống Tu Luyện (Cultivation)

| Lệnh | Mô tả | Ví dụ |
|------|-------|-------|
| `/vnmine cultivate info` | Xem thông tin tu vi (cấp, cảnh giới, exp, linh lực) | `/vnmine cultivate info` |
| `/vnmine cultivate toggle` | Bật/tắt hệ thống tu luyện | `/vnmine cultivate toggle` |
| `/vnmine elite toggle` | Bật/tắt quái tinh anh & boss | `/vnmine elite toggle` |
| `/vnmine elite info` | Xem trạng thái hệ thống elite mob | `/vnmine elite info` |

### 🔹 Hệ thống Công Pháp & Kỹ Năng

| Lệnh | Mô tả | Ví dụ |
|------|-------|-------|
| `/vnskill` | Mở menu học kỹ năng | `/vnskill` |
| `/vnskill my` | Mở menu quản lý Skill Bar cá nhân | `/vnskill my` |
| `/vnskill bar` | Mở menu quản lý Skill Bar | `/vnskill bar` |
| `/vnskill toggle` | Bật/tắt hệ thống công pháp | `/vnskill toggle` |
| `/vnskill reload` | Reload cấu hình hệ thống công pháp | `/vnskill reload` |
| `/vnskill book <người_chơi> <id> <phẩm_cấp> <phân_cấp>` | (Admin) Give sách kỹ năng | `/vnskill book hoanbh FIRE_BALL THIEN THUONG` |

### 🔹 Hệ thống Luyện Đan & Pháp Bảo

| Lệnh | Mô tả | Ví dụ |
|------|-------|-------|
| `/vnalchemy` | Mở lò luyện đan | `/vnalchemy` |
| `/vnitem toggle` | Bật/tắt hệ thống item & pháp bảo | `/vnitem toggle` |
| `/vnitem list` | Xem danh sách pháp bảo có trong hệ thống | `/vnitem list` |
| `/vnitem reload` | Reload cấu hình item & pháp bảo | `/vnitem reload` |

### 🔹 Hệ thống Linh Điền

| Lệnh | Mô tả | Ví dụ |
|------|-------|-------|
| `/vnfarm` | Mở menu quản lý linh điền | `/vnfarm` |

### 🔹 Hệ thống NPC Shop

| Lệnh | Mô tả | Ví dụ |
|------|-------|-------|
| `/vnnpc create <id>` | Tạo NPC tại vị trí hiện tại | `/vnnpc create skill_master` |
| `/vnnpc remove <id>` | Xóa NPC theo ID | `/vnnpc remove skill_master` |
| `/vnnpc removeall` | Xóa tất cả NPC | `/vnnpc removeall` |
| `/vnnpc list` | Danh sách tất cả NPC đã tạo | `/vnnpc list` |
| `/vnnpc tp <id>` | Dịch chuyển đến NPC | `/vnnpc tp skill_master` |
| `/vnnpc movehere <id>` | Di chuyển NPC đến vị trí hiện tại | `/vnnpc movehere skill_master` |
| `/vnnpc rename <id> <tên_mới>` | Đổi tên NPC | `/vnnpc rename skill_master "&bSư Phụ Mới"` |
| `/vnnpc skin <id> <tên_skin>` | Đổi skin NPC theo tên người chơi | `/vnnpc skin skill_master hoanbh` |
| `/vnnpc reload` | Reload cấu hình NPC | `/vnnpc reload` |

### 🔹 Hệ thống Linh Thạch & Tọa Kỵ

| Lệnh | Mô tả | Ví dụ |
|------|-------|-------|
| `/vnbalance` | Xem số dư linh thạch hiện tại | `/vnbalance` |
| `/vnpay <người_chơi> <số_lượng>` | Chuyển linh thạch cho người chơi khác | `/vnpay hoanbh 100` |
| `/mount summon <id>` | Triệu hồi tọa kỵ | `/mount summon PHUONG_HOANG` |
| `/mount dismiss` | Hủy tọa kỵ hiện tại | `/mount dismiss` |
| `/mount list` | Xem danh sách tọa kỵ đã mở khóa | `/mount list` |

### 🔹 Hệ thống Give (Admin)

| Lệnh | Mô tả | Ví dụ |
|------|-------|-------|
| `/vngive <người_chơi> skill <id>` | Give kỹ năng cho người chơi | `/vngive hoanbh skill FIRE_BALL` |
| `/vngive <người_chơi> pill <id> [số_lượng]` | Give đan dược | `/vngive hoanbh pill HOI_LINH_DAN 10` |
| `/vngive <người_chơi> artifact <id>` | Give pháp bảo | `/vngive hoanbh artifact FLYING_SWORD` |
| `/vngive <người_chơi> currency <số_lượng>` | Give linh thạch | `/vngive hoanbh currency 1000` |
| `/vngive <người_chơi> exp <số_lượng>` | Give EXP tu luyện | `/vngive hoanbh exp 5000` |
| `/vngive <người_chơi> level <cấp>` | Set cấp độ tu luyện | `/vngive hoanbh level 50` |
| `/vngive <người_chơi> mount <id>` | Mở khóa tọa kỵ | `/vngive hoanbh mount PHUONG_HOANG` |

### 🔹 Hệ thống Phân Quyền (Permission)

| Lệnh | Mô tả | Ví dụ |
|------|-------|-------|
| `/vnmine perm group list` | Danh sách tất cả nhóm quyền | `/vnmine perm group list` |
| `/vnmine perm group info <group>` | Xem thông tin chi tiết nhóm | `/vnmine perm group info admin` |
| `/vnmine perm group create <group>` | Tạo nhóm quyền mới | `/vnmine perm group create mod` |
| `/vnmine perm group addperm <group> <node>` | Thêm quyền cho nhóm | `/vnmine perm group addperm admin vnmine.command.reload` |
| `/vnmine perm group addparent <group> <parent>` | Thêm nhóm cha (kế thừa quyền) | `/vnmine perm group addparent admin member` |
| `/vnmine perm group setprefix <group> <prefix>` | Đặt prefix cho nhóm | `/vnmine perm group setprefix admin "&c[Admin]"` |
| `/vnmine perm player info <người_chơi>` | Xem thông tin quyền của người chơi | `/vnmine perm player info hoanbh` |
| `/vnmine perm player setgroup <người_chơi> <group>` | Gán nhóm cho người chơi | `/vnmine perm player setgroup hoanbh admin` |
| `/vnmine perm player addperm <người_chơi> <node>` | Thêm quyền riêng cho người chơi | `/vnmine perm player addperm hoanbh vnmine.command.admin` |
| `/vnmine perm check <người_chơi> <node>` | Kiểm tra người chơi có quyền không | `/vnmine perm check hoanbh vnmine.command.admin` |
| `/vnmine perm reload` | Reload hệ thống phân quyền | `/vnmine perm reload` |

### 🔹 Hệ thống World Generation & Block Drop

| Lệnh | Mô tả | Ví dụ |
|------|-------|-------|
| `/vnmine world gen <tên_world>` | Tạo world mới với cấu hình tuỳ chỉnh | `/vnmine world gen tu_tien` |
| `/vnmine world toggle` | Bật/tắt world generation | `/vnmine world toggle` |
| `/vnmine drop toggle` | Bật/tắt hệ thống block drop đặc biệt | `/vnmine drop toggle` |
| `/vnmine drop status` | Xem trạng thái hệ thống block drop | `/vnmine drop status` |

### 🔹 Admin Menu

| Lệnh | Mô tả | Ví dụ |
|------|-------|-------|
| `/vnadmin` | Mở menu admin GUI — lấy item test (đan dược, pháp bảo, nguyên liệu) chỉ với 1 click | `/vnadmin` |

---

## 🔐 Các Quyền Hạn (Permissions)

### 👤 Quyền dành cho Người chơi (mặc định: `true`)

| Node quyền | Mô tả |
|------------|-------|
| `vnmine.command.vnmine` | Cho phép dùng `/vnmine`, `/vn` và hầu hết các lệnh cơ bản |
| `vnmine.command.mount` | Cho phép sử dụng tọa kỵ (`/mount summon`, `/mount dismiss`, `/mount list`) |
| `vnmine.time.status` | Cho phép xem trạng thái thời gian (`/vnmine time status`) |

### 🛡️ Quyền dành cho Quản trị viên (mặc định: `op`)

| Node quyền | Mô tả |
|------------|-------|
| `vnmine.*` | Toàn bộ quyền của plugin (super node) |
| `vnmine.command.*` | Tất cả quyền command |
| `vnmine.command.tps` | Xem TPS server |
| `vnmine.command.saveall` | Lưu toàn bộ world |
| `vnmine.command.reload` | Reload cấu hình plugin |
| `vnmine.command.npc` | Quản lý NPC (tạo, xóa, dịch chuyển, đổi skin...) |
| `vnmine.command.give` | Give item/skill/pill/currency/exp cho người chơi |
| `vnmine.command.admin` | Mở menu admin lấy item test |
| `vnmine.time.*` | Tất cả quyền về thời gian |
| `vnmine.time.set` | Set thời gian ngày/đêm |
| `vnmine.time.toggle` | Bật/tắt chu kỳ thời gian tuỳ chỉnh |
| `vnmine.perm.*` | Tất cả quyền quản lý phân quyền |
| `vnmine.perm.admin` | Quản lý permission groups và players |
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
- Xem danh sách nhóm quyền và cấu hình chi tiết trong file `permissions.yml`

---

## ⚙️ Cấu Hình (Configuration)

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
  enabled: true          # Bật/tắt hệ thống phân quyền tích hợp
  groups:
    default:             # Nhóm mặc định cho người chơi mới
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
  players: {}            # Lưu thông tin người chơi riêng lẻ (tự động)

# ===================================
# III. HỆ THỐNG TU LUYỆN
# ===================================
cultivation:
  enabled: true
  exp-per-level-multiplier: 100   # EXP cần = level × 100
  realms:
    1:   { name: "Khí Động",  prefix: "&7[Khí Động" }
    11:  { name: "Luyện Khí", prefix: "&a[Luyện Khí" }
    21:  { name: "Trúc Cơ",   prefix: "&b[Trúc Cơ" }
    # ... (xem chi tiết trong file)
  exp-sources:
    kill-mob:   { enabled: true, amount: 10 }
    kill-elite: { enabled: true, amount: 50 }
    kill-boss:  { enabled: true, amount: 200 }
    break-ore:  { enabled: true, amount: 5 }
    fishing:    { enabled: true, amount: 15 }
  mana:
    base-max-mana: 100            # Linh lực tối đa cơ bản
    mana-per-level: 10            # +10 linh lực mỗi cấp
    regen:
      base-amount: 2              # Hồi 2 linh lực mỗi lần
      regen-interval-ticks: 40    # 2 giây/lần
      combat-delay-ticks: 100     # Delay 5s sau combat
  tribulation:
    enabled: true
    damage:
      base: 3.0
      per-strike-multiplier: 1.2
      damage-per-level: 0.8
      armor-reduction: 0.6
    level-drop-on-fail: 1         # Số level tụt khi độ kiếp thất bại

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
      # ... (xem chi tiết trong file)
    # ... (FIRE_BALL, LIGHTNING_STRIKE, TELEPORT, v.v.)

# ===================================
# V. HỆ THỐNG NPC SHOP
# ===================================
npc-shop:
  enabled: true
  npcs:
    skill_master:        # NPC dạy kỹ năng
      enabled: true
      name: "&b&l◆ Đại Sư Công Pháp ◆"
      type: "VILLAGER"
      trades:
        SKILL_BASIC_HEAL:
          type: "SKILL"
          price:
            material: "EMERALD"
            amount: 5
    artifact_master:     # NPC chế tạo pháp bảo
      enabled: true
      name: "&6&l◆ Luyện Khí Đại Sư ◆"
    pill_master:         # NPC bán đan dược
      enabled: true
      name: "&a&l◆ Luyện Đan Sư ◆"
    currency_master:     # NPC đổi linh thạch
      enabled: true
      name: "&e&l◆ Linh Thạch Thương ◆"

# ===================================
# VI. HỆ THỐNG LINH THẠCH
# ===================================
currency:
  enabled: true
  item:
    material: "PRISMARINE_SHARD"
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
  required-level: 30
  mana-per-second: 3
  mounts:
    PHUONG_HOANG:        # Phượng Hoàng Lửa — cấp 30
      speed: 0.5
      flight-speed: 0.8
    BACH_HO:             # Bạch Hổ — cấp 40
      speed: 0.7
      flight-speed: 0.9
    THANH_LONG:          # Thanh Long — cấp 50
      speed: 0.6
      flight-speed: 1.0
```

> 📝 Cấu hình đầy đủ (block drop, skill books, world gen, elite mob) xem trong file `config.yml` của plugin.

---

## 🆘 Hỗ trợ & Liên hệ

- **Báo lỗi & Đóng góp ý tưởng:** Mở issue trên GitHub
- **Wiki chi tiết:** Xem tại repository của dự án

---

## 📝 License

**VNMine Plugin** — All Rights Reserved  
Phát triển bởi VNMine Team  
Phiên bản 2.1.0 — Big Update Tu Tiên 🏔️✨