package com.payneteasy.logsender.application;

import com.payneteasy.logsender.api.srvlog.messages.SaveLogsMessage;
import com.payneteasy.logsender.application.parser.DateParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * @author rkononenko, 02.11.2021
 */
public class LogResolver {

    private final String program;
    private final Integer facility;

    private SaveLogsMessage currentMessage;
    private StringBuilder message = new StringBuilder();

    public LogResolver(String program, Integer facility) {
        this.program = program;
        this.facility = facility;
    }

    public Optional<SaveLogsMessage> getNext(BufferedReader reader) throws IOException {
        String line = reader.readLine();

        //todo: what should we do if empty line?
        if (line == null || line.isEmpty()) {
            SaveLogsMessage previousMessage = this.currentMessage;
            if (previousMessage != null) {
                previousMessage.setMessage(this.message.toString());
            }
            this.currentMessage = null;
            this.message = new StringBuilder();
            return previousMessage == null ? Optional.empty() : Optional.of(previousMessage);
        }

        //write line directly to stdout
        System.out.println(line);

        line = line.trim();
        if (line.length() < 30) {
            this.message.append("\n").append(line);
            return Optional.empty();
        }

        OffsetDateTime offsetDateTime = DateParser.substringLocalDateTime(line);
        if (offsetDateTime != null) {
            long time = offsetDateTime.toInstant().toEpochMilli();
            SaveLogsMessage currentMessage = new SaveLogsMessage();
            currentMessage.setTime(time);

            currentMessage.setProgram(program);

            String severityStr = line.substring(30);
            severityStr = severityStr.substring(0, severityStr.indexOf(" ")).toUpperCase();
            int severity = getSeverity(severityStr);
            currentMessage.setSeverity(severity);

            currentMessage.setFacility(facility);

            SaveLogsMessage previousMessage = null;
            if (this.currentMessage != null) {
                previousMessage = this.currentMessage;
                previousMessage.setMessage(this.message.toString());
            }

            this.currentMessage = currentMessage;
            this.message = new StringBuilder();
            this.message.append(line.substring(line.indexOf(severityStr) + severityStr.length()));

            return previousMessage == null ? Optional.empty() : Optional.of(previousMessage);
        } else {
            this.message.append("\n").append(line);
            return Optional.empty();
        }
    }

    private int getSeverity(String severityStr) {
        int severity;
        switch (severityStr) {
            case "TRACE":
            case "DEBUG":
                severity = 7;
                break;
            case "INFO":
                severity = 6;
                break;
            case "WARN":
                severity = 4;
                break;
            case "ERROR":
                severity = 3;
                break;
            default:
                //todo: which should be a default value?
                severity = 6;
                break;
        }
        return severity;
    }
}
