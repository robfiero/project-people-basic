package com.people.api;

public record CompanySummary(
        String name,
        String address,
        int employeeCount
) {
}
