package com.payneteasy.logsender.api.srvlog.messages;


import lombok.Data;

import java.util.List;

@Data
public class SaveLogsRequest {

    /**
     * Unique request identifier
     */
    private final String                requestId;

    /**
     * Messages
     */
    private final List<SaveLogsMessage> messages;
}
