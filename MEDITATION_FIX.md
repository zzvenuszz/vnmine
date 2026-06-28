# Bug Fix: Meditation Menu Item Leakage

## Mô tả lỗi
Khi mở menu Tọa Thiền, người chơi vẫn có thể lấy item (Emerald Block) ra khỏi menu.

## Log phân tích
```
[07:01:54 INFO]: [VNMine] [AlchemyDebug] Click detected! title='✧ Tọa Thiền ✧' rawSlot=2 click=LEFT clickedItem=ItemStack{EMERALD_BLOCK x 1...}
[07:01:55 INFO]: [VNMine] [AlchemyDebug] Click detected! title='✧ Tọa Thiền ✧' rawSlot=12 click=LEFT clickedItem=null
[07:02:09 INFO]: [VNMine] [MenuDebug] onInventoryClose player=hoanbh title='✧ Tọa Thiền ✧'
```

## Nguyên nhân
Trong file `MeditationListener.java`, dòng 52 có code:
```java
if (event.getRawSlot() >= topSize) return;
```

Vấn đề:
1. `topSize = 9` (GUI size là 9 slots)
2. Khi click vào slot 12 (player inventory), code xử lý `event.setCancelled(true)` và `event.setResult(Event.Result.DENY)` rồi return ngay
3. Việc return sớm sau khi set cancelled có thể gây ra edge case, cho phép item "leak" ra ngoài

## Giải pháp
Đã sửa code trong `MeditationListener.java` để:
1. **Cancel MỌI click** khi GUI Tọa Thiền đang mở
2. Loại bỏ early return gây bug (dòng 52 cũ)
3. Thêm bounds checking đúng đắn: `if (slot < 0 || slot >= event.getView().getTopInventory().getSize()) return;`
4. Chỉ xử lý click trên slots 2 (confirm) và 6 (cancel)
5. Tất cả các click khác đều bị deny hoàn toàn

### Pattern matching với các GUI khác trong codebase
- `SkillBarGUI` (line 531-533): Cancel MỌI click khi GUI mở
- `NPCShopGUI` (line 122-123): Cancel clicks trong top inventory
- `AdminMenuGUI` (line 343-345): Cancel + DENY cho tất cả top inventory clicks

## File đã thay đổi
- `src/main/java/com/vnmine/cultivation/MeditationListener.java` (lines 33-66)

## Cách test
1. Build và deploy plugin
2. Giữ Shift 10 ticks để mở menu Tọa Thiền
3. Thử các hành động:
   - Click vào Emerald Block (slot 2) → Confirm (OK)
   - Click vào Red Glass Pane (slot 6) → Cancel (OK)
   - Thử lấy item ra → Bị chặn (FIXED)
   - Thử shift-click từ inventory → Bị chặn (FIXED)
   - Thử drag item vào GUI → Bị chặn (FIXED)