CREATE DATABASE IF NOT EXISTS test_docker_q1_0_2026_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'test_docker_q1_0_2026_user'@'%'
  IDENTIFIED BY 'test_docker_q1_0_2026_user';

GRANT ALL PRIVILEGES ON test_docker_q1_0_2026_db.* TO 'test_docker_q1_0_2026_user'@'%';

FLUSH PRIVILEGES;