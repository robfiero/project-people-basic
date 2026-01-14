#!/usr/bin/env bash
set -euo pipefail

mvn -q -pl people-core,people-cli -am install
mvn -q -pl people-cli -am dependency:copy-dependencies -DincludeScope=runtime

CLI_CP="people-cli/target/classes:people-cli/target/dependency/*"
java -cp "$CLI_CP" com.people.cli.PeopleSeededCliMain
