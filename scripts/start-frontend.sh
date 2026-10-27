#!/bin/sh
set -eu

project_dir=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
frontend_dir="$project_dir/front"

cd "$frontend_dir"
if [ ! -d node_modules ]; then
  npm ci
fi

exec npm run serve
