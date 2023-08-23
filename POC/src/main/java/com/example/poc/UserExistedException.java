package com.example.poc;

public class UserExistedException extends RuntimeException {
    public UserExistedException(String name) {
        super("User Existed: " + name);
    }
}
