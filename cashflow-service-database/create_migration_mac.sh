#!/bin/bash

NAME=$1
if [ -z "$NAME" ]; then
    read -p "Informe o nome da migracao: " NAME
fi

FOLDER="src/main/resources/db/migration"
if [ ! -d "$FOLDER" ]; then
    mkdir -p "$FOLDER"
fi

B=$(date +"%Y%m%d%H%M%S")
MIGRATION="${FOLDER}/V${B}__${NAME}.sql"

echo "Criando ${MIGRATION}"
touch "${MIGRATION}"