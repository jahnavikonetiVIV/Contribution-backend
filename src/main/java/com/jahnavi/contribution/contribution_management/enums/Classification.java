package com.jahnavi.contribution.contribution_management.enums;

import java.util.Optional;

public enum Classification {
    PROPER,
    IMPROPER;

    public static Optional<Classification> from(String classification) {
        if (classification == null || classification.trim().isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(
                    Classification.valueOf(classification.trim().toUpperCase())
            );
        } catch (Exception ex) {
            return Optional.empty();
        }
    }
}
