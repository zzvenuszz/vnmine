<p align="center">
  <br/>
  <img src="https://raw.githubusercontent.com/VNMine/plugin/main/logo.png" alt="VNMine" width="200"/>
  <br/>
  <h1 align="center">⛏️ VNMine</h1>
  <p align="center">
    <em>All-in-One Minecraft Paper Plugin — Time, Permissions, Worlds, Block Drops</em>
    <br/>
    <em>Plugin đa năng cho Minecraft Paper — Thời gian, Phân quyền, Thế giới, Rơi khối</em>
  </p>
  <p align="center">
    <img src="https://img.shields.io/badge/version-1.0.0-blue?style=for-the-badge" alt="Version 1.0.0"/>
    <img src="https://img.shields.io/badge/Minecraft-1.21-success?style=for-the-badge" alt="Minecraft 1.21"/>
    <img src="https://img.shields.io/badge/API-Paper_26.1.2-informational?style=for-the-badge" alt="Paper API"/>
    <img src="https://img.shields.io/badge/Java-25-orange?style=for-the-badge" alt="Java 25"/>
    <img src="https://img.shields.io/badge/license-MIT-lightgrey?style=for-the-badge" alt="License MIT"/>
  </p>
</p>

---

# Tổng Quan · Overview

**VNMine** là một plugin Minecraft Paper đa năng, mạnh mẽ, dành cho quản trị viên máy chủ muốn kiểm soát toàn bộ trải nghiệm chơi game. Plugin kết hợp **4 hệ thống thiết yếu** trong một file JAR duy nhất, nhẹ và dễ cấu hình.

**VNMine** is a powerful all-in-one Minecraft Paper plugin designed for server administrators who want complete control over their gameplay experience. It combines **4 essential systems** into a single, lightweight, and highly configurable JAR.

| Tính năng · Feature | Mô tả · Description |
|---|---|
| ⏰ **Quản lý Thời gian · Time Management** | Tùy chỉnh chu kỳ ngày/đêm với thời lượng cấu hình được · Customizable day/night cycle |
| 🔐 **Hệ thống Phân quyền · Permission System** | Quản lý nhóm & quyền hạn như LuckPerms · Full group-based permission management |
| 🌍 **Tạo Thế giới · World Generation** | Tạo thế giới tùy chỉnh với tỉ lệ quặng, công trình, mob · Custom world creation with adjustable rates |
| ⛏️ **Hệ thống Rơi Khối · Block Drop System** | Khai thác động: thay thế vật phẩm, gãy cúp, phát nổ · Dynamic mining mechanics |

---

# Tính Năng · Features

## ⏰ Quản Lý Thời Gian · Time Management

| Tiếng Việt | English |
|---|---|
| Đặt thời lượng ban ngày và ban đêm (phút) | Set custom duration for day and night cycles (in minutes) |
| Bật/tắt chu kỳ thời gian tùy chỉnh | Toggle custom time cycle on/off at any time |
| Xem trạng thái thời gian hiện tại | View current time status and phase |
| Tiến trình thời gian dựa trên tick mượt mà | Smooth tick-based time progression |

## 🔐 Hệ Thống Phân Quyền · Permission System

| Tiếng Việt | English |
|---|---|
| Quản lý quyền dựa trên **nhóm** với thứ bậc weight | **Group-based** permission management with weight hierarchy |
| **Kế thừa** — nhóm con có thể thừa hưởng quyền từ nhóm cha | **Inheritance** — groups inherit permissions from parent groups |
| Ghi đè quyền **theo từng người chơi** | **Per-player** permission overrides |
| Hỗ trợ **Prefix/Suffix** hiển thị trong chat | **Prefix/Suffix** support for chat display |
| Kiểm tra quyền với wildcard (`vnmine.*`) | **Permission checking** with wildcard support |
| **Quyền âm** — từ chối rõ ràng một node | **Negative permissions** to explicitly deny nodes |
| Nhóm có sẵn: `default`, `member`, `vip`, `admin` | Pre-configured groups included |

## 🌍 Tạo Thế Giới · World Generation

| Tiếng Việt | English |
|---|---|
| Tạo thế giới tùy chỉnh với seed, loại và môi trường cụ thể | Create custom worlds with specific seeds, types, and environments |
| **Hệ số quặng** — giảm/tăng quặng theo từng thế giới | **Ore rate multipliers** — control ore spawns per world |
| **Hệ số công trình** — kiểm soát độ hiếm của công trình | **Structure rate multipliers** — control structure rarity |
| **Hệ số mob** — điều chỉnh spawn của mob thù địch, hiền lành, dưới nước | **Mob rate multipliers** — adjust various mob spawns |
| Tự động tạo thế giới khi khởi động máy chủ | Auto-generate worlds on server start |

