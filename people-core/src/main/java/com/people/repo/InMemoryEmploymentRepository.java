package com.people.repo;

import com.people.domain.Employment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryEmploymentRepository implements EmploymentRepository {
    private final Map<String, Map<String, Employment>> employmentByPerson = new ConcurrentHashMap<>();

    @Override
    public void create(Employment employment) {
        employmentByPerson
                .computeIfAbsent(employment.personId(), key -> new ConcurrentHashMap<>())
                .put(employment.id(), employment);
    }

    @Override
    public void update(Employment employment) {
        employmentByPerson
                .computeIfAbsent(employment.personId(), key -> new ConcurrentHashMap<>())
                .put(employment.id(), employment);
    }

    @Override
    public Employment delete(String personId, String employmentId) {
        Map<String, Employment> employment = employmentByPerson.get(personId);
        if (employment == null) {
            return null;
        }
        return employment.remove(employmentId);
    }

    @Override
    public Optional<Employment> find(String personId, String employmentId) {
        Map<String, Employment> employment = employmentByPerson.get(personId);
        if (employment == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(employment.get(employmentId));
    }

    @Override
    public List<Employment> list(String personId) {
        Map<String, Employment> employment = employmentByPerson.get(personId);
        if (employment == null) {
            return List.of();
        }
        return new ArrayList<>(employment.values());
    }

    @Override
    public List<Employment> listAll() {
        List<Employment> all = new ArrayList<>();
        for (Map<String, Employment> employment : employmentByPerson.values()) {
            all.addAll(employment.values());
        }
        return all;
    }

    @Override
    public void deleteAllForPerson(String personId) {
        employmentByPerson.remove(personId);
    }

    @Override
    public boolean exists(String personId, String employmentId) {
        Map<String, Employment> employment = employmentByPerson.get(personId);
        return employment != null && employment.containsKey(employmentId);
    }
}
