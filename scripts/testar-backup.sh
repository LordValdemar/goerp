#!/usr/bin/env bash
# Prova que o backup funciona: grava dados, faz backup, apaga o banco, restaura e confere.
# Usa um projeto docker-compose separado ("goerp-teste-backup"), sem tocar no ambiente normal.
# Uso: scripts/testar-backup.sh
set -euo pipefail
cd "$(dirname "$0")/.."

export COMPOSE_PROJECT_NAME=goerp-teste-backup
export BACKUP_PASTA
BACKUP_PASTA="$(mktemp -d)"
export DB_PORTA=55432
trap 'docker compose down -v >/dev/null 2>&1; rm -rf "$BACKUP_PASTA"' EXIT

sql() { docker compose exec -T db psql -v ON_ERROR_STOP=1 -U goerp -d goerp -tAq -c "$1"; }

echo "1. Subindo banco e serviço de backup..."
docker compose up -d --wait db >/dev/null 2>&1
docker compose up -d backup >/dev/null 2>&1

echo "2. Gravando dados de exemplo..."
sql "CREATE TABLE prova_backup (id INT PRIMARY KEY, texto TEXT, valor NUMERIC(15,2));
     INSERT INTO prova_backup SELECT g, 'linha ' || g, g * 1.25 FROM generate_series(1, 1000) g;"
ANTES=$(sql "SELECT count(*) || ':' || sum(valor) FROM prova_backup")

echo "3. Fazendo backup..."
docker compose exec -T backup /backup.sh >/dev/null
ARQUIVO=$(find "$BACKUP_PASTA/last" -type f -name "*.sql.gz" | sort | tail -n 1)
for PASTA in daily weekly monthly; do
  [[ -n "$(ls -A "$BACKUP_PASTA/$PASTA")" ]] || { echo "FALHOU: nada em $PASTA/"; exit 1; }
done

echo "4. Simulando desastre (apagando os dados)..."
sql "DROP TABLE prova_backup"

echo "5. Restaurando $(basename "$ARQUIVO")..."
scripts/restaurar-backup.sh "$ARQUIVO" --sim >/dev/null

DEPOIS=$(sql "SELECT count(*) || ':' || sum(valor) FROM prova_backup")
if [[ "$ANTES" == "$DEPOIS" ]]; then
  echo "OK: dados restaurados ($DEPOIS)"
else
  echo "FALHOU: antes=$ANTES depois=$DEPOIS"
  exit 1
fi
