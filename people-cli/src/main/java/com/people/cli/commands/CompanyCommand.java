package com.people.cli.commands;

import cli.Command;
import cli.CommandContext;
import com.people.api.CompanySummary;
import com.people.api.PeopleApi;

import java.util.Comparator;
import java.util.List;

public final class CompanyCommand implements Command {
    private final PeopleApi api;

    public CompanyCommand(PeopleApi api) {
        this.api = api;
    }

    @Override
    public String name() {
        return "company";
    }

    @Override
    public String description() {
        return "Company queries (type: company help for parameters)";
    }

    @Override
    public Object execute(List<String> args, CommandContext ctx) {
        if (args.isEmpty() || "help".equalsIgnoreCase(args.get(0))) {
            return usage();
        }
        String action = args.get(0).toLowerCase();
        return switch (action) {
            case "list" -> formatCompanies(api.listCompanies());
            default -> throw new IllegalArgumentException("Unknown action: " + action);
        };
    }

    private String usage() {
        return """
                company list
                """;
    }

    private String formatCompanies(List<CompanySummary> companies) {
        if (companies.isEmpty()) {
            return "No companies found.";
        }
        companies.sort(Comparator.comparing(CompanySummary::name));
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-24s  %-24s  %-9s%n", "Company", "Address", "Employees"));
        sb.append(String.format("%-24s  %-24s  %-9s%n",
                "-".repeat(24), "-".repeat(24), "-".repeat(9)));
        for (CompanySummary company : companies) {
            sb.append(String.format("%-24s  %-24s  %-9d%n",
                    truncate(company.name(), 24),
                    truncate(company.address(), 24),
                    company.employeeCount()));
        }
        return sb.toString();
    }

    private String truncate(String value, int max) {
        if (value == null) {
            return "";
        }
        if (value.length() <= max) {
            return value;
        }
        return value.substring(0, Math.max(0, max - 3)) + "...";
    }
}
