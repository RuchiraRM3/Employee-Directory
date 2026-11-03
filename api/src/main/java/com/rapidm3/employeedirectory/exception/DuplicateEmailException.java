package com.rapidm3.employeedirectory.exception;

public class DuplicateEmailException extends RuntimeException {

    private final boolean archived;

    /** @param archived true when the email belongs to an archived (deleted) employee */
    public DuplicateEmailException(boolean archived) {
        super("An employee with this email already exists.");
        this.archived = archived;
    }

    public boolean isArchived() {
        return archived;
    }
}
