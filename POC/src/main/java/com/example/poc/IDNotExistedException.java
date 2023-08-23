package com.example.poc;

public class IDNotExistedException extends RuntimeException{
    public IDNotExistedException(long id) {
        super("ID Not Existed: " + Long.toString(id));
    }
}
