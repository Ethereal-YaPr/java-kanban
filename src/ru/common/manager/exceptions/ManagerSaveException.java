package ru.common.manager.exceptions;

import java.io.IOException;

public class ManagerSaveException extends RuntimeException {
    public ManagerSaveException(String s) {
        super(s);
    }

    public ManagerSaveException(String s, IOException e) {
        super(s, e);
    }
}