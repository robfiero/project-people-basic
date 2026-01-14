package com.people.cli.commands;

import cli.Command;
import cli.CommandContext;
import com.people.api.PeopleApi;
import com.people.cli.CliArgs;
import com.people.domain.Gender;
import com.people.domain.Person;
import com.people.domain.PreferredGender;
import com.people.domain.PreferredGenderType;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class PersonCommand implements Command {
    private final PeopleApi api;

    public PersonCommand(PeopleApi api) {
        this.api = api;
    }

    @Override
    public String name() {
        return "person";
    }

    @Override
    public String description() {
        return "Manage people (type: person help for parameters)";
    }

    @Override
    public Object execute(List<String> args, CommandContext ctx) {
        if (args.isEmpty() || "help".equalsIgnoreCase(args.get(0))) {
            return usage();
        }
        String action = args.get(0).toLowerCase();
        return switch (action) {
            case "create" -> createPerson(args.subList(1, args.size()));
            case "update" -> updatePerson(args.subList(1, args.size()));
            case "delete" -> deletePerson(args.subList(1, args.size()));
            case "get" -> formatPerson(getPerson(args.subList(1, args.size())));
            case "list" -> formatPeople(api.listPeople());
            case "picture" -> setPicture(args.subList(1, args.size()));
            default -> throw new IllegalArgumentException("Unknown action: " + action);
        };
    }

    private String usage() {
        return """
                IDs are auto-generated on create; use list/get to see them.
                person create --first <text:1-100> --last <text:1-100> --dob <MM-dd-yyyy>
                              --gender <male|female|non-binary>
                              --preferred-gender <male|female|non-binary|other> [--preferred-gender-other <text:1-50>]
                              [--middle <text:1-100>]
                person update --id <text:1-50> --first <text:1-100> --last <text:1-100> --dob <MM-dd-yyyy>
                              --gender <male|female|non-binary>
                              --preferred-gender <male|female|non-binary|other> [--preferred-gender-other <text:1-50>]
                              [--middle <text:1-100>]
                person picture --id <text:1-50> --file <path:.png|.jpg>
                person delete --id <text:1-50>
                person get --id <text:1-50>
                person list
                """;
    }

    private Person createPerson(List<String> args) {
        Map<String, String> map = CliArgs.parse(args);
        if (map.containsKey("id")) {
            throw new IllegalArgumentException("id is auto-generated; omit --id");
        }
        if (map.containsKey("picture")) {
            throw new IllegalArgumentException("picture must be set with: person picture --id <id> --file <path>");
        }
        return api.createPerson(buildPerson(null, map, null));
    }

    private Person updatePerson(List<String> args) {
        Map<String, String> map = CliArgs.parse(args);
        if (map.containsKey("picture")) {
            throw new IllegalArgumentException("picture must be set with: person picture --id <id> --file <path>");
        }
        String id = CliArgs.require(map, "id");
        Person base = buildPerson(id, map, null);
        Person existing = api.getPerson(id);
        Person updated = new Person(base.id(), base.firstName(), base.middleName(), base.lastName(),
                base.dateOfBirth(), base.gender(), base.preferredGender(), existing.picturePath());
        return api.updatePerson(updated);
    }

    private Person deletePerson(List<String> args) {
        Map<String, String> map = CliArgs.parse(args);
        return api.deletePerson(CliArgs.require(map, "id"));
    }

    private Person getPerson(List<String> args) {
        Map<String, String> map = CliArgs.parse(args);
        return api.getPerson(CliArgs.require(map, "id"));
    }

    private Person buildPerson(String id, Map<String, String> map, String picture) {
        String first = CliArgs.require(map, "first");
        String middle = CliArgs.optional(map, "middle");
        String last = CliArgs.require(map, "last");
        LocalDate dob = CliArgs.parseDate(map, "dob");
        Gender gender = CliArgs.parseEnum(Gender.class, CliArgs.require(map, "gender"));
        PreferredGender preferredGender = parsePreferredGender(map);
        return new Person(id, first, middle, last, dob, gender, preferredGender, picture);
    }

    private Person setPicture(List<String> args) {
        Map<String, String> map = CliArgs.parse(args);
        String id = CliArgs.require(map, "id");
        String file = CliArgs.require(map, "file");
        String lower = file.toLowerCase();
        if (!(lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg"))) {
            throw new IllegalArgumentException("picture file must be .png or .jpg");
        }
        if (!Files.exists(Path.of(file))) {
            throw new IllegalArgumentException("picture file not found: " + file);
        }
        Person existing = api.getPerson(id);
        Person updated = new Person(existing.id(), existing.firstName(), existing.middleName(), existing.lastName(),
                existing.dateOfBirth(), existing.gender(), existing.preferredGender(), file);
        return api.updatePerson(updated);
    }

    private PreferredGender parsePreferredGender(Map<String, String> map) {
        String value = CliArgs.require(map, "preferred-gender");
        PreferredGenderType type = CliArgs.parseEnum(PreferredGenderType.class, value);
        if (type == PreferredGenderType.OTHER) {
            String other = CliArgs.require(map, "preferred-gender-other");
            return PreferredGender.other(other);
        }
        return PreferredGender.of(type);
    }

    private String formatPeople(List<Person> people) {
        if (people.isEmpty()) {
            return "No people found.";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
        people.sort(Comparator.comparing(Person::lastName).thenComparing(Person::firstName));
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-36s  %-22s  %-10s  %-10s  %-15s  %-7s%n",
                "ID", "Name", "DOB", "Gender", "Preferred", "Picture"));
        sb.append(String.format("%-36s  %-22s  %-10s  %-10s  %-15s  %-7s%n",
                "-".repeat(36), "-".repeat(22), "-".repeat(10), "-".repeat(10),
                "-".repeat(15), "-".repeat(7)));
        for (Person person : people) {
            String name = person.lastName() + ", " + person.firstName();
            if (person.middleName() != null) {
                name += " " + person.middleName();
            }
            String picture = person.picturePath() == null ? "no" : "yes";
            sb.append(String.format("%-36s  %-22s  %-10s  %-10s  %-15s  %-7s%n",
                    person.id(),
                    truncate(name, 22),
                    person.dateOfBirth().format(formatter),
                    person.gender(),
                    person.preferredGender(),
                    picture));
        }
        return sb.toString();
    }

    private String truncate(String value, int max) {
        if (value.length() <= max) {
            return value;
        }
        return value.substring(0, Math.max(0, max - 3)) + "...";
    }

    private String formatPerson(Person person) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
        StringBuilder sb = new StringBuilder();
        sb.append("ID: ").append(person.id()).append(System.lineSeparator());
        sb.append("First: ").append(person.firstName()).append(System.lineSeparator());
        sb.append("Middle: ").append(nullToEmpty(person.middleName())).append(System.lineSeparator());
        sb.append("Last: ").append(person.lastName()).append(System.lineSeparator());
        sb.append("DOB: ").append(person.dateOfBirth().format(formatter)).append(System.lineSeparator());
        sb.append("Gender: ").append(person.gender()).append(System.lineSeparator());
        sb.append("Preferred Gender: ").append(person.preferredGender()).append(System.lineSeparator());
        sb.append("Picture: ").append(nullToEmpty(person.picturePath())).append(System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append("Addresses:").append(System.lineSeparator());
        sb.append(formatAddresses(api.listAddresses(person.id()))).append(System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append("Relationships:").append(System.lineSeparator());
        sb.append(formatRelationships(api.listRelationships(person.id()))).append(System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append("Employment:").append(System.lineSeparator());
        sb.append(formatEmployments(api.listEmployments(person.id())));
        return sb.toString();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String formatAddresses(List<com.people.domain.Address> addresses) {
        if (addresses.isEmpty()) {
            return "No addresses found.";
        }
        addresses.sort((a, b) -> {
            if (a.primary() != b.primary()) {
                return a.primary() ? -1 : 1;
            }
            return a.address().compareToIgnoreCase(b.address());
        });
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-36s  %-7s  %-6s  %-10s  %-6s  %-8s  %-24s%n",
                "ID", "Primary", "Owns", "Type", "Beds", "Baths", "Address"));
        sb.append(String.format("%-36s  %-7s  %-6s  %-10s  %-6s  %-8s  %-24s%n",
                "-".repeat(36), "-".repeat(7), "-".repeat(6), "-".repeat(10),
                "-".repeat(6), "-".repeat(8), "-".repeat(24)));
        for (com.people.domain.Address address : addresses) {
            sb.append(String.format("%-36s  %-7s  %-6s  %-10s  %-6d  %-8d  %-24s%n",
                    address.id(),
                    address.primary() ? "yes" : "no",
                    address.owns() ? "yes" : "no",
                    address.type(),
                    address.bedrooms(),
                    address.bathrooms(),
                    truncate(address.address(), 24)));
        }
        return sb.toString().trim();
    }

    private String formatRelationships(List<com.people.domain.Relationship> relationships) {
        if (relationships.isEmpty()) {
            return "No relationships found.";
        }
        relationships.sort((a, b) -> a.type().compareTo(b.type()));
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-36s  %-12s  %-36s%n", "ID", "Type", "Related Person ID"));
        sb.append(String.format("%-36s  %-12s  %-36s%n",
                "-".repeat(36), "-".repeat(12), "-".repeat(36)));
        for (com.people.domain.Relationship relationship : relationships) {
            sb.append(String.format("%-36s  %-12s  %-36s%n",
                    relationship.id(),
                    relationship.type(),
                    relationship.relatedPersonId()));
        }
        return sb.toString().trim();
    }

    private String formatEmployments(List<com.people.domain.Employment> employments) {
        if (employments.isEmpty()) {
            return "No employment records found.";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
        employments.sort((a, b) -> {
            if (a.currentEmployer() != b.currentEmployer()) {
                return a.currentEmployer() ? -1 : 1;
            }
            return b.startDate().compareTo(a.startDate());
        });
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-36s  %-7s  %-10s  %-10s  %-10s  %-18s%n",
                "ID", "Current", "Start", "End", "PayType", "Company"));
        sb.append(String.format("%-36s  %-7s  %-10s  %-10s  %-10s  %-18s%n",
                "-".repeat(36), "-".repeat(7), "-".repeat(10), "-".repeat(10),
                "-".repeat(10), "-".repeat(18)));
        for (com.people.domain.Employment employment : employments) {
            String end = employment.endDate() == null ? "" : employment.endDate().format(formatter);
            sb.append(String.format("%-36s  %-7s  %-10s  %-10s  %-10s  %-18s%n",
                    employment.id(),
                    employment.currentEmployer() ? "yes" : "no",
                    employment.startDate().format(formatter),
                    end,
                    employment.payType(),
                    truncate(employment.name(), 18)));
        }
        return sb.toString().trim();
    }
}
