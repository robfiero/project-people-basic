package com.people.repo;

import com.people.domain.Relationship;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryRelationshipRepository implements RelationshipRepository {
    private final Map<String, Map<String, Relationship>> relationshipsByPerson = new ConcurrentHashMap<>();

    @Override
    public void create(Relationship relationship) {
        relationshipsByPerson
                .computeIfAbsent(relationship.personId(), key -> new ConcurrentHashMap<>())
                .put(relationship.id(), relationship);
    }

    @Override
    public void update(Relationship relationship) {
        relationshipsByPerson
                .computeIfAbsent(relationship.personId(), key -> new ConcurrentHashMap<>())
                .put(relationship.id(), relationship);
    }

    @Override
    public Relationship delete(String personId, String relationshipId) {
        Map<String, Relationship> relationships = relationshipsByPerson.get(personId);
        if (relationships == null) {
            return null;
        }
        return relationships.remove(relationshipId);
    }

    @Override
    public Optional<Relationship> find(String personId, String relationshipId) {
        Map<String, Relationship> relationships = relationshipsByPerson.get(personId);
        if (relationships == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(relationships.get(relationshipId));
    }

    @Override
    public List<Relationship> list(String personId) {
        Map<String, Relationship> relationships = relationshipsByPerson.get(personId);
        if (relationships == null) {
            return List.of();
        }
        return new ArrayList<>(relationships.values());
    }

    @Override
    public void deleteAllForPerson(String personId) {
        relationshipsByPerson.remove(personId);
    }

    @Override
    public void deleteAllRelatedTo(String personId) {
        for (Map<String, Relationship> relationships : relationshipsByPerson.values()) {
            relationships.values().removeIf(rel -> rel.relatedPersonId().equals(personId));
        }
    }

    @Override
    public boolean exists(String personId, String relationshipId) {
        Map<String, Relationship> relationships = relationshipsByPerson.get(personId);
        return relationships != null && relationships.containsKey(relationshipId);
    }
}
