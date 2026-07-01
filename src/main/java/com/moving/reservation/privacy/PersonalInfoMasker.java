package com.moving.reservation.privacy;

import org.springframework.util.StringUtils;

public final class PersonalInfoMasker {

    private PersonalInfoMasker() {
    }

    public static String maskName(String name) {
        if (!StringUtils.hasText(name)) {
            return "고객";
        }

        String trimmedName = name.trim();
        int firstCodePoint = trimmedName.codePointAt(0);
        return new String(Character.toChars(firstCodePoint)) + "**";
    }

    public static String maskReviewCustomerName(String name) {
        if (!StringUtils.hasText(name)) {
            return "이사 고객";
        }

        return maskName(name) + " 고객";
    }

    public static String maskPhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            return "";
        }

        String digits = phone.replaceAll("\\D", "");

        if (digits.length() >= 8) {
            int prefixLength = Math.min(3, digits.length() - 4);
            return digits.substring(0, prefixLength) + "-****-" + digits.substring(digits.length() - 4);
        }

        if (digits.length() > 4) {
            return "*".repeat(digits.length() - 4) + digits.substring(digits.length() - 4);
        }

        return "****";
    }

    public static String maskEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return null;
        }

        String trimmedEmail = email.trim();
        int atIndex = trimmedEmail.indexOf('@');

        if (atIndex <= 0 || atIndex == trimmedEmail.length() - 1) {
            return maskText(trimmedEmail);
        }

        return trimmedEmail.substring(0, 1) + "***" + trimmedEmail.substring(atIndex);
    }

    public static String maskAddress(String address) {
        if (!StringUtils.hasText(address)) {
            return "";
        }

        String[] parts = address.trim().split("\\s+");

        if (parts.length == 1) {
            return parts[0] + " ***";
        }

        return parts[0] + " " + parts[1] + " ***";
    }

    public static String maskContact(String contact) {
        if (!StringUtils.hasText(contact)) {
            return "";
        }

        return contact.contains("@") ? maskEmail(contact) : maskPhone(contact);
    }

    private static String maskText(String value) {
        if (value.length() <= 1) {
            return "*";
        }

        return value.substring(0, 1) + "***";
    }
}
