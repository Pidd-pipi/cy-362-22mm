-- 角色开演台：剧本、角色、主持人、场次、名单、抽签版本、座次、换角调整
-- 兼容 H2(PostgreSQL 模式) 与 PostgreSQL，全部使用 IF NOT EXISTS / WHERE NOT EXISTS 保证可重复执行

CREATE TABLE IF NOT EXISTS scripts (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(120) NOT NULL,
  genre VARCHAR(60),
  difficulty VARCHAR(20),
  duration_minutes INT NOT NULL,
  player_count INT NOT NULL,
  description TEXT
);

CREATE TABLE IF NOT EXISTS script_roles (
  id BIGSERIAL PRIMARY KEY,
  script_id BIGINT NOT NULL,
  name VARCHAR(80) NOT NULL,
  gender VARCHAR(10) NOT NULL,
  profile VARCHAR(240)
);

CREATE TABLE IF NOT EXISTS hosts (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(80) NOT NULL,
  title VARCHAR(80),
  phone VARCHAR(30)
);

CREATE TABLE IF NOT EXISTS stages (
  id BIGSERIAL PRIMARY KEY,
  script_id BIGINT NOT NULL,
  host_id BIGINT NOT NULL,
  start_at TIMESTAMP NOT NULL,
  end_at TIMESTAMP NOT NULL,
  clear_end_at TIMESTAMP NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  confirmed_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS stage_players (
  id BIGSERIAL PRIMARY KEY,
  stage_id BIGINT NOT NULL,
  player_name VARCHAR(80) NOT NULL,
  gender VARCHAR(10) NOT NULL,
  waiting BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS draw_versions (
  id BIGSERIAL PRIMARY KEY,
  stage_id BIGINT NOT NULL,
  version_no INT NOT NULL,
  draw_trigger VARCHAR(16) NOT NULL,
  note VARCHAR(160),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (stage_id, version_no)
);

CREATE TABLE IF NOT EXISTS seats (
  id BIGSERIAL PRIMARY KEY,
  version_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  player_id BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS role_changes (
  id BIGSERIAL PRIMARY KEY,
  stage_id BIGINT NOT NULL,
  version_id BIGINT,
  change_type VARCHAR(12) NOT NULL,
  from_player_id BIGINT,
  to_player_id BIGINT,
  role_id BIGINT,
  waiting_player_id BIGINT,
  reason VARCHAR(240),
  status VARCHAR(12) NOT NULL DEFAULT 'PENDING',
  manager_note VARCHAR(240),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  decided_at TIMESTAMP
);
