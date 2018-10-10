package com.budget_buddy.exception;

public class InvalidDataLabelException extends Exception {
    public InvalidDataLabelException(String msg) {
        super(msg);
    }
    public InvalidDataLabelException(String msg, Throwable thr) {
        super(msg, thr);
    }
    public InvalidDataLabelException(Throwable thr) {
        super(thr);
    }
}
