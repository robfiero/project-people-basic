package com.people.repo;

import com.people.domain.Person;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryPersonRepository implements PersonRepository {
    private final Map<String, Person> people = new ConcurrentHashMap<>();

    @Override
    public void create(Person person) {
        people.put(person.id(), person);
    }

    @Override
    public void update(Person person) {
        people.put(person.id(), person);
    }

    @Override
    public Person delete(String id) {
        return people.remove(id);
    }

    @Override
    public Optional<Person> find(String id) {
        return Optional.ofNullable(people.get(id));
    }

    @Override
    public List<Person> list() {
        return new ArrayList<>(people.values());
    }

    @Override
    public boolean exists(String id) {
        return people.containsKey(id);
    }
}
