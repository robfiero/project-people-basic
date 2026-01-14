package com.people.cli.commands;

import cli.Command;
import cli.CommandContext;
import com.people.api.PeopleApi;
import com.people.cli.CliArgs;
import com.people.domain.Address;
import com.people.domain.AddressType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public final class AddressCommand implements Command {
    private final PeopleApi api;

    public AddressCommand(PeopleApi api) {
        this.api = api;
    }

    @Override
    public String name() {
        return "address";
    }

    @Override
    public String description() {
        return "Manage addresses (type: address help for parameters)";
    }

    @Override
    public Object execute(List<String> args, CommandContext ctx) {
        if (args.isEmpty() || "help".equalsIgnoreCase(args.get(0))) {
            return usage();
        }
        String action = args.get(0).toLowerCase();
        return switch (action) {
            case "create" -> createAddress(args.subList(1, args.size()));
            case "update" -> updateAddress(args.subList(1, args.size()));
            case "delete" -> deleteAddress(args.subList(1, args.size()));
            case "get" -> formatAddress(getAddress(args.subList(1, args.size())));
            case "list" -> formatAddresses(listAddresses(args.subList(1, args.size())), args.subList(1, args.size()));
            default -> throw new IllegalArgumentException("Unknown action: " + action);
        };
    }

    private String usage() {
        return """
                IDs are auto-generated on create; use list/get to see them.
                address create --person-id <text:1-50> --street <text:1-500> --town <text:1-100>
                               --state <text:1-50>
                               --type <house|apartment|condo|flat|other> --owns <true|false> --primary <true|false>
                               --monthly-payment <number:0-1000000.00> --bedrooms <number:0-100>
                               --bathrooms <number:0-100> [--description <text:1-500>]
                address update --person-id <text:1-50> --id <text:1-50> --street <text:1-500> --town <text:1-100>
                               --state <text:1-50>
                               --type <house|apartment|condo|flat|other> --owns <true|false> --primary <true|false>
                               --monthly-payment <number:0-1000000.00> --bedrooms <number:0-100>
                               --bathrooms <number:0-100> [--description <text:1-500>]
                address delete --person-id <text:1-50> --id <text:1-50>
                address get --person-id <text:1-50> --id <text:1-50>
                address list --person-id <text:1-50>
                address list [--street <text>] [--street-contains <text>] [--town <text>] [--state <text>]
                """;
    }

    private Address createAddress(List<String> args) {
        Map<String, String> map = CliArgs.parse(args);
        if (map.containsKey("id")) {
            throw new IllegalArgumentException("id is auto-generated; omit --id");
        }
        return api.createAddress(buildAddress(null, map));
    }

    private Address updateAddress(List<String> args) {
        Map<String, String> map = CliArgs.parse(args);
        return api.updateAddress(buildAddress(CliArgs.require(map, "id"), map));
    }

    private Address deleteAddress(List<String> args) {
        Map<String, String> map = CliArgs.parse(args);
        String personId = CliArgs.require(map, "person-id");
        String id = CliArgs.require(map, "id");
        return api.deleteAddress(personId, id);
    }

    private Address getAddress(List<String> args) {
        Map<String, String> map = CliArgs.parse(args);
        String personId = CliArgs.require(map, "person-id");
        String id = CliArgs.require(map, "id");
        return api.getAddress(personId, id);
    }

    private List<Address> listAddresses(List<String> args) {
        Map<String, String> map = CliArgs.parse(args);
        String personId = CliArgs.optional(map, "person-id");
        if (personId != null) {
            return api.listAddresses(personId);
        }
        String street = CliArgs.optional(map, "street");
        String streetContains = CliArgs.optional(map, "street-contains");
        if (street != null && streetContains != null) {
            throw new IllegalArgumentException("use either --street or --street-contains, not both");
        }
        String town = CliArgs.optional(map, "town");
        String state = CliArgs.optional(map, "state");
        return api.listAddressesFiltered(street, town, state, streetContains);
    }

    private Address buildAddress(String id, Map<String, String> map) {
        String personId = CliArgs.require(map, "person-id");
        String street = CliArgs.require(map, "street");
        String town = CliArgs.require(map, "town");
        String state = CliArgs.require(map, "state");
        AddressType type = CliArgs.parseEnum(AddressType.class, CliArgs.require(map, "type"));
        String description = CliArgs.optional(map, "description");
        boolean owns = CliArgs.parseBoolean(map, "owns");
        boolean primary = CliArgs.parseBoolean(map, "primary");
        BigDecimal monthly = CliArgs.parseMoney(map, "monthly-payment");
        int bedrooms = CliArgs.parseInt(map, "bedrooms");
        int bathrooms = CliArgs.parseInt(map, "bathrooms");
        return new Address(id, personId, street, town, state, type, description, owns, primary, monthly, bedrooms,
                bathrooms);
    }

    private String formatAddresses(List<Address> addresses, List<String> rawArgs) {
        if (addresses.isEmpty()) {
            return "No addresses found.";
        }
        boolean isFiltered = !CliArgs.parse(rawArgs).containsKey("person-id");
        if (!isFiltered) {
            addresses.sort((a, b) -> {
                if (a.primary() != b.primary()) {
                    return a.primary() ? -1 : 1;
                }
                return a.address().compareToIgnoreCase(b.address());
            });
        } else {
            addresses.sort((a, b) -> a.address().compareToIgnoreCase(b.address()));
        }
        StringBuilder sb = new StringBuilder();
        if (isFiltered) {
            sb.append(String.format("%-36s  %-10s  %-6s  %-8s  %-20s  %-14s  %-8s%n",
                    "ID", "Type", "Beds", "Baths", "Street", "Town", "State"));
            sb.append(String.format("%-36s  %-10s  %-6s  %-8s  %-20s  %-14s  %-8s%n",
                    "-".repeat(36), "-".repeat(10), "-".repeat(6), "-".repeat(8),
                    "-".repeat(20), "-".repeat(14), "-".repeat(8)));
            for (Address address : addresses) {
                sb.append(String.format("%-36s  %-10s  %-6d  %-8d  %-20s  %-14s  %-8s%n",
                        address.id(),
                        address.type(),
                        address.bedrooms(),
                        address.bathrooms(),
                        truncate(address.address(), 20),
                        truncate(address.town(), 14),
                        truncate(address.state(), 8)));
            }
        } else {
            sb.append(String.format("%-36s  %-7s  %-6s  %-10s  %-6s  %-8s  %-24s%n",
                    "ID", "Primary", "Owns", "Type", "Beds", "Baths", "Street"));
            sb.append(String.format("%-36s  %-7s  %-6s  %-10s  %-6s  %-8s  %-24s%n",
                    "-".repeat(36), "-".repeat(7), "-".repeat(6), "-".repeat(10),
                    "-".repeat(6), "-".repeat(8), "-".repeat(24)));
            for (Address address : addresses) {
                sb.append(String.format("%-36s  %-7s  %-6s  %-10s  %-6d  %-8d  %-24s%n",
                        address.id(),
                        address.primary() ? "yes" : "no",
                        address.owns() ? "yes" : "no",
                        address.type(),
                        address.bedrooms(),
                        address.bathrooms(),
                        truncate(address.address(), 24)));
            }
        }
        return sb.toString();
    }

    private String truncate(String value, int max) {
        if (value.length() <= max) {
            return value;
        }
        return value.substring(0, Math.max(0, max - 3)) + "...";
    }

    private String formatAddress(Address address) {
        StringBuilder sb = new StringBuilder();
        sb.append("ID: ").append(address.id()).append(System.lineSeparator());
        sb.append("Person ID: ").append(address.personId()).append(System.lineSeparator());
        sb.append("Street: ").append(address.address()).append(System.lineSeparator());
        sb.append("Town: ").append(address.town()).append(System.lineSeparator());
        sb.append("State: ").append(address.state()).append(System.lineSeparator());
        sb.append("Type: ").append(address.type()).append(System.lineSeparator());
        sb.append("Description: ").append(nullToEmpty(address.description())).append(System.lineSeparator());
        sb.append("Owns: ").append(address.owns()).append(System.lineSeparator());
        sb.append("Primary: ").append(address.primary()).append(System.lineSeparator());
        sb.append("Monthly Payment: ").append(address.monthlyPayment()).append(System.lineSeparator());
        sb.append("Bedrooms: ").append(address.bedrooms()).append(System.lineSeparator());
        sb.append("Bathrooms: ").append(address.bathrooms());
        return sb.toString();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
