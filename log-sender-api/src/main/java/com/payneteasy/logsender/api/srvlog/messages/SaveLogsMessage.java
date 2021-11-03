package com.payneteasy.logsender.api.srvlog.messages;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SaveLogsMessage {

    /**
     * Time in ms from epoch
     */
    private long    time;

    /**
     * Program name
     */
    private String  program;

    /**
     * Facility. from 0 to 23
     */
    private Integer facility;

    /**
     * From 0 to 7
     */
    private Integer severity;

    /**
     * Formatted message
     */
    private String  message;
}
