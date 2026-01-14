package com.people.domain;

public record Relationship(
        String id,
        String personId,
        String relatedPersonId,
        RelationshipType type
) {
}
