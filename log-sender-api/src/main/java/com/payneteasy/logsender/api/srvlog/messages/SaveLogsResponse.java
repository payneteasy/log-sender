package com.payneteasy.logsender.api.srvlog.messages;

import lombok.Data;

@Data
public class SaveLogsResponse {

    /**
     * Request id
     */
    private final String         requestId;

    /**
     * Log processing status
     */
    private final SaveLogsStatus status;

    /**
     * Error message
     */
    private final String         errorMessage;

    /**
     * Unique error id
     */
    private final String         errorId;
}
