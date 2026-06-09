package ru.sibmobile.model;

public enum CarType {
    SOLARIS("Hyundai Solaris (2020–2023)", 4499, 2.99),
    GEELY_XINGYUE("Geely Xingyue S (Xingyue L)", 5699, 5.79),
    NISSAN_QASHQAI("Nissan Qashqai II (J11, рестайлинг 2017–2021)", 4999, 3.59);

    private final String displayName;
    private final double basePrepayment;
    private final double basePerKm;

    CarType(String displayName, double basePrepayment, double basePerKm) {
        this.displayName = displayName;
        this.basePrepayment = basePrepayment;
        this.basePerKm = basePerKm;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getBasePrepayment() {
        return basePrepayment;
    }

    public double getBasePerKm() {
        return basePerKm;
    }
}


