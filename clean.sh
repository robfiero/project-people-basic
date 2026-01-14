#!/usr/bin/env bash
set -euo pipefail

mvn -q -pl people-core,people-cli,people-tests -am clean
