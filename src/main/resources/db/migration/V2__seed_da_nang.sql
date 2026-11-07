-- 24 real places in and around Da Nang. Coordinates are approximate.
-- Road lengths are the straight-line distance times a detour factor (1.2 - 1.4), so a road is never
-- shorter than the straight line between its ends. A* relies on that to stay exact.
INSERT INTO place (name, category, latitude, longitude) VALUES
  ('Sân bay Quốc tế Đà Nẵng', 'TRANSPORT', 16.0544, 108.2022),
  ('Ga Đà Nẵng', 'TRANSPORT', 16.0716, 108.2094),
  ('Bến xe Trung tâm Đà Nẵng', 'TRANSPORT', 16.0571, 108.1724),
  ('Cầu Rồng', 'LANDMARK', 16.0612, 108.227),
  ('Cầu Sông Hàn', 'LANDMARK', 16.0721, 108.2265),
  ('Cầu Trần Thị Lý', 'LANDMARK', 16.0478, 108.229),
  ('Chợ Hàn', 'MARKET', 16.068, 108.224),
  ('Chợ Cồn', 'MARKET', 16.0681, 108.214),
  ('Bãi biển Mỹ Khê', 'BEACH', 16.06, 108.247),
  ('Bãi biển Phạm Văn Đồng', 'BEACH', 16.072, 108.246),
  ('Công viên Châu Á', 'ENTERTAINMENT', 16.0397, 108.2268),
  ('Ngũ Hành Sơn', 'LANDMARK', 15.999, 108.2633),
  ('Chùa Linh Ứng Sơn Trà', 'LANDMARK', 16.1003, 108.2776),
  ('Đại học FPT Đà Nẵng', 'UNIVERSITY', 15.9686, 108.2606),
  ('Đại học Bách khoa Đà Nẵng', 'UNIVERSITY', 16.0738, 108.15),
  ('Bảo tàng Điêu khắc Chăm', 'MUSEUM', 16.0605, 108.2235),
  ('Vincom Plaza Ngô Quyền', 'SHOPPING', 16.0717, 108.2306),
  ('Lotte Mart Đà Nẵng', 'SHOPPING', 16.0349, 108.229),
  ('Công viên Biển Đông', 'PARK', 16.07, 108.2455),
  ('Phố cổ Hội An', 'LANDMARK', 15.8801, 108.338),
  ('Ngã ba Huế', 'JUNCTION', 16.067, 108.179),
  ('Cầu Thuận Phước', 'LANDMARK', 16.092, 108.224),
  ('Cảng Tiên Sa', 'TRANSPORT', 16.121, 108.22),
  ('Bệnh viện Đà Nẵng', 'HOSPITAL', 16.0712, 108.2168);

