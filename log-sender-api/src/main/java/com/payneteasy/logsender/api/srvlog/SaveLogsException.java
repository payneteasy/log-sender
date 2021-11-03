package com.payneteasy.logsender.api.srvlog;

public class SaveLogsException extends Exception {

    public SaveLogsException(String message) {
        super(message);
    }

    public SaveLogsException(String message, Throwable cause) {
        super(message, cause);
    }
}
