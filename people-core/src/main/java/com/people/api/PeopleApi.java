package com.people.api;

import com.people.domain.Address;
import com.people.domain.Employment;
import com.people.domain.Person;
import com.people.domain.Relationship;

import java.util.List;

public interface PeopleApi {
    Person createPerson(Person person);

    Person updatePerson(Person person);

    Person deletePerson(String id);

    Person getPerson(String id);

    List<Person> listPeople();

    Address createAddress(Address address);

    Address updateAddress(Address address);

    Address deleteAddress(String personId, String addressId);

    Address getAddress(String personId, String addressId);

    List<Address> listAddresses(String personId);

    List<Address> listAddressesFiltered(String street, String town, String state, String streetContains);

    Employment createEmployment(Employment employment);

    Employment updateEmployment(Employment employment);

    Employment deleteEmployment(String personId, String employmentId);

    Employment getEmployment(String personId, String employmentId);

    List<Employment> listEmployments(String personId);

    List<Employment> listAllEmployments();

    List<CompanySummary> listCompanies();

    Relationship createRelationship(Relationship relationship);

    Relationship updateRelationship(Relationship relationship);

    Relationship deleteRelationship(String personId, String relationshipId);

    Relationship getRelationship(String personId, String relationshipId);

    List<Relationship> listRelationships(String personId);
}