INSERT INTO road (from_place_id, to_place_id, name, distance_m, speed_kmh, one_way)
SELECT f.id, t.id, 'Duy Tân - Hàm Nghi', 2680, 40, FALSE FROM place f, place t
WHERE f.name = 'Sân bay Quốc tế Đà Nẵng' AND t.name = 'Ga Đà Nẵng';
INSERT INTO road (from_place_id, to_place_id, name, distance_m, speed_kmh, one_way)
SELECT f.id, t.id, 'Tôn Đức Thắng', 3998, 50, FALSE FROM place f, place t
WHERE f.name = 'Sân bay Quốc tế Đà Nẵng' AND t.name = 'Bến xe Trung tâm Đà Nẵng';
INSERT INTO road (from_place_id, to_place_id, name, distance_m, speed_kmh, one_way)
SELECT f.id, t.id, 'Điện Biên Phủ', 3702, 50, FALSE FROM place f, place t
WHERE f.name = 'Sân bay Quốc tế Đà Nẵng' AND t.name = 'Ngã ba Huế';
INSERT INTO road (from_place_id, to_place_id, name, distance_m, speed_kmh, one_way)
SELECT f.id, t.id, 'Nguyễn Văn Linh', 2850, 40, FALSE FROM place f, place t
WHERE f.name = 'Sân bay Quốc tế Đà Nẵng' AND t.name = 'Bảo tàng Điêu khắc Chăm';
INSERT INTO road (from_place_id, to_place_id, name, distance_m, speed_kmh, one_way)
SELECT f.id, t.id, 'Xô Viết Nghệ Tĩnh', 4670, 40, FALSE FROM place f, place t
WHERE f.name = 'Sân bay Quốc tế Đà Nẵng' AND t.name = 'Lotte Mart Đà Nẵng';
INSERT INTO road (from_place_id, to_place_id, name, distance_m, speed_kmh, one_way)
SELECT f.id, t.id, 'Tôn Đức Thắng', 1569, 50, FALSE FROM place f, place t
WHERE f.name = 'Bến xe Trung tâm Đà Nẵng' AND t.name = 'Ngã ba Huế';
INSERT INTO road (from_place_id, to_place_id, name, distance_m, speed_kmh, one_way)
SELECT f.id, t.id, 'Tôn Đức Thắng', 3827, 40, FALSE FROM place f, place t
WHERE f.name = 'Ngã ba Huế' AND t.name = 'Đại học Bách khoa Đà Nẵng';
INSERT INTO road (from_place_id, to_place_id, name, distance_m, speed_kmh, one_way)
SELECT f.id, t.id, 'Điện Biên Phủ - Hải Phòng', 4110, 40, FALSE FROM place f, place t
WHERE f.name = 'Ngã ba Huế' AND t.name = 'Ga Đà Nẵng';
INSERT INTO road (from_place_id, to_place_id, name, distance_m, speed_kmh, one_way)
SELECT f.id, t.id, 'Hải Phòng', 815, 30, FALSE FROM place f, place t
WHERE f.name = 'Ga Đà Nẵng' AND t.name = 'Chợ Cồn';
INSERT INTO road (from_place_id, to_place_id, name, distance_m, speed_kmh, one_way)
SELECT f.id, t.id, 'Hải Phòng', 1030, 30, FALSE FROM place f, place t
WHERE f.name = 'Ga Đà Nẵng' AND t.name = 'Bệnh viện Đà Nẵng';
INSERT INTO road (from_place_id, to_place_id, name, distance_m, speed_kmh, one_way)
SELECT f.id, t.id, 'Ông Ích Khiêm', 593, 30, FALSE FROM place f, place t
WHERE f.name = 'Bệnh viện Đà Nẵng' AND t.name = 'Chợ Cồn';
INSERT INTO road (from_place_id, to_place_id, name, distance_m, speed_kmh, one_way)
SELECT f.id, t.id, 'Hùng Vương', 1389, 30, FALSE FROM place f, place t
WHERE f.name = 'Chợ Cồn' AND t.name = 'Chợ Hàn';
INSERT INTO road (from_place_id, to_place_id, name, distance_m, speed_kmh, one_way)
SELECT f.id, t.id, 'Lê Lợi', 1354, 30, FALSE FROM place f, place t
WHERE f.name = 'Bệnh viện Đà Nẵng' AND t.name = 'Cầu Sông Hàn';
INSERT INTO road (from_place_id, to_place_id, name, distance_m, speed_kmh, one_way)
SELECT f.id, t.id, 'Bạch Đằng', 660, 30, FALSE FROM place f, place t
WHERE f.name = 'Chợ Hàn' AND t.name = 'Cầu Sông Hàn';
INSERT INTO road (from_place_id, to_place_id, name, distance_m, speed_kmh, one_way)
SELECT f.id, t.id, 'Bạch Đằng', 1045, 30, FALSE FROM place f, place t
WHERE f.name = 'Bảo tàng Điêu khắc Chăm' AND t.name = 'Chợ Hàn';
INSERT INTO road (from_place_id, to_place_id, name, distance_m, speed_kmh, one_way)
SELECT f.id, t.id, 'Nguyễn Văn Linh', 458, 30, FALSE FROM place f, place t
WHERE f.name = 'Bảo tàng Điêu khắc Chăm' AND t.name = 'Cầu Rồng';
INSERT INTO road (from_place_id, to_place_id, name, distance_m, speed_kmh, one_way)
SELECT f.id, t.id, 'Bạch Đằng (một chiều)', 1027, 30, TRUE FROM place f, place t
WHERE f.name = 'Chợ Hàn' AND t.name = 'Cầu Rồng';
INSERT INTO road (from_place_id, to_place_id, name, distance_m, speed_kmh, one_way)
SELECT f.id, t.id, 'Võ Văn Kiệt', 2570, 40, FALSE FROM place f, place t
WHERE f.name = 'Cầu Rồng' AND t.name = 'Bãi biển Mỹ Khê';
INSERT INTO road (from_place_id, to_place_id, name, distance_m, speed_kmh, one_way)
SELECT f.id, t.id, 'Lê Văn Duyệt', 572, 30, FALSE FROM place f, place t
WHERE f.name = 'Cầu Sông Hàn' AND t.name = 'Vincom Plaza Ngô Quyền';
INSERT INTO road (from_place_id, to_place_id, name, distance_m, speed_kmh, one_way)
SELECT f.id, t.id, 'Phạm Văn Đồng', 1975, 40, FALSE FROM place f, place t
WHERE f.name = 'Vincom Plaza Ngô Quyền' AND t.name = 'Bãi biển Phạm Văn Đồng';
INSERT INTO road (from_place_id, to_place_id, name, distance_m, speed_kmh, one_way)
SELECT f.id, t.id, 'Võ Nguyên Giáp', 274, 30, FALSE FROM place f, place t
WHERE f.name = 'Bãi biển Phạm Văn Đồng' AND t.name = 'Công viên Biển Đông';
INSERT INTO road (from_place_id, to_place_id, name, distance_m, speed_kmh, one_way)
SELECT f.id, t.id, 'Võ Nguyên Giáp', 1348, 40, FALSE FROM place f, place t
WHERE f.name = 'Công viên Biển Đông' AND t.name = 'Bãi biển Mỹ Khê';
INSERT INTO road (from_place_id, to_place_id, name, distance_m, speed_kmh, one_way)
SELECT f.id, t.id, 'Bạch Đằng nối dài', 1912, 40, FALSE FROM place f, place t
WHERE f.name = 'Bảo tàng Điêu khắc Chăm' AND t.name = 'Cầu Trần Thị Lý';
INSERT INTO road (from_place_id, to_place_id, name, distance_m, speed_kmh, one_way)
SELECT f.id, t.id, '2 Tháng 9', 1210, 40, FALSE FROM place f, place t
WHERE f.name = 'Cầu Trần Thị Lý' AND t.name = 'Công viên Châu Á';
INSERT INTO road (from_place_id, to_place_id, name, distance_m, speed_kmh, one_way)
SELECT f.id, t.id, '2 Tháng 9', 758, 30, FALSE FROM place f, place t
WHERE f.name = 'Công viên Châu Á' AND t.name = 'Lotte Mart Đà Nẵng';
INSERT INTO road (from_place_id, to_place_id, name, distance_m, speed_kmh, one_way)
SELECT f.id, t.id, 'Ngũ Hành Sơn - Hồ Xuân Hương', 3060, 40, FALSE FROM place f, place t
WHERE f.name = 'Cầu Trần Thị Lý' AND t.name = 'Bãi biển Mỹ Khê';
INSERT INTO road (from_place_id, to_place_id, name, distance_m, speed_kmh, one_way)
SELECT f.id, t.id, 'Võ Nguyên Giáp', 8404, 50, FALSE FROM place f, place t
WHERE f.name = 'Bãi biển Mỹ Khê' AND t.name = 'Ngũ Hành Sơn';
INSERT INTO road (from_place_id, to_place_id, name, distance_m, speed_kmh, one_way)
SELECT f.id, t.id, 'Trường Sa', 4241, 50, FALSE FROM place f, place t
WHERE f.name = 'Ngũ Hành Sơn' AND t.name = 'Đại học FPT Đà Nẵng';
INSERT INTO road (from_place_id, to_place_id, name, distance_m, speed_kmh, one_way)
SELECT f.id, t.id, 'Lạc Long Quân', 20080, 50, FALSE FROM place f, place t
WHERE f.name = 'Ngũ Hành Sơn' AND t.name = 'Phố cổ Hội An';
INSERT INTO road (from_place_id, to_place_id, name, distance_m, speed_kmh, one_way)
SELECT f.id, t.id, 'Trường Sa - Lạc Long Quân', 16716, 50, FALSE FROM place f, place t
WHERE f.name = 'Đại học FPT Đà Nẵng' AND t.name = 'Phố cổ Hội An';
INSERT INTO road (from_place_id, to_place_id, name, distance_m, speed_kmh, one_way)
SELECT f.id, t.id, 'Lê Văn Hiến', 10542, 50, FALSE FROM place f, place t
WHERE f.name = 'Lotte Mart Đà Nẵng' AND t.name = 'Đại học FPT Đà Nẵng';
INSERT INTO road (from_place_id, to_place_id, name, distance_m, speed_kmh, one_way)
SELECT f.id, t.id, 'Như Nguyệt - Bạch Đằng', 2897, 40, FALSE FROM place f, place t
WHERE f.name = 'Cầu Sông Hàn' AND t.name = 'Cầu Thuận Phước';
INSERT INTO road (from_place_id, to_place_id, name, distance_m, speed_kmh, one_way)
SELECT f.id, t.id, 'Yết Kiêu', 4229, 50, FALSE FROM place f, place t
WHERE f.name = 'Cầu Thuận Phước' AND t.name = 'Cảng Tiên Sa';
INSERT INTO road (from_place_id, to_place_id, name, distance_m, speed_kmh, one_way)
SELECT f.id, t.id, 'Ngô Quyền', 4207, 40, FALSE FROM place f, place t
WHERE f.name = 'Cầu Thuận Phước' AND t.name = 'Bãi biển Phạm Văn Đồng';
INSERT INTO road (from_place_id, to_place_id, name, distance_m, speed_kmh, one_way)
SELECT f.id, t.id, 'Hoàng Sa', 6461, 40, FALSE FROM place f, place t
WHERE f.name = 'Bãi biển Phạm Văn Đồng' AND t.name = 'Chùa Linh Ứng Sơn Trà';
INSERT INTO road (from_place_id, to_place_id, name, distance_m, speed_kmh, one_way)
SELECT f.id, t.id, 'Yết Kiêu - Hoàng Sa', 9198, 40, FALSE FROM place f, place t
WHERE f.name = 'Cảng Tiên Sa' AND t.name = 'Chùa Linh Ứng Sơn Trà';
