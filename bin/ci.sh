#!/usr/bin/env bash

set -e

function wait-for-server-connection() {

    local server_port="$1"
    local max_seconds_to_wait="$2"
    i=0
    while ! nc -z 127.0.0.1 $server_port; do
        sleep 1
        echo -n "."
        ((i++)) || true
        if [ $i -gt $max_seconds_to_wait ]; then
            echo
            echo "Server connection timed out"
            exit 1
        fi
    done
    exit 0
}

echo "Running CI build..."
echo
lein ci

echo "Building uberjar..."
echo

lein uberjar

echo "Starting server..."
echo
export PORT=19832
eval $(cat Procfile | cut -c6-) & # Run the Procfile command

timeout_secs=15
echo "Waiting ${timeout_secs} seconds for server to boot up..."
echo
wait-for-server-connection $PORT timeout_secs
