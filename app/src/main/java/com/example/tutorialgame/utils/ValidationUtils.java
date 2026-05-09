package com.example.tutorialgame.utils;

import android.util.Patterns;

public class ValidationUtils {
    private ValidationUtils() {}
    public static boolean isEmailValid(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }

    public static boolean isNicknameValid(String nickname) {
        return nickname != null && nickname.trim().length() > 2 && nickname.trim().length() < 17;
    }
}
