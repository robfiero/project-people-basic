package com.people.repo;

import com.people.domain.Address;

import java.util.List;
import java.util.Optional;

public interface AddressRepository {
    void create(Address address);

    void update(Address address);

    Address delete(String personId, String addressId);

    Optional<Address> find(String personId, String addressId);

    List<Address> list(String personId);

    List<Address> listAll();

    void deleteAllForPerson(String personId);

    boolean exists(String personId, String addressId);
}