## ⛏️ Hệ Thống Rơi Khối · Block Drop System

Ba cơ chế động kích hoạt khi đào một số khối nhất định:
Three dynamic mechanics trigger when mining specific blocks:

1. **Thay thế Vật phẩm · Block Replacement** — khối đào ra có thể rơi vật phẩm khác (VD: kim cương rơi ra than)
2. **Gãy Cúp · Tool Breaking** — cúp có tỉ lệ bị gãy khi đào
3. **Phát Nổ · Explosions** — khối có thể phát nổ, gây sát thương và cháy

Mỗi cơ chế được cấu hình **theo từng khối** và **theo từng loại cúp**, với tỉ lệ và thông báo tùy chỉnh.

---

# Cài Đặt · Installation

## Yêu Cầu · Requirements

| Thành phần · Dependency | Yêu cầu · Version |
|---|---|
| [Paper API](https://papermc.io/) | 1.21+ (26.1.2) |
| [Java](https://jdk.java.net/) | 25+ |
| [Maven](https://maven.apache.org/) | 3.x (để build · for building) |

## Các Bước · Steps

```bash
# Tải file JAR mới nhất từ bản phát hành
# Download the latest VNMine JAR from releases

# Đặt vào thư mục plugins của máy chủ
# Place it in your server's plugins folder
cp VNMine-1.0.0.jar /path/to/server/plugins/

# Khởi động lại hoặc reload máy chủ
# Restart or reload your server
```

## Kiểm Tra · Verify

Khi khởi động thành công, bạn sẽ thấy dòng log:
When successfully started, you will see this log message:

```log
[VNMine] VNMine plugin da duoc bat!
```

Plugin sẽ tự động tạo file `config.yml` mặc định ở lần chạy đầu tiên.

---

# Lệnh · Commands

## Lệnh Chính · Main Command

| Lệnh · Command | Bí danh · Alias | Mô tả · Description | Quyền · Permission |
|---|---|---|---|
| `/vnmine` | `/vm` | Lệnh chính của plugin · Main plugin command | `vnmine.command.vnmine` |

## Lệnh Phụ · Subcommands

### ⏰ Thời Gian · Time (`/vnmine time`)

| Lệnh phụ · Subcommand | Mô tả · Description | Quyền · Permission |
|---|---|---|
| `set day <phút>` | Đặt thời gian ban ngày · Set day duration | `vnmine.time.set` |
| `set night <phút>` | Đặt thời gian ban đêm · Set night duration | `vnmine.time.set` |
| `on` | Bật chu kỳ thời gian tùy chỉnh · Enable custom cycle | `vnmine.time.toggle` |
| `off` | Tắt chu kỳ thời gian tùy chỉnh · Disable custom cycle | `vnmine.time.toggle` |
| `status` | Xem trạng thái thời gian · View time status | `vnmine.time.status` |

**Cách dùng · Usage:**
```
/vnmine time set day 15
/vnmine time set night 10
/vnmine time on
/vnmine time status
```

### 🔐 Phân Quyền · Permission (`/vnmine perm`)

#### Quản lý Nhóm · Group Management

| Lệnh phụ · Subcommand | Mô tả · Description |
|---|---|
| `group list` | Danh sách nhóm · List all groups |
| `group info <nhóm>` | Thông tin nhóm · Show group details |
| `group create <nhóm>` | Tạo nhóm mới · Create a new group |
| `group delete <nhóm>` | Xóa nhóm · Delete a group |
| `group setweight <nhóm> <weight>` | Đặt weight · Set group weight |
| `group setprefix <nhóm> <tiền-tố>` | Đặt prefix · Set group prefix |
| `group setsuffix <nhóm> <hậu-tố>` | Đặt suffix · Set group suffix |
| `group addperm <nhóm> <quyền>` | Thêm quyền · Add permission to group |
| `group removeperm <nhóm> <quyền>` | Xóa quyền · Remove permission from group |
| `group addparent <nhóm> <nhóm-cha>` | Thêm nhóm cha · Set parent group |
| `group removeparent <nhóm>` | Xóa nhóm cha · Remove parent group |
| `group setdefault <nhóm>` | Đặt làm mặc định · Set as default group |

#### Quản lý Người Chơi · Player Management

| Lệnh phụ · Subcommand | Mô tả · Description |
|---|---|
| `player info <người-chơi>` | Xem thông tin người chơi · Show player details |
| `player setgroup <người-chơi> <nhóm>` | Gán nhóm · Set player's primary group |
| `player addperm <người-chơi> <quyền>` | Thêm quyền riêng · Add per-player permission |
| `player removeperm <người-chơi> <quyền>` | Xóa quyền riêng · Remove per-player permission |

#### Tiện Ích · Utility

| Lệnh phụ · Subcommand | Mô tả · Description |
|---|---|
| `check <người-chơi> <quyền>` | Kiểm tra quyền · Check player permission |
| `reload` | Tải lại cấu hình phân quyền · Reload permission config |

### 🌍 Thế Giới · World (`/vnmine world`)

| Lệnh phụ · Subcommand | Mô tả · Description | Quyền · Permission |
|---|---|---|
| `gen <thế-giới>` | Tạo thế giới mới · Generate a new world | `vnmine.world.gen` |
| `toggle` | Bật/tắt tự động tạo thế giới · Toggle auto generation | `vnmine.world.toggle` |

### ⛏️ Rơi Khối · Block Drop (`/vnmine drop`)

| Lệnh phụ · Subcommand | Mô tả · Description | Quyền · Permission |
|---|---|---|
| `toggle` | Bật/tắt hệ thống · Enable/disable system | `vnmine.drop.toggle` |
| `status` | Xem trạng thái · View system status | `vnmine.drop.status` |
| `replace toggle` | Bật/tắt thay thế khối · Toggle block replacement | `vnmine.drop.toggle` |
| `break toggle` | Bật/tắt gãy cúp · Toggle tool breaking | `vnmine.drop.toggle` |
| `explode toggle` | Bật/tắt phát nổ · Toggle explosions | `vnmine.drop.toggle` |

### ⚙️ Lệnh Tiện Ích · Utility Commands

| Lệnh · Command | Mô tả · Description | Quyền · Permission |
|---|---|---|
| `/vnmine reload` | Tải lại toàn bộ cấu hình · Reload all configs | `vnmine.command.reload` |
| `/tps` | Xem TPS máy chủ · View server TPS | `vnmine.command.tps` |
| `/save-all` (`/save`) | Lưu toàn bộ thế giới · Save all worlds | `vnmine.command.saveall` |

---

# Quyền Hạn · Permissions

## Quyền Wildcard · Wildcard Permissions

| Quyền · Permission | Mô tả · Description | Mặc định · Default |
|---|---|---|
| `vnmine.*` | Tất cả quyền VNMine · All VNMine permissions | OP |
| `vnmine.command.*` | Tất cả quyền lệnh · All command permissions | OP |
| `vnmine.time.*` | Tất cả quyền thời gian · All time permissions | OP |
| `vnmine.perm.*` | Tất cả quyền quản lý phân quyền · All perm management | OP |
| `vnmine.world.*` | Tất cả quyền thế giới · All world permissions | OP |
| `vnmine.drop.*` | Tất cả quyền rơi khối · All block drop permissions | OP |

## Chi Tiết Quyền · Detailed Permission Nodes

### Quyền Lệnh · Command Permissions

| Quyền · Permission | Mô tả · Description | Mặc định · Default |
|---|---|---|
| `vnmine.command.vnmine` | Dùng lệnh `/vnmine` · Use `/vnmine` | **Mọi người · Everyone** |
| `vnmine.command.tps` | Xem TPS · View server TPS | OP |
| `vnmine.command.saveall` | Lưu thế giới · Save all worlds | OP |
| `vnmine.command.reload` | Tải lại cấu hình · Reload config | OP |

### Quyền Thời Gian · Time Permissions

| Quyền · Permission | Mô tả · Description | Mặc định · Default |
|---|---|---|
| `vnmine.time.set` | Đặt thời gian ngày/đêm · Set day/night | OP |
| `vnmine.time.toggle` | Bật/tắt chu kỳ tùy chỉnh · Toggle custom cycle | OP |
| `vnmine.time.status` | Xem trạng thái thời gian · View time status | **Mọi người · Everyone** |

### Quyền Quản Lý Phân Quyền · Permission Management

| Quyền · Permission | Mô tả · Description | Mặc định · Default |
|---|---|---|
| `vnmine.perm.admin` | Toàn quyền quản lý phân quyền · Full permission control | OP |

### Quyền Thế Giới · World Permissions

| Quyền · Permission | Mô tả · Description | Mặc định · Default |
|---|---|---|
| `vnmine.world.gen` | Tạo thế giới mới · Generate new worlds | OP |
| `vnmine.world.toggle` | Bật/tắt tạo thế giới · Toggle world generation | OP |

### Quyền Rơi Khối · Block Drop Permissions

| Quyền · Permission | Mô tả · Description | Mặc định · Default |
|---|---|---|
| `vnmine.drop.toggle` | Bật/tắt tính năng rơi khối · Toggle drop features | OP |
| `vnmine.drop.status` | Xem trạng thái · View drop system status | OP |

---

# Cấu Hình · Configuration

Plugin tự động tạo file `config.yml` đầy đủ ở lần khởi động đầu tiên. Dưới đây là các phần cấu hình chính.

The plugin generates a comprehensive `config.yml` on first startup. Below are the key configuration sections.

## I. Cài Đặt Thời Gian · Time Settings

```yaml
# Thời lượng ban ngày và ban đêm (phút)
# Duration of day and night in minutes
day-minutes: 10
night-minutes: 10
```

## II. Hệ Thống Phân Quyền · Permission System

```yaml
permission-system:
  enabled: true

  # Danh sách các nhóm · Groups list
  groups:
    default:
      weight: 0
      prefix: "&7"
      suffix: ""
      default: true           # Nhóm mặc định · Default group
      parents: []             # Nhóm cha · Parent groups
      permissions:
        - vnmine.time.status
        - vnmine.command.vnmine
        - minecraft.command.me
        - minecraft.command.tell
        - minecraft.command.help
        - minecraft.command.list

    member:
      weight: 10
      prefix: "&7[Member]"
      default: false
      parents: [default]
      permissions:
        - vnmine.command.tps

    vip:
      weight: 20
      prefix: "&6[VIP]"
      default: false
      parents: [member]
      permissions:
        - essentials.fly
        - essentials.feed
        - essentials.heal
        - minecraft.command.fly

    admin:
      weight: 100
      prefix: "&c[Admin]"
      default: false
      parents: [member]
      permissions:
        - vnmine.*
        - minecraft.command.op
        - minecraft.command.gamemode
        - minecraft.command.ban
        - minecraft.command.kick

  # Ghi đè theo người chơi · Per-player overrides
  players:
    # Steve:
    #   group: admin
    #   permissions:
    #     - vnmine.time.set: false
    #   prefix: "&4[Owner]"
    #   suffix: ""
```

### Luồng Kế Thừa · Inheritance Flow

```
default  (weight: 0)
  └── member  (weight: 10)
        ├── vip  (weight: 20)
        └── admin  (weight: 100)
```

## III. Tạo Thế Giới · World Generation

```yaml
world-settings:
  enabled: true

  worlds:
    vnmine_world:
      generate-if-not-exists: true   # Tự động tạo nếu chưa tồn tại
      seed: ""                        # Seed (để trống = random)
      type: NORMAL                    # NORMAL, FLAT, LARGE_BIOMES, AMPLIFIED
      environment: NORMAL             # NORMAL, NETHER, THE_END

      # Hệ số quặng · Ore spawn rates (multiplier vs default)
      ore-rates:
        coal_ore: 1.0
        iron_ore: 0.8
        gold_ore: 0.6
        diamond_ore: 0.5
        emerald_ore: 0.3
        redstone_ore: 0.7
        lapis_ore: 0.7
        copper_ore: 0.9
        ancient_debris: 0.4

      # Hệ số công trình · Structure spawn rates
      structure-rates:
        ancient_city: 0.3
        stronghold: 0.5
        mineshaft: 0.6
        fortress: 0.4
        bastion_remnant: 0.5
        monument: 0.4
        mansion: 0.3
        village: 0.8
        trial_chambers: 0.5
        ruined_portal: 0.7
        shipwreck: 0.6
        desert_pyramid: 0.5
        jungle_pyramid: 0.5
        igloo: 0.6
        swamp_hut: 0.6

      # Hệ số mob · Mob spawn rates
      mob-rates:
        hostiles: 2.0     # Mob thù địch · Hostile
        passive: 0.5      # Mob hiền lành · Passive
        ambient: 1.0      # Mob môi trường · Ambient
        water: 0.8        # Mob dưới nước · Water
```

## IV. Cấu Hình Rơi Khối · Block Drop Rules

```yaml
block-drop-settings:
  enabled: true
  replace-enabled: true    # Cho phép thay thế · Allow replacement
  break-enabled: true      # Cho phép gãy cúp · Allow tool break
  explode-enabled: true    # Cho phép nổ · Allow explosions

  rules:
    # Mỗi rule nhắm vào một khối cụ thể
    # Each rule targets a specific block
    - block: diamond_ore
      # Cấu hình theo từng loại cúp · Per-tool configuration
      iron_pickaxe:
        drop-replace:
          enabled: true
          replace-block: coal      # Thay thế bằng than · Replace with coal
          chance: 30               # 30% tỉ lệ · 30% chance
        tool-break:
          enabled: true
          chance: 5
          message: "&cCúp sắt của bạn đã bị gãy khi đào kim cương!"
        explode:
          enabled: true
          power: 3.0
          break-blocks: true
          set-fire: false
          damage-player: true
          chance: 10
          message: "&cBùm! Khối kim cương phát nổ khi bạn đào nó!"

      diamond_pickaxe:
        drop-replace:
          enabled: true
          replace-block: coal
          chance: 10
        tool-break:
          enabled: true
          chance: 1
          message: "&cCúp kim cương của bạn bị nứt!"
        explode:
          enabled: true
          power: 3.0
          chance: 5
          message: "&cKhối kim cương phát nổ!"
```

### Bảng Tỉ Lệ Rủi Ro · High-Risk Block Examples

| Khối · Block | Rủi ro · Risk | Thông báo · Warning Message |
|---|---|---|
| `ancient_debris` | 💥 **Nổ 5.0 + lửa** (20%), 3% gãy cúp, 25% thay thế | `&4Ancient Debris phát nổ! Lửa nether lan ra xung quanh!` |
| `deepslate_diamond_ore` | 💥 Nổ 3.5 (15% cúp sắt, 8% kim cương), 8% gãy cúp sắt | `&cKhối deepslate kim cương phát nổ dữ dội!` |
| `diamond_ore` | 💥 Nổ 3.0 (10% sắt, 5% kim cương), 5% gãy cúp, 30% thay thế | `&cBùm! Khối kim cương phát nổ khi bạn đào nó!` |
| `emerald_ore` | 💥 Nổ 2.5 (8%), 20% thay thế | `&aKhối ngọc lục bảo phát nổ!` |
| `gold_ore` | 💥 Nổ 2.0 (3-5%) | `&eVàng phát nổ!` |

---

# Bắt Đầu Nhanh · Quick Start

## 1. Thiết Lập Nhóm Quyền · Set Up a Permission Group

```bash
# Tiếng Việt:
# Tạo nhóm mới
/vnmine perm group create builder

# Đặt thuộc tính
/vnmine perm group setprefix builder "&a[Builder]"
/vnmine perm group setsuffix builder " &7✦"
/vnmine perm group setweight builder 30

# Thêm quyền
/vnmine perm group addperm builder vnmine.time.status
/vnmine perm group addperm builder vnmine.command.tps

# Thêm nhóm cha (kế thừa)
/vnmine perm group addparent builder member

# Gán người chơi
/vnmine perm player setgroup Steve builder

# --- English ---
# Create a new group
/vnmine perm group create builder
# Set properties, add permissions, assign player (same commands)
```

## 2. Tạo Thế Giới Mới · Create a Challenging World

```bash
# Cấu hình trong config.yml (xem phần Tạo Thế Giới)
# Configure in config.yml (see World Generation section)
# Sau đó tạo · Then generate:
/vnmine world gen hard_world
```

## 3. Tùy Chỉnh Rơi Khối · Customize Block Drops

Chỉnh sửa `config.yml` tại mục `block-drop-settings.rules` để thêm rule tùy chỉnh cho từng khối và từng loại cúp, sau đó reload:

Edit `config.yml` under `block-drop-settings.rules`, then reload:

```bash
/vnmine reload
```

---

# Build Từ Mã Nguồn · Building from Source

```bash
git clone https://github.com/VNMine/plugin.git
cd vnmine-plugin
mvn clean package
```

File JAR đã biên dịch sẽ nằm trong thư mục `target/VNMine-1.0.0.jar`.

The compiled JAR will be in `target/VNMine-1.0.0.jar`.

---

# Đóng Góp · Contributing

Mọi đóng góp đều được chào đón! Hãy gửi Pull Request.
Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork repository
2. Tạo nhánh tính năng · Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit thay đổi · Commit changes (`git commit -m 'Add amazing feature'`)
4. Push lên nhánh · Push to branch (`git push origin feature/amazing-feature`)
5. Mở Pull Request · Open a Pull Request

---

# Giấy Phép · License

Dự án này được cấp phép theo giấy phép MIT. Xem file [LICENSE](LICENSE) để biết thêm chi tiết.

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

<p align="center">
  Được tạo với ❤️ bởi đội ngũ VNMine · Made with ❤️ by the VNMine Team
  <br/>
  <sub>Dành cho Paper 1.21+ | Java 25 · Built for Paper 1.21+ | Java 25</sub>
</p>