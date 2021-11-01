package com.payneteasy.logsender.application;

import picocli.CommandLine;

import java.io.File;
import java.util.concurrent.Callable;

@CommandLine.Command(
          name                     = "log-sender"
        , mixinStandardHelpOptions = true
        , version                  = "log-sender 1.0.0"
        , description              = "Send logs to srvlog server"
)
public class LogSenderApplication implements Callable<Integer> {

    @CommandLine.Option(
              names       = {"-url", "--srvlog-base-url"}
            , description = "Srvlog server base url"
            , required    = true
    )
    private String baseUrl;

    @CommandLine.Option(
              names       = {"-dir", "--log-dir"}
            , description = "Directory to save log files"
            , required    = true
    )
    private File logDir;

    @Override
    public Integer call() throws Exception {
        return 0;
    }

    public static void main(String[] args) {
        System.exit(new CommandLine(new LogSenderApplication()).execute(args));
    }
}
