#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
mkdir -p target/classes
find src/main/java src/smoke/java -name '*.java' > /tmp/logistpulse_sources.txt
javac --release 17 -encoding UTF-8 -d target/classes @/tmp/logistpulse_sources.txt
java -cp target/classes ec.edu.logistpulse.ReviewSmokeTest
