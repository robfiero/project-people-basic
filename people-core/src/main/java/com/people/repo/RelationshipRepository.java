package com.people.repo;

import com.people.domain.Relationship;

import java.util.List;
import java.util.Optional;

public interface RelationshipRepository {
    void create(Relationship relationship);

    void update(Relationship relationship);

    Relationship delete(String personId, String relationshipId);

    Optional<Relationship> find(String personId, String relationshipId);

    List<Relationship> list(String personId);

    void deleteAllForPerson(String personId);

    void deleteAllRelatedTo(String personId);

    boolean exists(String personId, String relationshipId);
}
