#!/bin/sh
set -eu

project_dir=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
backend_dir="$project_dir/demo"
env_file="$backend_dir/.env"

if ! java -version >/dev/null 2>&1; then
  echo "Java 17 is required. Install a JDK, then retry." >&2
  exit 1
fi

if [ ! -f "$env_file" ]; then
  echo "Missing $env_file. Copy demo/.env.example to demo/.env and set DB_PASSWORD." >&2
  exit 1
fi

set -a
. "$env_file"
set +a

cd "$backend_dir"
exec ./mvnw spring-boot:run
