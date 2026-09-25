-- 演示数据：全部按名称关联、以 WHERE NOT EXISTS 去重，H2/PostgreSQL 均可重复执行

INSERT INTO scripts (name, genre, difficulty, duration_minutes, player_count, description)
SELECT '雾港旧案', '民国 · 硬核推理', '困难', 240, 6, '暴雨封港的旧码头，六位访客各自藏着与一桩旧案有关的秘密。'
WHERE NOT EXISTS (SELECT 1 FROM scripts WHERE name = '雾港旧案');

INSERT INTO scripts (name, genre, difficulty, duration_minutes, player_count, description)
SELECT '长安不眠夜', '古风 · 情感沉浸', '中等', 200, 7, '上元灯会之夜，长安城的灯火下交织着七段命运。'
WHERE NOT EXISTS (SELECT 1 FROM scripts WHERE name = '长安不眠夜');

INSERT INTO scripts (name, genre, difficulty, duration_minutes, player_count, description)
SELECT '月半小夜曲', '现代 · 情感还原本', '简单', 180, 5, '一支过气乐队重聚排练厅，旧旋律牵出每个人的遗憾。'
WHERE NOT EXISTS (SELECT 1 FROM scripts WHERE name = '月半小夜曲');

-- 雾港旧案：3 男 / 2 女 / 1 不限
INSERT INTO script_roles (script_id, name, gender, profile)
SELECT s.id, '探长 沈惊寒', 'MALE', '奉命重查旧案的港口探长，心思缜密。' FROM scripts s
WHERE s.name = '雾港旧案' AND NOT EXISTS (SELECT 1 FROM script_roles r WHERE r.script_id = s.id AND r.name = '探长 沈惊寒');
INSERT INTO script_roles (script_id, name, gender, profile)
SELECT s.id, '船王 顾远舟', 'MALE', '掌控码头货运的船王，与旧案利益盘根错节。' FROM scripts s
WHERE s.name = '雾港旧案' AND NOT EXISTS (SELECT 1 FROM script_roles r WHERE r.script_id = s.id AND r.name = '船王 顾远舟');
INSERT INTO script_roles (script_id, name, gender, profile)
SELECT s.id, '记者 白斯文', 'MALE', '嗅觉灵敏的小报记者，握有关键证词。' FROM scripts s
WHERE s.name = '雾港旧案' AND NOT EXISTS (SELECT 1 FROM script_roles r WHERE r.script_id = s.id AND r.name = '记者 白斯文');
INSERT INTO script_roles (script_id, name, gender, profile)
SELECT s.id, '歌女 阮青黛', 'FEMALE', '金嗓子歌女，案发当晚最后一个见到死者的人。' FROM scripts s
WHERE s.name = '雾港旧案' AND NOT EXISTS (SELECT 1 FROM script_roles r WHERE r.script_id = s.id AND r.name = '歌女 阮青黛');
INSERT INTO script_roles (script_id, name, gender, profile)
SELECT s.id, '医生 苏绮兰', 'FEMALE', '留洋归来的女医生，验尸报告上藏着疑点。' FROM scripts s
WHERE s.name = '雾港旧案' AND NOT EXISTS (SELECT 1 FROM script_roles r WHERE r.script_id = s.id AND r.name = '医生 苏绮兰');
INSERT INTO script_roles (script_id, name, gender, profile)
SELECT s.id, '神秘访客', 'ANY', '身份成谜的不速之客，真假身份贯穿全剧。' FROM scripts s
WHERE s.name = '雾港旧案' AND NOT EXISTS (SELECT 1 FROM script_roles r WHERE r.script_id = s.id AND r.name = '神秘访客');

