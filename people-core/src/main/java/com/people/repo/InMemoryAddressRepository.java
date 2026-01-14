package com.people.repo;

import com.people.domain.Address;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryAddressRepository implements AddressRepository {
    private final Map<String, Map<String, Address>> addressesByPerson = new ConcurrentHashMap<>();

    @Override
    public void create(Address address) {
        addressesByPerson
                .computeIfAbsent(address.personId(), key -> new ConcurrentHashMap<>())
                .put(address.id(), address);
    }

    @Override
    public void update(Address address) {
        addressesByPerson
                .computeIfAbsent(address.personId(), key -> new ConcurrentHashMap<>())
                .put(address.id(), address);
    }

    @Override
    public Address delete(String personId, String addressId) {
        Map<String, Address> addresses = addressesByPerson.get(personId);
        if (addresses == null) {
            return null;
        }
        return addresses.remove(addressId);
    }

    @Override
    public Optional<Address> find(String personId, String addressId) {
        Map<String, Address> addresses = addressesByPerson.get(personId);
        if (addresses == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(addresses.get(addressId));
    }

    @Override
    public List<Address> list(String personId) {
        Map<String, Address> addresses = addressesByPerson.get(personId);
        if (addresses == null) {
            return List.of();
        }
        return new ArrayList<>(addresses.values());
    }

    @Override
    public List<Address> listAll() {
        List<Address> all = new ArrayList<>();
        for (Map<String, Address> addresses : addressesByPerson.values()) {
            all.addAll(addresses.values());
        }
        return all;
    }

    @Override
    public void deleteAllForPerson(String personId) {
        addressesByPerson.remove(personId);
    }

    @Override
    public boolean exists(String personId, String addressId) {
        Map<String, Address> addresses = addressesByPerson.get(personId);
        return addresses != null && addresses.containsKey(addressId);
    }
}
