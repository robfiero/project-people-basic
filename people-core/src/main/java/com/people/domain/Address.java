package com.people.domain;

import java.math.BigDecimal;

public record Address(
        String id,
        String personId,
        String address,
        String town,
        String state,
        AddressType type,
        String description,
        boolean owns,
        boolean primary,
        BigDecimal monthlyPayment,
        int bedrooms,
        int bathrooms
) {
}
