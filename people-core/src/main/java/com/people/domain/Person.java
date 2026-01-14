package com.people.domain;

import java.time.LocalDate;

public record Person(
        String id,
        String firstName,
        String middleName,
        String lastName,
        LocalDate dateOfBirth,
        Gender gender,
        PreferredGender preferredGender,
        String picturePath
) {
}
