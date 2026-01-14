package com.people.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Employment(
        String id,
        String personId,
        String name,
        String description,
        String address,
        String jobTitle,
        PayType payType,
        BigDecimal rateOfPay,
        boolean currentEmployer,
        LocalDate startDate,
        LocalDate endDate
) {
}
