package com.people.repo;

import com.people.domain.Person;

import java.util.List;
import java.util.Optional;

public interface PersonRepository {
    void create(Person person);

    void update(Person person);

    Person delete(String id);

    Optional<Person> find(String id);

    List<Person> list();

    boolean exists(String id);
}
