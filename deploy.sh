#!/usr/bin/env bash
set -Eeuo pipefail

# =========================
# restart-user 自动构建与部署
# 在 Mac 本地执行：./deploy.sh
# =========================

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="/Users/treasure/Project/config/restart-server.env"

REMOTE_USER="treasure"
REMOTE_HOST="124.222.46.122"
REMOTE="${REMOTE_USER}@${REMOTE_HOST}"

REMOTE_DIR="/opt/restart-user"
REMOTE_JAR="${REMOTE_DIR}/restart-user.jar"
SERVICE_NAME="restart-user"

log() {
  printf '\n[%s] %s\n' "$(date '+%H:%M:%S')" "$*"
}

fail() {
  printf '\n[ERROR] %s\n' "$*" >&2
  exit 1
}

# 1. 检查本地环境
command -v mvn >/dev/null 2>&1 || fail "未找到 mvn，请先确认 Maven 已配置。"
command -v scp >/dev/null 2>&1 || fail "未找到 scp。"
command -v ssh >/dev/null 2>&1 || fail "未找到 ssh。"
[[ -f "${ENV_FILE}" ]] || fail "环境变量文件不存在：${ENV_FILE}"

cd "${PROJECT_DIR}"

# Maven/Surefire 测试进程需要继承这些环境变量
log "加载本地环境变量：${ENV_FILE}"
set -a
# shellcheck disable=SC1090
source "${ENV_FILE}"
set +a

# 2. 构建
log "开始执行 mvn clean package"
mvn clean package

# 3. 定位唯一的可部署 JAR
shopt -s nullglob
JARS=( "${PROJECT_DIR}"/target/restart-user-*.jar )
shopt -u nullglob

[[ ${#JARS[@]} -gt 0 ]] || fail "没有找到 target/restart-user-*.jar"
[[ ${#JARS[@]} -eq 1 ]] || fail "发现多个 restart-user JAR，请检查 target 目录：${JARS[*]}"

LOCAL_JAR="${JARS[0]}"
log "构建成功：${LOCAL_JAR}"

# 4. 上传 JAR
log "上传 JAR 到 ${REMOTE}:${REMOTE_JAR}"
scp "${LOCAL_JAR}" "${REMOTE}:${REMOTE_JAR}"

# 5. 重启远程服务并检查状态
log "重启远程 systemd 服务：${SERVICE_NAME}"
ssh -t "${REMOTE}" \
  "sudo systemctl enable ${SERVICE_NAME} &&
   sudo systemctl restart ${SERVICE_NAME} &&
   sleep 2 &&
   if sudo systemctl is-active --quiet ${SERVICE_NAME}; then
     echo '${SERVICE_NAME} is active';
     sudo systemctl --no-pager --full status ${SERVICE_NAME};
   else
     echo '${SERVICE_NAME} 启动失败，最近日志如下：';
     sudo journalctl -u ${SERVICE_NAME} -n 80 --no-pager;
     exit 1;
   fi"

log "部署完成。"
