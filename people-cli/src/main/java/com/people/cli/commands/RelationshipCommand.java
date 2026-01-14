package com.people.cli.commands;

import cli.Command;
import cli.CommandContext;
import com.people.api.PeopleApi;
import com.people.cli.CliArgs;
import com.people.domain.Relationship;
import com.people.domain.RelationshipType;

import java.util.List;
import java.util.Map;

public final class RelationshipCommand implements Command {
    private final PeopleApi api;

    public RelationshipCommand(PeopleApi api) {
        this.api = api;
    }

    @Override
    public String name() {
        return "relationship";
    }

    @Override
    public String description() {
        return "Manage relationships (type: relationship help for parameters)";
    }

    @Override
    public Object execute(List<String> args, CommandContext ctx) {
        if (args.isEmpty() || "help".equalsIgnoreCase(args.get(0))) {
            return usage();
        }
        String action = args.get(0).toLowerCase();
        return switch (action) {
            case "create" -> createRelationship(args.subList(1, args.size()));
            case "update" -> updateRelationship(args.subList(1, args.size()));
            case "delete" -> deleteRelationship(args.subList(1, args.size()));
            case "get" -> formatRelationship(getRelationship(args.subList(1, args.size())));
            case "list" -> formatRelationships(listRelationships(args.subList(1, args.size())));
            default -> throw new IllegalArgumentException("Unknown action: " + action);
        };
    }

    private String usage() {
        return """
                IDs are auto-generated on create; use list/get to see them.
                relationship create --person-id <text:1-50> --related-person-id <text:1-50>
                                    --type <spouse|child|aunt|uncle|niece|nephew|grandparent|grandchild|cousin>
                relationship update --person-id <text:1-50> --id <text:1-50> --related-person-id <text:1-50>
                                    --type <spouse|child|aunt|uncle|niece|nephew|grandparent|grandchild|cousin>
                relationship delete --person-id <text:1-50> --id <text:1-50>
                relationship get --person-id <text:1-50> --id <text:1-50>
                relationship list --person-id <text:1-50>
                """;
    }

    private Relationship createRelationship(List<String> args) {
        Map<String, String> map = CliArgs.parse(args);
        if (map.containsKey("id")) {
            throw new IllegalArgumentException("id is auto-generated; omit --id");
        }
        return api.createRelationship(buildRelationship(null, map));
    }

    private Relationship updateRelationship(List<String> args) {
        Map<String, String> map = CliArgs.parse(args);
        return api.updateRelationship(buildRelationship(CliArgs.require(map, "id"), map));
    }

    private Relationship deleteRelationship(List<String> args) {
        Map<String, String> map = CliArgs.parse(args);
        String personId = CliArgs.require(map, "person-id");
        String id = CliArgs.require(map, "id");
        return api.deleteRelationship(personId, id);
    }

    private Relationship getRelationship(List<String> args) {
        Map<String, String> map = CliArgs.parse(args);
        String personId = CliArgs.require(map, "person-id");
        String id = CliArgs.require(map, "id");
        return api.getRelationship(personId, id);
    }

    private List<Relationship> listRelationships(List<String> args) {
        Map<String, String> map = CliArgs.parse(args);
        String personId = CliArgs.require(map, "person-id");
        return api.listRelationships(personId);
    }

    private Relationship buildRelationship(String id, Map<String, String> map) {
        String personId = CliArgs.require(map, "person-id");
        String relatedPersonId = CliArgs.require(map, "related-person-id");
        RelationshipType type = CliArgs.parseEnum(RelationshipType.class, CliArgs.require(map, "type"));
        return new Relationship(id, personId, relatedPersonId, type);
    }

    private String formatRelationships(List<Relationship> relationships) {
        if (relationships.isEmpty()) {
            return "No relationships found.";
        }
        relationships.sort((a, b) -> a.type().compareTo(b.type()));
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-36s  %-12s  %-36s%n", "ID", "Type", "Related Person ID"));
        sb.append(String.format("%-36s  %-12s  %-36s%n",
                "-".repeat(36), "-".repeat(12), "-".repeat(36)));
        for (Relationship relationship : relationships) {
            sb.append(String.format("%-36s  %-12s  %-36s%n",
                    relationship.id(),
                    relationship.type(),
                    relationship.relatedPersonId()));
        }
        return sb.toString();
    }

    private String formatRelationship(Relationship relationship) {
        StringBuilder sb = new StringBuilder();
        sb.append("ID: ").append(relationship.id()).append(System.lineSeparator());
        sb.append("Person ID: ").append(relationship.personId()).append(System.lineSeparator());
        sb.append("Related Person ID: ").append(relationship.relatedPersonId()).append(System.lineSeparator());
        sb.append("Type: ").append(relationship.type());
        return sb.toString();
    }
}
