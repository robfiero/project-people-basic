#!/usr/bin/env bash
set -euo pipefail

mvn -q -pl people-tests -am test
