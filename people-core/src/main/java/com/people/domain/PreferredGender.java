package com.people.domain;

import java.util.Objects;

public final class PreferredGender {
    private final PreferredGenderType type;
    private final String otherLabel;

    private PreferredGender(PreferredGenderType type, String otherLabel) {
        this.type = Objects.requireNonNull(type, "type");
        this.otherLabel = otherLabel;
    }

    public static PreferredGender of(PreferredGenderType type) {
        return new PreferredGender(type, null);
    }

    public static PreferredGender other(String label) {
        return new PreferredGender(PreferredGenderType.OTHER, label);
    }

    public PreferredGenderType type() {
        return type;
    }

    public String otherLabel() {
        return otherLabel;
    }

    @Override
    public String toString() {
        if (type == PreferredGenderType.OTHER) {
            return "OTHER(" + otherLabel + ")";
        }
        return type.name();
    }
}
