package com.payneteasy.logsender.api.srvlog.messages;


import lombok.Data;

@Data
public class SaveLogsMessage {

    /**
     * Time in ms from epoch
     */
    private final long    time;

    /**
     * Program name
     */
    private final String  program;

    /**
     * Facility. from 0 to 23
     */
    private final Integer facility;

    /**
     * From 0 to 7
     */
    private final Integer severity;

    /**
     * Formatted message
     */
    private final String  message;
}
