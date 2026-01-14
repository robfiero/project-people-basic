package com.people.cli.commands;

import cli.Command;
import cli.CommandContext;
import com.people.api.PeopleApi;
import com.people.cli.CliArgs;
import com.people.domain.Employment;
import com.people.domain.PayType;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public final class EmploymentCommand implements Command {
    private final PeopleApi api;

    public EmploymentCommand(PeopleApi api) {
        this.api = api;
    }

    @Override
    public String name() {
        return "employment";
    }

    @Override
    public String description() {
        return "Manage employment (type: employment help for parameters)";
    }

    @Override
    public Object execute(List<String> args, CommandContext ctx) {
        if (args.isEmpty() || "help".equalsIgnoreCase(args.get(0))) {
            return usage();
        }
        String action = args.get(0).toLowerCase();
        return switch (action) {
            case "create" -> createEmployment(args.subList(1, args.size()));
            case "update" -> updateEmployment(args.subList(1, args.size()));
            case "delete" -> deleteEmployment(args.subList(1, args.size()));
            case "get" -> formatEmployment(getEmployment(args.subList(1, args.size())));
            case "list" -> formatEmployments(listEmployments(args.subList(1, args.size())));
            default -> throw new IllegalArgumentException("Unknown action: " + action);
        };
    }

    private String usage() {
        return """
                IDs are auto-generated on create; use list/get to see them.
                employment create --person-id <text:1-50> --name <text:1-200>
                                  --address <text:1-500> --job-title <text:1-100>
                                  --pay-type <salary|hourly> --rate <number:0-1000000000.00>
                                  --current <true|false> --start-date <MM-dd-yyyy> [--end-date <MM-dd-yyyy>]
                                  [--description <text:1-500>]
                employment update --person-id <text:1-50> --id <text:1-50> --name <text:1-200>
                                  --address <text:1-500> --job-title <text:1-100>
                                  --pay-type <salary|hourly> --rate <number:0-1000000000.00>
                                  --current <true|false> --start-date <MM-dd-yyyy> [--end-date <MM-dd-yyyy>]
                                  [--description <text:1-500>]
                employment delete --person-id <text:1-50> --id <text:1-50>
                employment get --person-id <text:1-50> --id <text:1-50>
                employment list --person-id <text:1-50>
                """;
    }

    private Employment createEmployment(List<String> args) {
        Map<String, String> map = CliArgs.parse(args);
        if (map.containsKey("id")) {
            throw new IllegalArgumentException("id is auto-generated; omit --id");
        }
        return api.createEmployment(buildEmployment(null, map));
    }

    private Employment updateEmployment(List<String> args) {
        Map<String, String> map = CliArgs.parse(args);
        return api.updateEmployment(buildEmployment(CliArgs.require(map, "id"), map));
    }

    private Employment deleteEmployment(List<String> args) {
        Map<String, String> map = CliArgs.parse(args);
        String personId = CliArgs.require(map, "person-id");
        String id = CliArgs.require(map, "id");
        return api.deleteEmployment(personId, id);
    }

    private Employment getEmployment(List<String> args) {
        Map<String, String> map = CliArgs.parse(args);
        String personId = CliArgs.require(map, "person-id");
        String id = CliArgs.require(map, "id");
        return api.getEmployment(personId, id);
    }

    private List<Employment> listEmployments(List<String> args) {
        Map<String, String> map = CliArgs.parse(args);
        String personId = CliArgs.require(map, "person-id");
        return api.listEmployments(personId);
    }

    private Employment buildEmployment(String id, Map<String, String> map) {
        String personId = CliArgs.require(map, "person-id");
        String name = CliArgs.require(map, "name");
        String description = CliArgs.optional(map, "description");
        String address = CliArgs.require(map, "address");
        String jobTitle = CliArgs.require(map, "job-title");
        PayType payType = CliArgs.parseEnum(PayType.class, CliArgs.require(map, "pay-type"));
        BigDecimal rate = CliArgs.parseMoney(map, "rate");
        boolean current = CliArgs.parseBoolean(map, "current");
        return new Employment(id, personId, name, description, address, jobTitle, payType, rate,
                current, CliArgs.parseDate(map, "start-date"), parseEndDate(map, current));
    }

    private String formatEmployments(List<Employment> employments) {
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
        for (Employment employment : employments) {
            String end = employment.endDate() == null ? "-" : employment.endDate().format(formatter);
            sb.append(String.format("%-36s  %-7s  %-10s  %-10s  %-10s  %-18s%n",
                    employment.id(),
                    employment.currentEmployer() ? "yes" : "no",
                    employment.startDate().format(formatter),
                    end,
                    employment.payType(),
                    truncate(employment.name(), 18)));
        }
        return sb.toString();
    }

    private String truncate(String value, int max) {
        if (value.length() <= max) {
            return value;
        }
        return value.substring(0, Math.max(0, max - 3)) + "...";
    }

    private java.time.LocalDate parseEndDate(Map<String, String> map, boolean current) {
        String end = CliArgs.optional(map, "end-date");
        if (end == null || end.isBlank()) {
            return null;
        }
        if (current) {
            throw new IllegalArgumentException("current employers must not include --end-date");
        }
        return CliArgs.parseDate(Map.of("end-date", end), "end-date");
    }

    private String formatEmployment(Employment employment) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
        StringBuilder sb = new StringBuilder();
        sb.append("ID: ").append(employment.id()).append(System.lineSeparator());
        sb.append("Person ID: ").append(employment.personId()).append(System.lineSeparator());
        sb.append("Company: ").append(employment.name()).append(System.lineSeparator());
        sb.append("Description: ").append(nullToEmpty(employment.description())).append(System.lineSeparator());
        sb.append("Address: ").append(employment.address()).append(System.lineSeparator());
        sb.append("Job Title: ").append(employment.jobTitle()).append(System.lineSeparator());
        sb.append("Pay Type: ").append(employment.payType()).append(System.lineSeparator());
        sb.append("Rate: ").append(employment.rateOfPay()).append(System.lineSeparator());
        sb.append("Current: ").append(employment.currentEmployer()).append(System.lineSeparator());
        sb.append("Start Date: ").append(employment.startDate().format(formatter)).append(System.lineSeparator());
        sb.append("End Date: ").append(employment.endDate() == null ? "" : employment.endDate().format(formatter));
        return sb.toString();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
