# 📑 VNMine Mega Update - Cultivation Checklist

### [ ] PHẦN 1: TỌA THIỀN & LINH KHÍ BIOME (Ambient Qi System)
- [x] Logic Tọa thiền (`/vnmine meditate` hoặc giữ Shift 10s khi đứng yên). Hiển thị hiệu ứng hạt (particles) tùy chỉnh, cộng EXP thụ động mỗi 5 giây.
- [x] Hệ thống kiểm tra Biome thời gian thực để áp dụng `Biome-Qi-Modifier`:
  - [x] X2 Tốc độ hồi Mana tại: Cherry Grove, Jagged Peaks, Sunflower Plains.
  - [x] Giảm 50% hồi Mana và rút dần Mana khi combat tại: Nether, Deep Dark, Basalt Deltas.

### [ ] PHẦN 2: TÂM MA, DƯỢC ĐỘC & LÔI KIẾP BIẾN DỊ
- [ ] Thêm thuộc tính ẩn `Tam-Ma` và `Duoc-Doc` vào dữ liệu người chơi (lưu qua PersistentDataContainer). Điểm tăng khi cắn đan dược liên tục.
- [ ] Cập nhật cơ chế Lôi Kiếp:
  - [ ] Nếu điểm Tâm Ma cao, áp dụng hiệu ứng BLINDNESS, DARKNESS trong suốt quá trình độ kiếp.
  - [ ] Triệu hồi một Boss thực thể (Phantom/Zombie) mang tên "Tâm Ma của <Tên_Player>" sở hữu skin của chính Player (sử dụng ProtocolLib). Người chơi phải diệt Tâm Ma để hoàn thành độ kiếp.

### [ ] PHẦN 3: ĐỘNG LỰC HỌC LUYỆN ĐAN (Dị Hỏa Modifiers)
- [ ] Cập nhật hàm check block dưới Lò Luyện Đan khi bắt đầu đếm ngược 60 giây:
  - [ ] CAMPFIRE / MAGMA_BLOCK: Tỷ lệ gốc.
  - [ ] SOUL_FIRE / SOUL_CAMPFIRE (Linh Hỏa): +10% tỷ lệ thành công, có tỷ lệ bạo kích nhảy bậc phẩm cấp đan dược.
  - [ ] CUSTOM LAVA / BOSS DROP BLOCK (Dị Hỏa): +25% tỷ lệ thành công, khóa phẩm cấp đầu ra tối thiểu từ Địa/Thiên trở lên.

### [ ] PHẦN 4: THẦN THỨC & Ý CHÍ (Soul Sense System)
- [ ] Thêm thanh chỉ số phụ: Thần Thức (Soul Power) tăng tiến theo Cảnh Giới.
- [ ] Tính năng "Quét Thần Thức": Người chơi tiêu hao Mana để kích hoạt Thần Thức, làm phát quang (Glow Effect) tất cả Elite Mob, Boss, hoặc người chơi khác trong bán kính X block (X phụ thuộc vào cảnh giới).
- [ ] Cơ chế "Áp Chế Thần Thức": Người chơi có cảnh giới cao hơn có thể bật áp chế, gây hiệu ứng SLOW hoặc WEAKNESS lên thực thể có cảnh giới thấp hơn xung quanh mình.

### [ ] PHẦN 5: KHÍ VẬN & CÔNG ĐỨC (Karma & Fortune)
- [ ] Thêm chỉ số Khí Vận (Mặc định ngẫu nhiên khi khởi đầu hành trình tu tiên) và Công Đức (Tăng khi diệt quái hung dữ/Boss, giảm khi đồ sát người chơi khác).
- [ ] Khí Vận/Công Đức ảnh hưởng trực tiếp đến:
  - [ ] Tỷ lệ sét đánh trúng/trượt hoặc giảm sát thương khi Độ Kiếp.
  - [ ] Tỷ lệ thành công khi học Sách Công Pháp (Skill Book) từ 20-90%.
  - [ ] Tỷ lệ rơi Linh Thạch từ quái.

### [ ] PHẦN 6: THỂ CHẤT & HUYẾT MẠCH (Physique & Bloodline)
- [ ] Tạo hệ thống Thể Chất ngẫu nhiên khi bấm `/vn start` (Ví dụ: Hoang Cổ Thánh Thể, Tiên Thiên Kiếm Thai, Cửu Âm Tuyệt Mạch).
- [ ] Mỗi thể chất mang lại một Passive độc quyền:
  - [ ] Hoang Cổ Thánh Thể: Miễn nhiễm giật lùi (Knockback), tăng sát thương tay.
  - [ ] Tiên Thiên Kiếm Thai: Tăng 30% sát thương khi cầm Kiếm (Sword/Pháp bảo Kiếm).
  - [ ] Cửu Âm Tuyệt Mạch: Đòn đánh kèm hiệu ứng SLOW, nhưng giảm tốc độ hồi Mana mặc định.

### [ ] PHẦN 7: BÍ CẢNH PHỔ (Dynamic Dungeon Generator)
- [ ] Tích hợp sâu vào cơ chế World Generation của plugin. Khi có lệnh admin hoặc định kỳ theo thời gian server, tự động tạo một World tạm thời (Bí Cảnh) bằng mã nguồn tự tạo (custom seed/flat/amplified).
- [ ] Tự động spawn mật độ quặng hiếm (ore-rates) và Elite Mobs cực cao.
- [ ] Tự động đóng Bí Cảnh sau 30 phút và dịch chuyển toàn bộ người chơi về World chính.
