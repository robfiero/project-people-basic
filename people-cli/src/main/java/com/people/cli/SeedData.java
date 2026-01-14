package com.people.cli;

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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class SeedData {
    private SeedData() {
    }

    public static void populate(PeopleApi api) {
        List<Person> people = new ArrayList<>();
        String[] firstNames = {
                "Ada", "Grace", "Alan", "Katherine", "Edsger",
                "Barbara", "Donald", "Margaret", "Tim", "Linus",
                "Claude", "Jean", "Hedy", "Niklaus", "Dorothy"
        };
        String[] lastNames = {
                "Lovelace", "Hopper", "Turing", "Johnson", "Dijkstra",
                "Liskov", "Knuth", "Hamilton", "BernersLee", "Torvalds",
                "Shannon", "Bartik", "Lamarr", "Wirth", "Vaughan"
        };

        for (int i = 0; i < 15; i++) {
            Gender gender = (i % 3 == 0) ? Gender.NON_BINARY : (i % 2 == 0 ? Gender.MALE : Gender.FEMALE);
            PreferredGender preferredGender = PreferredGender.of(PreferredGenderType.values()[i % 3]);
            if (i % 7 == 0) {
                preferredGender = PreferredGender.other("other-" + (i + 1));
            }
            LocalDate dob = LocalDate.of(1970 + (i % 30), (i % 12) + 1, (i % 28) + 1);
            Person created = api.createPerson(new Person(null, firstNames[i], null, lastNames[i], dob, gender,
                    preferredGender, null));
            people.add(created);

            for (int a = 0; a < 3; a++) {
                AddressType type = AddressType.values()[a % AddressType.values().length];
                boolean owns = a == 0;
                boolean primary = a == 0;
                BigDecimal monthly = owns ? BigDecimal.ZERO : BigDecimal.valueOf(1200 + (a * 250));
                String town = (i % 2 == 0) ? "Springfield" : "Riverton";
                String state = (i % 3 == 0) ? "CA" : (i % 3 == 1 ? "NY" : "TX");
                Address address = new Address(null, created.id(),
                        (100 + i) + " Main St Apt " + (a + 1),
                        town,
                        state,
                        type,
                        "Residence " + (a + 1) + " for " + created.firstName(),
                        owns,
                        primary,
                        monthly,
                        1 + (a % 4),
                        1 + (a % 2));
                api.createAddress(address);
            }

            for (int e = 0; e < 2; e++) {
                PayType payType = (e % 2 == 0) ? PayType.SALARY : PayType.HOURLY;
                BigDecimal rate = (payType == PayType.SALARY)
                        ? BigDecimal.valueOf(75000 + (i * 1000L))
                        : BigDecimal.valueOf(40 + i);
                boolean currentEmployer = e == 0;
                LocalDate startDate = LocalDate.of(2010 + (i % 10), (e + 3), (i % 28) + 1);
                LocalDate endDate = currentEmployer ? null : startDate.plusYears(3);
                Employment employment = new Employment(null, created.id(),
                        "Company " + (e + 1) + " for " + created.firstName(),
                        "Example employer " + (e + 1),
                        (200 + i) + " Business Rd",
                        e == 0 ? "Engineer" : "Analyst",
                        payType,
                        rate,
                        currentEmployer,
                        startDate,
                        endDate);
                api.createEmployment(employment);
            }
        }

        RelationshipType[] types = {
                RelationshipType.SPOUSE,
                RelationshipType.CHILD,
                RelationshipType.AUNT,
                RelationshipType.UNCLE,
                RelationshipType.NIECE,
                RelationshipType.NEPHEW,
                RelationshipType.COUSIN
        };
        for (int i = 0; i < types.length; i++) {
            Person a = people.get(i % people.size());
            Person b = people.get((i + 1) % people.size());
            Relationship relationship = new Relationship(null, a.id(), b.id(), types[i]);
            api.createRelationship(relationship);
        }
    }
}
