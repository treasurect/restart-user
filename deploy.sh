
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
REMOTE_JAR="${REMOTE_DIR}/restart-user-service.jar"
REMOTE_TEMP="/tmp/restart-user-service.jar"

SERVICE_NAME="restart-user"

log() {
    printf '\n[%s] %s\n' "$(date '+%H:%M:%S')" "$*"
}

fail() {
    printf '\n[ERROR] %s\n' "$*" >&2
    exit 1
}

# =========================
# 1. 检查本地环境
# =========================

command -v mvn >/dev/null 2>&1 || fail "未找到 mvn，请先确认 Maven 已配置。"
command -v scp >/dev/null 2>&1 || fail "未找到 scp。"
command -v ssh >/dev/null 2>&1 || fail "未找到 ssh。"

[[ -f "${ENV_FILE}" ]] || fail "环境变量文件不存在：${ENV_FILE}"

cd "${PROJECT_DIR}"

# =========================
# 2. 加载本地环境变量
# =========================

log "加载本地环境变量：${ENV_FILE}"

set -a
# shellcheck disable=SC1090
source "${ENV_FILE}"
set +a

# =========================
# 3. Maven 构建
# =========================

log "开始执行 mvn clean package"

mvn clean package

# =========================
# 4. 定位唯一的可部署 JAR
# =========================

shopt -s nullglob
JARS=()

for file in "${PROJECT_DIR}"/target/restart-user-service*.jar; do
    [[ -f "$file" ]] || continue

    case "$file" in
        *-sources.jar|-javadoc.jar|-plain.jar|*.original)
            continue
            ;;
    esac

    JARS+=("$file")
done

shopt -u nullglob

[[ ${#JARS[@]} -gt 0 ]] || fail "没有找到 target/restart-user-service*.jar"
[[ ${#JARS[@]} -eq 1 ]] || fail "发现多个 restart-user-service JAR，请检查 target 目录。"

LOCAL_JAR="${JARS[0]}"

log "构建成功：${LOCAL_JAR}"

# =========================
# 5. 检查服务器部署环境
# =========================

log "检查服务器部署环境"

ssh "${REMOTE}" "
    test -d '${REMOTE_DIR}' &&
    test -f '/etc/restart-server/restart-server.env' &&
    sudo systemctl cat '${SERVICE_NAME}' >/dev/null
" || fail "服务器部署目录、环境变量文件或 systemd 服务不存在。"

# =========================
# 6. 上传 JAR 到临时目录
# =========================

log "上传 JAR 到 ${REMOTE}:${REMOTE_TEMP}"

scp "${LOCAL_JAR}" "${REMOTE}:${REMOTE_TEMP}"

# =========================
# 7. 备份旧 JAR 并替换
# =========================

log "备份旧版本并替换 JAR"

ssh "${REMOTE}" "
    set -e

    if [ -f '${REMOTE_JAR}' ]; then
        sudo cp '${REMOTE_JAR}' \
            '${REMOTE_JAR}.backup.'\$(date +%Y%m%d%H%M%S)
    fi

    sudo mv '${REMOTE_TEMP}' '${REMOTE_JAR}'
    sudo chown '${REMOTE_USER}:${REMOTE_USER}' '${REMOTE_JAR}'
"

# =========================
# 8. 重启远程服务
# =========================

log "重启远程 systemd 服务：${SERVICE_NAME}"

if ! ssh "${REMOTE}" "
    sudo systemctl enable '${SERVICE_NAME}' &&
    sudo systemctl restart '${SERVICE_NAME}'
"; then
    log "服务重启命令执行失败"
    ssh "${REMOTE}" "sudo journalctl -u '${SERVICE_NAME}' -n 80 --no-pager"
    exit 1
fi

# =========================
# 9. 检查启动状态
# =========================

log "等待服务启动"

sleep 3

if ! ssh "${REMOTE}" "
    sudo systemctl is-active --quiet '${SERVICE_NAME}'
"; then

    log "${SERVICE_NAME} 启动失败"

    ssh "${REMOTE}" "
        sudo systemctl status '${SERVICE_NAME}' --no-pager || true
        sudo journalctl -u '${SERVICE_NAME}' -n 80 --no-pager
    "

    exit 1
fi

# =========================
# 10. 部署结果
# =========================

log "${SERVICE_NAME} 发布成功"

ssh "${REMOTE}" "
    sudo systemctl status '${SERVICE_NAME}' --no-pager

    echo '========== 最近启动日志 =========='

    sudo journalctl -u '${SERVICE_NAME}' -n 30 --no-pager
"

log "部署完成。"
