package com.rahulrathore.util;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class PasswordEncoderUtil {

    public static String encodePassword(String password) {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray());
    }

    public static boolean matches(String rawPassword, String encodedPassword) {
        BCrypt.Result result = BCrypt.verifyer().verify(rawPassword.toCharArray(), encodedPassword);
        return result.verified;
    }
}