-- 长安不眠夜：3 男 / 3 女 / 1 不限
INSERT INTO script_roles (script_id, name, gender, profile)
SELECT s.id, '金吾卫 裴行简', 'MALE', '巡夜金吾卫，奉命追查灯会失踪案。' FROM scripts s
WHERE s.name = '长安不眠夜' AND NOT EXISTS (SELECT 1 FROM script_roles r WHERE r.script_id = s.id AND r.name = '金吾卫 裴行简');
INSERT INTO script_roles (script_id, name, gender, profile)
SELECT s.id, '书生 柳持正', 'MALE', '赴京赶考的书生，灯下拾得一封密信。' FROM scripts s
WHERE s.name = '长安不眠夜' AND NOT EXISTS (SELECT 1 FROM script_roles r WHERE r.script_id = s.id AND r.name = '书生 柳持正');
INSERT INTO script_roles (script_id, name, gender, profile)
SELECT s.id, '剑客 燕九霄', 'MALE', '浪迹长安的剑客，寻人亦寻仇。' FROM scripts s
WHERE s.name = '长安不眠夜' AND NOT EXISTS (SELECT 1 FROM script_roles r WHERE r.script_id = s.id AND r.name = '剑客 燕九霄');
INSERT INTO script_roles (script_id, name, gender, profile)
SELECT s.id, '公主 李昭阳', 'FEMALE', '微服观灯的公主，被卷入风波中心。' FROM scripts s
WHERE s.name = '长安不眠夜' AND NOT EXISTS (SELECT 1 FROM script_roles r WHERE r.script_id = s.id AND r.name = '公主 李昭阳');
INSERT INTO script_roles (script_id, name, gender, profile)
SELECT s.id, '胡姬 阿鹿', 'FEMALE', '酒肆胡姬，人脉通达，消息灵通。' FROM scripts s
WHERE s.name = '长安不眠夜' AND NOT EXISTS (SELECT 1 FROM script_roles r WHERE r.script_id = s.id AND r.name = '胡姬 阿鹿');
INSERT INTO script_roles (script_id, name, gender, profile)
SELECT s.id, '道姑 玄机子', 'FEMALE', '夜观天象的道姑，偈语暗藏天机。' FROM scripts s
WHERE s.name = '长安不眠夜' AND NOT EXISTS (SELECT 1 FROM script_roles r WHERE r.script_id = s.id AND r.name = '道姑 玄机子');
INSERT INTO script_roles (script_id, name, gender, profile)
SELECT s.id, '提灯人', 'ANY', '穿行灯市的提灯人，似与每个人都有旧约。' FROM scripts s
WHERE s.name = '长安不眠夜' AND NOT EXISTS (SELECT 1 FROM script_roles r WHERE r.script_id = s.id AND r.name = '提灯人');

-- 月半小夜曲：2 男 / 2 女 / 1 不限
INSERT INTO script_roles (script_id, name, gender, profile)
SELECT s.id, '主唱 江野', 'MALE', '乐队主唱，解散那晚负气出走。' FROM scripts s
WHERE s.name = '月半小夜曲' AND NOT EXISTS (SELECT 1 FROM script_roles r WHERE r.script_id = s.id AND r.name = '主唱 江野');
INSERT INTO script_roles (script_id, name, gender, profile)
SELECT s.id, '鼓手 阿凯', 'MALE', '留到最后的鼓手，攒下了重聚的局。' FROM scripts s
WHERE s.name = '月半小夜曲' AND NOT EXISTS (SELECT 1 FROM script_roles r WHERE r.script_id = s.id AND r.name = '鼓手 阿凯');
INSERT INTO script_roles (script_id, name, gender, profile)
SELECT s.id, '键盘手 夏萤', 'FEMALE', '远嫁归来的键盘手，琴谱里夹着旧信。' FROM scripts s
WHERE s.name = '月半小夜曲' AND NOT EXISTS (SELECT 1 FROM script_roles r WHERE r.script_id = s.id AND r.name = '键盘手 夏萤');
INSERT INTO script_roles (script_id, name, gender, profile)
SELECT s.id, '经纪人 唐姊', 'FEMALE', '当年的经纪人，揣着一份没送出的合同。' FROM scripts s
WHERE s.name = '月半小夜曲' AND NOT EXISTS (SELECT 1 FROM script_roles r WHERE r.script_id = s.id AND r.name = '经纪人 唐姊');
INSERT INTO script_roles (script_id, name, gender, profile)
SELECT s.id, '调音师', 'ANY', '深夜来访的调音师，似乎认得这间排练厅。' FROM scripts s
WHERE s.name = '月半小夜曲' AND NOT EXISTS (SELECT 1 FROM script_roles r WHERE r.script_id = s.id AND r.name = '调音师');

