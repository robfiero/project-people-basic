package com.people.service;

import com.people.api.CompanySummary;
import com.people.api.PeopleApi;
import com.people.domain.Address;
import com.people.domain.Employment;
import com.people.domain.Person;
import com.people.domain.Relationship;
import com.people.repo.AddressRepository;
import com.people.repo.EmploymentRepository;
import com.people.repo.InMemoryAddressRepository;
import com.people.repo.InMemoryEmploymentRepository;
import com.people.repo.InMemoryPersonRepository;
import com.people.repo.InMemoryRelationshipRepository;
import com.people.repo.PersonRepository;
import com.people.repo.RelationshipRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public final class PeopleService implements PeopleApi {
    private final PersonRepository personRepository;
    private final AddressRepository addressRepository;
    private final EmploymentRepository employmentRepository;
    private final RelationshipRepository relationshipRepository;

    public PeopleService(PersonRepository personRepository,
                         AddressRepository addressRepository,
                         EmploymentRepository employmentRepository,
                         RelationshipRepository relationshipRepository) {
        this.personRepository = personRepository;
        this.addressRepository = addressRepository;
        this.employmentRepository = employmentRepository;
        this.relationshipRepository = relationshipRepository;
    }

    public static PeopleService createInMemory() {
        return new PeopleService(
                new InMemoryPersonRepository(),
                new InMemoryAddressRepository(),
                new InMemoryEmploymentRepository(),
                new InMemoryRelationshipRepository()
        );
    }

    @Override
    public Person createPerson(Person person) {
        rejectProvidedId(person.id(), "person id");
        Person created = new Person(generateId(), person.firstName(), person.middleName(), person.lastName(),
                person.dateOfBirth(), person.gender(), person.preferredGender(), person.picturePath());
        Validators.validatePerson(created);
        if (personRepository.exists(created.id())) {
            throw new IllegalArgumentException("person id already exists");
        }
        personRepository.create(created);
        return created;
    }

    @Override
    public Person updatePerson(Person person) {
        Validators.validatePerson(person);
        requirePersonExists(person.id());
        personRepository.update(person);
        return person;
    }

    @Override
    public Person deletePerson(String id) {
        requirePersonExists(id);
        addressRepository.deleteAllForPerson(id);
        employmentRepository.deleteAllForPerson(id);
        relationshipRepository.deleteAllForPerson(id);
        relationshipRepository.deleteAllRelatedTo(id);
        return personRepository.delete(id);
    }

    @Override
    public Person getPerson(String id) {
        return personRepository.find(id)
                .orElseThrow(() -> new IllegalArgumentException("person not found"));
    }

    @Override
    public List<Person> listPeople() {
        return personRepository.list();
    }

    @Override
    public Address createAddress(Address address) {
        rejectProvidedId(address.id(), "address id");
        Address created = new Address(generateId(), address.personId(), address.address(), address.town(),
                address.state(), address.type(), address.description(), address.owns(), address.primary(),
                address.monthlyPayment(), address.bedrooms(),
                address.bathrooms());
        Validators.validateAddress(created);
        requirePersonExists(address.personId());
        if (addressRepository.exists(address.personId(), created.id())) {
            throw new IllegalArgumentException("address id already exists for person");
        }
        addressRepository.create(created);
        return created;
    }

    @Override
    public Address updateAddress(Address address) {
        Validators.validateAddress(address);
        requirePersonExists(address.personId());
        requireAddressExists(address.personId(), address.id());
        addressRepository.update(address);
        return address;
    }

    @Override
    public Address deleteAddress(String personId, String addressId) {
        requirePersonExists(personId);
        requireAddressExists(personId, addressId);
        return addressRepository.delete(personId, addressId);
    }

    @Override
    public Address getAddress(String personId, String addressId) {
        requirePersonExists(personId);
        return addressRepository.find(personId, addressId)
                .orElseThrow(() -> new IllegalArgumentException("address not found"));
    }

    @Override
    public List<Address> listAddresses(String personId) {
        requirePersonExists(personId);
        return addressRepository.list(personId);
    }

    @Override
    public List<Address> listAddressesFiltered(String street, String town, String state, String streetContains) {
        return filterAddresses(addressRepository.listAll(), street, town, state, streetContains);
    }

    @Override
    public Employment createEmployment(Employment employment) {
        rejectProvidedId(employment.id(), "employment id");
        Employment created = new Employment(generateId(), employment.personId(), employment.name(),
                employment.description(), employment.address(), employment.jobTitle(), employment.payType(),
                employment.rateOfPay(), employment.currentEmployer(), employment.startDate(), employment.endDate());
        Validators.validateEmployment(created);
        requirePersonExists(employment.personId());
        if (employmentRepository.exists(employment.personId(), created.id())) {
            throw new IllegalArgumentException("employment id already exists for person");
        }
        employmentRepository.create(created);
        return created;
    }

    @Override
    public Employment updateEmployment(Employment employment) {
        Validators.validateEmployment(employment);
        requirePersonExists(employment.personId());
        requireEmploymentExists(employment.personId(), employment.id());
        employmentRepository.update(employment);
        return employment;
    }

    @Override
    public Employment deleteEmployment(String personId, String employmentId) {
        requirePersonExists(personId);
        requireEmploymentExists(personId, employmentId);
        return employmentRepository.delete(personId, employmentId);
    }

    @Override
    public Employment getEmployment(String personId, String employmentId) {
        requirePersonExists(personId);
        return employmentRepository.find(personId, employmentId)
                .orElseThrow(() -> new IllegalArgumentException("employment not found"));
    }

    @Override
    public List<Employment> listEmployments(String personId) {
        requirePersonExists(personId);
        return employmentRepository.list(personId);
    }

    @Override
    public List<Employment> listAllEmployments() {
        return employmentRepository.listAll();
    }

    @Override
    public List<CompanySummary> listCompanies() {
        Map<String, CompanySummaryBuilder> summaryByKey = new TreeMap<>();
        for (Employment employment : employmentRepository.listAll()) {
            String key = employment.name().toLowerCase() + "|" + employment.address().toLowerCase();
            summaryByKey.computeIfAbsent(key, k -> new CompanySummaryBuilder(employment.name(), employment.address()))
                    .addEmployee(employment.personId());
        }
        List<CompanySummary> summaries = new ArrayList<>();
        for (CompanySummaryBuilder builder : summaryByKey.values()) {
            summaries.add(builder.build());
        }
        summaries.sort(Comparator.comparing(CompanySummary::name));
        return summaries;
    }

    @Override
    public Relationship createRelationship(Relationship relationship) {
        rejectProvidedId(relationship.id(), "relationship id");
        Relationship created = new Relationship(generateId(), relationship.personId(),
                relationship.relatedPersonId(), relationship.type());
        Validators.validateRelationship(created);
        requirePersonExists(relationship.personId());
        requirePersonExists(relationship.relatedPersonId());
        if (relationshipRepository.exists(relationship.personId(), created.id())) {
            throw new IllegalArgumentException("relationship id already exists for person");
        }
        relationshipRepository.create(created);
        return created;
    }

    @Override
    public Relationship updateRelationship(Relationship relationship) {
        Validators.validateRelationship(relationship);
        requirePersonExists(relationship.personId());
        requirePersonExists(relationship.relatedPersonId());
        requireRelationshipExists(relationship.personId(), relationship.id());
        relationshipRepository.update(relationship);
        return relationship;
    }

    @Override
    public Relationship deleteRelationship(String personId, String relationshipId) {
        requirePersonExists(personId);
        requireRelationshipExists(personId, relationshipId);
        return relationshipRepository.delete(personId, relationshipId);
    }

    @Override
    public Relationship getRelationship(String personId, String relationshipId) {
        requirePersonExists(personId);
        return relationshipRepository.find(personId, relationshipId)
                .orElseThrow(() -> new IllegalArgumentException("relationship not found"));
    }

    @Override
    public List<Relationship> listRelationships(String personId) {
        requirePersonExists(personId);
        return relationshipRepository.list(personId);
    }

    private void requirePersonExists(String id) {
        if (!personRepository.exists(id)) {
            throw new IllegalArgumentException("person not found");
        }
    }

    private void requireAddressExists(String personId, String addressId) {
        if (!addressRepository.exists(personId, addressId)) {
            throw new IllegalArgumentException("address not found");
        }
    }

    private void requireEmploymentExists(String personId, String employmentId) {
        if (!employmentRepository.exists(personId, employmentId)) {
            throw new IllegalArgumentException("employment not found");
        }
    }

    private void requireRelationshipExists(String personId, String relationshipId) {
        if (!relationshipRepository.exists(personId, relationshipId)) {
            throw new IllegalArgumentException("relationship not found");
        }
    }

    private List<Address> filterAddresses(List<Address> addresses, String street, String town, String state,
                                          String streetContains) {
        String streetFilter = normalizeFilter(street);
        String townFilter = normalizeFilter(town);
        String stateFilter = normalizeFilter(state);
        String streetContainsFilter = normalizeFilter(streetContains);
        if (streetFilter == null && townFilter == null && stateFilter == null && streetContainsFilter == null) {
            return addresses;
        }
        List<Address> filtered = new ArrayList<>();
        for (Address address : addresses) {
            if (streetFilter != null && !address.address().equalsIgnoreCase(streetFilter)) {
                continue;
            }
            if (streetContainsFilter != null
                    && !address.address().toLowerCase().contains(streetContainsFilter.toLowerCase())) {
                continue;
            }
            if (townFilter != null && !address.town().equalsIgnoreCase(townFilter)) {
                continue;
            }
            if (stateFilter != null && !address.state().equalsIgnoreCase(stateFilter)) {
                continue;
            }
            filtered.add(address);
        }
        return filtered;
    }

    private String normalizeFilter(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String generateId() {
        return UUID.randomUUID().toString();
    }

    private void rejectProvidedId(String id, String label) {
        if (id != null && !id.isBlank()) {
            throw new IllegalArgumentException(label + " must not be provided");
        }
    }

    private static final class CompanySummaryBuilder {
        private final String name;
        private final String address;
        private final Map<String, Boolean> employeeIds = new TreeMap<>();

        private CompanySummaryBuilder(String name, String address) {
            this.name = name;
            this.address = address;
        }

        private void addEmployee(String personId) {
            employeeIds.put(personId, Boolean.TRUE);
        }

        private CompanySummary build() {
            return new CompanySummary(name, address, employeeIds.size());
        }
    }
}
