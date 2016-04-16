#!/usr/bin/env bash

project_home="$(dirname $0)/.."

norm_name="$1"
if [[ "$norm_name" = "" ]]; then
    echo "Usage: $0 NORM_NAME"
    exit 1
fi

timestamp=$(date +%Y%m%d%H%M%S)

norm="${project_home}/resources/database/norms/${timestamp}-${norm_name}.edn"

echo "Creating norm: ${norm}..."
touch $norm
