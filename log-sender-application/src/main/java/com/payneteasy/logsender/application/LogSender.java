package com.payneteasy.logsender.application;

import com.google.gson.Gson;
import com.payneteasy.logsender.api.srvlog.ISrvLogRemoteService;
import com.payneteasy.logsender.api.srvlog.SaveLogsException;
import com.payneteasy.logsender.api.srvlog.messages.SaveLogsMessage;
import com.payneteasy.logsender.api.srvlog.messages.SaveLogsRequest;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.queue.RollCycles;
import net.openhft.chronicle.queue.impl.StoreFileListener;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;
import net.openhft.chronicle.threads.Pauser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author rkononenko, 02.11.2021
 */
public class LogSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogSender.class);

    private static final int DEFAULT_MAX_FLUSH_TIME = 1_000;

    private final Gson gson = new Gson();
    private final Pauser pauser = Pauser.balanced();
    private final AtomicInteger tailerCycle = new AtomicInteger();

    private final Worker worker = new Worker();

    private final String queueDir;
    private final ISrvLogRemoteService srvLogRemoteService;
    private final boolean deleteRolledFiles;
    private final String rollingCycle;
    private final LogResolver logResolver;

    private ChronicleQueue queue;

    private class Worker extends Thread {

        public void run() {
            String message;
            ExcerptTailer tailer = queue.createTailer("WorkerThread");
            while (this.isAlive()) {
                tailerCycle.set(tailer.cycle());
                if ((message = tailer.readText()) == null) {
                    pauser.pause();
                    continue;
                } else {
                    pauser.reset();
                }
                try {
                    srvLogRemoteService.saveLogs(message);
                } catch (SaveLogsException e) {
                    LOGGER.error("Failed to save log", e);
                }
            }
            LOGGER.info("Worker thread will flush remaining events before exiting. ");
        }

    }

    public LogSender(String queueDir, String baseUrl, boolean deleteRolledFiles, String rollingCycle, String program,
                     Integer facility) {
        this.queueDir = queueDir;
        this.srvLogRemoteService = new SrvLogRemoteServiceImpl(baseUrl);
        this.deleteRolledFiles = deleteRolledFiles;
        this.rollingCycle = rollingCycle;
        this.logResolver = new LogResolver(program, facility);
    }

    public void start() {
        SingleChronicleQueueBuilder builder = SingleChronicleQueueBuilder.single(queueDir);
        if (deleteRolledFiles) {
            builder.storeFileListener(new StoreFileListener() {

                private File currentFile;

                @Override public void onAcquired(int cycle, File file) {
                    this.currentFile = file;
                }

                @Override public void onReleased(int cycle, File file) {
                    int retries = 0;
                    while (cycle == tailerCycle.get()) {
                        pauser.unpause();
                        if (retries++ > 100) {
                            LOGGER.warn("Failed to delete released file: still in use. " + file.getAbsolutePath());
                            return;
                        }
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }

                    if (currentFile != null && !file.getAbsolutePath().equals(currentFile.getAbsolutePath())
                            && Files.exists(file.toPath())) {
                        try {
                            Files.delete(file.toPath());
                        } catch (IOException e) {
                            LOGGER.warn("Failed to delete released file: " + file.getAbsolutePath(), e);
                        }
                    }

                }
            });
        }
        if (rollingCycle != null) {
            try {
                RollCycles rollCycle = RollCycles.valueOf(rollingCycle);
                builder.rollCycle(rollCycle);
            } catch (IllegalArgumentException e) {
                LOGGER.warn("no such RollCycle " + rollingCycle + ", using default");
            }
        }

        queue = builder.build();

        //worker.setDaemon(true);
        worker.setName("LogSender-Worker-" + Thread.currentThread().getName());
        worker.start();
    }

    public void close() {
        worker.interrupt();
        try {
            int maxFlushTime = DEFAULT_MAX_FLUSH_TIME;
            worker.join(maxFlushTime);
            if (worker.isAlive()) {
                LOGGER.info("Max queue flush timeout (" + maxFlushTime + " ms) exceeded.");
            } else {
                LOGGER.info("Queue flush finished successfully within timeout.");
            }
        } catch (InterruptedException e) {
            LOGGER.error("Failed to join worker thread. queued events may be discarded. ", e);
        } finally {
            if (!queue.isClosed()) {
                queue.close();
            }
        }
    }

    public void waitAndProcessNextRecord(BufferedReader reader) {
        try {
            Optional<SaveLogsMessage> next = logResolver.getNext(reader);
            SaveLogsMessage saveLogsMessage = next.orElse(null);
            if (saveLogsMessage != null) {
                SaveLogsRequest request = new SaveLogsRequest(UUID.randomUUID().toString(), Collections.singletonList(saveLogsMessage));
                ExcerptAppender appender = queue.acquireAppender();
                appender.writeText(gson.toJson(request));
            }
        } catch (IOException e) {
            LOGGER.error("reader closed, exiting", e);
            close();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

}
