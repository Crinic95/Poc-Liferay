CREATE DATABASE IF NOT EXISTS test_docker_q4_0_2025_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'test_docker_q4_0_2025_user'@'%'
  IDENTIFIED BY 'test_docker_q4_0_2025_user';

GRANT ALL PRIVILEGES ON test_docker_q4_0_2025_db.* TO 'test_docker_q4_0_2025_user'@'%';

FLUSH PRIVILEGES;