#!/usr/bin/env bash
# Restaura um backup no banco do docker-compose, SUBSTITUINDO todos os dados atuais.
#
# Uso: scripts/restaurar-backup.sh backups/last/goerp-20261120-030000.sql.gz [--sim]
#   --sim  não pede confirmação (para automação)
#
# Passos: para a aplicação, apaga o schema public, restaura o arquivo e sobe a aplicação de novo.
set -euo pipefail
cd "$(dirname "$0")/.."

ARQUIVO="${1:-}"
CONFIRMADO="${2:-}"
BANCO="${DB_NAME:-goerp}"
USUARIO="${DB_USERNAME:-goerp}"

if [[ -z "$ARQUIVO" || ! -f "$ARQUIVO" ]]; then
  echo "Informe um arquivo de backup existente. Disponíveis:" >&2
  ls -1t "${BACKUP_PASTA:-./backups}"/last/*.sql.gz 2>/dev/null | head -n 10 >&2 || true
  exit 1
fi

if ! gzip -t "$ARQUIVO"; then
  echo "Arquivo corrompido (gzip inválido): $ARQUIVO" >&2
  exit 1
fi

if [[ "$CONFIRMADO" != "--sim" ]]; then
  read -r -p "Isto APAGA os dados atuais do banco '$BANCO' e restaura '$ARQUIVO'. Digite RESTAURAR para continuar: " RESPOSTA
  [[ "$RESPOSTA" == "RESTAURAR" ]] || { echo "Cancelado."; exit 1; }
fi

APP_RODANDO=$(docker compose ps --status running --services 2>/dev/null | grep -x app || true)
if [[ -n "$APP_RODANDO" ]]; then
  echo "Parando a aplicação..."
  docker compose stop app
fi

echo "Limpando o banco '$BANCO'..."
docker compose exec -T db psql -v ON_ERROR_STOP=1 -U "$USUARIO" -d "$BANCO" -q \
  -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"

echo "Restaurando $ARQUIVO..."
gunzip -c "$ARQUIVO" | docker compose exec -T db psql -v ON_ERROR_STOP=1 -U "$USUARIO" -d "$BANCO" -q

if [[ -n "$APP_RODANDO" ]]; then
  echo "Subindo a aplicação..."
  docker compose start app
fi
echo "Restauração concluída."
