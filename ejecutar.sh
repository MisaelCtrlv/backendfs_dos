#!/usr/bin/env bash
# Carga las variables de .env y ejecuta un microservicio.
# Uso:  ./ejecutar.sh ms-usuarios     o     ./ejecutar.sh ms-catalogo
set -e
cd "$(dirname "$0")"
if [ ! -f .env ]; then
  echo "Falta el archivo .env. Copia .env.example como .env y completa los valores."
  exit 1
fi
if [ -z "$1" ] || [ ! -d "$1" ]; then
  echo "Indica el microservicio: ./ejecutar.sh ms-usuarios  o  ./ejecutar.sh ms-catalogo"
  exit 1
fi
set -a; source .env; set +a
cd "$1"
mvn spring-boot:run
