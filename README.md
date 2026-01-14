# project-people-basic
This project provides an in-memory people registry with a CLI frontend using `project-cli`.
IDs are auto-generated on create; use `list` or `get` commands to see them.

## Structure
- `people-core`: Domain, repositories, validations, and the public API (`PeopleApi`).
- `people-cli`: CLI adapter that wires commands to the public API.
- `people-tests`: Standalone test module for public API coverage.

## Build Prerequisite
The CLI depends on `cli-framework` from `project-cli`. Install it locally:

```bash
cd /Users/robertfiero/Coding\ Projects/people/project-cli
mvn -q -pl cli-framework -am install
```

## Build
```bash
mvn -q -pl people-core,people-cli,people-tests -am test
```

## Run CLI
```bash
mvn -q -pl people-cli -am package
java -jar people-cli/target/people-cli-1.0.0-SNAPSHOT.jar
```

## CLI Usage
Commands use `--key value` pairs.

```bash
person create --first Ada --last Lovelace --dob 12-10-1815 --gender female --preferred-gender female
person list
address create --person-id <personId> --street "123 Test St" --town Springfield --state MA --type house --owns true --primary true --monthly-payment 1200 --bedrooms 2 --bathrooms 1
employment create --person-id <personId> --name Acme --address "1 Main St" --job-title Engineer --pay-type salary --rate 90000 --current true --start-date 01-01-2020
relationship create --person-id <personId> --type spouse --related-person-id <relatedPersonId>
person picture --id <personId> --file /path/to/photo.jpg
company list
address list --town Springfield
address list --street-contains "Main St"
```
