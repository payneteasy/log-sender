package com.payneteasy.logsender.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;

@CommandLine.Command(
          name                     = "log-sender"
        , mixinStandardHelpOptions = true
        , version                  = "log-sender 1.0.0"
        , description              = "Send logs to srvlog server"
)
public class LogSenderApplication implements Callable<Integer> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogSenderApplication.class);

    @CommandLine.Option(
              names       = {"-url", "--srvlog-base-url"}
            , description = "Srvlog server base url"
            , required    = true
    )
    private String baseUrl;

    @CommandLine.Option(
              names       = {"-dir", "--log-dir"}
            , description = "Directory to save log files"
            , required    = false
    )
    private File logDir;

    @CommandLine.Option(
            names         = {"-qdir", "--queue-dir"}
            , description = "Directory to save queue files"
            , required    = true
    )
    private String queueDir;

    @CommandLine.Option(
            names         = {"-del", "--delete-rolled"}
            , description = "Delete queue files after roll"
            , required    = false
    )
    private boolean deleteRolledFiles;

    @CommandLine.Option(
            names         = {"-cycle", "--roll-cycle"}
            , description = "Queue roll cycle"
            , required    = false
    )
    private String rollCycle;

    @CommandLine.Option(
            names         = {"-prg", "--program"}
            , description = "Program value"
            , required    = true
    )
    private String program;

    @CommandLine.Option(
            names         = {"-fac", "--facility"}
            , description = "Facility value"
            , required    = true
    )
    private Integer facility;

    @Override
    public Integer call() {
        LogSender logSender = new LogSender(queueDir, baseUrl, deleteRolledFiles, rollCycle, program, facility);
        logSender.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                logSender.waitAndProcessNextRecord(reader);
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return 1;
        }
    }

    public static void main(String[] args) {
        System.exit(new CommandLine(new LogSenderApplication()).execute(args));
    }
}
