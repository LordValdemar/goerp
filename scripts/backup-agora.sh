#!/usr/bin/env bash
# Faz um backup imediato (além do agendado). Uso: scripts/backup-agora.sh
set -euo pipefail
cd "$(dirname "$0")/.."

docker compose exec -T backup /backup.sh
echo "Backups mais recentes:"
ls -lh "${BACKUP_PASTA:-./backups}/last/" | tail -n 3
