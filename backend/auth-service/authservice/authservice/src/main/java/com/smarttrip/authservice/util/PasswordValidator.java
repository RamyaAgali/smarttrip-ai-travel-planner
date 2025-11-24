package com.smarttrip.authservice.util;

import java.util.regex.Pattern;

public class PasswordValidator {

    // At least 8 chars, one uppercase, one lowercase, one number, one special char
    private static final String PASSWORD_PATTERN =
            "^(?=.*[0-9])" +          // at least 1 digit
            "(?=.*[a-z])" +          // at least 1 lowercase
            "(?=.*[A-Z])" +          // at least 1 uppercase
            "(?=.*[@#$%^&+=!])" +    // at least 1 special char
            "(?=\\S+$).{8,}$";       // no whitespace + min 8 chars

    private static final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);

    public static boolean isValid(String password) {
        return password != null && pattern.matcher(password).matches();
    }
}
