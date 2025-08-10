-- Seed statuses (adjust table/column names if different)
INSERT INTO ticket_statues (id, name, description) VALUES
  (1, 'Açık', 'Yeni açılan ticket'),
  (2, 'Atanmış', 'Bir temsilciye atandı'),
  (3, 'İşlemde', 'Temsilci tarafından işleniyor'),
  (4, 'Beklemede', 'Müşteri yanıtı veya ek bilgi bekleniyor'),
  (5, 'Çözüldü', 'Sorun çözüldü'),
  (6, 'Kapalı', 'Ticket kapatıldı')
ON DUPLICATE KEY UPDATE name=VALUES(name), description=VALUES(description);

-- Seed priorities
INSERT INTO ticket_priorities (id, name, level) VALUES
  (1, 'Düşük', 1),
  (2, 'Normal', 2),
  (3, 'Yüksek', 3),
  (4, 'Acil', 4),
  (5, 'Kritik', 5)
ON DUPLICATE KEY UPDATE name=VALUES(name), level=VALUES(level);
