package com.people.tests;

import com.people.api.PeopleApi;
import com.people.domain.Address;
import com.people.domain.AddressType;
import com.people.domain.Employment;
import com.people.domain.Gender;
import com.people.domain.PayType;
import com.people.domain.Person;
import com.people.domain.PreferredGender;
import com.people.domain.PreferredGenderType;
import com.people.domain.Relationship;
import com.people.domain.RelationshipType;
import com.people.service.PeopleService;
import com.people.service.ValidationRules;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PeopleServiceTest {

    @Test
    void createAndGetPerson() {
        PeopleApi api = PeopleService.createInMemory();
        Person person = samplePerson(null);

        Person created = api.createPerson(person);

        Person fetched = api.getPerson(created.id());
        assertEquals(created.id(), fetched.id());
        assertEquals("Ada", fetched.firstName());
    }

    @Test
    void createPersonRejectsBlankFirstName() {
        PeopleApi api = PeopleService.createInMemory();
        Person person = new Person(null, " ", null, "Lovelace", LocalDate.of(1815, 12, 10),
                Gender.FEMALE, PreferredGender.of(PreferredGenderType.FEMALE), null);

        assertThrows(IllegalArgumentException.class, () -> api.createPerson(person));
    }

    @Test
    void createPersonRejectsLongLastName() {
        PeopleApi api = PeopleService.createInMemory();
        String longName = "x".repeat(ValidationRules.NAME_MAX + 1);
        Person person = new Person(null, "Ada", null, longName, LocalDate.of(1815, 12, 10),
                Gender.FEMALE, PreferredGender.of(PreferredGenderType.FEMALE), null);

        assertThrows(IllegalArgumentException.class, () -> api.createPerson(person));
    }

    @Test
    void createPersonRejectsNullGender() {
        PeopleApi api = PeopleService.createInMemory();
        Person person = new Person(null, "Ada", null, "Lovelace", LocalDate.of(1815, 12, 10),
                null, PreferredGender.of(PreferredGenderType.FEMALE), null);

        assertThrows(IllegalArgumentException.class, () -> api.createPerson(person));
    }

    @Test
    void updateMissingPersonFails() {
        PeopleApi api = PeopleService.createInMemory();
        assertThrows(IllegalArgumentException.class, () -> api.updatePerson(samplePerson("p1")));
    }

    @Test
    void addressValidationChecksRanges() {
        PeopleApi api = PeopleService.createInMemory();
        Person person = api.createPerson(samplePerson(null));

        Address badAddress = new Address(null, person.id(), "123 Test St", "Springfield", "MA", AddressType.HOUSE,
                null, true, true, BigDecimal.valueOf(-1), 2, 1);

        assertThrows(IllegalArgumentException.class, () -> api.createAddress(badAddress));
    }

    @Test
    void addressRejectsTooManyBedrooms() {
        PeopleApi api = PeopleService.createInMemory();
        Person person = api.createPerson(samplePerson(null));

        Address badAddress = new Address(null, person.id(), "123 Test St", "Springfield", "MA",
                AddressType.HOUSE, null, true, true, BigDecimal.valueOf(1000), ValidationRules.MAX_ROOMS + 1, 1);

        assertThrows(IllegalArgumentException.class, () -> api.createAddress(badAddress));
    }

    @Test
    void addressRejectsLongAddress() {
        PeopleApi api = PeopleService.createInMemory();
        Person person = api.createPerson(samplePerson(null));
        String longAddress = "a".repeat(ValidationRules.ADDRESS_MAX + 1);

        Address badAddress = new Address(null, person.id(), longAddress, "Springfield", "MA", AddressType.HOUSE,
                null, true, true, BigDecimal.valueOf(1000), 2, 1);

        assertThrows(IllegalArgumentException.class, () -> api.createAddress(badAddress));
    }

    @Test
    void employmentRequiresValidRate() {
        PeopleApi api = PeopleService.createInMemory();
        Person person = api.createPerson(samplePerson(null));

        Employment badEmployment = new Employment(null, person.id(), "Acme", null,
                "1 Main St", "Engineer", PayType.SALARY, BigDecimal.valueOf(-10),
                false, LocalDate.of(2020, 1, 1), LocalDate.of(2022, 1, 1));

        assertThrows(IllegalArgumentException.class, () -> api.createEmployment(badEmployment));
    }

    @Test
    void employmentRejectsNullRate() {
        PeopleApi api = PeopleService.createInMemory();
        Person person = api.createPerson(samplePerson(null));

        Employment badEmployment = new Employment(null, person.id(), "Acme", null,
                "1 Main St", "Engineer", PayType.SALARY, null,
                false, LocalDate.of(2020, 1, 1), LocalDate.of(2022, 1, 1));

        assertThrows(IllegalArgumentException.class, () -> api.createEmployment(badEmployment));
    }

    @Test
    void relationshipRequiresExistingRelatedPerson() {
        PeopleApi api = PeopleService.createInMemory();
        Person person = api.createPerson(samplePerson(null));

        Relationship rel = new Relationship(null, person.id(), "p2", RelationshipType.SPOUSE);

        assertThrows(IllegalArgumentException.class, () -> api.createRelationship(rel));
    }

    @Test
    void relationshipCreateAndList() {
        PeopleApi api = PeopleService.createInMemory();
        Person person1 = api.createPerson(samplePerson(null));
        Person person2 = api.createPerson(samplePerson(null));

        Relationship rel = new Relationship(null, person1.id(), person2.id(), RelationshipType.SPOUSE);
        Relationship created = api.createRelationship(rel);

        assertEquals(1, api.listRelationships(person1.id()).size());
        assertEquals(RelationshipType.SPOUSE, api.getRelationship(person1.id(), created.id()).type());
    }

    @Test
    void preferredGenderOtherRequiresLabel() {
        PeopleApi api = PeopleService.createInMemory();
        Person person = new Person(null, "Ada", null, "Lovelace", LocalDate.of(1815, 12, 10),
                Gender.FEMALE, PreferredGender.of(PreferredGenderType.OTHER), null);

        assertThrows(IllegalArgumentException.class, () -> api.createPerson(person));
    }

    @Test
    void deletePersonCascadesRelatedData() {
        PeopleApi api = PeopleService.createInMemory();
        Person person1 = api.createPerson(samplePerson(null));
        Person person2 = api.createPerson(samplePerson(null));

        api.createAddress(new Address(null, person1.id(), "123 Test St", "Springfield", "MA",
                AddressType.HOUSE, null, true, true, BigDecimal.valueOf(1200), 2, 1));
        api.createEmployment(new Employment(null, person1.id(), "Acme", null,
                "1 Main St", "Engineer", PayType.SALARY, BigDecimal.valueOf(90000),
                true, LocalDate.of(2019, 1, 1), null));
        api.createRelationship(new Relationship(null, person2.id(), person1.id(), RelationshipType.COUSIN));

        api.deletePerson(person1.id());

        assertThrows(IllegalArgumentException.class, () -> api.listAddresses(person1.id()));
        assertEquals(0, api.listRelationships(person2.id()).size());
    }

    @Test
    void createEmploymentAndGet() {
        PeopleApi api = PeopleService.createInMemory();
        Person person = api.createPerson(samplePerson(null));

        Employment employment = new Employment(null, person.id(), "Acme", "Widgets",
                "1 Main St", "Engineer", PayType.HOURLY, BigDecimal.valueOf(45.50),
                true, LocalDate.of(2021, 5, 1), null);

        Employment created = api.createEmployment(employment);

        Employment fetched = api.getEmployment(person.id(), created.id());
        assertNotNull(fetched);
        assertEquals(PayType.HOURLY, fetched.payType());
    }

    private Person samplePerson(String id) {
        return new Person(id, "Ada", null, "Lovelace", LocalDate.of(1815, 12, 10),
                Gender.FEMALE, PreferredGender.of(PreferredGenderType.FEMALE), null);
    }
}
