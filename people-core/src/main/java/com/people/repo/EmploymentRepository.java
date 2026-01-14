package com.people.repo;

import com.people.domain.Employment;

import java.util.List;
import java.util.Optional;

public interface EmploymentRepository {
    void create(Employment employment);

    void update(Employment employment);

    Employment delete(String personId, String employmentId);

    Optional<Employment> find(String personId, String employmentId);

    List<Employment> list(String personId);

    List<Employment> listAll();

    void deleteAllForPerson(String personId);

    boolean exists(String personId, String employmentId);
}