INSERT INTO hosts (name, title, phone)
SELECT '阿K', '硬核推理主持', '138-0000-1001' WHERE NOT EXISTS (SELECT 1 FROM hosts WHERE name = '阿K');
INSERT INTO hosts (name, title, phone)
SELECT '林默', '情感沉浸主持', '138-0000-2002' WHERE NOT EXISTS (SELECT 1 FROM hosts WHERE name = '林默');
INSERT INTO hosts (name, title, phone)
SELECT '大白', '机制欢乐主持', '138-0000-3003' WHERE NOT EXISTS (SELECT 1 FROM hosts WHERE name = '大白');
INSERT INTO hosts (name, title, phone)
SELECT '小棠', '古风沉浸主持', '138-0000-4004' WHERE NOT EXISTS (SELECT 1 FROM hosts WHERE name = '小棠');

-- 预置场次：2026-09-25 19:00，阿K 带雾港旧案（240 分钟 + 20 分钟清场，占用至 23:20），用于演示撞档
INSERT INTO stages (script_id, host_id, start_at, end_at, clear_end_at, status)
SELECT s.id, h.id, TIMESTAMP '2026-09-25 19:00:00', TIMESTAMP '2026-09-25 23:00:00', TIMESTAMP '2026-09-25 23:20:00', 'SCHEDULED'
FROM scripts s, hosts h
WHERE s.name = '雾港旧案' AND h.name = '阿K'
  AND NOT EXISTS (SELECT 1 FROM stages st WHERE st.host_id = h.id AND st.start_at = TIMESTAMP '2026-09-25 19:00:00');

-- 为预置场次配齐 6 位到场玩家（3 男 3 女，角色为 3 男 2 女 1 不限，可直接抽签）
INSERT INTO stage_players (stage_id, player_name, gender, waiting)
SELECT st.id, '陈默', 'MALE', FALSE FROM stages st
WHERE st.start_at = TIMESTAMP '2026-09-25 19:00:00'
  AND NOT EXISTS (SELECT 1 FROM stage_players p WHERE p.stage_id = st.id AND p.player_name = '陈默');
INSERT INTO stage_players (stage_id, player_name, gender, waiting)
SELECT st.id, '赵一帆', 'MALE', FALSE FROM stages st
WHERE st.start_at = TIMESTAMP '2026-09-25 19:00:00'
  AND NOT EXISTS (SELECT 1 FROM stage_players p WHERE p.stage_id = st.id AND p.player_name = '赵一帆');
INSERT INTO stage_players (stage_id, player_name, gender, waiting)
SELECT st.id, '孙岩', 'MALE', FALSE FROM stages st
WHERE st.start_at = TIMESTAMP '2026-09-25 19:00:00'
  AND NOT EXISTS (SELECT 1 FROM stage_players p WHERE p.stage_id = st.id AND p.player_name = '孙岩');
INSERT INTO stage_players (stage_id, player_name, gender, waiting)
SELECT st.id, '林小鹿', 'FEMALE', FALSE FROM stages st
WHERE st.start_at = TIMESTAMP '2026-09-25 19:00:00'
  AND NOT EXISTS (SELECT 1 FROM stage_players p WHERE p.stage_id = st.id AND p.player_name = '林小鹿');
INSERT INTO stage_players (stage_id, player_name, gender, waiting)
SELECT st.id, '周以宁', 'FEMALE', FALSE FROM stages st
WHERE st.start_at = TIMESTAMP '2026-09-25 19:00:00'
  AND NOT EXISTS (SELECT 1 FROM stage_players p WHERE p.stage_id = st.id AND p.player_name = '周以宁');
INSERT INTO stage_players (stage_id, player_name, gender, waiting)
SELECT st.id, '吴双', 'FEMALE', FALSE FROM stages st
WHERE st.start_at = TIMESTAMP '2026-09-25 19:00:00'
  AND NOT EXISTS (SELECT 1 FROM stage_players p WHERE p.stage_id = st.id AND p.player_name = '吴双');
