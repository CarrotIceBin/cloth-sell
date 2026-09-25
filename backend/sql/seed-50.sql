-- 50 件服装，封面留空，之后在后台上传。每件含 黑/米 × S/M 四个规格。
INSERT INTO mall_product (name, cover_url, on_shelf, creator, create_time, updater, update_time, deleted) VALUES
('亚麻长袖衬衫', NULL, 1, 'photo-seed', NOW(), 'photo-seed', NOW(), 0),
('牛津纺衬衫', NULL, 1, 'photo-seed', NOW(), 'photo-seed', NOW(), 0),
('真丝吊带', NULL, 1, 'photo-seed', NOW(), 'photo-seed', NOW(), 0),
('圆领纯棉T恤', NULL, 1, 'photo-seed', NOW(), 'photo-seed', NOW(), 0),
('条纹海魂衫', NULL, 1, 'photo-seed', NOW(), 'photo-seed', NOW(), 0),
('牛仔夹克', NULL, 1, 'photo-seed', NOW(), 'photo-seed', NOW(), 0),
('羊毛大衣', NULL, 1, 'photo-seed', NOW(), 'photo-seed', NOW(), 0),
('针织开衫', NULL, 1, 'photo-seed', NOW(), 'photo-seed', NOW(), 0),
('高领毛衣', NULL, 1, 'photo-seed', NOW(), 'photo-seed', NOW(), 0),
('宽松卫衣', NULL, 1, 'photo-seed', NOW(), 'photo-seed', NOW(), 0),
('西装外套', NULL, 1, 'photo-seed', NOW(), 'photo-seed', NOW(), 0),
('风衣', NULL, 1, 'photo-seed', NOW(), 'photo-seed', NOW(), 0),
('棉质衬衫裙', NULL, 1, 'photo-seed', NOW(), 'photo-seed', NOW(), 0),
('百褶半裙', NULL, 1, 'photo-seed', NOW(), 'photo-seed', NOW(), 0),
('直筒牛仔裤', NULL, 1, 'photo-seed', NOW(), 'photo-seed', NOW(), 0),
('宽腿西装裤', NULL, 1, 'photo-seed', NOW(), 'photo-seed', NOW(), 0),
('工装短裤', NULL, 1, 'photo-seed', NOW(), 'photo-seed', NOW(), 0),
('白色背心', NULL, 1, 'photo-seed', NOW(), 'photo-seed', NOW(), 0),
('方领上衣', NULL, 1, 'photo-seed', NOW(), 'photo-seed', NOW(), 0),
('泡泡袖上衣', NULL, 1, 'photo-seed', NOW(), 'photo-seed', NOW(), 0),
('镂空针织衫', NULL, 1, 'photo-seed', NOW(), 'photo-seed', NOW(), 0),
('法兰绒衬衫', NULL, 1, 'photo-seed', NOW(), 'photo-seed', NOW(), 0),
('灯芯绒裤', NULL, 1, 'photo-seed', NOW(), 'photo-seed', NOW(), 0),
('运动卫裤', NULL, 1, 'photo-seed', NOW(), 'photo-seed', NOW(), 0),
('丝质衬衫', NULL, 1, 'photo-seed', NOW(), 'photo-seed', NOW(), 0),
('小香风外套', NULL, 1, 'photo-seed', NOW(), 'photo-seed', NOW(), 0),
('短款羽绒服', NULL, 1, 'photo-seed', NOW(), 'photo-seed', NOW(), 0),
('长款羽绒服', NULL, 1, 'photo-seed', NOW(), 'photo-seed', NOW(), 0),
('棒球服', NULL, 1, 'photo-seed', NOW(), 'photo-seed', NOW(), 0),
('连帽外套', NULL, 1, 'photo-seed', NOW(), 'photo-seed', NOW(), 0),
('碎花连衣裙', NULL, 1, 'photo-seed', NOW(), 'photo-seed', NOW(), 0),
('吊带连衣裙', NULL, 1, 'photo-seed', NOW(), 'photo-seed', NOW(), 0),
('衬衫连衣裙', NULL, 1, 'photo-seed', NOW(), 'photo-seed', NOW(), 0),
('针织连衣裙', NULL, 1, 'photo-seed', NOW(), 'photo-seed', NOW(), 0),
('A字半身裙', NULL, 1, 'photo-seed', NOW(), 'photo-seed', NOW(), 0),
('包臀裙', NULL, 1, 'photo-seed', NOW(), 'photo-seed', NOW(), 0),
('阔腿裤', NULL, 1, 'photo-seed', NOW(), 'photo-seed', NOW(), 0),
('烟管裤', NULL, 1, 'photo-seed', NOW(), 'photo-seed', NOW(), 0),
('高腰短裤', NULL, 1, 'photo-seed', NOW(), 'photo-seed', NOW(), 0),
('工装马甲', NULL, 1, 'photo-seed', NOW(), 'photo-seed', NOW(), 0),
('针织马甲', NULL, 1, 'photo-seed', NOW(), 'photo-seed', NOW(), 0),
('polo衫', NULL, 1, 'photo-seed', NOW(), 'photo-seed', NOW(), 0),
('亨利领上衣', NULL, 1, 'photo-seed', NOW(), 'photo-seed', NOW(), 0),
('法式方领裙', NULL, 1, 'photo-seed', NOW(), 'photo-seed', NOW(), 0),
('蕾丝衫', NULL, 1, 'photo-seed', NOW(), 'photo-seed', NOW(), 0),
('雪纺衫', NULL, 1, 'photo-seed', NOW(), 'photo-seed', NOW(), 0),
('牛仔衬衫', NULL, 1, 'photo-seed', NOW(), 'photo-seed', NOW(), 0),
('牛仔半裙', NULL, 1, 'photo-seed', NOW(), 'photo-seed', NOW(), 0),
('卡其裤', NULL, 1, 'photo-seed', NOW(), 'photo-seed', NOW(), 0),
('羊毛西装裤', NULL, 1, 'photo-seed', NOW(), 'photo-seed', NOW(), 0);

INSERT INTO mall_sku (product_id, color, size, price, stock, creator, create_time, updater, update_time, deleted)
SELECT p.id, spec.color, spec.size,
       128.00 + (p.id % 50) * 4 + spec.extra,
       6 + (p.id % 8),
       'photo-seed', NOW(), 'photo-seed', NOW(), 0
FROM mall_product p
JOIN (
  SELECT '黑' AS color, 'S' AS size, 0.00 AS extra
  UNION ALL SELECT '黑', 'M', 0.00
  UNION ALL SELECT '米', 'S', 20.00
  UNION ALL SELECT '米', 'M', 20.00
) spec
WHERE p.creator = 'photo-seed'
  AND p.deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM mall_sku k WHERE k.product_id = p.id AND k.deleted = 0
  );
