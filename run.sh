#!/usr/bin/env bash
set -e

CONFIG_PATH="${1:-config/forest.properties}"

echo "== Compilation =="
rm -rf out out-test
mkdir -p out out-test
javac -d out $(find src/main -name "*.java")
javac -cp out -d out-test $(find src/test -name "*.java")

echo
echo "== Tests =="
java -cp out:out-test com.jennie.forestfire.TestRunner

echo
echo "== Simulation (config: ${CONFIG_PATH}) =="
java -cp out com.jennie.forestfire.app.Main "${CONFIG_PATH}"
