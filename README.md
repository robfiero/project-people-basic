# project-people-basic
This project provides an in-memory people registry with a CLI frontend using `project-cli`.
IDs are auto-generated on create; use `list` or `get` commands to see them.

## The idea behind this project was to start seeing what the capabilities are of ChatGPT.  The prompt I originally gave in the Codex panel in my VS Code IDE to generate the files is below.  I needed to do some refinement after this, including:

 - Working with the AI to get the code to build, run and test properly from the provided commands.  
 - Creating build, run, test, clean and populate scripts for convenience.
 - Fixing the display of list commands, which originally just output one long string with all the variables just concatenated together.
 - Addressing the get commands to output not only the object attributes in a readable fashion, but including tables of related objects below the basic attributes.
 - Adding the populate.sh so I didn't have to create items to display every time I ran the project.  (It is an in-memory project for the moment). This could use another iteration to add larger data sets and more geographically diverse information, but I'll add that capability in a future iteration of the project that will have the ability to persist data in files or a database.

All told, this project from start to finish probably took me about 3 or 4 hours.  I unfortunately didn't track the exact time.

## Here is the output from my project metrics that tracks all the interesting stuff of what was created, with some information 

Root: /<redacted>/Coding Projects/people/project-people-basic
Profile: all
Files counted: 43
Total size: 95.1 KB
Text files skipped (binary/unreadable): 0
Tool files excluded: 2

Line counts (heuristic):
  Total:   2578
  Code:    2204
  Comment: 5
  Blank:   369

By extension:
  .java      files=    33  lines=     2379  code=     2038  cmt=        0  blank=      341
  .sh        files=     5  lines=       28  code=       16  cmt=        5  blank=        7
  .xml       files=     4  lines=      129  code=      115  cmt=        0  blank=       14
  .md        files=     1  lines=       42  code=       35  cmt=        0  blank=        7

Top 10 largest files:
    13.0 KB  people-core/src/main/java/com/people/service/PeopleService.java
    12.7 KB  people-cli/src/main/java/com/people/cli/commands/PersonCommand.java
     9.4 KB  people-cli/src/main/java/com/people/cli/commands/AddressCommand.java
     8.1 KB  people-cli/src/main/java/com/people/cli/commands/EmploymentCommand.java
     8.0 KB  people-tests/src/test/java/com/people/tests/PeopleServiceTest.java
     6.4 KB  people-core/src/main/java/com/people/service/Validators.java
     5.2 KB  people-cli/src/main/java/com/people/cli/commands/RelationshipCommand.java
     4.5 KB  people-cli/src/main/java/com/people/cli/SeedData.java
     2.6 KB  people-cli/src/main/java/com/people/cli/CliArgs.java
     2.4 KB  people-core/src/main/java/com/people/repo/InMemoryRelationshipRepository.java

Top 10 longest files (by total lines):
        340 lines  people-core/src/main/java/com/people/service/PeopleService.java
        282 lines  people-cli/src/main/java/com/people/cli/commands/PersonCommand.java
        205 lines  people-cli/src/main/java/com/people/cli/commands/AddressCommand.java
        202 lines  people-tests/src/test/java/com/people/tests/PeopleServiceTest.java
        181 lines  people-cli/src/main/java/com/people/cli/commands/EmploymentCommand.java
        141 lines  people-core/src/main/java/com/people/service/Validators.java
        125 lines  people-cli/src/main/java/com/people/cli/commands/RelationshipCommand.java
        107 lines  people-cli/src/main/java/com/people/cli/SeedData.java
         77 lines  people-cli/src/main/java/com/people/cli/CliArgs.java
         74 lines  people-core/src/main/java/com/people/repo/InMemoryEmploymentRepository.java


## Original prompt to create this project and files:

"I would like to create a new Java project.  It will have the following features.  

All items described below will be stored in memory for the time being, but I'd like it structured so that all data can easily be stored and read from a file or a database in a later iteration.  

There should be a public API that can be used to run all commands.  In a future iteration, I'd like to make this public API into a REST interface that can be called by a remote client.  This current iteration should use the CLI created in https://github.com/robfiero/project-cli.git to execute commands.

Commands will be specified with each "object" below.

0..n people, which will consist of a first, middle and last name, date of birth, gender (male, female, non-binary), preferred gender (male, female, non-binary, or one of a list of other preferred genders), identifying id or number that is unique, and optional picture.  Commands that the person object will support are create, update, delete, list, and get.

Each person can have 0..n addresses.  This represents a place where they live, either an owned house, rental, or temporary accomodation.  Each address should include the address which could be US or international format, type such as house, apartment, condo, or flat, description, owns or rents, montly payment, number of bedrooms, number of bathrooms.  Commands for each address include create, update, delete, list and get.

Each person can have 0..n spouses, children, aunts, uncles, nieces, nephews, granparents, grandchildren, and cousins.  These are relationships to other existing people in the system.  Each relationship needs a command to create, update, delete, list and get.

Each person can have 0..n places of employment.  A place of employment has a name, company description, address, job title, flag for salary or hourly employee, rate of pay which could be either annual salary or hourly rate.  Commands for places of employment include create, update, delete, list and get.

I'd like a series of tests created that will extensively test all public interfaces.  Tests should include blank or null values, values that are very long, range checks, and any standard tests that would traditionally be included.

This should all be structured such that the tests are separate from the base project, as the base project in the future may be built into a Java Jar."


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
