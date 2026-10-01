#!/usr/bin/env sh
# ─────────────────────────────────────────────────────────────────────────────
#  Presence Numerique - script d'execution (Linux / macOS)
#
#  Usage :  ./lancer.sh          compile puis lance l'application
#           ./lancer.sh test     lance les tests unitaires
#
#  Prerequis : Java 17+ et PostgreSQL (base "attendance", user admin/admin).
#  Pour une autre base, definir avant le lancement :
#    SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME, SPRING_DATASOURCE_PASSWORD
#  Maven n'est pas necessaire : le Maven Wrapper (mvnw) le telecharge.
# ─────────────────────────────────────────────────────────────────────────────
set -e

cd "$(dirname "$0")/app_gestion_presences"

# 1. Verification de Java
if ! command -v java >/dev/null 2>&1; then
    echo "[ERREUR] Java est introuvable. Installez un JDK 17 ou superieur."
    exit 1
fi
JAVA_MAJOR=$(java -version 2>&1 | head -1 | sed -E 's/.*version "([0-9]+).*/\1/')
if [ "$JAVA_MAJOR" -lt 17 ] 2>/dev/null; then
    echo "[ERREUR] Java $JAVA_MAJOR detecte : Java 17 ou superieur est requis."
    exit 1
fi
echo "[OK] Java $JAVA_MAJOR"

chmod +x mvnw

# 2. Mode test
if [ "$1" = "test" ]; then
    echo "[..] Lancement des tests unitaires"
    ./mvnw test
    exit 0
fi

# 3. Compilation
echo "[..] Compilation du projet (premier lancement : quelques minutes)"
./mvnw -q package -DskipTests
echo "[OK] Compilation terminee"

# 4. Lancement
PORT_AFFICHE="${PORT:-8080}"
echo
echo "  Application : http://localhost:$PORT_AFFICHE"
echo "  Comptes     : admin@test.com / admin123      ab@test.com / secretary123"
echo "                jp@test.com / responsable123   cd@test.com / teacher123"
echo "                alice@test.com / password123"
echo "  Arret       : Ctrl+C"
echo
exec java -jar target/app_gestion_presences-0.0.1-SNAPSHOT.jar
