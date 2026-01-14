package com.people.service;

import com.people.domain.Address;
import com.people.domain.Employment;
import com.people.domain.Person;
import com.people.domain.PreferredGender;
import com.people.domain.PreferredGenderType;
import com.people.domain.Relationship;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public final class Validators {
    private Validators() {
    }

    public static void validatePerson(Person person) {
        Objects.requireNonNull(person, "person");
        requireId(person.id(), "person id");
        requireNonBlank(person.firstName(), "first name", ValidationRules.NAME_MAX);
        requireOptional(person.middleName(), "middle name", ValidationRules.NAME_MAX);
        requireNonBlank(person.lastName(), "last name", ValidationRules.NAME_MAX);
        requireDate(person.dateOfBirth(), "date of birth");
        if (person.gender() == null) {
            throw new IllegalArgumentException("gender must be provided");
        }
        validatePreferredGender(person.preferredGender());
        requireOptional(person.picturePath(), "picture", ValidationRules.ADDRESS_MAX);
    }

    public static void validateAddress(Address address) {
        Objects.requireNonNull(address, "address");
        requireId(address.id(), "address id");
        requireId(address.personId(), "person id");
        requireNonBlank(address.address(), "street", ValidationRules.ADDRESS_MAX);
        requireNonBlank(address.town(), "town", ValidationRules.TOWN_MAX);
        requireNonBlank(address.state(), "state", ValidationRules.STATE_MAX);
        Objects.requireNonNull(address.type(), "address type");
        requireOptional(address.description(), "description", ValidationRules.DESCRIPTION_MAX);
        requireMoney(address.monthlyPayment(), "monthly payment", ValidationRules.MONTHLY_PAYMENT_MAX);
        requireRange(address.bedrooms(), "bedrooms", ValidationRules.MIN_ROOMS, ValidationRules.MAX_ROOMS);
        requireRange(address.bathrooms(), "bathrooms", ValidationRules.MIN_ROOMS, ValidationRules.MAX_ROOMS);
    }

    public static void validateEmployment(Employment employment) {
        Objects.requireNonNull(employment, "employment");
        requireId(employment.id(), "employment id");
        requireId(employment.personId(), "person id");
        requireNonBlank(employment.name(), "company name", ValidationRules.COMPANY_NAME_MAX);
        requireOptional(employment.description(), "company description", ValidationRules.DESCRIPTION_MAX);
        requireNonBlank(employment.address(), "employment address", ValidationRules.ADDRESS_MAX);
        requireNonBlank(employment.jobTitle(), "job title", ValidationRules.JOB_TITLE_MAX);
        if (employment.payType() == null) {
            throw new IllegalArgumentException("pay type must be provided");
        }
        requireMoney(employment.rateOfPay(), "rate of pay", ValidationRules.RATE_OF_PAY_MAX);
        requireDate(employment.startDate(), "start date");
        if (employment.endDate() != null && employment.endDate().isBefore(employment.startDate())) {
            throw new IllegalArgumentException("end date must be on or after start date");
        }
        if (employment.currentEmployer() && employment.endDate() != null) {
            throw new IllegalArgumentException("current employer must not have an end date");
        }
    }

    public static void validateRelationship(Relationship relationship) {
        Objects.requireNonNull(relationship, "relationship");
        requireId(relationship.id(), "relationship id");
        requireId(relationship.personId(), "person id");
        requireId(relationship.relatedPersonId(), "related person id");
        if (relationship.type() == null) {
            throw new IllegalArgumentException("relationship type must be provided");
        }
        if (relationship.personId().equals(relationship.relatedPersonId())) {
            throw new IllegalArgumentException("related person id must be different from person id");
        }
    }

    private static void validatePreferredGender(PreferredGender preferredGender) {
        if (preferredGender == null) {
            throw new IllegalArgumentException("preferred gender must be provided");
        }
        if (preferredGender.type() == PreferredGenderType.OTHER) {
            requireNonBlank(preferredGender.otherLabel(), "preferred gender other label",
                    ValidationRules.PREFERRED_GENDER_OTHER_MAX);
        }
    }

    private static void requireId(String value, String label) {
        requireNonBlank(value, label, ValidationRules.ID_MAX);
    }

    private static void requireNonBlank(String value, String label, int maxLen) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " must be provided");
        }
        if (value.length() > maxLen) {
            throw new IllegalArgumentException(label + " must be at most " + maxLen + " characters");
        }
    }

    private static void requireOptional(String value, String label, int maxLen) {
        if (value == null) {
            return;
        }
        if (value.isBlank()) {
            throw new IllegalArgumentException(label + " must not be blank");
        }
        if (value.length() > maxLen) {
            throw new IllegalArgumentException(label + " must be at most " + maxLen + " characters");
        }
    }

    private static void requireDate(LocalDate date, String label) {
        if (date == null) {
            throw new IllegalArgumentException(label + " must be provided");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(label + " must not be in the future");
        }
    }

    private static void requireRange(int value, String label, int min, int max) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(label + " must be between " + min + " and " + max);
        }
    }

    private static void requireMoney(BigDecimal value, String label, long max) {
        if (value == null) {
            throw new IllegalArgumentException(label + " must be provided");
        }
        if (value.scale() > 2) {
            throw new IllegalArgumentException(label + " must have at most 2 decimal places");
        }
        if (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(BigDecimal.valueOf(max)) > 0) {
            throw new IllegalArgumentException(label + " must be between 0 and " + max);
        }
    }
}
