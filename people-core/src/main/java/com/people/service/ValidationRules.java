package com.people.service;

public final class ValidationRules {
    public static final int ID_MAX = 50;
    public static final int NAME_MAX = 100;
    public static final int COMPANY_NAME_MAX = 200;
    public static final int JOB_TITLE_MAX = 100;
    public static final int ADDRESS_MAX = 500;
    public static final int TOWN_MAX = 100;
    public static final int STATE_MAX = 50;
    public static final int DESCRIPTION_MAX = 500;
    public static final int PREFERRED_GENDER_OTHER_MAX = 50;

    public static final int MIN_ROOMS = 0;
    public static final int MAX_ROOMS = 100;

    public static final long MONTHLY_PAYMENT_MAX = 1_000_000L;
    public static final long RATE_OF_PAY_MAX = 1_000_000_000L;

    private ValidationRules() {
    }
}
