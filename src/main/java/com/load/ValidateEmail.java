package com.load;

import java.util.regex.Pattern;

public class ValidateEmail {
    private final Pattern pattern;

    public ValidateEmail() {
        pattern = Pattern.compile("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$");
    }

    public boolean validate(String email) {
        return pattern.matcher(email).matches();
    }
}
